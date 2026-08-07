package plugin.siren.Commands.Marry;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Utils.MarriageUtil;

import javax.annotation.Nonnull;

/** {@code /marry renew} - swear it all again. */
public class RenewCmd extends AbstractPlayerCommand {
    public RenewCmd() {
        super("renew", "server.commands.marry.renew.desc");

        this.setPermissionGroups("hytale:None");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        MarriageUtil.renewVows(store, ref, playerRef);
    }
}
