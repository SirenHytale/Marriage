package plugin.siren.Events;

import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import plugin.siren.Systems.MarriageBondOfPainSystem;
import plugin.siren.Utils.Bond.PartnerPositions;
import plugin.siren.Utils.EmoteManager;

/**
 * Frees the off-entity state a departing player leaves behind.
 *
 * <p>Everything dropped here is UUID-keyed and lives outside the ECS — it has to,
 * being written from world threads and read from the world map thread — so nothing
 * releases it when the entity unloads. Without this pass, every player who has
 * ever logged in since boot keeps an entry for the life of the process, and a
 * partner's map pin would sit at the spot they logged out from forever.</p>
 *
 * <p>The death site is deliberately <em>not</em> dropped: where your partner fell
 * is exactly the thing you want to still be there after they disconnect, and it
 * expires on its own clock (see {@code PartnerPositions.getDeath}).</p>
 */
public class PlayerDisconnectEventM {
    public static void onPlayerDisconnect(PlayerDisconnectEvent event){
        PlayerRef playerRef = event.getPlayerRef();
        if(playerRef == null){
            return;
        }

        PartnerPositions.forget(playerRef.getUuid());
        MarriageBondOfPainSystem.forget(playerRef.getUuid());
        EmoteManager.forget(playerRef.getUuid());
    }
}
