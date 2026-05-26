package plugin.siren.Commands.Marry;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Systems.MarriageComponent;
import plugin.siren.Systems.MarriageSettingsComponent;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        MarriageSettingsComponent marriageSettings = store.getComponent(ref, MarriageSettingsComponent.getComponentType());

        if(marriageSettings == null){
            Marriage.LOGGER.atInfo().log("Failed to get Marriage Settings Component : DivorceCmd");
        }else{
            if(marriageSettings.isMarried()) {
                MarriageComponent marriage = store.getComponent(ref, MarriageComponent.getComponentType());

                if (marriage == null) {
                    Marriage.LOGGER.atInfo().log("Failed to get Marriage Component : DivorceCmd");
                } else {
                    PlayerRef partnerPlayerRef = Universe.get().getPlayer(marriageSettings.getPartnerUUID());

                    if (partnerPlayerRef == null) {
                        Marriage.LOGGER.atInfo().log("Failed to get partnerPlayerRef : DivorceCmd");
                    } else {
                        if (marriage.getDivorceTimer() >= 1) {
                            MarriageSettingsComponent partnerMarriageSettings = store.getComponent(partnerPlayerRef.getReference(), MarriageSettingsComponent.getComponentType());
                            if (partnerMarriageSettings == null) {
                                Marriage.LOGGER.atInfo().log("Failed to get partnerPlayerRef Marriage Settings Component : DivorceCmd");
                            } else {
                                partnerMarriageSettings.setMarried(false);
                                partnerMarriageSettings.clearPartnerUUID();
                                partnerMarriageSettings.setPartnerUsername("");
                            }

                            marriageSettings.setMarried(false);
                            marriageSettings.clearPartnerUUID();
                            marriageSettings.setPartnerUsername("");

                            playerRef.sendMessage(Message.translation("server.commands.marry.divorce.player.msg").param("partnerUsername",partnerPlayerRef.getUsername()));

                            partnerPlayerRef.sendMessage(Message.translation("server.commands.marry.divorce.player.msg").param("partnerUsername",playerRef.getUsername()));

                            Message divorceMessage = Message.translation("server.commands.marry.divorce.console.alert").param("usernameOne", playerRef.getUsername()).param("usernameTwo", partnerPlayerRef.getUsername());
                            Collection<PlayerRef> onlinePlayersCollection = Universe.get().getPlayers();
                            List<PlayerRef> onlinePlayers = onlinePlayersCollection.stream().toList();
                            for(PlayerRef plyRef : onlinePlayers){
                                plyRef.sendMessage(divorceMessage);
                            }

                        } else {
                            marriage.setDivorceTimer(1);

                            playerRef.sendMessage(Message.translation("server.commands.marry.divorce.confirmation"));

                            HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
                                world.execute(() -> {
                                    marriage.setDivorceTimer(0);
                                });
                            }, 10, TimeUnit.SECONDS);
                        }
                    }
                }
            }else{
                playerRef.sendMessage(Message.translation("server.commands.marry.divorce.unmarried"));
            }
        }

        if(Marriage.ifDebug()) {
            Marriage.LOGGER.atInfo().log(Message.translation("server.commands.marry.divorce.success").param("username",playerRef.getUsername()).getAnsiMessage());
        }
    }
}