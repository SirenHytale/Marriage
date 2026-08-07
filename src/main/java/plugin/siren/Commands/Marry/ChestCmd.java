package plugin.siren.Commands.Marry;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Utils.HopeChestManager;

import javax.annotation.Nonnull;

/** {@code /marry chest} - opens the Hope Chest the couple share. */
public class ChestCmd extends AbstractPlayerCommand {
    public ChestCmd() {
        super("chest", "server.commands.marry.chest.desc");

        if(Marriage.getConfig().get().ifCmdPermission()){
            this.requirePermission("marriage.chest");
        }else{
            this.setPermissionGroups("hytale:None");
        }
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        HopeChestManager.open(store, ref, playerRef);
    }
}
