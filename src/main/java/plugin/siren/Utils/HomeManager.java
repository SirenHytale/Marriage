package plugin.siren.Utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import plugin.siren.API.MarriageEvents;
import plugin.siren.API.MarriageInfo;
import plugin.siren.Marriage;
import plugin.siren.Utils.Bond.BondManager;
import plugin.siren.Utils.Bond.BondPerk;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.UUID;

/**
 * The couple's shared home: one point either partner can set, and either can
 * travel to.
 *
 * <p>Deliberately one home rather than one each — a shared home is a place the two
 * of you agreed on, and the fact that your partner can move it is the feature, not
 * a flaw. Whoever moves it, the other is told.</p>
 *
 * <h2>Cross-world travel</h2>
 *
 * <p>Unlike the respawn perk, this does hop worlds: the destination is a stored
 * coordinate rather than a live entity, so there is no other thread's component to
 * read and {@link Teleport}'s own world parameter does the whole job. The world is
 * resolved by name at travel time, so a home in a world the server no longer loads
 * fails with a message instead of a null dereference.</p>
 */
public final class HomeManager {

    private HomeManager(){}

    /**
     * Records where the player is standing as the couple's home.
     * Must be called on the player's own world thread.
     */
    public static void setHome(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                               @Nonnull PlayerRef playerRef, @Nonnull World world){
        UUID uuid = playerRef.getUuid();
        MarriageRecord record = MarriageManager.getRecord(uuid);

        if(!checkAvailable(playerRef, record)){
            return;
        }

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if(transform == null){
            return;
        }
        Vector3dc position = transform.getPosition();

        HeadRotation head = store.getComponent(ref, HeadRotation.getComponentType());
        float yaw = head == null ? 0f : head.getRotation().yaw();
        float pitch = head == null ? 0f : head.getRotation().pitch();

        boolean moved = record != null && record.hasHome();
        if(!MarriageManager.setHome(uuid, world.getName(), position.x(), position.y(), position.z(), yaw, pitch)){
            return;
        }

        playerRef.sendMessage(Message.translation(moved ? "server.marriage.home.moved" : "server.marriage.home.set")
                .color(Color.PINK));

        // The other half of a shared home finding out it has moved is not optional -
        // otherwise /marry home silently sends them somewhere they have never been.
        UUID partnerUuid = record == null ? null : record.getPartnerUuid(uuid);
        PlayerRef partner = partnerUuid == null ? null : Universe.get().getPlayer(partnerUuid);
        if(partner != null && partner.isValid()){
            partner.sendMessage(Message.translation("server.marriage.home.partnerSet")
                    .param("partnerUsername", playerRef.getUsername())
                    .color(Color.PINK));
        }

        MarriageInfo info = MarriageInfo.of(MarriageManager.getRecord(uuid));
        if(info != null){
            MarriageEvents.fireHomeSet(new MarriageEvents.HomeSetEvent(info, uuid,
                    world.getName(), position.x(), position.y(), position.z()));
        }
    }

    /**
     * Sends the player to the couple's home. Must be called on the player's own
     * world thread; the teleport itself may cross into another world.
     */
    public static void travelHome(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                                  @Nonnull PlayerRef playerRef){
        UUID uuid = playerRef.getUuid();
        MarriageRecord record = MarriageManager.getRecord(uuid);

        if(!checkAvailable(playerRef, record)){
            return;
        }
        if(record == null || !record.hasHome()){
            playerRef.sendMessage(Message.translation("server.marriage.home.notSet"));
            return;
        }

        long cooldown = MarriageManager.getHomeCooldownRemaining(uuid);
        if(cooldown > 0L){
            playerRef.sendMessage(Message.translation("server.marriage.home.cooldown")
                    .param("seconds", String.valueOf(cooldown)));
            return;
        }

        World destination = resolveWorld(record.getHomeWorld());
        if(destination == null){
            playerRef.sendMessage(Message.translation("server.marriage.home.worldGone")
                    .param("world", record.getHomeWorld()));
            return;
        }

        Vector3d position = new Vector3d(record.getHomeX(), record.getHomeY(), record.getHomeZ());
        Rotation3f rotation = new Rotation3f(record.getHomePitch(), record.getHomeYaw(), 0f);

        // Same world resolves to a null target on the Teleport, matching how
        // TeleportHomeCommand does a purely local hop.
        World current = store.getExternalData().getWorld();
        World target = destination == current ? null : destination;

        // Already on the way somewhere - vanilla's TeleporterInteraction refuses in
        // exactly this state, and stacking a second Teleport is how a double-click
        // lands you at the wrong end of the first one.
        if(store.getArchetype(ref).contains(Teleport.getComponentType())){
            return;
        }

        MarriageManager.markHomeUsed(uuid);
        playerRef.sendMessage(Message.translation("server.marriage.home.travelled").color(Color.PINK));

        // Deferred onto the world executor rather than added inline. This is reached
        // from the /marriage menu as well as the command, and a menu click resolves
        // inside the page's own event handling - which is precisely where
        // MarriageUIOption warns against putting a component on the Store. Running a
        // tick later costs nothing a player can perceive and is safe from both.
        current.execute(() -> {
            if(!ref.isValid() || ref.getStore() != store){
                return;
            }
            store.addComponent(ref, Teleport.getComponentType(), Teleport.createForPlayer(target, position, rotation));
        });
    }

    /** Forgets the home, telling both partners. */
    public static void clearHome(@Nonnull PlayerRef playerRef){
        UUID uuid = playerRef.getUuid();
        MarriageRecord record = MarriageManager.getRecord(uuid);
        if(!checkAvailable(playerRef, record)){
            return;
        }
        if(!MarriageManager.clearHome(uuid)){
            playerRef.sendMessage(Message.translation("server.marriage.home.notSet"));
            return;
        }

        playerRef.sendMessage(Message.translation("server.marriage.home.cleared"));

        UUID partnerUuid = record == null ? null : record.getPartnerUuid(uuid);
        PlayerRef partner = partnerUuid == null ? null : Universe.get().getPlayer(partnerUuid);
        if(partner != null && partner.isValid()){
            partner.sendMessage(Message.translation("server.marriage.home.partnerCleared")
                    .param("partnerUsername", playerRef.getUsername()));
        }
    }

    /**
     * The three shared gates — feature on, married, bond deep enough — with the
     * matching message sent on failure.
     */
    private static boolean checkAvailable(@Nonnull PlayerRef playerRef, @Nullable MarriageRecord record){
        if(!Marriage.getConfig().get().ifSharedHomeEnabled()){
            playerRef.sendMessage(Message.translation("server.marriage.home.disabled"));
            return false;
        }
        if(record == null){
            playerRef.sendMessage(Message.translation("server.marriage.home.unmarried"));
            return false;
        }
        if(!BondManager.hasPerk(record, BondPerk.SHARED_HOME)){
            playerRef.sendMessage(Message.translation("server.marriage.perk.locked")
                    .param("perk", Message.translation(BondPerk.SHARED_HOME.getNameKey()))
                    .param("level", String.valueOf(BondManager.requiredLevelFor(record, BondPerk.SHARED_HOME))));
            return false;
        }
        return true;
    }

    /** The world a home was saved in, or null if the server no longer has it. */
    @Nullable
    private static World resolveWorld(@Nonnull String name){
        return Universe.get().getWorld(name);
    }
}
