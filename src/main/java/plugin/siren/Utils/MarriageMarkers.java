package plugin.siren.Utils;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.packets.worldmap.TintComponent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import plugin.siren.Marriage;
import plugin.siren.Utils.Bond.BondManager;
import plugin.siren.Utils.Bond.BondPerk;
import plugin.siren.Utils.Bond.PartnerPositions;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Puts a marriage on the world map: where your partner is, where they last fell,
 * and where the two of you call home.
 *
 * <h2>This runs on the world MAP thread, not the world thread</h2>
 *
 * <p>{@code WorldMapManager} calls every registered provider from its own thread,
 * where the ECS is entirely off limits — one {@code Store.getComponent} takes that
 * thread down. So nothing below reads an entity. The partner's position comes from
 * {@link PartnerPositions}, a plain concurrent map that world threads write and
 * this one reads, and everything else comes from the marriage record (whose fields
 * are written under {@code MarriageManager}'s monitor and read here without one —
 * a stale read can only mean a pin that lags a home relocation by one map update).</p>
 *
 * <h2>Marker images</h2>
 *
 * <p>These use the vanilla semantic images rather than the {@code UserA}–{@code UserF}
 * slots, because for once the vanilla meanings are exactly right: a partner IS a
 * player, a death site IS a death, a home IS a home. It also leaves all six user
 * slots free for other mods — Cultivation already claims every one of them, and two
 * of this workspace's mods fighting over {@code UserA} would put two unrelated pins
 * on one map with no way to tell them apart.</p>
 *
 * <p>Tint is what separates these from vanilla's own pins of the same image: every
 * marriage marker is rose, the mod's color throughout.</p>
 */
public final class MarriageMarkers implements WorldMapManager.MarkerProvider {

    public static final MarriageMarkers INSTANCE = new MarriageMarkers();

    /** The key this provider registers under, so a later call can replace it. */
    public static final String PROVIDER_KEY = "marriage_partner";

    private static final String PARTNER_MARKER = "Player.png";
    private static final String DEATH_MARKER = "Death.png";
    private static final String HOME_MARKER = "Home.png";

    /** The mod's rose, matching @RPink in MarriageTheme.ui. */
    private static final Color ROSE = rgb(0xFF5C9C);
    /** A deeper red for the death site - the one marker that is not good news. */
    private static final Color GRIEF = rgb(0xC03A55);

    /**
     * A partner pin older than this is dropped rather than shown at a stale spot.
     * Generous next to the two-second presence tick: it only ever bites when a
     * player's world thread has genuinely stopped ticking them.
     */
    private static final long POSITION_MAX_AGE_MILLIS = 30_000L;

    private MarriageMarkers(){}

    @Override
    public void update(@Nonnull World world, @Nonnull Player player, @Nonnull MarkersCollector collector){
        // getPlayerRef is deprecated in favor of "fetch the PlayerRef component
        // manually" - which is precisely what must not happen here, since a
        // Store.getComponent on this thread takes the map down. Until the engine
        // hands providers a PlayerRef directly, this accessor is the only safe
        // route, and it is the one every vanilla and Cultivation provider takes.
        @SuppressWarnings("removal")
        PlayerRef playerRef = player.getPlayerRef();
        if(playerRef == null || !playerRef.isValid()){
            return;
        }

        UUID uuid = playerRef.getUuid();
        MarriageRecord record = MarriageManager.getRecord(uuid);
        if(record == null){
            return;
        }

        UUID partnerUuid = record.getPartnerUuid(uuid);
        if(partnerUuid == null){
            return;
        }

        String worldName = world.getName();
        addPartner(record, uuid, partnerUuid, worldName, collector);
        addDeathSite(record, uuid, partnerUuid, worldName, collector);
        addHome(record, worldName, collector);
    }

    /** Your partner, where a world thread last saw them, if they are in this world. */
    private static void addPartner(@Nonnull MarriageRecord record, @Nonnull UUID viewer, @Nonnull UUID partnerUuid,
                                   @Nonnull String worldName, @Nonnull MarkersCollector collector){
        if(!BondManager.hasPerk(record, BondPerk.PARTNER_MARKER)){
            return;
        }

        PartnerPositions.Snapshot at = PartnerPositions.get(partnerUuid);
        if(at == null || !at.world().equals(worldName) || at.isStale(System.currentTimeMillis(), POSITION_MAX_AGE_MILLIS)){
            return;
        }

        Transform where = new Transform(at.x(), at.y(), at.z());
        if(!collector.isInViewDistance(where)){
            return;
        }

        collector.add(new MapMarkerBuilder(PROVIDER_KEY + "_partner", PARTNER_MARKER, where)
                .withName(Message.translation("server.marriage.marker.partner")
                        .param("partnerUsername", record.getPartnerName(viewer)))
                .withComponent(new TintComponent(ROSE))
                .build());
    }

    /**
     * Where your partner last fell, for as long as Death-Marker-Minutes says.
     *
     * <p>Real Y here, unlike the partner pin's, because "they died below you" is
     * exactly the thing you need to know when the body is down a ravine.</p>
     */
    private static void addDeathSite(@Nonnull MarriageRecord record, @Nonnull UUID viewer, @Nonnull UUID partnerUuid,
                                     @Nonnull String worldName, @Nonnull MarkersCollector collector){
        if(!BondManager.hasPerk(record, BondPerk.DEATH_ALERT)){
            return;
        }
        double minutes = Marriage.getConfig().get().getDeathMarkerMinutes();
        if(minutes <= 0.0){
            return;
        }

        PartnerPositions.Snapshot death = PartnerPositions.getDeath(partnerUuid, (long) (minutes * 60_000.0));
        if(death == null || !death.world().equals(worldName)){
            return;
        }

        Transform where = new Transform(death.x(), death.y(), death.z());
        if(!collector.isInViewDistance(where)){
            return;
        }

        collector.add(new MapMarkerBuilder(PROVIDER_KEY + "_death", DEATH_MARKER, where)
                .withName(Message.translation("server.marriage.marker.death")
                        .param("partnerUsername", record.getPartnerName(viewer)))
                .withComponent(new TintComponent(GRIEF))
                .build());
    }

    /** The couple's shared home, if they have set one in this world. */
    private static void addHome(@Nonnull MarriageRecord record, @Nonnull String worldName,
                                @Nonnull MarkersCollector collector){
        if(!Marriage.getConfig().get().ifShowHomeOnMap()
                || !BondManager.hasPerk(record, BondPerk.SHARED_HOME)
                || !record.hasHome()
                || !record.getHomeWorld().equals(worldName)){
            return;
        }

        Transform where = new Transform(record.getHomeX(), record.getHomeY(), record.getHomeZ());
        if(!collector.isInViewDistance(where)){
            return;
        }

        collector.add(new MapMarkerBuilder(PROVIDER_KEY + "_home", HOME_MARKER, where)
                .withName(Message.translation("server.marriage.marker.home"))
                .withComponent(new TintComponent(ROSE))
                .build());
    }

    /** Packs a 0xRRGGBB literal into the protocol's byte-triple Color. */
    @Nonnull
    private static Color rgb(int rgb){
        return new Color((byte) ((rgb >> 16) & 0xFF), (byte) ((rgb >> 8) & 0xFF), (byte) (rgb & 0xFF));
    }
}
