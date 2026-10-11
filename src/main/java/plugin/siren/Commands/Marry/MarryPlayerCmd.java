package plugin.siren.Commands.Marry;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Utils.MarriageUtil;

import javax.annotation.Nonnull;

public class MarryPlayerCmd extends AbstractPlayerCommand {
    public MarryPlayerCmd() {
        super("player", "server.commands.marry.player.desc");

        if(Marriage.getConfig().get().ifCmdPermission()){
            this.requirePermission("marriage.marry");
            // empty (not null): do not inherit MarryCmd's hytale:None group, which would grant the node to everyone
            this.setPermissionGroups();
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

        // The ring gate, the handshake and all eight replies live in MarriageUtil,
        // so this command and the menu's Propose page cannot drift apart.
        MarriageUtil.proposeTo(store, ref, playerRef, targetPlayerRef);

        if(Marriage.ifDebug()) {
            Marriage.LOGGER.atInfo().log(Message.translation("server.commands.marry.player.success").param("username",playerRef.getUsername()).getAnsiMessage());
        }
    }
}
