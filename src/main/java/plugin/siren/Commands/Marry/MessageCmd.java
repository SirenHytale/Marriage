package plugin.siren.Commands.Marry;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Systems.MarriageSettingsComponent;

import javax.annotation.Nonnull;
import java.awt.*;

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
        MarriageSettingsComponent marriageSettings = store.getComponent(ref, MarriageSettingsComponent.getComponentType());

        if(marriageSettings == null){
            Marriage.LOGGER.atInfo().log("Failed to get Marriage Settings Component : MessageCmd");
        }else{
            if(marriageSettings.isMarried()){
                PlayerRef partnerPlayerRef = Universe.get().getPlayer(marriageSettings.getPartnerUUID());
                if(partnerPlayerRef == null){
                    Marriage.LOGGER.atInfo().log("Failed to get partnerPlayerRef : MessageCmd");
                    playerRef.sendMessage(Message.translation("server.commands.marry.message.partnerOffline.player.msg"));
                }else {
                    String msg = commandContext.getInputString();

                    if(msg.length() < 10){
                        playerRef.sendMessage(Message.translation("server.commands.marry.message.missingMsg.player.msg"));
                    }else{
                        msg = msg.substring(10);

                        partnerPlayerRef.sendMessage(Message.raw(playerRef.getUsername() + ": " + msg).color(Color.PINK));
                        playerRef.sendMessage(Message.translation("server.commands.marry.message.sentMsg").param("partnerUsername",partnerPlayerRef.getUsername()).param("msg",msg));
                    }
                }
            }else{
                playerRef.sendMessage(Message.translation("server.commands.marry.message.unmarried"));
            }
        }

        if(Marriage.ifDebug()) {
            Marriage.LOGGER.atInfo().log(Message.translation("server.commands.marry.message.success").param("username",playerRef.getUsername()).getAnsiMessage());
        }
    }
}