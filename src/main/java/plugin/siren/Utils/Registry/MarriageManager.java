package plugin.siren.Utils.Registry;

import com.hypixel.hytale.server.core.util.Config;
import plugin.siren.API.MarriageEvents;
import plugin.siren.API.MarriageInfo;
import plugin.siren.Marriage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The global marriage registry. Source of truth for who is married to whom —
 * including offline players. Mirrors the SectManager pattern: static synchronized
 * mutations that each end in persist(), lock-free ConcurrentHashMap reads,
 * save-on-every-mutation as the whole persistence strategy.
 *
 * Proposals, divorce confirmations, and cooldowns are transient in-memory state
 * (lost on restart by design) so no cross-player component access is ever needed.
 */
public final class MarriageManager {

    private static Config<MarriagesData> dataFile;

    /** Two entries per marriage — one per partner — both mapping to the shared record. */
    private static final Map<UUID, MarriageRecord> BY_PLAYER = new ConcurrentHashMap<>();

    /** A pending marriage request: the requester's display name and when it expires. */
    public record PendingRequest(String requesterName, long expiryMillis) {}

    /** target -> (requester -> pending request). */
    private static final Map<UUID, Map<UUID, PendingRequest>> REQUESTS = new ConcurrentHashMap<>();

    /** player -> divorce confirmation expiry epoch millis. */
    private static final Map<UUID, Long> DIVORCE_CONFIRM = new ConcurrentHashMap<>();

    /** player -> epoch millis the cooldown ends. */
    private static final Map<UUID, Long> GIFT_COOLDOWN = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> TP_COOLDOWN = new ConcurrentHashMap<>();

    /** Cache of each player's "accept marriage requests" preference (default true). */
    private static final Map<UUID, Boolean> ACCEPTS_REQUESTS = new ConcurrentHashMap<>();

    /**
     * Players whose settings component still says married although the marriage
     * ended while they were offline; persisted so a restart cannot resurrect an
     * ended marriage from stale 1.x save data.
     */
    private static final Set<UUID> PENDING_DIVORCE_SYNC = ConcurrentHashMap.newKeySet();

    private MarriageManager(){}

    public static void init(@Nonnull Config<MarriagesData> file){
        dataFile = file;
        BY_PLAYER.clear();
        for(MarriageRecord record : file.get().getMarriages()){
            indexRecord(record);
        }
        PENDING_DIVORCE_SYNC.clear();
        for(String uuid : file.get().getPendingDivorceSync()){
            try{
                PENDING_DIVORCE_SYNC.add(UUID.fromString(uuid));
            }catch(IllegalArgumentException ignored){
            }
        }
        Marriage.LOGGER.atInfo().log("Loaded " + getMarriedCoupleCount() + " marriage(s) from the registry.");
    }

    private static void indexRecord(MarriageRecord record){
        try{
            BY_PLAYER.put(UUID.fromString(record.getPlayer1Uuid()), record);
            BY_PLAYER.put(UUID.fromString(record.getPlayer2Uuid()), record);
        }catch(IllegalArgumentException ignored){
            Marriage.LOGGER.atSevere().log("Skipped a marriage record with a corrupt UUID (" + record.getPlayer1Name() + " & " + record.getPlayer2Name() + ").");
        }
    }

    // === reads (lock-free, safe from any world thread) ===

    public static boolean isMarried(@Nonnull UUID player){
        return BY_PLAYER.containsKey(player);
    }

    @Nullable
    public static MarriageRecord getRecord(@Nonnull UUID player){
        return BY_PLAYER.get(player);
    }

    @Nullable
    public static UUID getPartnerUuid(@Nonnull UUID player){
        MarriageRecord record = BY_PLAYER.get(player);
        return record == null ? null : record.getPartnerUuid(player);
    }

    @Nullable
    public static String getPartnerName(@Nonnull UUID player){
        MarriageRecord record = BY_PLAYER.get(player);
        return record == null ? null : record.getPartnerName(player);
    }

    /** True when the two players are married to each other. */
    public static boolean areMarried(@Nonnull UUID playerA, @Nonnull UUID playerB){
        MarriageRecord record = BY_PLAYER.get(playerA);
        return record != null && playerB.equals(record.getPartnerUuid(playerA));
    }

    /** Every marriage on the server, offline couples included, oldest first. */
    @Nonnull
    public static List<MarriageInfo> getAllMarriages(){
        Set<MarriageRecord> seen = new HashSet<>();
        List<MarriageInfo> marriages = new ArrayList<>();
        for(MarriageRecord record : BY_PLAYER.values()){
            if(seen.add(record)){
                MarriageInfo info = MarriageInfo.of(record);
                if(info != null){
                    marriages.add(info);
                }
            }
        }
        marriages.sort(Comparator.comparingLong(MarriageInfo::getMarriedAtMillis));
        return marriages;
    }

    public static int getMarriedCoupleCount(){
        Set<MarriageRecord> seen = new HashSet<>(BY_PLAYER.values());
        return seen.size();
    }

    public static int getTotalMarriagesEver(){
        return dataFile == null ? 0 : dataFile.get().getTotalMarriagesEver();
    }

    public static int getTotalDivorces(){
        return dataFile == null ? 0 : dataFile.get().getTotalDivorces();
    }

    // === proposals ===

    public enum ProposeResult {
        SELF,
        ALREADY_MARRIED,
        TARGET_MARRIED,
        TARGET_NOT_ACCEPTING,
        REQUEST_SENT,
        ALREADY_REQUESTED,
        MARRIED,
        CANCELLED
    }

    /**
     * One call handles both sides of the handshake: if the target already proposed
     * to the requester this completes the marriage, otherwise it files a request.
     */
    @Nonnull
    public static synchronized ProposeResult propose(@Nonnull UUID requester, @Nonnull String requesterName,
                                                     @Nonnull UUID target, @Nonnull String targetName){
        if(requester.equals(target)){
            return ProposeResult.SELF;
        }
        if(isMarried(requester)){
            return ProposeResult.ALREADY_MARRIED;
        }
        if(isMarried(target)){
            return ProposeResult.TARGET_MARRIED;
        }

        long now = System.currentTimeMillis();

        Map<UUID, PendingRequest> myRequests = REQUESTS.get(requester);
        if(myRequests != null){
            PendingRequest pending = myRequests.get(target);
            if(pending != null && pending.expiryMillis() > now){
                // Mutual — the target proposed first, this call accepts.
                if(!MarriageEvents.firePreMarry(new MarriageEvents.PreMarryEvent(target, targetName, requester, requesterName))){
                    return ProposeResult.CANCELLED;
                }
                myRequests.remove(target);
                Map<UUID, PendingRequest> theirRequests = REQUESTS.get(target);
                if(theirRequests != null){
                    theirRequests.remove(requester);
                }
                marry(target, targetName, requester, requesterName);
                return ProposeResult.MARRIED;
            }
        }

        if(!acceptsRequests(target)){
            return ProposeResult.TARGET_NOT_ACCEPTING;
        }

        long expiry = now + Marriage.getConfig().get().getRequestExpirySeconds() * 1000L;
        Map<UUID, PendingRequest> targetRequests = REQUESTS.computeIfAbsent(target, key -> new ConcurrentHashMap<>());
        PendingRequest existing = targetRequests.put(requester, new PendingRequest(requesterName, expiry));
        if(existing != null && existing.expiryMillis() > now){
            return ProposeResult.ALREADY_REQUESTED;
        }
        return ProposeResult.REQUEST_SENT;
    }

    /** Pending, unexpired requests waiting on this player, requester UUID -> request. */
    @Nonnull
    public static Map<UUID, PendingRequest> getPendingRequests(@Nonnull UUID target){
        Map<UUID, PendingRequest> requests = REQUESTS.get(target);
        if(requests == null){
            return Map.of();
        }
        long now = System.currentTimeMillis();
        requests.entrySet().removeIf(entry -> entry.getValue().expiryMillis() <= now);
        return requests;
    }

    /** Accept a specific pending request (menu accept button). */
    @Nonnull
    public static synchronized ProposeResult acceptRequest(@Nonnull UUID target, @Nonnull String targetName,
                                                           @Nonnull UUID requester){
        PendingRequest pending = getPendingRequests(target).get(requester);
        if(pending == null){
            return ProposeResult.REQUEST_SENT; // nothing pending — treat as a no-op miss
        }
        return propose(target, targetName, requester, pending.requesterName());
    }

    /** Decline a pending request (menu decline button). Returns true if one was removed. */
    public static boolean declineRequest(@Nonnull UUID target, @Nonnull UUID requester){
        Map<UUID, PendingRequest> requests = REQUESTS.get(target);
        return requests != null && requests.remove(requester) != null;
    }

    private static void marry(UUID player1, String player1Name, UUID player2, String player2Name){
        MarriageRecord record = new MarriageRecord(player1, player1Name, player2, player2Name,
                System.currentTimeMillis(), Marriage.getConfig().get().ifDefaultPvpProtection());
        indexRecord(record);
        REQUESTS.remove(player1);
        REQUESTS.remove(player2);
        // A stale divorce-sync marker must not clear this brand-new marriage on join.
        PENDING_DIVORCE_SYNC.remove(player1);
        PENDING_DIVORCE_SYNC.remove(player2);
        if(dataFile != null){
            dataFile.get().incrementTotalMarriagesEver();
        }
        persist();
        MarriageInfo info = MarriageInfo.of(record);
        if(info != null){
            MarriageEvents.fireMarry(info);
        }
    }

    /**
     * Marries two players directly (API/admin path). The caller is responsible for
     * firing the cancellable PreMarryEvent first; the post MarryEvent fires here.
     */
    public static synchronized boolean adminMarry(@Nonnull UUID player1, @Nonnull String player1Name,
                                                  @Nonnull UUID player2, @Nonnull String player2Name){
        if(player1.equals(player2) || isMarried(player1) || isMarried(player2)){
            return false;
        }
        marry(player1, player1Name, player2, player2Name);
        return true;
    }

    /**
     * Ends a marriage immediately without the chat confirmation window (API/admin
     * path). The caller is responsible for firing the cancellable PreDivorceEvent
     * first; the post DivorceEvent fires here. Returns true if a marriage ended.
     */
    public static synchronized boolean forceDivorce(@Nonnull UUID player){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record == null){
            return false;
        }
        MarriageInfo info = MarriageInfo.of(record);
        DIVORCE_CONFIRM.remove(player);
        UUID partner = record.getPartnerUuid(player);
        BY_PLAYER.remove(player);
        PENDING_DIVORCE_SYNC.add(player);
        if(partner != null){
            BY_PLAYER.remove(partner);
            DIVORCE_CONFIRM.remove(partner);
            PENDING_DIVORCE_SYNC.add(partner);
        }
        if(dataFile != null){
            dataFile.get().incrementTotalDivorces();
        }
        persist();
        if(info != null){
            MarriageEvents.fireDivorce(info);
        }
        return true;
    }

    /**
     * Recognizes a marriage stored in 1.x per-player save data. Called from
     * PlayerReadyEvent when a player's settings component says married but the
     * registry has no record. MarriedAt becomes the import time.
     */
    public static synchronized boolean importLegacy(@Nonnull UUID player, @Nonnull String playerName,
                                                    @Nonnull UUID partner, @Nonnull String partnerName){
        if(isMarried(player) || isMarried(partner)){
            return false;
        }
        MarriageRecord record = new MarriageRecord(player, playerName, partner, partnerName,
                System.currentTimeMillis(), Marriage.getConfig().get().ifDefaultPvpProtection());
        indexRecord(record);
        PENDING_DIVORCE_SYNC.remove(player);
        PENDING_DIVORCE_SYNC.remove(partner);
        if(dataFile != null){
            dataFile.get().incrementTotalMarriagesEver();
        }
        persist();
        Marriage.LOGGER.atInfo().log("Imported legacy marriage: " + playerName + " & " + partnerName + ".");
        return true;
    }

    // === divorce ===

    public enum DivorceResult {
        NOT_MARRIED,
        CONFIRM_NEEDED,
        DIVORCED,
        CANCELLED
    }

    @Nonnull
    public static synchronized DivorceResult divorce(@Nonnull UUID player){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record == null){
            return DivorceResult.NOT_MARRIED;
        }

        long now = System.currentTimeMillis();
        Long confirmExpiry = DIVORCE_CONFIRM.get(player);
        if(confirmExpiry == null || confirmExpiry <= now){
            DIVORCE_CONFIRM.put(player, now + Marriage.getConfig().get().getDivorceConfirmSeconds() * 1000L);
            return DivorceResult.CONFIRM_NEEDED;
        }

        MarriageInfo info = MarriageInfo.of(record);
        if(info != null && !MarriageEvents.firePreDivorce(new MarriageEvents.PreDivorceEvent(info, player))){
            DIVORCE_CONFIRM.remove(player);
            return DivorceResult.CANCELLED;
        }

        DIVORCE_CONFIRM.remove(player);
        UUID partner = record.getPartnerUuid(player);
        BY_PLAYER.remove(player);
        PENDING_DIVORCE_SYNC.add(player);
        if(partner != null){
            BY_PLAYER.remove(partner);
            DIVORCE_CONFIRM.remove(partner);
            PENDING_DIVORCE_SYNC.add(partner);
        }
        if(dataFile != null){
            dataFile.get().incrementTotalDivorces();
        }
        persist();
        if(info != null){
            MarriageEvents.fireDivorce(info);
        }
        return DivorceResult.DIVORCED;
    }

    /**
     * Removes a player's pending divorce-sync marker; true when one existed. The
     * caller then clears their settings component (either right after the divorce
     * for online players, or from PlayerReadyEvent on next join).
     */
    public static synchronized boolean consumePendingDivorceSync(@Nonnull UUID player){
        if(PENDING_DIVORCE_SYNC.remove(player)){
            persist();
            return true;
        }
        return false;
    }

    // === per-couple settings and stats ===

    public static synchronized boolean setPvpProtection(@Nonnull UUID player, boolean enabled){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record == null){
            return false;
        }
        if(record.isPvpProtection() != enabled){
            record.setPvpProtection(enabled);
            persist();
            MarriageInfo info = MarriageInfo.of(record);
            if(info != null){
                MarriageEvents.firePvpProtectionToggle(info);
            }
        }
        return true;
    }

    /** The PvP-system check: married to each other, couple toggle on, master config on. */
    public static boolean isPvpProtected(@Nonnull UUID attacker, @Nonnull UUID victim){
        if(!Marriage.getConfig().get().ifPartnerPvpProtection()){
            return false;
        }
        MarriageRecord record = BY_PLAYER.get(victim);
        return record != null && attacker.equals(record.getPartnerUuid(victim)) && record.isPvpProtection();
    }

    public static synchronized void recordGift(@Nonnull UUID giver){
        MarriageRecord record = BY_PLAYER.get(giver);
        if(record != null){
            record.addGiftFrom(giver);
            persist();
        }
    }

    public static synchronized void recordTeleport(@Nonnull UUID player){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record != null){
            record.incrementTeleportsUsed();
            persist();
        }
    }

    public static synchronized void recordMessage(@Nonnull UUID player){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record != null){
            record.incrementMessagesSent();
            persist();
        }
    }

    /** Refresh the cached display name after a rename; persists only if it changed. */
    public static synchronized void refreshName(@Nonnull UUID player, @Nonnull String username){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record != null && !username.equals(record.getNameOf(player))){
            record.refreshName(player, username);
            persist();
        }
    }

    // === request-acceptance preference (cache of the persisted per-player setting) ===

    public static boolean acceptsRequests(@Nonnull UUID player){
        return ACCEPTS_REQUESTS.getOrDefault(player, true);
    }

    public static void cacheAcceptsRequests(@Nonnull UUID player, boolean accepts){
        ACCEPTS_REQUESTS.put(player, accepts);
    }

    // === cooldowns (transient) ===

    /** Seconds left on the gift cooldown, 0 when ready. */
    public static long getGiftCooldownRemaining(@Nonnull UUID player){
        return cooldownRemaining(GIFT_COOLDOWN, player);
    }

    public static void markGiftUsed(@Nonnull UUID player){
        GIFT_COOLDOWN.put(player, System.currentTimeMillis() + Marriage.getConfig().get().getGiftCooldownSeconds() * 1000L);
    }

    /** Seconds left on the teleport cooldown, 0 when ready. */
    public static long getTpCooldownRemaining(@Nonnull UUID player){
        return cooldownRemaining(TP_COOLDOWN, player);
    }

    public static void markTpUsed(@Nonnull UUID player){
        TP_COOLDOWN.put(player, System.currentTimeMillis() + Marriage.getConfig().get().getTpCooldownSeconds() * 1000L);
    }

    private static long cooldownRemaining(Map<UUID, Long> cooldowns, UUID player){
        Long until = cooldowns.get(player);
        if(until == null){
            return 0L;
        }
        long remainingMillis = until - System.currentTimeMillis();
        if(remainingMillis <= 0L){
            cooldowns.remove(player);
            return 0L;
        }
        return (remainingMillis + 999L) / 1000L;
    }

    // === persistence ===

    private static void persist(){
        if(dataFile == null){
            return;
        }
        // BY_PLAYER holds each record twice (once per partner); distinct() keeps one.
        dataFile.get().setMarriages(BY_PLAYER.values().stream().distinct().toArray(MarriageRecord[]::new));
        dataFile.get().setPendingDivorceSync(PENDING_DIVORCE_SYNC.stream().map(UUID::toString).toArray(String[]::new));
        dataFile.save();
    }
}
