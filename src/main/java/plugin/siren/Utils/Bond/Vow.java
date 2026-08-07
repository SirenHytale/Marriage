package plugin.siren.Utils.Bond;

import plugin.siren.Marriage;
import plugin.siren.Utils.MarriageConfig;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

/**
 * The promise a couple made each other, and what it shapes.
 *
 * <h2>Why this exists</h2>
 *
 * <p>Before vows, every couple at the same bond level had exactly the same
 * marriage. A vow makes the depth mean something particular: two couples both at
 * level 10 now differ in what that depth reached first and what it is best at.</p>
 *
 * <h2>How it acts</h2>
 *
 * <p>Two levers, both already built for something else, which is what makes this
 * cheap: a <b>perk level discount</b> on the vow's own perks (the same mechanism
 * {@link RingTier} uses, applied through
 * {@link BondManager#requiredLevelFor}), and a small set of <b>tuning bonuses</b>
 * read at the point each perk is used. A vow never unlocks a perk the server has
 * switched off, and never grants a perk outside its own set.</p>
 *
 * <p>{@link #NONE} must stay first: it is what every marriage from before vows
 * existed decodes to, and what a couple has until they choose.</p>
 */
public enum Vow {

    /** Not yet sworn. No discounts, no bonuses - the 2.1.0 behavior exactly. */
    NONE("none",
            Set.of(),
            config -> 0,
            config -> 0.0),

    /** The hearth: a home and a chest, sooner and larger. */
    HEARTH("hearth",
            Set.of(BondPerk.SHARED_HOME, BondPerk.HOPE_CHEST),
            MarriageConfig::getVowPerkDiscount,
            MarriageConfig::getVowHearthChestBonusSlots),

    /** The blade: what one of you takes, the other shares - harder, and sooner. */
    BLADE("blade",
            Set.of(BondPerk.BOND_OF_PAIN, BondPerk.LAST_STAND),
            MarriageConfig::getVowPerkDiscount,
            MarriageConfig::getVowBladeShareBonusPercent),

    /** The road: never far apart for long. */
    WANDER("wander",
            Set.of(BondPerk.RESPAWN_AT_PARTNER, BondPerk.PARTNER_MARKER),
            MarriageConfig::getVowPerkDiscount,
            MarriageConfig::getVowWanderCooldownCutPercent);

    private static final String LANG = "server.marriage.vow.";

    private final String id;
    private final Set<BondPerk> perks;
    private final ToIntFunction<MarriageConfig> discount;
    private final ToDoubleFunction<MarriageConfig> bonus;

    Vow(String id, Set<BondPerk> perks, ToIntFunction<MarriageConfig> discount, ToDoubleFunction<MarriageConfig> bonus){
        this.id = id;
        this.perks = perks;
        this.discount = discount;
        this.bonus = bonus;
    }

    @Nonnull
    public String getId(){
        return this.id;
    }

    @Nonnull
    public String getNameKey(){
        return LANG + this.id + ".name";
    }

    @Nonnull
    public String getDetailKey(){
        return LANG + this.id + ".detail";
    }

    /** The perks this vow favors. Empty for {@link #NONE}. */
    @Nonnull
    public Set<BondPerk> getPerks(){
        return this.perks;
    }

    public boolean favors(@Nonnull BondPerk perk){
        return this.perks.contains(perk);
    }

    /** Bond levels this vow knocks off the requirement of a perk it favors. */
    public int getPerkDiscount(){
        return this == NONE ? 0 : Math.max(0, this.discount.applyAsInt(Marriage.getConfig().get()));
    }

    /**
     * This vow's one tuning number, meaning whatever the vow is about: extra
     * Hope Chest slots, extra bond-of-pain share, or percent off the teleport and
     * respawn cooldowns. Zero for {@link #NONE}.
     */
    public double getBonus(){
        return this == NONE ? 0.0 : Math.max(0.0, this.bonus.applyAsDouble(Marriage.getConfig().get()));
    }

    /** The vow a stored ordinal names, falling back to {@link #NONE}. */
    @Nonnull
    public static Vow fromOrdinal(int ordinal){
        Vow[] vows = values();
        return ordinal >= 0 && ordinal < vows.length ? vows[ordinal] : NONE;
    }

    /** The vow with this id, or {@link #NONE}. */
    @Nonnull
    public static Vow fromId(String id){
        if(id != null){
            for(Vow vow : values()){
                if(vow.id.equalsIgnoreCase(id)){
                    return vow;
                }
            }
        }
        return NONE;
    }

    /** Every vow a couple may actually swear - {@link #NONE} is not a choice. */
    @Nonnull
    public static Vow[] choices(){
        return new Vow[]{ HEARTH, BLADE, WANDER };
    }
}
