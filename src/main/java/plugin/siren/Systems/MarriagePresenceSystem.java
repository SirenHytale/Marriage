package plugin.siren.Systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3dc;
import plugin.siren.Marriage;
import plugin.siren.Utils.Bond.PartnerPositions;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;
import plugin.siren.Utils.UI.PartnerHud;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The couple's heartbeat: caches each married player's position for the world map
 * to read, and drives the bond clock.
 *
 * <p>Two jobs in one system because they need the same three lookups (am I
 * married, where am I, where is my partner) and doing them twice per player per
 * tick would be the whole cost of the feature.</p>
 *
 * <h2>Cadence</h2>
 *
 * <p>Fixed at {@link #INTERVAL_SECONDS} rather than config-driven, deliberately.
 * The partner marker's smoothness and the bond's accrual rate are both derived
 * from it, and neither is something a server owner should have to reason about —
 * bond XP is expressed per hour and per minute in config and converted from the
 * real elapsed window here, so changing this constant would not change how fast
 * anybody's bond grows.</p>
 *
 * <h2>Why the bond window is capped</h2>
 *
 * <p>{@link #MAX_BOND_WINDOW_MILLIS} is what makes "XP per hour married" mean per
 * hour <em>while somebody was playing</em>. Without it a couple could log off for
 * a month and return at maximum bond, which would make every other XP source
 * pointless. Two ticks' worth of slack absorbs an ordinary lag spike without
 * paying out a whole absence.</p>
 */
public class MarriagePresenceSystem extends DelayedEntitySystem<EntityStore> {

    public static final float INTERVAL_SECONDS = 2f;

    /** How much elapsed time one bond tick may pay out for. */
    private static final long MAX_BOND_WINDOW_MILLIS = (long) (INTERVAL_SECONDS * 1000L) * 2L;

    /** Bond bookkeeping runs a minute apart, not every presence tick. */
    private static final long BOND_TICK_INTERVAL_MILLIS = 60_000L;

    private final ComponentType<EntityStore, MarriageComponent> marriageComponentType;

    public MarriagePresenceSystem(ComponentType<EntityStore, MarriageComponent> marriageComponentType){
        super(INTERVAL_SECONDS);
        this.marriageComponentType = marriageComponentType;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery(){
        return Query.and(this.marriageComponentType);
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer){
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if(ref == null || !ref.isValid()){
            return;
        }
        PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        if(playerRef == null || !playerRef.isValid()){
            return;
        }

        UUID uuid = playerRef.getUuid();
        MarriageRecord record = MarriageManager.getRecord(uuid);
        if(record == null){
            // Not (or no longer) married - make sure no stale pin outlives the marriage.
            PartnerPositions.forget(uuid);
            return;
        }

        TransformComponent selfTransform = commandBuffer.getComponent(ref, TransformComponent.getComponentType());
        if(selfTransform == null){
            return;
        }
        Vector3dc position = selfTransform.getPosition();
        String worldName = store.getExternalData().getWorld().getName();
        PartnerPositions.record(uuid, worldName, position.x(), position.y(), position.z());

        // Resolved once and reused by both the HUD and the bond check below -
        // they ask the same question and doing it twice per player per tick would
        // be the whole cost of the feature.
        Ref<EntityStore> partnerRef = reachablePartner(store, record, uuid);

        refreshPartnerHud(store, ref, commandBuffer, playerRef, record, uuid, position, partnerRef);

        // The bond only needs looking at once a minute. Gating here rather than
        // registering a second, slower system keeps the "am I married / where is my
        // partner" work to one pass.
        MarriageComponent marriage = commandBuffer.getComponent(ref, this.marriageComponentType);
        if(marriage == null){
            return;
        }
        long now = System.currentTimeMillis();
        if(now - marriage.getLastBondTickMillis() < BOND_TICK_INTERVAL_MILLIS){
            return;
        }
        marriage.setLastBondTickMillis(now);

        boolean together = partnerRef != null
                && withinOf(commandBuffer, partnerRef, position, Marriage.getConfig().get().getBondRangeBlocks());
        MarriageManager.bondTick(uuid, together, BOND_TICK_INTERVAL_MILLIS + MAX_BOND_WINDOW_MILLIS);
    }

    /**
     * Puts the partner plate on screen while they are close, and takes it off when
     * they are not.
     *
     * <p>Removal is guarded on the plate actually being shown, so the common case
     * — a married player nowhere near their partner — costs one map lookup per
     * tick rather than a packet.</p>
     */
    private static void refreshPartnerHud(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                                          @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull PlayerRef playerRef,
                                          @Nonnull MarriageRecord record, @Nonnull UUID uuid,
                                          @Nonnull Vector3dc position, @Nullable Ref<EntityStore> partnerRef){
        boolean wanted = Marriage.getConfig().get().ifPartnerHudEnabled() && partnerRef != null;

        if(wanted){
            MarriageSettingsComponent settings = commandBuffer.getComponent(ref, MarriageSettingsComponent.getComponentType());
            wanted = settings == null || settings.isPartnerHud();
        }

        double range = Marriage.getConfig().get().getPartnerHudRangeBlocks();
        if(wanted){
            wanted = withinOf(commandBuffer, partnerRef, position, range);
        }

        if(!wanted){
            if(PartnerHud.isShown(store, ref)){
                PartnerHud.remove(store, ref, playerRef);
            }
            return;
        }

        TransformComponent partnerTransform = commandBuffer.getComponent(partnerRef, TransformComponent.getComponentType());
        EntityStatMap partnerStats = commandBuffer.getComponent(partnerRef, EntityStatMap.getComponentType());
        if(partnerTransform == null){
            return;
        }

        float fraction = 1f;
        if(partnerStats != null){
            EntityStatValue health = partnerStats.get(DefaultEntityStatTypes.getHealth());
            if(health != null && health.getMax() > health.getMin()){
                fraction = (health.get() - health.getMin()) / (health.getMax() - health.getMin());
                fraction = Math.min(1f, Math.max(0f, fraction));
            }
        }

        PartnerHud hud = PartnerHud.showOrGet(store, ref, playerRef);
        if(hud != null){
            hud.refresh(record.getPartnerName(uuid), fraction,
                    Math.sqrt(position.distanceSquared(partnerTransform.getPosition())));
        }
    }

    /**
     * The partner's entity, if they are online and on this very world thread.
     * Null otherwise — which is the only safe answer, since their components
     * belong to whichever store owns them.
     */
    @Nullable
    private static Ref<EntityStore> reachablePartner(@Nonnull Store<EntityStore> store,
                                                     @Nonnull MarriageRecord record, @Nonnull UUID self){
        UUID partnerUuid = record.getPartnerUuid(self);
        if(partnerUuid == null){
            return null;
        }
        PlayerRef partner = Universe.get().getPlayer(partnerUuid);
        if(partner == null || !partner.isValid()){
            return null;
        }
        Ref<EntityStore> partnerRef = partner.getReference();
        if(partnerRef == null || !partnerRef.isValid() || partnerRef.getStore() != store){
            return null;
        }
        return partnerRef;
    }

    /** Whether an already-reachable partner is within {@code range} of a position. */
    private static boolean withinOf(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nullable Ref<EntityStore> partnerRef,
                                    @Nonnull Vector3dc position, double range){
        if(partnerRef == null){
            return false;
        }
        TransformComponent partnerTransform = commandBuffer.getComponent(partnerRef, TransformComponent.getComponentType());
        return partnerTransform != null
                && position.distanceSquared(partnerTransform.getPosition()) <= range * range;
    }

    /**
     * Whether two players are within bond range of one another right now, for the
     * co-op kill credit. Shares this system's same-store rule.
     */
    public static boolean isWithinBondRange(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> selfRef,
                                            @Nonnull Ref<EntityStore> partnerRef, double range){
        if(!selfRef.isValid() || !partnerRef.isValid() || partnerRef.getStore() != store){
            return false;
        }
        TransformComponent selfTransform = store.getComponent(selfRef, TransformComponent.getComponentType());
        TransformComponent partnerTransform = store.getComponent(partnerRef, TransformComponent.getComponentType());
        if(selfTransform == null || partnerTransform == null){
            return false;
        }
        return selfTransform.getPosition().distanceSquared(partnerTransform.getPosition()) <= range * range;
    }
}
