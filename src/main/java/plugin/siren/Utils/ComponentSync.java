package plugin.siren.Utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Systems.MarriageSettingsComponent;
import plugin.siren.Utils.Registry.MarriageManager;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Keeps the per-player persisted MarriageSettingsComponent (a cache since 2.0.0 -
 * the registry is the source of truth) in step with registry mutations. Every
 * write hops onto the target player's own world thread, so these are safe to call
 * from anywhere. Offline players are reconciled on next join instead.
 */
public final class ComponentSync {

    private ComponentSync(){}

    /** Marks the player married in their settings component and counts the marriage. */
    public static void syncMarried(@Nonnull PlayerRef target, @Nonnull UUID partnerUuid, @Nonnull String partnerName){
        onSettings(target, settings -> {
            settings.setMarried(true);
            settings.setPartnerUUID(partnerUuid);
            settings.setPartnerUsername(partnerName);
            settings.incrementTimesMarried();
        });
    }

    /** Clears the married state in the player's settings component and counts the divorce. */
    public static void syncDivorced(@Nonnull PlayerRef target){
        onSettings(target, settings -> {
            // Consume the marker only where the settings actually get cleared - if
            // this hop never runs, the marker survives for the next-join reconcile.
            MarriageManager.consumePendingDivorceSync(target.getUuid());
            if(settings.isMarried()){
                settings.setMarried(false);
                settings.clearPartnerUUID();
                settings.setPartnerUsername("");
                settings.incrementDivorces();
            }
        });
    }

    private interface SettingsMutation {
        void apply(MarriageSettingsComponent settings);
    }

    private static void onSettings(PlayerRef target, SettingsMutation mutation){
        Ref<EntityStore> ref = target.getReference();
        if(ref == null || !ref.isValid()){
            return; // offline - PlayerReadyEvent reconciles on next join
        }
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();
        CompletableFuture.runAsync(() -> {
            // If they changed worlds mid-hop their components belong to another
            // thread now; skip - PlayerReadyEvent reconciles the cache on next join.
            if(!ref.isValid() || ref.getStore() != store){
                return;
            }
            MarriageSettingsComponent settings = store.getComponent(ref, MarriageSettingsComponent.getComponentType());
            if(settings == null){
                Marriage.LOGGER.atInfo().log("Failed to get Marriage Settings Component : ComponentSync for " + target.getUsername());
                return;
            }
            mutation.apply(settings);
        }, world);
    }
}
