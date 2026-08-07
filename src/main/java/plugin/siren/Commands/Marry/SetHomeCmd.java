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

/**
 * {@code /marry sethome} - makes where you stand the couple's home.
 *
 * <p>A sibling command rather than {@code /marry home set}: {@code home} has to
 * stay a plain executable command so that the bare {@code /marry home} still
 * travels, and this mod does not use flag-style arguments.</p>
 */
public class SetHomeCmd extends AbstractPlayerCommand {
    public SetHomeCmd() {
        super("sethome", "server.commands.marry.sethome.desc");

        if(Marriage.getConfig().get().ifCmdPermission()){
            this.requirePermission("marriage.home");
        }else{
            this.setPermissionGroups("hytale:None");
        }
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        HomeManager.setHome(store, ref, playerRef, world);
    }
}
