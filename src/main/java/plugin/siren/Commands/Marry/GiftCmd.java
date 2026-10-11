package plugin.siren.Commands.Marry;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Utils.GiftManager;

import javax.annotation.Nonnull;

/** Hands the item stack you are holding to your married partner. */
public class GiftCmd extends AbstractPlayerCommand {
    public GiftCmd() {
        super("gift", "server.commands.marry.gift.desc");

        if(Marriage.getConfig().get().ifCmdPermission()){
            this.requirePermission("marriage.gift");
            // empty (not null): do not inherit MarryCmd's hytale:None group, which would grant the node to everyone
            this.setPermissionGroups();
        }else{
            this.setPermissionGroups("hytale:None");
        }
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        GiftManager.gift(store, ref, playerRef);

        if(Marriage.ifDebug()) {
            Marriage.LOGGER.atInfo().log(Message.translation("server.commands.marry.gift.success").param("username",playerRef.getUsername()).getAnsiMessage());
        }
    }
}
