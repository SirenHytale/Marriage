package plugin.siren.Utils.Bond;

import plugin.siren.Marriage;
import plugin.siren.Utils.Registry.MarriageRecord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * What to call a marriage of a given depth.
 *
 * <h2>Derived, never stored</h2>
 *
 * <p>A title is a reading of the bond level, so it has no field of its own and
 * cannot drift out of step with the number it describes.</p>
 *
 * <p>The thresholds are <b>fractions of the configured maximum</b> rather than
 * fixed levels, so a server that sets {@code Bond-Max-Level} to 5 or to 50 still
 * gets the whole ladder spread across it. Hard-coding "level 8 is Inseparable"
 * would have made four of five titles unreachable on a short ladder and
 * near-instant on a long one.</p>
 *
 * <h2>Deliberately not in chat</h2>
 *
 * <p>These appear in the menus and nowhere else. Putting them on the chat line
 * means wrapping {@code PlayerChatEvent}'s formatter, which this mod already does
 * for the couple channel and Cultivation does for its own titles — three
 * wrappers deep, the outcome depends on load order, and the failure mode is
 * every chat line on the server. Not worth it for a decoration.</p>
 */
public enum BondTitle {

    NEWLYWEDS("newlyweds", 0.0),
    DEVOTED("devoted", 0.25),
    STEADFAST("steadfast", 0.5),
    INSEPARABLE("inseparable", 0.75),
    ETERNAL("eternal", 1.0);

    private static final String LANG = "server.marriage.bondTitle.";

    private final String id;
    /** Fraction of the maximum bond level at which this title begins. */
    private final double atFraction;

    BondTitle(String id, double atFraction){
        this.id = id;
        this.atFraction = atFraction;
    }

    @Nonnull
    public String getId(){
        return this.id;
    }

    @Nonnull
    public String getNameKey(){
        return LANG + this.id;
    }

    /** The title for a bond level, on this server's ladder. */
    @Nonnull
    public static BondTitle forLevel(int level){
        int max = Math.max(1, Marriage.getConfig().get().getBondMaxLevel());
        double fraction = Math.min(1.0, Math.max(0.0, (double) level / max));

        BondTitle title = NEWLYWEDS;
        for(BondTitle candidate : values()){
            if(fraction >= candidate.atFraction){
                title = candidate;
            }
        }
        return title;
    }

    /** The title for a marriage, or {@link #NEWLYWEDS} for none. */
    @Nonnull
    public static BondTitle of(@Nullable MarriageRecord record){
        return record == null ? NEWLYWEDS : forLevel(record.getBondLevel());
    }
}
