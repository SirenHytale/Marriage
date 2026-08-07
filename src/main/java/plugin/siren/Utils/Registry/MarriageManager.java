package plugin.siren.Utils.Registry;

import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.Config;
import plugin.siren.API.MarriageEvents;
import plugin.siren.API.MarriageInfo;
import plugin.siren.Marriage;
import plugin.siren.Utils.Bond.BondManager;
import plugin.siren.Utils.Bond.BondXpReason;
import plugin.siren.Utils.Bond.RingTier;
import plugin.siren.Utils.Bond.Vow;
import plugin.siren.Utils.MarriageConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final Map<UUID, Long> HOME_COOLDOWN = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> RESPAWN_COOLDOWN = new ConcurrentHashMap<>();

    /** Cache of each player's "accept marriage requests" preference (default true). */
    private static final Map<UUID, Boolean> ACCEPTS_REQUESTS = new ConcurrentHashMap<>();

    /** Cache of each player's couple-channel preference (default false). Read from the async chat event. */
    private static final Map<UUID, Boolean> COUPLE_CHANNEL = new ConcurrentHashMap<>();

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
        TALLIES.clear();
        for(PlayerTally tally : file.get().getPlayerTallies()){
            if(tally.getUuid() != null && !tally.getUuid().isBlank()){
                TALLIES.put(tally.getUuid(), tally);
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
        CANCELLED,
        /** The proposer is still inside their remarry cooldown. */
        COOLDOWN,
        /** The target is still inside theirs. */
        TARGET_COOLDOWN
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
        // Checked for BOTH sides, and before the mutual-acceptance branch below:
        // otherwise a couple could divorce and immediately remarry each other by
        // both proposing, which is precisely the loop the cooldown exists to close.
        if(getRemarryCooldownRemaining(requester) > 0L){
            return ProposeResult.COOLDOWN;
        }
        if(getRemarryCooldownRemaining(target) > 0L){
            return ProposeResult.TARGET_COOLDOWN;
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
        tallyFor(player1.toString(), player1Name).incrementMarriages();
        tallyFor(player2.toString(), player2Name).incrementMarriages();
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
        endMarriage(record, player);
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
        CANCELLED,
        /** Filed, and now waiting out the period before it completes. */
        FILED,
        /** Already filed by this same player - nothing further to do but wait. */
        ALREADY_FILED
    }

    /**
     * Files, accepts, or completes a divorce - the whole weighted flow.
     *
     * <p>The shape, in the order the checks run:</p>
     * <ul>
     *   <li>The partner already filed → this is <b>mutual consent</b>, and it ends
     *       immediately. Two people who both want out should not be made to wait.</li>
     *   <li>Nothing filed yet → the existing click-twice confirmation still guards
     *       the act, then it is <b>filed</b> and the waiting period begins.</li>
     *   <li>Already filed by this player → tell them how long is left.</li>
     * </ul>
     *
     * <p>With the waiting period switched off, the first branch is unreachable and
     * this behaves exactly as 2.1.0 did.</p>
     */
    @Nonnull
    public static synchronized DivorceResult fileDivorce(@Nonnull UUID player){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record == null){
            return DivorceResult.NOT_MARRIED;
        }

        MarriageConfig config = Marriage.getConfig().get();
        if(!config.ifDivorceWaitingPeriod()){
            return divorce(player);
        }

        // Both want out: no waiting period earned by mutual agreement.
        if(record.isDivorcePending() && !record.isDivorceFiledBy(player)){
            return completeDivorce(player, record);
        }

        if(record.isDivorcePending()){
            return DivorceResult.ALREADY_FILED;
        }

        long now = System.currentTimeMillis();
        Long confirmExpiry = DIVORCE_CONFIRM.get(player);
        if(confirmExpiry == null || confirmExpiry <= now){
            DIVORCE_CONFIRM.put(player, now + config.getDivorceConfirmSeconds() * 1000L);
            return DivorceResult.CONFIRM_NEEDED;
        }
        DIVORCE_CONFIRM.remove(player);

        record.fileDivorce(player, now);
        persist();
        return DivorceResult.FILED;
    }

    /** Marks a pending divorce contested. Returns false when there is none to contest. */
    public static synchronized boolean contestDivorce(@Nonnull UUID player){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record == null || !record.isDivorcePending()
                || record.isDivorceFiledBy(player) || record.isDivorceContested()){
            return false;
        }
        record.contestDivorce();
        persist();
        return true;
    }

    /**
     * Withdraws a divorce the caller filed themselves. Returns false for anyone
     * else's filing - the person being divorced cannot simply cancel it, only
     * contest it or agree.
     */
    public static synchronized boolean withdrawDivorce(@Nonnull UUID player){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record == null || !record.isDivorcePending() || !record.isDivorceFiledBy(player)){
            return false;
        }
        record.clearPendingDivorce();
        persist();
        return true;
    }

    /** Seconds until a pending divorce completes, or 0 when none is pending. */
    public static long getDivorceRemainingSeconds(@Nonnull MarriageRecord record){
        if(!record.isDivorcePending()){
            return 0L;
        }
        long remaining = divorceCompletesAt(record) - System.currentTimeMillis();
        return remaining <= 0L ? 0L : (remaining + 999L) / 1000L;
    }

    /**
     * When a filing matures.
     *
     * <p>Contesting does not block the divorce - nobody should be able to trap
     * somebody in a marriage by objecting - it makes them serve the full period
     * rather than the shortened one an uncontested filing gets. Objecting buys
     * time to talk, which is the most it should buy.</p>
     */
    private static long divorceCompletesAt(@Nonnull MarriageRecord record){
        MarriageConfig config = Marriage.getConfig().get();
        double hours = record.isDivorceContested()
                ? config.getDivorceWaitingHours()
                : config.getDivorceWaitingHours() * config.getDivorceUncontestedFactor();
        return record.getDivorceFiledAtMillis() + (long) (hours * 60.0 * 60.0 * 1000.0);
    }

    /**
     * Completes a pending divorce whose period has elapsed. Called from the bond
     * tick, so it lands while somebody is online to be told - a couple both
     * offline simply have it complete when either next logs in.
     *
     * @return true if a marriage ended.
     */
    public static synchronized boolean completeDivorceIfDue(@Nonnull UUID player){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record == null || !record.isDivorcePending()
                || System.currentTimeMillis() < divorceCompletesAt(record)){
            return false;
        }
        return completeDivorce(player, record) == DivorceResult.DIVORCED;
    }

    /** The actual ending, shared by mutual consent and by the period elapsing. */
    @Nonnull
    private static DivorceResult completeDivorce(@Nonnull UUID player, @Nonnull MarriageRecord record){
        MarriageInfo info = MarriageInfo.of(record);
        if(info != null && !MarriageEvents.firePreDivorce(new MarriageEvents.PreDivorceEvent(info, player))){
            return DivorceResult.CANCELLED;
        }

        endMarriage(record, player);

        if(info != null){
            MarriageEvents.fireDivorce(info);
        }
        return DivorceResult.DIVORCED;
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

        endMarriage(record, player);
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
            tallyFor(giver.toString(), record.getNameOf(giver)).incrementGiftsGiven();
            persist();
            // Giving deepens the bond. Re-entrant on this same monitor, and it
            // persists again - a gift is rare enough that the second write is
            // cheaper than threading the two changes together.
            addBondXp(giver, Marriage.getConfig().get().getBondXpPerGift(), BondXpReason.GIFT);
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

    // === bond ===

    /**
     * Adds bond XP to a player's marriage, announces any level crossed, and pays
     * out any anniversary that became due in the same breath.
     *
     * @return the couple's new bond level, or -1 when the player is unmarried.
     */
    public static synchronized int addBondXp(@Nonnull UUID player, double amount, @Nonnull BondXpReason reason){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record == null){
            return -1;
        }

        int oldLevel = BondManager.applyXp(record, amount);
        persist();
        BondManager.announceLevelChange(record, oldLevel, reason);

        // Strictly after this grant's own announcement. awardDueAnniversaries
        // applies XP and announces any level IT crosses itself, so folding the two
        // together would announce a level twice - once as the anniversary's, once
        // again as this grant's, measured from before the anniversary ran.
        if(BondManager.awardDueAnniversaries(record)){
            persist();
        }
        return record.getBondLevel();
    }

    /**
     * One bond heartbeat for a couple: time married, time together, and decay,
     * all measured from the same elapsed window so they cannot disagree.
     *
     * <p>Whichever partner's tick arrives first consumes the window and moves the
     * clock, so a couple with both halves online is not paid twice. {@code
     * maxWindowMillis} is what makes "time married" mean time married <em>while
     * somebody was playing</em>: a couple who logged off for a month comes back to
     * one window's worth of XP, not a month's.</p>
     *
     * @param together whether the partners are within bond range of each other right now.
     */
    public static synchronized void bondTick(@Nonnull UUID player, boolean together, long maxWindowMillis){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record == null){
            return;
        }

        // A filing whose period has run out ends here, on the tick, so it lands
        // while somebody is online to be told rather than silently on next login.
        if(record.isDivorcePending() && completeDivorceIfDue(player)){
            return;
        }

        MarriageConfig config = Marriage.getConfig().get();
        long now = System.currentTimeMillis();
        long last = record.getLastBondTickMillis();

        if(last <= 0L || last > now){
            // First tick since 2.1.0 (or a clock that moved backwards): start the
            // clock rather than back-paying every hour since the wedding.
            record.setLastBondTickMillis(now);
            if(record.getLastTogetherMillis() <= 0L){
                record.setLastTogetherMillis(now);
            }
            persist();
            return;
        }

        long window = Math.min(now - last, Math.max(1000L, maxWindowMillis));
        if(window < 1000L){
            return; // the other partner already consumed this window
        }
        record.setLastBondTickMillis(now);

        double xp = 0.0;
        BondXpReason reason = BondXpReason.TIME_MARRIED;

        if(config.ifBondEnabled()){
            xp += (window / 3_600_000.0) * config.getBondXpPerHourMarried();

            if(together){
                double minutes = window / 60_000.0;
                xp += minutes * config.getBondXpPerMinuteTogether();
                record.addTogetherMinutes(Math.round(minutes));
                record.setLastTogetherMillis(now);
                reason = BondXpReason.TOGETHER;
            }else{
                double decay = decayFor(record, config, now, window);
                if(decay > 0.0){
                    xp -= decay;
                    reason = BondXpReason.DECAY;
                }
            }
        }

        int oldLevel = BondManager.applyXp(record, xp);
        clampToDecayFloor(record, config, xp);
        persist();
        BondManager.announceLevelChange(record, oldLevel, reason);

        // Same ordering rule as addBondXp: the anniversary announces its own level
        // crossing, so it must run after this tick's has already been reported.
        if(BondManager.awardDueAnniversaries(record)){
            persist();
        }
    }

    /**
     * Bond XP this window costs a couple who have been apart past the grace period.
     *
     * <p>The grace check is wall-clock ("you have not seen each other in three
     * weeks") but the drain itself is paced by the window, so a couple who return
     * after a long absence lose the bond gradually while playing rather than
     * arriving to find it already gone.</p>
     */
    private static double decayFor(MarriageRecord record, MarriageConfig config, long now, long window){
        if(!config.ifBondDecayEnabled()){
            return 0.0;
        }
        long since = record.getLastTogetherMillis() > 0L ? record.getLastTogetherMillis() : record.getMarriedAtMillis();
        long apartMillis = now - since;
        long graceMillis = (long) (config.getBondDecayGraceDays() * 24.0 * 60.0 * 60.0 * 1000.0);
        if(apartMillis <= graceMillis){
            return 0.0;
        }
        return (window / 86_400_000.0) * config.getBondDecayXpPerDay();
    }

    /** Decay must never drag a couple below Bond-Decay-Min-Level. */
    private static void clampToDecayFloor(MarriageRecord record, MarriageConfig config, double appliedXp){
        if(appliedXp >= 0.0){
            return;
        }
        int floor = config.getBondDecayMinLevel();
        if(floor <= 0 || record.getBondLevel() >= floor){
            return;
        }
        record.setBondXp(BondManager.xpForLevel(floor));
        record.setBondLevel(floor);
    }

    /**
     * Records the band a couple married with. Called once, from the wedding -
     * see {@link plugin.siren.Utils.Bond.RingTier} for why it is never re-read
     * from the players' inventories afterwards.
     */
    public static synchronized void setRingTier(@Nonnull UUID player, @Nonnull RingTier tier){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record != null && record.getRingTier() != tier){
            record.setRingTier(tier);
            persist();
        }
    }

    // === vows and renewal ===

    /**
     * Swears a vow. Refused once the couple has already chosen one - renewing
     * their vows is what reopens the choice.
     */
    public static synchronized boolean setVow(@Nonnull UUID player, @Nonnull Vow vow){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record == null || record.isVowChosen() || vow == Vow.NONE){
            return false;
        }
        record.setVow(vow);
        persist();
        return true;
    }

    /** player -> epoch millis their renewal offer lapses. Transient by design. */
    private static final Map<UUID, Long> RENEW_OFFER = new ConcurrentHashMap<>();

    public enum RenewResult {
        NOT_MARRIED,
        /** Too soon since the wedding, or since the last renewal. */
        TOO_SOON,
        /** Offered; the partner must answer to complete it. */
        OFFERED,
        /** Both have now offered - renewed. */
        RENEWED
    }

    /**
     * Renewing vows: a mutual act, so it takes both of them.
     *
     * <p>Symmetric rather than a proposal with an accept button - the same call
     * from either side, and whoever runs it second completes it. There is nothing
     * adversarial here to protect against, so a handshake with no separate
     * "accept" verb is both simpler and truer to the thing.</p>
     *
     * <p>On renewal: a bond boost, the decay clock reset, the vow choice reopened,
     * and the band re-read so a couple who have since crafted a better one are
     * upgraded. The tier only ever goes up.</p>
     */
    @Nonnull
    public static synchronized RenewResult renewVows(@Nonnull UUID player, @Nullable RingTier heldTier){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record == null){
            return RenewResult.NOT_MARRIED;
        }

        MarriageConfig config = Marriage.getConfig().get();
        long now = System.currentTimeMillis();
        long since = Math.max(record.getMarriedAtMillis(), record.getLastRenewedAtMillis());
        if(now - since < (long) (config.getRenewMinDays() * 24.0 * 60.0 * 60.0 * 1000.0)){
            return RenewResult.TOO_SOON;
        }

        UUID partner = record.getPartnerUuid(player);
        Long theirOffer = partner == null ? null : RENEW_OFFER.get(partner);
        if(theirOffer == null || theirOffer <= now){
            RENEW_OFFER.put(player, now + config.getRenewOfferSeconds() * 1000L);
            return RenewResult.OFFERED;
        }

        RENEW_OFFER.remove(player);
        RENEW_OFFER.remove(partner);

        record.setLastRenewedAtMillis(now);
        record.setLastTogetherMillis(now);   // the decay clock starts again
        record.reopenVow();

        // Upgrade only - a renewal must never cost a couple the band they already had.
        if(heldTier != null && heldTier.ordinal() > record.getRingTier().ordinal()){
            record.setRingTier(heldTier);
        }

        int oldLevel = BondManager.applyXp(record, config.getRenewBondXp());
        persist();
        BondManager.announceLevelChange(record, oldLevel, BondXpReason.RENEWAL);
        return RenewResult.RENEWED;
    }

    // === shared home ===

    /** Sets the couple's shared home. Returns false when the player is unmarried. */
    public static synchronized boolean setHome(@Nonnull UUID player, @Nonnull String world,
                                               double x, double y, double z, float yaw, float pitch){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record == null){
            return false;
        }
        record.setHome(world, x, y, z, yaw, pitch);
        persist();
        return true;
    }

    /** Forgets the couple's shared home. Returns true if there was one. */
    public static synchronized boolean clearHome(@Nonnull UUID player){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record == null || !record.hasHome()){
            return false;
        }
        record.clearHome();
        persist();
        return true;
    }

    // === hope chest ===

    /**
     * The couple's chest, created at the configured size on first use.
     * Null when the player is unmarried.
     */
    @Nullable
    public static synchronized SimpleItemContainer getOrCreateHopeChest(@Nonnull UUID player){
        MarriageRecord record = BY_PLAYER.get(player);
        if(record == null){
            return null;
        }
        // Vow-scaled: the Hearth vow adds slots (see BondManager.chestSlotsFor).
        SimpleItemContainer chest = record.getOrCreateHopeChest(BondManager.chestSlotsFor(record));
        persist();
        return chest;
    }

    /** Writes the chest's current contents to disk — called when a partner closes it. */
    public static synchronized void saveHopeChest(){
        persist();
    }

    /**
     * Credits both partners a divorce, remembers who they were married to and for
     * how long, and starts their remarry cooldown. Called before the record is
     * dropped - none of it is knowable afterwards.
     */
    private static void tallyDivorce(MarriageRecord record){
        long now = System.currentTimeMillis();
        long lasted = Math.max(0L, now - record.getMarriedAtMillis());
        long cooldownUntil = now + Marriage.getConfig().get().getRemarryCooldownSeconds() * 1000L;

        PlayerTally one = tallyFor(record.getPlayer1Uuid(), record.getPlayer1Name());
        one.incrementDivorces();
        one.recordMarriageLength(lasted);
        one.addExPartner(new ExPartner(record.getPlayer2Uuid(), record.getPlayer2Name(), now, lasted));
        one.setRemarryCooldownUntilMillis(cooldownUntil);

        PlayerTally two = tallyFor(record.getPlayer2Uuid(), record.getPlayer2Name());
        two.incrementDivorces();
        two.recordMarriageLength(lasted);
        two.addExPartner(new ExPartner(record.getPlayer1Uuid(), record.getPlayer1Name(), now, lasted));
        two.setRemarryCooldownUntilMillis(cooldownUntil);
    }

    /**
     * Tears a marriage down: splits the chest, writes both histories, unindexes
     * both partners, marks them for settings reconciliation, counts the divorce
     * and persists.
     *
     * <p>One copy, called by every path that ends a marriage - the confirmed
     * command, mutual consent, a matured filing, and the admin/API force. Before
     * this existed the teardown was duplicated three times, which is exactly how
     * one of them would have ended up forgetting the chest.</p>
     *
     * @param initiator whichever partner's action ended it, for the divorce-sync markers.
     */
    private static void endMarriage(@Nonnull MarriageRecord record, @Nonnull UUID initiator){
        splitHopeChest(record);
        tallyDivorce(record);

        DIVORCE_CONFIRM.remove(initiator);
        UUID partner = record.getPartnerUuid(initiator);
        BY_PLAYER.remove(initiator);
        PENDING_DIVORCE_SYNC.add(initiator);
        if(partner != null){
            BY_PLAYER.remove(partner);
            DIVORCE_CONFIRM.remove(partner);
            PENDING_DIVORCE_SYNC.add(partner);
        }

        if(dataFile != null){
            dataFile.get().incrementTotalDivorces();
        }
        persist();
    }

    /** Seconds until this player may marry again, 0 when they may now. */
    public static long getRemarryCooldownRemaining(@Nonnull UUID player){
        if(Marriage.getConfig().get().getRemarryCooldownSeconds() <= 0){
            return 0L;
        }
        PlayerTally tally = TALLIES.get(player.toString());
        return tally == null ? 0L : tally.getRemarryCooldownRemainingSeconds();
    }

    /**
     * Empties a divorcing couple's chest into two pending deliveries, one per
     * partner, dealt out stack by stack so neither side can be handed only the
     * junk. Called from inside the divorce path, before the record is dropped.
     */
    private static void splitHopeChest(MarriageRecord record){
        SimpleItemContainer chest = record.getHopeChest();
        if(chest == null || chest.isEmpty()){
            return;
        }

        short capacity = chest.getCapacity();
        SimpleItemContainer forPlayer1 = new SimpleItemContainer(capacity);
        SimpleItemContainer forPlayer2 = new SimpleItemContainer(capacity);
        short next1 = 0;
        short next2 = 0;
        boolean toFirst = true;

        for(short slot = 0; slot < capacity; slot++){
            ItemStack stack = chest.getItemStack(slot);
            if(ItemStack.isEmpty(stack)){
                continue;
            }
            if(toFirst){
                forPlayer1.setItemStackForSlot(next1++, stack, false);
            }else{
                forPlayer2.setItemStackForSlot(next2++, stack, false);
            }
            toFirst = !toFirst;
        }

        record.clearHopeChest();

        if(next1 > 0){
            queueDelivery(record.getPlayer1Uuid(), forPlayer1);
        }
        if(next2 > 0){
            queueDelivery(record.getPlayer2Uuid(), forPlayer2);
        }
    }

    // === lifetime tallies ===

    /** In-memory index of the persisted tallies, keyed by UUID string. */
    private static final Map<String, PlayerTally> TALLIES = new ConcurrentHashMap<>();

    /** This player's lifetime record, created empty on first use. */
    @Nonnull
    private static PlayerTally tallyFor(@Nonnull String uuid, @Nonnull String name){
        PlayerTally tally = TALLIES.computeIfAbsent(uuid, key -> new PlayerTally(key, name));
        if(!name.isBlank() && !name.equals(tally.getName())){
            tally.setName(name);
        }
        return tally;
    }

    /**
     * Finds a player by username for the admin commands, online or not.
     *
     * <p>Online players are matched first through the universe, which is the only
     * authoritative source. Failing that, the registry's own cached display names
     * are searched — marriage records first, then lifetime tallies — so an admin
     * can still act on somebody who has not logged in for months. That is the
     * whole reason these commands take a name rather than an online-player
     * argument: the person you most need to force-divorce is usually the one who
     * is not here.</p>
     *
     * <p>Cached names can be stale if a player renamed while away; the UUID they
     * resolve to is still correct, which is what matters.</p>
     */
    @Nullable
    public static UUID findPlayerUuid(@Nonnull String username){
        PlayerRef online = Universe.get().getPlayer(username, NameMatching.EXACT_IGNORE_CASE);
        if(online != null && online.isValid()){
            return online.getUuid();
        }

        for(MarriageRecord record : BY_PLAYER.values()){
            if(username.equalsIgnoreCase(record.getPlayer1Name())){
                return parseUuid(record.getPlayer1Uuid());
            }
            if(username.equalsIgnoreCase(record.getPlayer2Name())){
                return parseUuid(record.getPlayer2Uuid());
            }
        }

        for(PlayerTally tally : TALLIES.values()){
            if(username.equalsIgnoreCase(tally.getName())){
                return parseUuid(tally.getUuid());
            }
        }
        return null;
    }

    @Nullable
    private static UUID parseUuid(@Nullable String uuid){
        if(uuid == null || uuid.isBlank()){
            return null;
        }
        try{
            return UUID.fromString(uuid);
        }catch(IllegalArgumentException corrupt){
            return null;
        }
    }

    /** The best display name known for a UUID, for admin output. */
    @Nonnull
    public static String getKnownName(@Nonnull UUID player){
        PlayerRef online = Universe.get().getPlayer(player);
        if(online != null && online.isValid()){
            return online.getUsername();
        }
        MarriageRecord record = BY_PLAYER.get(player);
        if(record != null){
            return record.getNameOf(player);
        }
        PlayerTally tally = TALLIES.get(player.toString());
        return tally != null ? tally.getName() : player.toString();
    }

    /** This player's lifetime tally, or null if they have never married. */
    @Nullable
    public static PlayerTally getTally(@Nonnull UUID player){
        return TALLIES.get(player.toString());
    }

    /** Every lifetime tally on the server. Used by the Records page. */
    @Nonnull
    public static List<PlayerTally> getAllTallies(){
        return new ArrayList<>(TALLIES.values());
    }

    /** Refreshes the cached display name on a player's tally as they join. */
    public static synchronized void touchTally(@Nonnull UUID player, @Nonnull String name){
        String key = player.toString();
        PlayerTally tally = TALLIES.get(key);
        if(tally != null && !name.equals(tally.getName())){
            tally.setName(name);
            persist();
        }
    }

    /** Writes TALLIES back onto the data object. Called from persist(). */
    private static void syncTallies(){
        if(dataFile == null){
            return;
        }
        dataFile.get().setPlayerTallies(TALLIES.values().toArray(PlayerTally[]::new));
    }

    /**
     * Gives both partners a copy of an item, straight into their hands when they
     * are online and into the pending queue when they are not.
     *
     * <p>Everything goes through the queue rather than being handed over inline,
     * even for an online player: this is called from the bond tick, which runs on
     * one partner's world thread and must not touch the other's inventory. The
     * queue is drained on join, and for somebody already online that is their next
     * login - a small delay for the offline-safety of never dropping an item.</p>
     */
    public static synchronized void deliverToBoth(@Nonnull MarriageRecord record, @Nonnull ItemStack stack,
                                                  @Nonnull String reason){
        for(String uuid : new String[]{ record.getPlayer1Uuid(), record.getPlayer2Uuid() }){
            SimpleItemContainer parcel = new SimpleItemContainer((short) 1);
            parcel.setItemStackForSlot((short) 0, stack, false);
            queueDelivery(uuid, parcel, reason);
        }
        persist();
    }

    /** Appends a delivery for a player, merging with anything already owed them. */
    private static void queueDelivery(String uuid, SimpleItemContainer items){
        queueDelivery(uuid, items, PendingDelivery.REASON_HOPE_CHEST);
    }

    private static void queueDelivery(String uuid, SimpleItemContainer items, String reason){
        if(dataFile == null || uuid == null || uuid.isBlank()){
            return;
        }
        List<PendingDelivery> deliveries = new ArrayList<>(Arrays.asList(dataFile.get().getPendingDeliveries()));
        deliveries.add(new PendingDelivery(uuid, reason, items));
        dataFile.get().setPendingDeliveries(deliveries.toArray(PendingDelivery[]::new));
    }

    /**
     * Takes everything owed to a player off the queue. The caller is responsible
     * for actually putting the items somewhere — anything it cannot place must be
     * dropped at the player's feet, never discarded.
     */
    @Nonnull
    public static synchronized List<PendingDelivery> consumeDeliveries(@Nonnull UUID player){
        if(dataFile == null){
            return List.of();
        }
        PendingDelivery[] all = dataFile.get().getPendingDeliveries();
        if(all.length == 0){
            return List.of();
        }

        String wanted = player.toString();
        List<PendingDelivery> mine = new ArrayList<>();
        List<PendingDelivery> rest = new ArrayList<>();
        for(PendingDelivery delivery : all){
            if(wanted.equals(delivery.getUuid())){
                mine.add(delivery);
            }else{
                rest.add(delivery);
            }
        }
        if(mine.isEmpty()){
            return List.of();
        }

        dataFile.get().setPendingDeliveries(rest.toArray(PendingDelivery[]::new));
        persist();
        return mine;
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

    /**
     * Whether this player's couple channel is on.
     *
     * <p>Cached here rather than read from their settings component because the
     * only consumer is {@code PlayerChatEvent}, which is asynchronous and carries
     * no guarantee of running on the sender's world thread. World threads write
     * this on join and on toggle; any thread may read it.</p>
     */
    public static boolean isCoupleChannelOn(@Nonnull UUID player){
        return COUPLE_CHANNEL.getOrDefault(player, false);
    }

    public static void setCoupleChannelOn(@Nonnull UUID player, boolean on){
        COUPLE_CHANNEL.put(player, on);
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
        // Bond-scaled rather than the flat config value - a deepened bond is what
        // shortens the wait (see BondManager.tpCooldownFor).
        TP_COOLDOWN.put(player, System.currentTimeMillis()
                + BondManager.tpCooldownFor(BY_PLAYER.get(player)) * 1000L);
    }

    /** Seconds left on the shared-home cooldown, 0 when ready. */
    public static long getHomeCooldownRemaining(@Nonnull UUID player){
        return cooldownRemaining(HOME_COOLDOWN, player);
    }

    public static void markHomeUsed(@Nonnull UUID player){
        HOME_COOLDOWN.put(player, System.currentTimeMillis() + Marriage.getConfig().get().getHomeCooldownSeconds() * 1000L);
    }

    /** Seconds left on the respawn-at-partner cooldown, 0 when ready. */
    public static long getRespawnCooldownRemaining(@Nonnull UUID player){
        return cooldownRemaining(RESPAWN_COOLDOWN, player);
    }

    public static void markRespawnUsed(@Nonnull UUID player){
        // Vow-scaled: the Wander vow shortens it (see BondManager.respawnCooldownFor).
        RESPAWN_COOLDOWN.put(player, System.currentTimeMillis()
                + BondManager.respawnCooldownFor(BY_PLAYER.get(player)) * 1000L);
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
        syncTallies();
        dataFile.save();
    }
}
