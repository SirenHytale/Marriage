package plugin.siren.Utils.Bond;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import plugin.siren.API.MarriageEvents;
import plugin.siren.API.MarriageInfo;
import plugin.siren.Marriage;
import plugin.siren.Utils.MarriageConfig;
import plugin.siren.Utils.MarriageUtil;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;
import plugin.siren.Utils.Registry.PendingDelivery;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.UUID;

/**
 * The bond: a marriage's own progression. Everything that reads or writes bond XP,
 * turns it into a level, and decides what a level is worth goes through here.
 *
 * <h2>Why the level is stored rather than derived</h2>
 *
 * <p>{@link #levelForXp} is pure and could be called at every read, but the level
 * is also persisted on the record. That is deliberate: the level is what fires the
 * "your bond deepened" announcement and what unlocks a perk, so the system needs
 * to know the level the couple was at <em>last</em> tick to notice a crossing. A
 * derived-only level would re-announce every level on every restart, or need a
 * second field to remember it anyway.</p>
 *
 * <h2>Threading</h2>
 *
 * <p>Every mutator here is a {@code synchronized} static on {@link MarriageManager}
 * or ends in one, matching the registry's existing "mutate under the class monitor,
 * read lock-free" contract. Grants arrive from world threads (a gift, a kill, the
 * bond tick) and there is no ordering requirement between them — XP is commutative.</p>
 */
public final class BondManager {

    /** Anniversary milestones, in days married. Index order IS the award order. */
    public static final int[] ANNIVERSARY_DAYS = {1, 7, 30, 365};

    private static final String LANG = "server.marriage.bond.";

    private BondManager(){}

    // === the curve ===

    /**
     * Total XP needed to reach {@code level} from nothing.
     *
     * <p>Quadratic rather than linear: a bond that costs the same to deepen at
     * level 9 as at level 1 makes the last level a formality. The config exposes
     * the base only — the shape is fixed so that a server owner cannot
     * accidentally author a curve that never reaches the max level.</p>
     */
    public static double xpForLevel(int level){
        if(level <= 0){
            return 0.0;
        }
        double base = Marriage.getConfig().get().getBondXpPerLevel();
        return base * level * (level + 1) / 2.0;
    }

    /** The level a given total XP buys, capped at the configured maximum. */
    public static int levelForXp(double xp){
        int max = Marriage.getConfig().get().getBondMaxLevel();
        int level = 0;
        while(level < max && xp >= xpForLevel(level + 1)){
            level++;
        }
        return level;
    }

    /** XP banked toward the next level, 0 when already at the maximum. */
    public static double xpIntoLevel(@Nonnull MarriageRecord record){
        int level = record.getBondLevel();
        if(level >= Marriage.getConfig().get().getBondMaxLevel()){
            return 0.0;
        }
        return Math.max(0.0, record.getBondXp() - xpForLevel(level));
    }

    /** XP the next level costs from the start of the current one, 0 when maxed. */
    public static double xpForNextLevel(@Nonnull MarriageRecord record){
        int level = record.getBondLevel();
        if(level >= Marriage.getConfig().get().getBondMaxLevel()){
            return 0.0;
        }
        return Math.max(1.0, xpForLevel(level + 1) - xpForLevel(level));
    }

    /** 0..1 progress through the current level, 1 when maxed. */
    public static float progress(@Nonnull MarriageRecord record){
        double needed = xpForNextLevel(record);
        if(needed <= 0.0){
            return 1f;
        }
        return (float) Math.min(1.0, Math.max(0.0, xpIntoLevel(record) / needed));
    }

    public static boolean isMaxLevel(@Nonnull MarriageRecord record){
        return record.getBondLevel() >= Marriage.getConfig().get().getBondMaxLevel();
    }

    // === granting ===

    /**
     * Adds (or, for a negative amount, removes) bond XP for the couple this player
     * belongs to and announces any level the change crossed.
     *
     * @param reason what earned it, purely for the level-up message and the event.
     * @return the new level, or -1 when the player is not married.
     */
    public static int addXp(@Nonnull UUID player, double amount, @Nonnull BondXpReason reason){
        if(!Marriage.getConfig().get().ifBondEnabled() || amount == 0.0){
            MarriageRecord existing = MarriageManager.getRecord(player);
            return existing == null ? -1 : existing.getBondLevel();
        }
        return MarriageManager.addBondXp(player, amount, reason);
    }

    /**
     * Applies an XP change to an already-resolved record and reports whether the
     * level moved. Called from inside {@link MarriageManager}'s monitor — it does
     * not persist or announce, the caller does.
     *
     * @return the level before the change, so the caller can tell up from down.
     */
    public static int applyXp(@Nonnull MarriageRecord record, double amount){
        int before = record.getBondLevel();
        record.setBondXp(record.getBondXp() + scaleByRing(record, amount));
        record.setBondLevel(levelForXp(record.getBondXp()));
        return before;
    }

    /**
     * A gain multiplied by the couple's wedding band; a loss left alone.
     *
     * <p>Decay deliberately does not scale: a better band should make a bond grow
     * faster, not make neglect cost more. Multiplying a negative by 1.5 would have
     * punished the couple who paid the most for their ring.</p>
     */
    private static double scaleByRing(@Nonnull MarriageRecord record, double amount){
        if(amount <= 0.0){
            return amount;
        }
        double scaled = amount;
        if(Marriage.getConfig().get().ifRingTiersEnabled()){
            scaled *= record.getRingTier().getXpMultiplier();
        }
        if(isHoneymoon(record)){
            scaled *= Marriage.getConfig().get().getHoneymoonXpMultiplier();
        }
        return scaled;
    }

    // === honeymoon ===

    /**
     * Whether this couple are still in the first flush of it.
     *
     * <p>Measured from the wedding, not from the last renewal: a honeymoon is the
     * beginning of a marriage, and letting a renewal restart it would turn a
     * ceremony into a farmable XP button.</p>
     */
    public static boolean isHoneymoon(@Nullable MarriageRecord record){
        MarriageConfig config = Marriage.getConfig().get();
        if(record == null || !config.ifHoneymoonEnabled()){
            return false;
        }
        long window = (long) (config.getHoneymoonHours() * 60.0 * 60.0 * 1000.0);
        return System.currentTimeMillis() - record.getMarriedAtMillis() < window;
    }

    /** Minutes left of the honeymoon, 0 when it is over (or off). */
    public static long honeymoonMinutesLeft(@Nullable MarriageRecord record){
        if(!isHoneymoon(record)){
            return 0L;
        }
        MarriageConfig config = Marriage.getConfig().get();
        long window = (long) (config.getHoneymoonHours() * 60.0 * 60.0 * 1000.0);
        long left = record.getMarriedAtMillis() + window - System.currentTimeMillis();
        return left <= 0L ? 0L : (left + 59_999L) / 60_000L;
    }

    /**
     * Tells the couple their bond changed level. Sent to whichever partners are
     * online; a partner who was away simply sees the new level in the menu.
     */
    public static void announceLevelChange(@Nonnull MarriageRecord record, int oldLevel, @Nonnull BondXpReason reason){
        int newLevel = record.getBondLevel();
        if(newLevel == oldLevel){
            return;
        }

        boolean deepened = newLevel > oldLevel;
        String key = LANG + (deepened ? "levelUp" : "levelDown");
        Message message = Message.translation(key)
                .param("level", String.valueOf(newLevel))
                .color(deepened ? Color.PINK : Color.GRAY);

        sendToCouple(record, message);

        if(deepened){
            // A level that unlocks something says so, rather than leaving the
            // player to go hunting through the Bond page for what changed.
            for(BondPerk perk : BondPerk.values()){
                if(perk.isEnabled() && requiredLevelFor(record, perk) == newLevel){
                    sendToCouple(record, Message.translation(LANG + "perkUnlocked")
                            .param("perk", Message.translation(perk.getNameKey()))
                            .color(Color.PINK));
                }
            }
        }

        MarriageInfo info = MarriageInfo.of(record);
        if(info != null){
            MarriageEvents.fireBondLevelChange(new MarriageEvents.BondLevelChangeEvent(info, oldLevel, newLevel, reason));
        }
    }

    // === anniversaries ===

    /** How many milestones the couple has earned by now, by days married. */
    public static int earnedAnniversaries(@Nonnull MarriageRecord record){
        long days = daysMarried(record);
        int earned = 0;
        for(int milestone : ANNIVERSARY_DAYS){
            if(days >= milestone){
                earned++;
            }
        }
        return earned;
    }

    public static long daysMarried(@Nonnull MarriageRecord record){
        long millis = Math.max(0L, System.currentTimeMillis() - record.getMarriedAtMillis());
        return millis / (24L * 60L * 60L * 1000L);
    }

    /** The next milestone in days, or -1 once every one has been passed. */
    public static int nextAnniversaryDays(@Nonnull MarriageRecord record){
        long days = daysMarried(record);
        for(int milestone : ANNIVERSARY_DAYS){
            if(days < milestone){
                return milestone;
            }
        }
        return -1;
    }

    /**
     * Pays out every anniversary the couple has reached but not yet been given.
     * Catches up more than one at a time on purpose: a couple whose server was
     * down over their first week should not have the 7-day milestone silently
     * skipped, and a marriage imported from 1.x is already days old on arrival.
     *
     * @return true if anything was awarded (so the caller persists).
     */
    public static boolean awardDueAnniversaries(@Nonnull MarriageRecord record){
        MarriageConfig config = Marriage.getConfig().get();
        if(!config.ifAnniversariesEnabled()){
            return false;
        }

        int earned = earnedAnniversaries(record);
        int awarded = record.getAnniversariesAwarded();
        if(earned <= awarded){
            return false;
        }

        for(int index = awarded; index < earned; index++){
            int days = ANNIVERSARY_DAYS[index];
            int oldLevel = applyXp(record, config.getAnniversaryBondXp());

            Message toCouple = Message.translation(LANG + "anniversary")
                    .param("days", String.valueOf(days))
                    .color(Color.PINK);
            sendToCouple(record, toCouple);

            if(config.ifAnnounceAnniversaries()){
                MarriageUtil.broadcast(Message.translation(LANG + "anniversary.broadcast")
                        .param("usernameOne", record.getPlayer1Name())
                        .param("usernameTwo", record.getPlayer2Name())
                        .param("days", String.valueOf(days))
                        .color(Color.PINK));
            }

            announceLevelChange(record, oldLevel, BondXpReason.ANNIVERSARY);
            awardAnniversaryItem(record, index, days);

            MarriageInfo info = MarriageInfo.of(record);
            if(info != null){
                MarriageEvents.fireAnniversary(new MarriageEvents.AnniversaryEvent(info, days));
            }

            Marriage.LOGGER.atInfo().log(record.getPlayer1Name() + " and " + record.getPlayer2Name()
                    + " reached their " + days + "-day anniversary.");
        }

        record.setAnniversariesAwarded(earned);
        return true;
    }

    /**
     * Hands both partners the item their milestone is worth.
     *
     * <p>Rewards escalate with the milestone — the defaults run a Love Letter at a
     * week, a Gold Band at a month, a Diamond Band at a year — which closes a loop
     * with vow renewal: the better band an anniversary gives you is one you can
     * actually put on by renewing your vows.</p>
     *
     * <p>An offline partner is not skipped: their share goes to the same pending
     * delivery queue a divorced Hope Chest uses, so an anniversary reached while
     * they were away is still waiting when they return. A blank item id for a
     * milestone simply awards nothing.</p>
     */
    private static void awardAnniversaryItem(@Nonnull MarriageRecord record, int milestoneIndex, int days){
        String itemId = Marriage.getConfig().get().getAnniversaryRewardItem(milestoneIndex);
        int quantity = Marriage.getConfig().get().getAnniversaryRewardQuantity();
        if(itemId.isBlank() || quantity <= 0){
            return;
        }

        MarriageManager.deliverToBoth(record, new ItemStack(itemId, quantity),
                PendingDelivery.REASON_ANNIVERSARY);

        sendToCouple(record, Message.translation(LANG + "anniversary.gift")
                .param("days", String.valueOf(days))
                .color(Color.PINK));
    }

    // === perk gating ===

    /**
     * Whether this couple has unlocked a perk: the feature is switched on in
     * config and their bond has reached its required level.
     */
    public static boolean hasPerk(@Nullable MarriageRecord record, @Nonnull BondPerk perk){
        if(record == null || !perk.isEnabled()){
            return false;
        }
        // Bond disabled entirely means levels are meaningless, so every enabled
        // perk is simply available - a server that wants the perks without the
        // progression gets exactly that by turning Enable-Bond off.
        if(!Marriage.getConfig().get().ifBondEnabled()){
            return true;
        }
        return record.getBondLevel() >= requiredLevelFor(record, perk);
    }

    /**
     * The bond level this particular couple needs for a perk — the perk's own
     * requirement, less whatever their wedding band knocks off.
     *
     * <p>Floored at zero rather than allowed to go negative, so a generous
     * discount grants the perk on the wedding day instead of wrapping into
     * nonsense. A disabled perk keeps its raw requirement: the discount must
     * never be the thing that makes a switched-off feature look available.</p>
     */
    public static int requiredLevelFor(@Nullable MarriageRecord record, @Nonnull BondPerk perk){
        int required = perk.getRequiredLevel();
        if(record == null){
            return required;
        }

        int discount = 0;
        if(Marriage.getConfig().get().ifRingTiersEnabled()){
            discount += record.getRingTier().getPerkDiscount();
        }
        // The vow discounts only the perks it is about - that is what makes a vow
        // a choice rather than a flat bonus everyone takes.
        if(Marriage.getConfig().get().ifVowsEnabled() && record.getVow().favors(perk)){
            discount += record.getVow().getPerkDiscount();
        }
        return Math.max(0, required - discount);
    }

    public static boolean hasPerk(@Nonnull UUID player, @Nonnull BondPerk perk){
        return hasPerk(MarriageManager.getRecord(player), perk);
    }

    // === level-scaled tuning ===

    /**
     * The partner-regen radius this couple has earned. Flat config radius plus the
     * configured percentage per bond level.
     */
    public static double regenRadiusFor(@Nullable MarriageRecord record){
        MarriageConfig config = Marriage.getConfig().get();
        double base = config.getRegenRadiusBlocks();
        if(record == null || !config.ifBondEnabled()){
            return base;
        }
        return base * (1.0 + record.getBondLevel() * config.getBondRegenRadiusPercentPerLevel() / 100.0);
    }

    /**
     * The teleport cooldown this couple has earned, in seconds. Shrinks by the
     * configured percentage per level and never goes below zero.
     */
    public static int tpCooldownFor(@Nullable MarriageRecord record){
        MarriageConfig config = Marriage.getConfig().get();
        int base = config.getTpCooldownSeconds();
        if(record == null || base <= 0){
            return base;
        }
        double reduction = 0.0;
        if(config.ifBondEnabled()){
            reduction += record.getBondLevel() * config.getBondTpCooldownPercentPerLevel() / 100.0;
        }
        reduction += wanderCut(record);
        return (int) Math.max(0.0, Math.round(base * (1.0 - Math.min(1.0, reduction))));
    }

    /**
     * The Wander vow's cut of a cooldown, as a 0..1 fraction. Zero for every
     * other vow, and when vows are switched off.
     */
    public static double wanderCut(@Nullable MarriageRecord record){
        if(record == null || !Marriage.getConfig().get().ifVowsEnabled() || record.getVow() != Vow.WANDER){
            return 0.0;
        }
        return Math.min(1.0, record.getVow().getBonus() / 100.0);
    }

    /** The respawn-at-partner cooldown this couple has earned, in seconds. */
    public static int respawnCooldownFor(@Nullable MarriageRecord record){
        int base = Marriage.getConfig().get().getRespawnAtPartnerCooldownSeconds();
        if(record == null || base <= 0){
            return base;
        }
        return (int) Math.max(0.0, Math.round(base * (1.0 - wanderCut(record))));
    }

    /**
     * The Hope Chest size this couple gets: the configured slots plus whatever
     * the Hearth vow adds. Clamped to the same ceiling the config uses, so a vow
     * cannot produce a container the client has no room to draw.
     */
    public static short chestSlotsFor(@Nullable MarriageRecord record){
        MarriageConfig config = Marriage.getConfig().get();
        int slots = config.getHopeChestSlots();
        if(record != null && config.ifVowsEnabled() && record.getVow() == Vow.HEARTH){
            slots += (int) record.getVow().getBonus();
        }
        return (short) Math.min(54, Math.max(1, slots));
    }

    /**
     * The share of damage the bond of pain moves, as a 0..1 fraction, including
     * whatever the Blade vow adds. Capped at the same 90% the config is, so a vow
     * can never make one partner effectively immortal.
     */
    public static double painShareFor(@Nullable MarriageRecord record){
        MarriageConfig config = Marriage.getConfig().get();
        double percent = config.getBondOfPainSharePercent();
        if(record != null && config.ifVowsEnabled() && record.getVow() == Vow.BLADE){
            percent += record.getVow().getBonus();
        }
        return Math.min(90.0, Math.max(0.0, percent)) / 100.0;
    }

    // === helpers ===

    /** Sends a message to whichever halves of the couple are online right now. */
    public static void sendToCouple(@Nonnull MarriageRecord record, @Nonnull Message message){
        sendTo(record.getPlayer1Uuid(), message);
        sendTo(record.getPlayer2Uuid(), message);
    }

    private static void sendTo(@Nonnull String uuidString, @Nonnull Message message){
        UUID uuid;
        try{
            uuid = UUID.fromString(uuidString);
        }catch(IllegalArgumentException corrupt){
            return;
        }
        PlayerRef playerRef = Universe.get().getPlayer(uuid);
        if(playerRef != null && playerRef.isValid()){
            playerRef.sendMessage(message);
        }
    }
}
