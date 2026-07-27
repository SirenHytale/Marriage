package plugin.siren.Commands.Marry;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.API.MarriageInfo;
import plugin.siren.Marriage;
import plugin.siren.Utils.MarriageUtil;
import plugin.siren.Utils.Registry.MarriageManager;

import javax.annotation.Nonnull;
import java.util.UUID;

public class PartnerCmd extends AbstractPlayerCommand {
    public PartnerCmd() {
        super("partner", "server.commands.marry.partner.player.desc");

        this.setPermissionGroups("hytale:None");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        MarriageInfo marriage = MarriageInfo.of(MarriageManager.getRecord(playerRef.getUuid()));

        if(marriage == null){
            playerRef.sendMessage(Message.translation("server.commands.marry.partner.unmarried"));
        }else{
            UUID partnerUuid = marriage.getPartnerUuid(playerRef.getUuid());
            String partnerName = marriage.getPartnerName(playerRef.getUuid());
            String marriedFor = MarriageUtil.formatDuration(marriage.getMarriedForMillis());

            PlayerRef partnerPlayerRef = partnerUuid == null ? null : Universe.get().getPlayer(partnerUuid);
            if(partnerPlayerRef != null && partnerPlayerRef.isValid()){
                playerRef.sendMessage(Message.translation("server.commands.marry.partner.player.msg")
                        .param("partnerUsername", partnerPlayerRef.getUsername())
                        .param("duration", marriedFor));
            }else{
                playerRef.sendMessage(Message.translation("server.commands.marry.partner.player.msg.offline")
                        .param("partnerUsername", partnerName)
                        .param("duration", marriedFor));
            }
        }

        if(Marriage.ifDebug()) {
            Marriage.LOGGER.atInfo().log(Message.translation("server.commands.marry.partner.success").param("username",playerRef.getUsername()).getAnsiMessage());
        }
    }
}
