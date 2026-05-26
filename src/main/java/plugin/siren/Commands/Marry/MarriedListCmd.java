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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MarriedListCmd extends AbstractPlayerCommand {
    public MarriedListCmd() {
        super("list", "server.commands.marry.list.desc");

        this.setPermissionGroups("hytale:None");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Collection<PlayerRef> onlinePlayersCollection = Universe.get().getPlayers();
        List<PlayerRef> onlinePlayers = onlinePlayersCollection.stream().toList();

        if(onlinePlayers.isEmpty()){
            Marriage.LOGGER.atInfo().log("No online players.");
        }else{
            List<PlayerRef> marriedList = new ArrayList<>();
            for(PlayerRef plyRef : onlinePlayers){
                MarriageSettingsComponent marrSettings = store.getComponent(plyRef.getReference(), MarriageSettingsComponent.getComponentType());

                if(marrSettings == null){
                    Marriage.LOGGER.atInfo().log("Failed to get marrSettings Marriage Settings Component : MarriedListCmd");
                }else{
                    if(marrSettings.isMarried()){
                        if(marriedList.isEmpty()){
                            marriedList.add(plyRef);
                        }else{
                            boolean addToList = true;
                            PlayerRef checkMarriedPlayer = Universe.get().getPlayer(marrSettings.getPartnerUUID());

                            if(checkMarriedPlayer == null){
                                Marriage.LOGGER.atInfo().log("Failed to get checkMarriedPlayer PlayerRef : MarriedListCmd");
                            }else{
                                for(PlayerRef marrPlyRef : marriedList){
                                    if(checkMarriedPlayer == marrPlyRef){
                                        addToList = false;
                                    }
                                }
                            }

                            if(addToList){
                                marriedList.add(plyRef);
                            }
                        }
                    }
                }
            }

            if(marriedList.isEmpty()){
                playerRef.sendMessage(Message.raw("No online players are currently married."));
            }else{
                for(PlayerRef marrPlyRef : marriedList){
                    MarriageSettingsComponent marrSettings = store.getComponent(marrPlyRef.getReference(), MarriageSettingsComponent.getComponentType());

                    if(marrSettings == null){
                        Marriage.LOGGER.atInfo().log("Failed to get marrSettings Marriage Settings Component : MarriedListCmd");
                    }else{
                        PlayerRef marriedPlayerPartner = Universe.get().getPlayer(marrSettings.getPartnerUUID());

                        if(marriedPlayerPartner == null){
                            Marriage.LOGGER.atInfo().log("Failed to get marriedPlayerPartner PlayerRef : MarriedListCmd");
                        }else{
                            playerRef.sendMessage(Message.translation("server.commands.marry.list.married").param("usernameOne", marrPlyRef.getUsername()).param("usernameTwo", marriedPlayerPartner.getUsername()));
                        }
                    }

                }
            }
        }
        if(Marriage.ifDebug()) {
            Marriage.LOGGER.atInfo().log(Message.translation("server.commands.marry.list.success").param("username",playerRef.getUsername()).getAnsiMessage());
        }
    }
}