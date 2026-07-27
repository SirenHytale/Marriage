package plugin.siren.Utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.API.MarriageInfo;
import plugin.siren.Marriage;
import plugin.siren.Utils.Registry.MarriageManager;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Shared bits of the marriage flows used by both the commands and the menu. */
public final class MarriageUtil {

    public static final String RING_ITEM_ID = "Marriage_Ring";

    private MarriageUtil(){}

    /**
     * Runs the celebration after MarriageManager.propose returned MARRIED: syncs
     * both settings components, consumes rings when configured, congratulates the
     * couple, and announces to the server when configured. Call on the accepter's
     * world thread with the accepter's store/ref.
     */
    public static void completeMarriage(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                                        @Nonnull PlayerRef accepter, @Nonnull PlayerRef requester){
        ComponentSync.syncMarried(accepter, requester.getUuid(), requester.getUsername());
        ComponentSync.syncMarried(requester, accepter.getUuid(), accepter.getUsername());

        MarriageConfig config = Marriage.getConfig().get();
        if(config.ifRequireRing() && config.ifConsumeRing()){
            // The accepter is on this thread; the requester's ring goes on theirs.
            consumeRingInline(store, ref);
            consumeRingRemote(requester);
        }

        accepter.sendMessage(Message.translation("server.commands.marry.player.marry.player.msg").param("partnerUsername", requester.getUsername()).color(Color.PINK));
        requester.sendMessage(Message.translation("server.commands.marry.player.marry.player.msg").param("partnerUsername", accepter.getUsername()).color(Color.PINK));

        if(config.ifAnnounceMarriages()){
            broadcast(Message.translation("server.commands.marry.player.console.alert")
                    .param("usernameOne", requester.getUsername())
                    .param("usernameTwo", accepter.getUsername())
                    .color(Color.PINK));
        }

        Marriage.LOGGER.atInfo().log(requester.getUsername() + " and " + accepter.getUsername() + " just got Married!");
    }

    public static void broadcast(@Nonnull Message message){
        for(PlayerRef onlinePlayer : Universe.get().getPlayers()){
            if(onlinePlayer != null && onlinePlayer.isValid()){
                onlinePlayer.sendMessage(message);
            }
        }
    }

    /**
     * Runs the aftermath of MarriageManager.divorce returning DIVORCED: settings
     * sync for both sides (offline partner reconciles on next join), messages,
     * and the config-gated announcement. Pass the snapshot taken BEFORE divorcing.
     */
    public static void finishDivorce(@Nonnull PlayerRef initiator, @Nonnull MarriageInfo endedMarriage){
        String partnerName = endedMarriage.getPartnerName(initiator.getUuid());

        ComponentSync.syncDivorced(initiator);
        initiator.sendMessage(Message.translation("server.commands.marry.divorce.player.msg").param("partnerUsername", partnerName));

        UUID partnerUuid = endedMarriage.getPartnerUuid(initiator.getUuid());
        PlayerRef partnerPlayerRef = partnerUuid == null ? null : Universe.get().getPlayer(partnerUuid);
        if(partnerPlayerRef != null && partnerPlayerRef.isValid()){
            ComponentSync.syncDivorced(partnerPlayerRef);
            partnerPlayerRef.sendMessage(Message.translation("server.commands.marry.divorce.player.msg").param("partnerUsername", initiator.getUsername()));
        }

        if(Marriage.getConfig().get().ifAnnounceDivorces()){
            broadcast(Message.translation("server.commands.marry.divorce.console.alert")
                    .param("usernameOne", initiator.getUsername())
                    .param("usernameTwo", partnerName));
        }
    }

    /**
     * The /marry tp flow, shared by the command and the menu action. Checks the
     * feature toggle, marriage, cooldown, and partner presence itself and sends
     * all feedback messages.
     */
    public static void teleportToPartner(@Nonnull PlayerRef playerRef){
        if(!Marriage.getConfig().get().ifTpEnabled()){
            playerRef.sendMessage(Message.translation("server.commands.marry.tp.disabled"));
            return;
        }
        UUID partnerUuid = MarriageManager.getPartnerUuid(playerRef.getUuid());
        if(partnerUuid == null){
            playerRef.sendMessage(Message.translation("server.commands.marry.tp.unmarried"));
            return;
        }
        long cooldownRemaining = MarriageManager.getTpCooldownRemaining(playerRef.getUuid());
        if(cooldownRemaining > 0L){
            playerRef.sendMessage(Message.translation("server.commands.marry.tp.cooldown").param("seconds", String.valueOf(cooldownRemaining)));
            return;
        }
        PlayerRef partnerPlayerRef = Universe.get().getPlayer(partnerUuid);
        if(partnerPlayerRef == null || !partnerPlayerRef.isValid()){
            playerRef.sendMessage(Message.translation("server.commands.marry.tp.partnerOffline.player.msg"));
            return;
        }

        String consoleRunCommand = "tp " + playerRef.getUsername() + " " + partnerPlayerRef.getUsername();
        CommandManager.get().handleCommand(ConsoleSender.INSTANCE, consoleRunCommand);

        MarriageManager.markTpUsed(playerRef.getUuid());
        MarriageManager.recordTeleport(playerRef.getUuid());

        playerRef.sendMessage(Message.translation("server.commands.marry.tp.player.msg"));
        partnerPlayerRef.sendMessage(Message.translation("server.commands.marry.tp.partner.msg"));
    }

    private static void consumeRingInline(Store<EntityStore> store, Ref<EntityStore> ref){
        InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        if(hotbar != null){
            hotbar.getInventory().removeItemStack(new ItemStack(RING_ITEM_ID, 1));
        }
    }

    private static void consumeRingRemote(PlayerRef target){
        Ref<EntityStore> targetRef = target.getReference();
        if(targetRef == null || !targetRef.isValid()){
            return;
        }
        Store<EntityStore> targetStore = targetRef.getStore();
        World targetWorld = targetStore.getExternalData().getWorld();
        CompletableFuture.runAsync(() -> {
            // Skip if they changed worlds mid-hop - their components now belong
            // to another thread, and a missed ring beats a cross-thread touch.
            if(targetRef.isValid() && targetRef.getStore() == targetStore){
                consumeRingInline(targetStore, targetRef);
            }
        }, targetWorld);
    }

    /** "3d 4h" style duration for married-since displays. */
    public static String formatDuration(long millis){
        long totalMinutes = Math.max(0L, millis) / 60000L;
        long days = totalMinutes / (60L * 24L);
        long hours = (totalMinutes / 60L) % 24L;
        long minutes = totalMinutes % 60L;
        if(days > 0L){
            return days + "d " + hours + "h";
        }
        if(hours > 0L){
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }
}
