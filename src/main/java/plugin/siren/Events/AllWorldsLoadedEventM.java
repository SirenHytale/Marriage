package plugin.siren.Events;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import plugin.siren.Utils.MarriageUpdateChecker;

import java.util.concurrent.TimeUnit;

public class AllWorldsLoadedEventM {
    public static void onAllWorldsLoaded(AllWorldsLoadedEvent event){
        //MermaidsPUpdateChecker
        Runnable updateCheckRunnable = new Runnable() {
            @Override
            public void run() {
                MarriageUpdateChecker.sendUpdateMessage(true);
            }
        };

        HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(updateCheckRunnable, 3, 60*60, TimeUnit.SECONDS);
    }
}
