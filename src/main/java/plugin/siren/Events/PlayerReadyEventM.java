package plugin.siren.Events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Systems.MarriageComponent;
import plugin.siren.Systems.MarriageSettingsComponent;
import plugin.siren.Utils.MarriageUpdateChecker;

import java.awt.*;

public class PlayerReadyEventM {
    public static void onPlayerReadyEvent(PlayerReadyEvent event){
        World world = event.getPlayer().getWorld();
        world.execute(() -> {
            Ref<EntityStore> ref = event.getPlayerRef();
            Store<EntityStore> store = ref.getStore();

            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            boolean validPlayerRef = playerRef != null && playerRef.isValid();

            MarriageComponent marComp = store.getComponent(ref, MarriageComponent.getComponentType());
            if (marComp == null) {
                MarriageComponent marriageComponent = new MarriageComponent();

                store.putComponent(ref, MarriageComponent.getComponentType(), marriageComponent);

                if (Marriage.ifDebug() && validPlayerRef) {
                    playerRef.sendMessage(Message.raw("You now have the Marriage Component!"));

                    Marriage.LOGGER.atInfo().log(playerRef.getUsername() + " now has the Marriage Component.");
                }
            }else{
                if (Marriage.ifDebug() && validPlayerRef) {
                    playerRef.sendMessage(Message.raw("You already have the Marriage Component!"));

                    Marriage.LOGGER.atInfo().log(playerRef.getUsername() + " tried to receive Marriage Component but already has it.");
                }
            }

            MarriageSettingsComponent marSett = store.getComponent(ref, MarriageSettingsComponent.getComponentType());
            if (marSett == null) {
                MarriageSettingsComponent marriageSettings = new MarriageSettingsComponent();

                store.putComponent(ref, MarriageSettingsComponent.getComponentType(), marriageSettings);

                if (Marriage.ifDebug() && validPlayerRef) {
                    playerRef.sendMessage(Message.raw("You now have the Marriage Settings Component!"));

                    Marriage.LOGGER.atInfo().log(playerRef.getUsername() + " now has the Marriage Settings Component.");
                }
            }else{
                if (Marriage.ifDebug() && validPlayerRef) {
                    playerRef.sendMessage(Message.raw("You already have the Marriage Settings Component!"));

                    Marriage.LOGGER.atInfo().log(playerRef.getUsername() + " tried to receive Marriage Settings Component but already has it.");
                }
            }

            if (playerRef != null && playerRef.isValid()) {
                MarriageComponent marriage = store.ensureAndGetComponent(ref, MarriageComponent.getComponentType());
                if (marriage == null) {
                    MarriageUpdateChecker.sendUpdateMessage(playerRef, MarriageUpdateChecker.Type.PlayerReadyEvent);
                } else {
                    if (!marriage.getUpdateCheckerCheck()) {
                        MarriageUpdateChecker.sendUpdateMessage(playerRef, MarriageUpdateChecker.Type.PlayerReadyEvent);

                        marriage.setCheckOnUpdateChecker(true);
                    }
                }
            }
        });
    }
}
