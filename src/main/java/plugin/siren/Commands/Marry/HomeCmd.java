package plugin.siren.Commands.Marry;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Utils.HomeManager;

import javax.annotation.Nonnull;

/** {@code /marry home} - travel to the home the couple share. */
public class HomeCmd extends AbstractPlayerCommand {
    public HomeCmd() {
        super("home", "server.commands.marry.home.desc");

        if(Marriage.getConfig().get().ifCmdPermission()){
            this.requirePermission("marriage.home");
            // empty (not null): do not inherit MarryCmd's hytale:None group, which would grant the node to everyone
            this.setPermissionGroups();
        }else{
            this.setPermissionGroups("hytale:None");
        }
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        HomeManager.travelHome(store, ref, playerRef);
    }
}
