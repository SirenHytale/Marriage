package plugin.siren.API;

import plugin.siren.Marriage;
import plugin.siren.Utils.Bond.BondManager;
import plugin.siren.Utils.Bond.BondPerk;
import plugin.siren.Utils.Bond.BondXpReason;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Public facade for other mods. Everything is UUID-keyed and works for offline
 * players — the global registry is the source of truth, not entity components.
 * All reads are lock-free and safe from any world thread.
 */
public final class MarriageAPI {

    private MarriageAPI(){}

    // === lookups ===

    public static boolean isMarried(@Nonnull UUID player){
        return MarriageManager.isMarried(player);
    }

    /** True when the two players are married to each other. */
    public static boolean areMarried(@Nonnull UUID playerA, @Nonnull UUID playerB){
        return MarriageManager.areMarried(playerA, playerB);
    }

    @Nullable
    public static UUID getPartner(@Nonnull UUID player){
        return MarriageManager.getPartnerUuid(player);
    }

    /** Cached display name of the partner (refreshed on join), null when unmarried. */
    @Nullable
    public static String getPartnerName(@Nonnull UUID player){
        return MarriageManager.getPartnerName(player);
    }

    /** Immutable snapshot of this player's marriage, null when unmarried. */
    @Nullable
    public static MarriageInfo getMarriage(@Nonnull UUID player){
        return MarriageInfo.of(MarriageManager.getRecord(player));
    }

    /** Every marriage on the server — offline couples included — oldest first. */
    @Nonnull
    public static List<MarriageInfo> getAllMarriages(){
        return MarriageManager.getAllMarriages();
    }

    // === stats ===

    /** Number of currently married couples (offline included). */
    public static int getMarriedCoupleCount(){
        return MarriageManager.getMarriedCoupleCount();
    }

    /** All-time marriage count, including marriages that later ended. */
    public static int getTotalMarriagesEver(){
        return MarriageManager.getTotalMarriagesEver();
    }

    /** All-time divorce count. */
    public static int getTotalDivorces(){
        return MarriageManager.getTotalDivorces();
    }

    /** The couple married the longest right now, null when nobody is married. */
    @Nullable
    public static MarriageInfo getLongestCurrentMarriage(){
        return MarriageManager.getAllMarriages().stream()
                .min(Comparator.comparingLong(MarriageInfo::getMarriedAtMillis))
                .orElse(null);
    }

    /** Whether damage from {@code attacker} to {@code victim} is blocked by partner PvP protection. */
    public static boolean isPvpProtected(@Nonnull UUID attacker, @Nonnull UUID victim){
        return MarriageManager.isPvpProtected(attacker, victim);
    }

    // === mutations (for admin/companion mods; fire the same events as commands) ===

    /**
     * Marries two players directly, skipping the request handshake (still fires the
     * cancellable PreMarryEvent). Names are display caches. Returns false if either
     * is already married or a listener cancelled.
     */
    public static boolean marry(@Nonnull UUID player1, @Nonnull String player1Name,
                                @Nonnull UUID player2, @Nonnull String player2Name){
        if(player1.equals(player2) || isMarried(player1) || isMarried(player2)){
            return false;
        }
        if(!MarriageEvents.firePreMarry(new MarriageEvents.PreMarryEvent(player1, player1Name, player2, player2Name))){
            return false;
        }
        return MarriageManager.adminMarry(player1, player1Name, player2, player2Name);
    }

    /**
     * Ends a player's marriage immediately (no chat confirmation, still fires the
     * cancellable PreDivorceEvent). Returns true if a marriage ended.
     */
    public static boolean divorce(@Nonnull UUID player){
        MarriageInfo marriage = getMarriage(player);
        if(marriage == null){
            return false;
        }
        if(!MarriageEvents.firePreDivorce(new MarriageEvents.PreDivorceEvent(marriage, player))){
            return false;
        }
        return MarriageManager.forceDivorce(player);
    }

    /** Toggles the couple's PvP protection. Returns false when unmarried. */
    public static boolean setPvpProtection(@Nonnull UUID player, boolean enabled){
        return MarriageManager.setPvpProtection(player, enabled);
    }

    // === bond (2.1.0) ===

    /** The couple's bond level, or -1 when the player is unmarried. */
    public static int getBondLevel(@Nonnull UUID player){
        MarriageRecord record = MarriageManager.getRecord(player);
        return record == null ? -1 : record.getBondLevel();
    }

    /** 0..1 progress through the current bond level, or 0 when unmarried. */
    public static float getBondProgress(@Nonnull UUID player){
        MarriageRecord record = MarriageManager.getRecord(player);
        return record == null ? 0f : BondManager.progress(record);
    }

    /**
     * Grants (or, for a negative amount, removes) bond XP, announcing any level it
     * crosses exactly as an in-game source would.
     *
     * @return the couple's new bond level, or -1 when the player is unmarried.
     */
    public static int addBondXp(@Nonnull UUID player, double amount){
        return MarriageManager.addBondXp(player, amount, BondXpReason.EXTERNAL);
    }

    /** Whether this couple has unlocked a given perk — feature on and bond deep enough. */
    public static boolean hasPerk(@Nonnull UUID player, @Nonnull BondPerk perk){
        return BondManager.hasPerk(player, perk);
    }

    /** Total XP needed to reach a bond level from nothing. */
    public static double getXpForLevel(int level){
        return BondManager.xpForLevel(level);
    }

    /** The highest bond level this server allows. */
    public static int getMaxBondLevel(){
        return Marriage.getConfig().get().getBondMaxLevel();
    }

    // === listeners ===

    /** Shortcut for {@link MarriageEvents#registerListener}. */
    public static void registerListener(@Nonnull MarriageEvents.MarriageListener listener){
        MarriageEvents.registerListener(listener);
    }

    /** The running Marriage mod version. */
    @Nonnull
    public static String getVersion(){
        return Marriage.getVersion();
    }
}
