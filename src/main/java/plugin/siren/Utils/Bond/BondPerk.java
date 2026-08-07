package plugin.siren.Utils.Bond;

import plugin.siren.Marriage;
import plugin.siren.Utils.MarriageConfig;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * The perks a bond unlocks as it deepens.
 *
 * <p>Each constant binds its own master switch and level requirement straight to
 * the config getters rather than caching them, so an admin editing either one on
 * the in-game Config page takes effect on the next click — no restart, no
 * re-registration.</p>
 *
 * <p>Ordinal order is display order on the Bond page. Adding a perk here and
 * giving it a name/detail lang key is all a new perk needs to appear there; the
 * gate itself lives wherever the perk is used, behind
 * {@link BondManager#hasPerk}.</p>
 */
public enum BondPerk {

    /**
     * Told where your partner fell, with a marker on the spot. First because it is
     * the one perk whose whole value is that you have it before you need it.
     */
    DEATH_ALERT("death_alert",
            MarriageConfig::ifDeathAlertEnabled,
            MarriageConfig::getDeathAlertBondLevel),

    /** Your partner's live position on the world map. */
    PARTNER_MARKER("partner_marker",
            MarriageConfig::ifPartnerMarkerEnabled,
            MarriageConfig::getPartnerMarkerBondLevel),

    /** One home point the couple shares, set and travelled to by either of them. */
    SHARED_HOME("shared_home",
            MarriageConfig::ifSharedHomeEnabled,
            MarriageConfig::getSharedHomeBondLevel),

    /** A share of the damage aimed at one partner lands on the other instead. */
    BOND_OF_PAIN("bond_of_pain",
            MarriageConfig::ifBondOfPainEnabled,
            MarriageConfig::getBondOfPainBondLevel),

    /** A parting gift when your partner falls: a burst of regeneration. */
    LAST_STAND("last_stand",
            MarriageConfig::ifLastStandEnabled,
            MarriageConfig::getLastStandBondLevel),

    /** One container both partners can open from anywhere. */
    HOPE_CHEST("hope_chest",
            MarriageConfig::ifHopeChestEnabled,
            MarriageConfig::getHopeChestBondLevel),

    /** Respawn beside your partner instead of at your bed. */
    RESPAWN_AT_PARTNER("respawn_at_partner",
            MarriageConfig::ifRespawnAtPartnerEnabled,
            MarriageConfig::getRespawnAtPartnerBondLevel);

    private static final String LANG = "server.marriage.bond.perk.";

    private final String id;
    private final Function<MarriageConfig, Boolean> enabled;
    private final ToIntFunction<MarriageConfig> requiredLevel;

    BondPerk(String id, Function<MarriageConfig, Boolean> enabled, ToIntFunction<MarriageConfig> requiredLevel){
        this.id = id;
        this.enabled = enabled;
        this.requiredLevel = requiredLevel;
    }

    @Nonnull
    public String getId(){
        return this.id;
    }

    /** The perk's master config switch. A disabled perk is never unlocked for anyone. */
    public boolean isEnabled(){
        return Boolean.TRUE.equals(this.enabled.apply(Marriage.getConfig().get()));
    }

    /** The bond level this perk unlocks at. 0 means "from the wedding day". */
    public int getRequiredLevel(){
        return Math.max(0, this.requiredLevel.applyAsInt(Marriage.getConfig().get()));
    }

    @Nonnull
    public String getNameKey(){
        return LANG + this.id + ".name";
    }

    @Nonnull
    public String getDetailKey(){
        return LANG + this.id + ".detail";
    }
}
