package plugin.siren.Commands.Marry;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Systems.MarriageComponent;
import plugin.siren.Systems.MarriageSettingsComponent;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collection;
import java.util.List;

public class MarryPlayerCmd extends AbstractPlayerCommand {
    public MarryPlayerCmd() {
        super("player", "server.commands.marry.player.desc");

        if(Marriage.getConfig().get().ifCmdPermission()){
            this.requirePermission("marriage.marry");
        }else{
            this.setPermissionGroups("hytale:None");
        }
    }

    RequiredArg<PlayerRef> msgMarryPlayerArg = this.withRequiredArg("Player Username", "server.commands.marry.player.arg.username.desc", ArgTypes.PLAYER_REF);

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        PlayerRef partnerPlayerRef = msgMarryPlayerArg.get(commandContext);

        MarriageSettingsComponent marriageSettings = store.getComponent(ref, MarriageSettingsComponent.getComponentType());

        if(marriageSettings == null){
            Marriage.LOGGER.atInfo().log("Failed to get Marriage Settings Component : MarryPlayerCmd");
        }else {
            if (marriageSettings.isMarried()) {
                playerRef.sendMessage(Message.translation("server.commands.marry.player.alreadyMarried"));
            } else {
                boolean marriageAllowed = false;
                if(Marriage.getConfig().get().ifRequireRing()){
                    InventoryComponent.Hotbar hotbarComponent = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
                    if(hotbarComponent != null) {
                        ItemStack itemInHand = hotbarComponent.getActiveItem();
                        if (itemInHand != null && itemInHand.getItemId().equalsIgnoreCase("marriage_ring")) {
                            marriageAllowed = true;
                        } else {
                            playerRef.sendMessage(Message.translation("server.commands.marry.player.missingRing"));
                        }
                    }
                }else{
                    marriageAllowed = true;
                }

                if(marriageAllowed) {
                    if (partnerPlayerRef == null || !partnerPlayerRef.isValid()) {
                        Marriage.LOGGER.atInfo().log("Failed to get partnerPlayerRef reference : MarryPlayerCmd");
                    } else {
                        MarriageComponent partnerMarriage = store.getComponent(partnerPlayerRef.getReference(), MarriageComponent.getComponentType());

                        if (partnerMarriage == null) {
                            Marriage.LOGGER.atInfo().log("Failed to get partnerPlayerRef Marriage Component : MarryPlayerCmd");
                        } else {
                            List<PlayerRef> requests = partnerMarriage.getRequestsList();

                            boolean aRequest = false;

                            if (requests.isEmpty()) {
                                Marriage.LOGGER.atInfo().log(playerRef.getUsername() + " has no current requests");
                            } else {
                                for (PlayerRef plyRefs : requests) {
                                    if (plyRefs == playerRef) {
                                        if (!playerRef.getUsername().equalsIgnoreCase(partnerPlayerRef.getUsername())) {
                                            aRequest = true;
                                        }
                                    }
                                }
                            }
                            MarriageComponent marriage = store.getComponent(ref, MarriageComponent.getComponentType());

                            if (marriage == null) {
                                Marriage.LOGGER.atInfo().log("Failed to get ref Marriage Component : MarryPlayerCmd");
                            } else {
                                marriage.addRequestToList(partnerPlayerRef);

                                if (aRequest) {
                                    MarriageSettingsComponent partnerMarriageSettings = store.getComponent(partnerPlayerRef.getReference(), MarriageSettingsComponent.getComponentType());

                                    marriageSettings.setPartnerUUID(partnerPlayerRef.getUuid());
                                    marriageSettings.setPartnerUsername(partnerPlayerRef.getUsername());
                                    marriageSettings.setMarried(true);
                                    marriage.clearRequestsList();

                                    partnerMarriageSettings.setPartnerUUID(playerRef.getUuid());
                                    partnerMarriageSettings.setPartnerUsername(playerRef.getUsername());
                                    partnerMarriageSettings.setMarried(true);
                                    partnerMarriage.clearRequestsList();

                                    playerRef.sendMessage(Message.translation("server.commands.marry.player.marry.player.msg").param("partnerUsername",partnerPlayerRef.getUsername()));

                                    partnerPlayerRef.sendMessage(Message.translation("server.commands.marry.player.marry.player.msg").param("partnerUsername",playerRef.getUsername()));


                                    Message marriageMessage = Message.translation("server.commands.marry.player.console.alert").param("usernameOne", playerRef.getUsername()).param("usernameTwo", partnerPlayerRef.getUsername());

                                    Collection<PlayerRef> onlinePlayersCollection = Universe.get().getPlayers();
                                    List<PlayerRef> onlinePlayers = onlinePlayersCollection.stream().toList();
                                    for(PlayerRef plyRef : onlinePlayers){
                                        plyRef.sendMessage(marriageMessage.color(Color.PINK));
                                    }

                                    Marriage.LOGGER.atInfo().log(playerRef.getUsername() + " and " + partnerPlayerRef.getUsername() + " just got Married!");
                                } else {
                                    if (!playerRef.getUsername().equalsIgnoreCase(partnerPlayerRef.getUsername())) {
                                        playerRef.sendMessage(Message.translation("server.commands.marry.player.request.player.msg").param("username",partnerPlayerRef.getUsername()));

                                        partnerPlayerRef.sendMessage(Message.translation("server.commands.marry.player.receiveRequest.player.msg").param("username",partnerPlayerRef.getUsername()));
                                    } else {
                                        playerRef.sendMessage(Message.translation("server.commands.marry.player.self.player.msg"));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if(Marriage.ifDebug()) {
            Marriage.LOGGER.atInfo().log(Message.translation("server.commands.marry.player.success").param("username",playerRef.getUsername()).getAnsiMessage());
        }
    }
}