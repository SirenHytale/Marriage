package plugin.siren.Systems;

import com.hypixel.hytale.assetstore.map.AssetMapWithIndexes;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3dc;
import plugin.siren.Marriage;
import plugin.siren.Utils.Bond.BondManager;
import plugin.siren.Utils.Bond.BondPerk;
import plugin.siren.Utils.Bond.BondXpReason;
import plugin.siren.Utils.Bond.PartnerPositions;
import plugin.siren.Utils.MarriageConfig;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.UUID;

/**
 * Everything a marriage cares about when something dies.
 *
 * <p>Three separate features share this one hook because they all key off the same
 * moment and the same two lookups:</p>
 * <ul>
 *   <li><b>Death alert</b> — the fallen player's partner is told where they fell
 *       and gets a marker on the spot.</li>
 *   <li><b>Last stand</b> — the surviving partner, if they are near enough to have
 *       watched it happen, gets a burst of regeneration.</li>
 *   <li><b>Co-op kill</b> — a kill landed with your partner beside you deepens the
 *       bond.</li>
 * </ul>
 *
 * <p>Modelled on Cultivation's {@code CultivationDeathSystem}: an
 * {@link DeathSystems.OnDeathSystem} whose {@code onComponentAdded} runs under the
 * store's processing lock, which is why nothing here ever calls
 * {@code Store.putComponent} — the effect below goes on through the
 * {@link CommandBuffer}.</p>
 */
public class MarriageDeathSystem extends DeathSystems.OnDeathSystem {

    private static final Query<EntityStore> QUERY = Query.and(EntityStatMap.getComponentType());

    @Nonnull
    @Override
    public Query<EntityStore> getQuery(){
        return QUERY;
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component,
                                 @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer){
        Damage damage = component.getDeathInfo();

        PlayerRef victimPlayerRef = store.getComponent(ref, Player.getComponentType()) == null
                ? null
                : store.getComponent(ref, PlayerRef.getComponentType());

        if(victimPlayerRef != null && victimPlayerRef.isValid()){
            onPartnerDied(ref, store, commandBuffer, victimPlayerRef);
        }

        creditCoopKill(ref, store, commandBuffer, damage);
    }

    // --- Death alert + last stand ---------------------------------------------

    /**
     * Tells the fallen player's partner where it happened, pins the spot on their
     * map, and — if they were close enough to see it — hands them the last stand.
     */
    private static void onPartnerDied(@Nonnull Ref<EntityStore> victimRef, @Nonnull Store<EntityStore> store,
                                      @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull PlayerRef victim){
        UUID victimUuid = victim.getUuid();
        MarriageRecord record = MarriageManager.getRecord(victimUuid);
        if(record == null){
            return;
        }

        UUID partnerUuid = record.getPartnerUuid(victimUuid);
        if(partnerUuid == null){
            return;
        }

        TransformComponent transform = commandBuffer.getComponent(victimRef, TransformComponent.getComponentType());
        Vector3dc where = transform == null ? null : transform.getPosition();
        String worldName = store.getExternalData().getWorld().getName();

        MarriageConfig config = Marriage.getConfig().get();
        PlayerRef partner = Universe.get().getPlayer(partnerUuid);

        if(BondManager.hasPerk(record, BondPerk.DEATH_ALERT) && where != null){
            // Recorded whether or not the partner is online: they should be able to
            // log in and still find where it happened, for as long as the marker lasts.
            PartnerPositions.recordDeath(victimUuid, worldName, where.x(), where.y(), where.z());

            if(partner != null && partner.isValid()){
                partner.sendMessage(Message.translation("server.marriage.perk.deathAlert")
                        .param("partnerUsername", victim.getUsername())
                        .param("x", String.valueOf(Math.round(where.x())))
                        .param("y", String.valueOf(Math.round(where.y())))
                        .param("z", String.valueOf(Math.round(where.z())))
                        .color(Color.PINK));
            }
        }

        if(!BondManager.hasPerk(record, BondPerk.LAST_STAND) || partner == null || !partner.isValid()){
            return;
        }
        Ref<EntityStore> partnerRef = partner.getReference();
        if(partnerRef == null || !partnerRef.isValid()){
            return;
        }
        // Same store, and near enough to have been in the fight. Last stand is a
        // reaction to watching your partner fall, not a notification.
        if(!MarriagePresenceSystem.isWithinBondRange(store, victimRef, partnerRef, config.getBondRangeBlocks())){
            return;
        }

        if(applyLastStand(commandBuffer, partnerRef, config)){
            partner.sendMessage(Message.translation("server.marriage.perk.lastStand")
                    .param("partnerUsername", victim.getUsername())
                    .color(Color.PINK));
        }
    }

    /**
     * Puts the configured entity effect on the survivor.
     *
     * <p>A clean no-op when the id is blank or names an effect this build does not
     * ship — a server whose asset set differs loses the buff, not the feature, and
     * that is why the id is config rather than a constant.</p>
     */
    private static boolean applyLastStand(@Nonnull CommandBuffer<EntityStore> commandBuffer,
                                          @Nonnull Ref<EntityStore> partnerRef, @Nonnull MarriageConfig config){
        String effectId = config.getLastStandEffect();
        if(effectId.isBlank()){
            return false;
        }

        int effectIndex = EntityEffect.getAssetMap().getIndex(effectId);
        if(effectIndex == AssetMapWithIndexes.NOT_FOUND){
            return false;
        }
        EntityEffect effect = EntityEffect.getAssetMap().getAsset(effectIndex);
        EffectControllerComponent controller = commandBuffer.getComponent(partnerRef, EffectControllerComponent.getComponentType());
        if(effect == null || controller == null){
            return false;
        }

        controller.addEffect(partnerRef, effect, (float) config.getLastStandDurationSeconds(),
                OverlapBehavior.EXTEND, commandBuffer);
        return true;
    }

    // --- Co-op kills ---------------------------------------------------------

    /**
     * Bond XP for a kill landed with your partner nearby.
     *
     * <p>Self-inflicted deaths are excluded by index comparison ({@code Ref} has no
     * {@code equals}) — without it, dying to your own projectile would pay you for
     * a co-op kill on yourself. Killing your own partner pays nothing either, for
     * the obvious reason.</p>
     */
    private static void creditCoopKill(@Nonnull Ref<EntityStore> victimRef, @Nonnull Store<EntityStore> store,
                                       @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nullable Damage damage){
        MarriageConfig config = Marriage.getConfig().get();
        if(!config.ifBondEnabled() || config.getBondXpPerCoopKill() <= 0.0){
            return;
        }
        if(!(damage != null && damage.getSource() instanceof Damage.EntitySource entitySource)){
            return;
        }

        Ref<EntityStore> killerRef = entitySource.getRef();
        if(killerRef == null || !killerRef.isValid() || killerRef.getIndex() == victimRef.getIndex()){
            return;
        }
        PlayerRef killer = commandBuffer.getComponent(killerRef, PlayerRef.getComponentType());
        if(killer == null || !killer.isValid()){
            return;
        }

        UUID killerUuid = killer.getUuid();
        MarriageRecord record = MarriageManager.getRecord(killerUuid);
        if(record == null){
            return;
        }

        UUID partnerUuid = record.getPartnerUuid(killerUuid);
        if(partnerUuid == null){
            return;
        }

        // Killing your own partner is not a hunt you went on together.
        PlayerRef victim = store.getComponent(victimRef, PlayerRef.getComponentType());
        if(victim != null && partnerUuid.equals(victim.getUuid())){
            return;
        }

        PlayerRef partner = Universe.get().getPlayer(partnerUuid);
        if(partner == null || !partner.isValid()){
            return;
        }
        Ref<EntityStore> partnerRef = partner.getReference();
        if(partnerRef == null
                || !MarriagePresenceSystem.isWithinBondRange(store, killerRef, partnerRef, config.getBondRangeBlocks())){
            return;
        }

        MarriageManager.addBondXp(killerUuid, config.getBondXpPerCoopKill(), BondXpReason.COOP_KILL);
    }
}
