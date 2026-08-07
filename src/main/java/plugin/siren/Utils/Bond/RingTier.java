package plugin.siren.Utils.Bond;

import plugin.siren.Marriage;
import plugin.siren.Utils.MarriageConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

/**
 * The band a couple married with, and what it is worth to them afterwards.
 *
 * <h2>Why the tier is recorded on the marriage</h2>
 *
 * <p>The ring is either consumed at the wedding or engraved and kept, so it
 * cannot be the thing that is checked later — a couple would lose their perks by
 * putting the ring in a chest. The tier is read once, at the moment they marry,
 * and stored on the record. What they do with the band afterwards is theirs.</p>
 *
 * <h2>What a better band buys</h2>
 *
 * <p>Two things, both configurable: bond XP accrues faster, and every perk
 * unlocks a level or two sooner. The discount is what makes a Diamond band
 * genuinely "better perks" rather than merely "the same perks, eventually" —
 * and it is capped by the perk's own required level, so a discount can never
 * push a requirement below zero or unlock something the server disabled.</p>
 *
 * <p>Ordinal order is tier order, and {@link #COPPER} must stay first: it is the
 * fallback for a marriage recorded before 2.1.0 and for a wedding held with no
 * ring at all.</p>
 */
public enum RingTier {

    /**
     * The original {@code Marriage_Ring}. Kept under its 2.0.x item id so that
     * every ring already sitting in a chest stays a valid wedding band - renaming
     * it would have silently voided them.
     */
    COPPER("Marriage_Ring",
            config -> 1.0,
            config -> 0),

    GOLD("Marriage_Ring_Gold",
            MarriageConfig::getGoldRingXpMultiplier,
            MarriageConfig::getGoldRingPerkDiscount),

    DIAMOND("Marriage_Ring_Diamond",
            MarriageConfig::getDiamondRingXpMultiplier,
            MarriageConfig::getDiamondRingPerkDiscount);

    private static final String LANG = "server.marriage.ring.";

    private final String itemId;
    private final ToDoubleFunction<MarriageConfig> xpMultiplier;
    private final ToIntFunction<MarriageConfig> perkDiscount;

    RingTier(String itemId, ToDoubleFunction<MarriageConfig> xpMultiplier, ToIntFunction<MarriageConfig> perkDiscount){
        this.itemId = itemId;
        this.xpMultiplier = xpMultiplier;
        this.perkDiscount = perkDiscount;
    }

    @Nonnull
    public String getItemId(){
        return this.itemId;
    }

    @Nonnull
    public String getNameKey(){
        return LANG + name().toLowerCase() + ".name";
    }

    /** How much faster this band grows a bond. */
    public double getXpMultiplier(){
        return Math.max(0.0, this.xpMultiplier.applyAsDouble(Marriage.getConfig().get()));
    }

    /** How many bond levels this band knocks off every perk requirement. */
    public int getPerkDiscount(){
        return Math.max(0, this.perkDiscount.applyAsInt(Marriage.getConfig().get()));
    }

    /** The tier for an item id, or null when the id is not a wedding band at all. */
    @Nullable
    public static RingTier fromItemId(@Nullable String itemId){
        if(itemId == null){
            return null;
        }
        for(RingTier tier : values()){
            if(tier.itemId.equalsIgnoreCase(itemId)){
                return tier;
            }
        }
        return null;
    }

    /** The tier a stored ordinal names, falling back to {@link #COPPER}. */
    @Nonnull
    public static RingTier fromOrdinal(int ordinal){
        RingTier[] tiers = values();
        return ordinal >= 0 && ordinal < tiers.length ? tiers[ordinal] : COPPER;
    }

    /** Whether this item id is any wedding band. */
    public static boolean isRing(@Nullable String itemId){
        return fromItemId(itemId) != null;
    }
}
