package plugin.siren.Commands.Marry;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.API.MarriageInfo;
import plugin.siren.Marriage;
import plugin.siren.Utils.MarriageUtil;
import plugin.siren.Utils.Registry.MarriageManager;

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
        // Snapshot before the divorce - the record is gone once it succeeds.
        MarriageInfo marriage = MarriageInfo.of(MarriageManager.getRecord(playerRef.getUuid()));

        MarriageManager.DivorceResult result = MarriageManager.divorce(playerRef.getUuid());
        switch(result){
            case NOT_MARRIED -> playerRef.sendMessage(Message.translation("server.commands.marry.divorce.unmarried"));
            case CONFIRM_NEEDED -> playerRef.sendMessage(Message.translation("server.commands.marry.divorce.confirmation")
                    .param("seconds", String.valueOf(Marriage.getConfig().get().getDivorceConfirmSeconds())));
            case CANCELLED -> playerRef.sendMessage(Message.translation("server.commands.marry.divorce.cancelled"));
            case DIVORCED -> {
                if(marriage != null){
                    MarriageUtil.finishDivorce(playerRef, marriage);
                }
            }
        }

        if(Marriage.ifDebug()) {
            Marriage.LOGGER.atInfo().log(Message.translation("server.commands.marry.divorce.success").param("username",playerRef.getUsername()).getAnsiMessage());
        }
    }
}
