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
import plugin.siren.Marriage;
import plugin.siren.Utils.Registry.MarriageManager;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.UUID;

public class MessageCmd extends AbstractPlayerCommand {
    public MessageCmd() {
        super("msg", "server.commands.marry.message.desc");

        if(Marriage.getConfig().get().ifCmdPermission()){
            this.requirePermission("marriage.msg");
        }else{
            this.setPermissionGroups("hytale:None");
        }

        setAllowsExtraArguments(true);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if(!Marriage.getConfig().get().ifMsgEnabled()){
            playerRef.sendMessage(Message.translation("server.commands.marry.message.disabled"));
            return;
        }

        UUID partnerUuid = MarriageManager.getPartnerUuid(playerRef.getUuid());
        if(partnerUuid == null){
            playerRef.sendMessage(Message.translation("server.commands.marry.message.unmarried"));
            return;
        }

        PlayerRef partnerPlayerRef = Universe.get().getPlayer(partnerUuid);
        if(partnerPlayerRef == null || !partnerPlayerRef.isValid()){
            playerRef.sendMessage(Message.translation("server.commands.marry.message.partnerOffline.player.msg"));
            return;
        }

        String msg = commandContext.getInputString();
        if(msg.length() < 10){
            playerRef.sendMessage(Message.translation("server.commands.marry.message.missingMsg.player.msg"));
            return;
        }
        msg = msg.substring(10);

        partnerPlayerRef.sendMessage(Message.raw(playerRef.getUsername() + ": " + msg).color(Color.PINK));
        playerRef.sendMessage(Message.translation("server.commands.marry.message.sentMsg").param("partnerUsername", partnerPlayerRef.getUsername()).param("msg", msg));

        MarriageManager.recordMessage(playerRef.getUuid());

        if(Marriage.ifDebug()) {
            Marriage.LOGGER.atInfo().log(Message.translation("server.commands.marry.message.success").param("username",playerRef.getUsername()).getAnsiMessage());
        }
    }
}
