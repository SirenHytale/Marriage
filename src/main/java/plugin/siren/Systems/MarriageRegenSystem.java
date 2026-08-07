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
import plugin.siren.Marriage;
import plugin.siren.Utils.Bond.BondManager;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Faster health regeneration near your partner: every interval, each married
 * player whose partner is in the same world within the configured radius heals a
 * configured amount. Each half of the couple heals only itself on its own tick,
 * so the rate is symmetric without double-healing. Partners in another world are
 * skipped via the store identity gate - no cross-world thread hops, ever.
 * The tick interval is fixed at registration; changing it in config needs a restart.
 */
public class MarriageRegenSystem extends DelayedEntitySystem<EntityStore> {

    private final ComponentType<EntityStore, MarriageComponent> marriageComponentType;

    public MarriageRegenSystem(float intervalSec, ComponentType<EntityStore, MarriageComponent> marriageComponentType){
        super(intervalSec);
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
        if(!Marriage.getConfig().get().ifPartnerRegen()){
            return;
        }

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if(ref == null || !ref.isValid()){
            return;
        }
        PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        if(playerRef == null || !playerRef.isValid()){
            return;
        }

        MarriageRecord record = MarriageManager.getRecord(playerRef.getUuid());
        if(record == null){
            return;
        }
        UUID partnerUuid = record.getPartnerUuid(playerRef.getUuid());
        if(partnerUuid == null){
            return;
        }
        PlayerRef partner = Universe.get().getPlayer(partnerUuid);
        if(partner == null || !partner.isValid()){
            return;
        }
        Ref<EntityStore> partnerRef = partner.getReference();
        if(partnerRef == null || !partnerRef.isValid()){
            return;
        }
        // Same-world gate: identity-compare the partner's store against the one this
        // system is ticking. Cross-world partners simply get no regen this tick.
        if(partnerRef.getStore() != store){
            return;
        }

        TransformComponent selfTransform = commandBuffer.getComponent(ref, TransformComponent.getComponentType());
        TransformComponent partnerTransform = commandBuffer.getComponent(partnerRef, TransformComponent.getComponentType());
        if(selfTransform == null || partnerTransform == null){
            return;
        }
        // Bond-scaled, not the flat config radius: a deepened bond reaches further
        // (see BondManager.regenRadiusFor).
        double radius = BondManager.regenRadiusFor(record);
        if(selfTransform.getPosition().distanceSquared(partnerTransform.getPosition()) > radius * radius){
            return;
        }

        EntityStatMap statMap = commandBuffer.getComponent(ref, EntityStatMap.getComponentType());
        if(statMap == null){
            return;
        }
        EntityStatValue health = statMap.get(DefaultEntityStatTypes.getHealth());
        if(health == null || health.get() <= health.getMin() || health.get() >= health.getMax()){
            // Dead (at min) or already full - nothing to heal.
            return;
        }
        statMap.addStatValue(DefaultEntityStatTypes.getHealth(), (float) Marriage.getConfig().get().getRegenAmountPerInterval());
    }
}
