package plugin.siren.Commands.Marry;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Utils.MarriageUtil;
import plugin.siren.Utils.Registry.MarriageManager;

import javax.annotation.Nonnull;

public class MarryPlayerCmd extends AbstractPlayerCommand {
    public MarryPlayerCmd() {
        super("player", "server.commands.marry.player.desc");

        if(Marriage.getConfig().get().ifCmdPermission()){
            this.requirePermission("marriage.marry");
        }else{
            this.setPermissionGroups("hytale:None");
        }
    }

    RequiredArg<PlayerRef> msgMarryPlayerArg = this.withRequiredArg("Player Username", "server.commands.marry.player.arg.username.desc", ArgTypes.PLAYER_REF);

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        PlayerRef targetPlayerRef = msgMarryPlayerArg.get(commandContext);
        if(targetPlayerRef == null || !targetPlayerRef.isValid()){
            Marriage.LOGGER.atInfo().log("Failed to get targetPlayerRef reference : MarryPlayerCmd");
            return;
        }

        if(Marriage.getConfig().get().ifRequireRing()){
            InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
            ItemStack itemInHand = hotbar == null ? null : hotbar.getActiveItem();
            if(itemInHand == null || !itemInHand.getItemId().equalsIgnoreCase(MarriageUtil.RING_ITEM_ID)){
                playerRef.sendMessage(Message.translation("server.commands.marry.player.missingRing"));
                return;
            }
        }

        MarriageManager.ProposeResult result = MarriageManager.propose(
                playerRef.getUuid(), playerRef.getUsername(),
                targetPlayerRef.getUuid(), targetPlayerRef.getUsername());

        switch(result){
            case SELF -> playerRef.sendMessage(Message.translation("server.commands.marry.player.self.player.msg"));
            case ALREADY_MARRIED -> playerRef.sendMessage(Message.translation("server.commands.marry.player.alreadyMarried"));
            case TARGET_MARRIED -> playerRef.sendMessage(Message.translation("server.commands.marry.player.targetMarried").param("username", targetPlayerRef.getUsername()));
            case TARGET_NOT_ACCEPTING -> playerRef.sendMessage(Message.translation("server.commands.marry.player.notAccepting").param("username", targetPlayerRef.getUsername()));
            case ALREADY_REQUESTED -> playerRef.sendMessage(Message.translation("server.commands.marry.player.alreadyRequested").param("username", targetPlayerRef.getUsername()));
            case CANCELLED -> playerRef.sendMessage(Message.translation("server.commands.marry.player.cancelled"));
            case REQUEST_SENT -> {
                playerRef.sendMessage(Message.translation("server.commands.marry.player.request.player.msg").param("username", targetPlayerRef.getUsername()));
                targetPlayerRef.sendMessage(Message.translation("server.commands.marry.player.receiveRequest.player.msg").param("username", playerRef.getUsername()));
            }
            case MARRIED -> MarriageUtil.completeMarriage(store, ref, playerRef, targetPlayerRef);
        }

        if(Marriage.ifDebug()) {
            Marriage.LOGGER.atInfo().log(Message.translation("server.commands.marry.player.success").param("username",playerRef.getUsername()).getAnsiMessage());
        }
    }
}
