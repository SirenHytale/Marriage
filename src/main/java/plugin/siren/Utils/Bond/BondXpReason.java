package plugin.siren.Utils.Bond;

/**
 * What earned (or cost) a couple bond XP. Travels with
 * {@code MarriageEvents.BondLevelChangeEvent} so a companion mod can tell a
 * milestone reached by living together from one bought with a gift.
 */
public enum BondXpReason {
    /** Time spent married, granted per hour whether or not either partner is online. */
    TIME_MARRIED,
    /** Time spent within bond range of each other. */
    TOGETHER,
    /** A gift handed to the partner. */
    GIFT,
    /** A creature or player killed with the partner nearby. */
    COOP_KILL,
    /** An anniversary milestone. */
    ANNIVERSARY,
    /** Bond decay from time apart. */
    DECAY,
    /** The couple renewed their vows. */
    RENEWAL,
    /** An admin or another mod set it directly through the API. */
    EXTERNAL
}
