package plugin.siren.Systems;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.RespawnSystems;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import plugin.siren.Marriage;
import plugin.siren.Utils.Bond.BondManager;
import plugin.siren.Utils.Bond.BondPerk;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.UUID;

/**
 * Puts a player back beside their living partner instead of at their bed.
 *
 * <h2>Where this runs</h2>
 *
 * <p>{@link RespawnSystems.OnRespawnSystem} is a {@code RefChangeSystem} over
 * {@link DeathComponent}, and a respawn is that component being <em>removed</em> —
 * so the work belongs in {@code onComponentRemoved}, after vanilla's own
 * {@code RespawnController} has already placed the player at their bed or world
 * spawn. Moving them from there is a plain {@link Teleport} component, exactly as
 * {@code TeleportHomeCommand} does it, which means the client is told about the
 * move through the same path any other teleport uses.</p>
 *
 * <h2>Why the partner must be in the same world</h2>
 *
 * <p>{@code Teleport} does support a cross-world hop, and the shared home uses it.
 * This does not: the partner's live position can only be read safely from the world
 * thread that owns them, and a respawn resolves on the <em>dead</em> player's
 * thread. Rather than hop threads mid-respawn — which would land the player at
 * their bed first and yank them a tick later — a cross-world partner simply does
 * not trigger the perk, and the player is told why.</p>
 */
public class MarriageRespawnSystem extends RespawnSystems.OnRespawnSystem {

    /** A short hop out of the partner's own hitbox so the two do not spawn inside each other. */
    private static final double SIDE_STEP_BLOCKS = 1.0;

    @Nonnull
    @Override
    public Query<EntityStore> getQuery(){
        return Query.and(Player.getComponentType(), PlayerRef.getComponentType());
    }

    @Override
    public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component,
                                   @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer){
        if(!Marriage.getConfig().get().ifRespawnAtPartnerEnabled()){
            return;
        }

        PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        if(playerRef == null || !playerRef.isValid()){
            return;
        }

        UUID uuid = playerRef.getUuid();
        MarriageRecord record = MarriageManager.getRecord(uuid);
        if(!BondManager.hasPerk(record, BondPerk.RESPAWN_AT_PARTNER) || record == null){
            return;
        }

        UUID partnerUuid = record.getPartnerUuid(uuid);
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
        // Same store: see the class note. A partner in another world is not an
        // error, just not somewhere this perk can reach.
        if(partnerRef.getStore() != store){
            playerRef.sendMessage(Message.translation("server.marriage.perk.respawn.otherWorld")
                    .param("partnerUsername", record.getPartnerName(uuid)));
            return;
        }

        // Checked after the partner is confirmed reachable, so a wasted attempt
        // never burns the cooldown.
        long cooldown = MarriageManager.getRespawnCooldownRemaining(uuid);
        if(cooldown > 0L){
            playerRef.sendMessage(Message.translation("server.marriage.perk.respawn.cooldown")
                    .param("minutes", String.valueOf((cooldown + 59L) / 60L)));
            return;
        }

        TransformComponent partnerTransform = commandBuffer.getComponent(partnerRef, TransformComponent.getComponentType());
        if(partnerTransform == null){
            return;
        }
        Vector3dc at = partnerTransform.getPosition();

        HeadRotation partnerHead = commandBuffer.getComponent(partnerRef, HeadRotation.getComponentType());
        Rotation3f facing = partnerHead == null ? new Rotation3f() : new Rotation3f(partnerHead.getRotation());

        Vector3d destination = new Vector3d(at.x() + SIDE_STEP_BLOCKS, at.y(), at.z());

        // null world - a same-world teleport, which is the only kind this perk does.
        commandBuffer.addComponent(ref, Teleport.getComponentType(),
                Teleport.createForPlayer(null, destination, facing));

        MarriageManager.markRespawnUsed(uuid);

        playerRef.sendMessage(Message.translation("server.marriage.perk.respawn.arrived")
                .param("partnerUsername", record.getPartnerName(uuid))
                .color(Color.PINK));
        partner.sendMessage(Message.translation("server.marriage.perk.respawn.partnerArrived")
                .param("partnerUsername", playerRef.getUsername())
                .color(Color.PINK));
    }
}
