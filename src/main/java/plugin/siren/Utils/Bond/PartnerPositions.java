package plugin.siren.Utils.Bond;

import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Where each married player was, last time a world thread looked.
 *
 * <h2>Why this exists at all</h2>
 *
 * <p>The partner map marker has to answer "where is my partner right now" from the
 * world MAP thread, and the ECS is entirely off limits there — a single
 * {@code Store.getComponent} takes the map thread down (the same constraint
 * Cultivation's {@code SpiritSenseMarkers} documents). So the position cannot be
 * read when the marker is drawn; it has to already be somewhere safe by then.</p>
 *
 * <p>{@code MarriagePresenceSystem} writes here from the world thread that owns
 * each player, and the marker provider reads. A {@link ConcurrentHashMap} of
 * immutable snapshots is the whole synchronization story: a marker one tick stale
 * is a pin that lags its player by two seconds, which is what a map marker looks
 * like anyway.</p>
 *
 * <h2>Deaths</h2>
 *
 * <p>Death sites live here too rather than in a second class, because they are the
 * same shape of thing (a world, a point, an expiry) read from the same thread by
 * the same provider. A death entry outlives the player's own marker and expires on
 * its own clock — see {@link #recordDeath}.</p>
 */
public final class PartnerPositions {

    /** An immutable "here, in this world, as of then" snapshot. */
    public record Snapshot(@Nonnull String world, double x, double y, double z, long atMillis) {

        @Nonnull
        public Vector3d toVector(){
            return new Vector3d(this.x, this.y, this.z);
        }

        public boolean isStale(long now, long maxAgeMillis){
            return now - this.atMillis > maxAgeMillis;
        }
    }

    /** Live positions, refreshed every presence tick while the player is online. */
    private static final Map<UUID, Snapshot> POSITIONS = new ConcurrentHashMap<>();

    /** Where each player last died, and when the marker for it stops being shown. */
    private static final Map<UUID, Snapshot> DEATHS = new ConcurrentHashMap<>();

    private PartnerPositions(){}

    public static void record(@Nonnull UUID player, @Nonnull String world, double x, double y, double z){
        POSITIONS.put(player, new Snapshot(world, x, y, z, System.currentTimeMillis()));
    }

    @Nullable
    public static Snapshot get(@Nonnull UUID player){
        return POSITIONS.get(player);
    }

    /** Drops a player's live position — called on disconnect so their pin goes away. */
    public static void forget(@Nonnull UUID player){
        POSITIONS.remove(player);
    }

    public static void recordDeath(@Nonnull UUID player, @Nonnull String world, double x, double y, double z){
        DEATHS.put(player, new Snapshot(world, x, y, z, System.currentTimeMillis()));
    }

    /**
     * Where this player last died, or null when they have not died recently enough
     * for the marker to still be worth showing.
     */
    @Nullable
    public static Snapshot getDeath(@Nonnull UUID player, long maxAgeMillis){
        Snapshot death = DEATHS.get(player);
        if(death == null){
            return null;
        }
        if(death.isStale(System.currentTimeMillis(), maxAgeMillis)){
            // Pruned on read rather than on a timer: nothing else ever needs to
            // walk this map, so an expired entry costs nothing until it is asked
            // for, and asking is exactly when it can be cleaned up.
            DEATHS.remove(player, death);
            return null;
        }
        return death;
    }

    public static void forgetDeath(@Nonnull UUID player){
        DEATHS.remove(player);
    }

    /** Drops everything remembered about a player. Called on divorce and disconnect. */
    public static void clear(@Nonnull UUID player){
        POSITIONS.remove(player);
        DEATHS.remove(player);
    }
}
