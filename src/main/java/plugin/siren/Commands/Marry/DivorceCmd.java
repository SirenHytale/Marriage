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
import plugin.siren.Utils.MarriageUtil;

import javax.annotation.Nonnull;

public class DivorceCmd extends AbstractPlayerCommand {
    public DivorceCmd() {
        super("divorce", "server.commands.marry.divorce.desc");

        if(Marriage.getConfig().get().ifCmdPermission()){
            this.requirePermission("marriage.divorce");
        }else{
            this.setPermissionGroups("hytale:None");
        }
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        // Filing, agreeing and the waiting period all live in MarriageUtil, so
        // this command and the menu's Divorce button behave identically.
        MarriageUtil.divorceFlow(playerRef);

        if(Marriage.ifDebug()) {
            Marriage.LOGGER.atInfo().log(Message.translation("server.commands.marry.divorce.success").param("username",playerRef.getUsername()).getAnsiMessage());
        }
    }
}
