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

/** {@code /marry clearhome} - forgets the couple's home, telling the other partner. */
public class ClearHomeCmd extends AbstractPlayerCommand {
    public ClearHomeCmd() {
        super("clearhome", "server.commands.marry.clearhome.desc");

        if(Marriage.getConfig().get().ifCmdPermission()){
            this.requirePermission("marriage.home");
        }else{
            this.setPermissionGroups("hytale:None");
        }
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        HomeManager.clearHome(playerRef);
    }
}
