package plugin.siren.Events;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import plugin.siren.Compatibility.CultivationBridge;
import plugin.siren.Utils.MarriageUpdateChecker;

import plugin.siren.Marriage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AllWorldsLoadedEventM {
    public static void onAllWorldsLoaded(AllWorldsLoadedEvent event){
        // Probe the Cultivation pairing once, here rather than in setup(). Marriage
        // declares no dependency on Cultivation - doing so would be a load cycle,
        // see CultivationBridge - so at setup() time Cultivation may not have
        // loaded yet and the probe would find nothing. By the time every world is
        // up, every plugin is. Doing it here also means the boot log states plainly
        // which way the pairing went, exactly as Cultivation's own log does for
        // its side, instead of the answer first appearing when a player happens to
        // open a menu.
        CultivationBridge.isAvailable();

        //MermaidsPUpdateChecker
        Runnable updateCheckRunnable = new Runnable() {
            @Override
            public void run() {
                // SCHEDULED_EXECUTOR is one thread shared by the whole server, so the
                // blocking HTTP goes to the common pool and the timer only hands off.
                CompletableFuture.runAsync(() -> {
                    try{
                        MarriageUpdateChecker.sendUpdateMessage(true);
                    }catch(Exception e){
                        Marriage.LOGGER.atWarning().log("Update check failed: " + e);
                    }
                });
            }
        };

        HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(updateCheckRunnable, 3, 60*60, TimeUnit.SECONDS);
    }
}
