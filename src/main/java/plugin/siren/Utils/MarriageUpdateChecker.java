package plugin.siren.Utils;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import plugin.siren.Marriage;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MarriageUpdateChecker {
    /** How long the API gets to answer before the check gives up, per phase. */
    private static final int TIMEOUT_MILLIS = 5_000;

    // volatile: written by whichever thread ran the last fetch, read on world threads at join
    private static volatile java.util.List<String> latestVersions;

    /** Blocking HTTP - never call from a world thread or HytaleServer.SCHEDULED_EXECUTOR itself. */
    public static java.util.List<String> getVersionStrings(){
        try{
            URL url = URI.create("https://api.mermaids.dev/versions/marriage/release/").toURL();

            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(TIMEOUT_MILLIS);
            connection.setReadTimeout(TIMEOUT_MILLIS);
            InputStream inputStream = connection.getInputStream();

            java.util.List<String> list = new ArrayList<>();

            try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))){
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    if(line.contains("<h1>{ version: ") && line.contains(" }</h1>")){
                        line = line.substring(line.indexOf("<h1>{ version: ") + 15);
                        line = line.substring(0, line.indexOf(" }</"));
                        list.add(line);
                    }

                    if(line.contains("<h3>{ ignore-version: ") && line.contains(" }</h3>")){
                        line = line.substring(line.indexOf("<h3>{ ignore-version: ") + 22);
                        line = line.substring(0, line.indexOf(" }</"));
                        list.add(line);
                    }
                }

                return list;
            }
        } catch (Exception e) {
            Marriage.LOGGER.atInfo().log("Exception with MarriageUpdateChecker : " + e.toString());

            java.util.List<String> list = new ArrayList<>();
            list.add(Marriage.getVersion());

            return list;
        }
    }

    public static void sendUpdateMessage(){
        sendUpdateMessage(null, false, Type.Default);
    }

    public static String sendUpdateMessage(Type type){
        if(Type.StartUp.getValue() == type.getValue()) {
            java.util.List<String> recentVersions = MarriageUpdateChecker.getVersionStrings();
            latestVersions = recentVersions;

            if(recentVersions.isEmpty()){
                return null;
            }

            boolean outDated = true;
            String latestVersion = recentVersions.getFirst();
            for(int i = 0; i < recentVersions.size(); i++){
                if(recentVersions.get(i).equalsIgnoreCase(Marriage.getVersion())){
                    outDated = false;
                }
            }

            if(outDated) {
                Marriage.LOGGER.atInfo().log("= =- -=- -=- -=- -=- -=- -=- -=- -= =");
                String versionMessage = "The Marriage Mod version is outdated, Marriage has released v" + latestVersion +".";
                Marriage.LOGGER.atInfo().log(versionMessage);
            }
        }else if(Type.InfoCmd.getValue() == type.getValue()){
            java.util.List<String> recentVersions = MarriageUpdateChecker.getVersionStrings();
            latestVersions = recentVersions;

            if(recentVersions.isEmpty()){
                return Marriage.getVersion();
            }else{
                return recentVersions.getFirst();
            }
        }else if(Type.MermaidsUI.getValue() == type.getValue()){
            java.util.List<String> cached = latestVersions;
            if(cached == null || cached.isEmpty()){
                return Marriage.getVersion();
            }else{
                return cached.getFirst();
            }
        }else{
            sendUpdateMessage(false);
        }

        return null;
    }

    public static void sendUpdateMessage(boolean requestAPI){
        sendUpdateMessage(null, false, Type.Default, requestAPI);
    }

    public static void sendUpdateMessage(PlayerRef playerRef){
        sendUpdateMessage(playerRef, true, Type.Default);
    }

    public static void sendUpdateMessage(PlayerRef playerRef, Type type){
        sendUpdateMessage(playerRef, true, type);
    }

    public static void sendUpdateMessage(@Nullable PlayerRef playerRef, boolean sendToPlayer, Type type){
        sendUpdateMessage(playerRef, sendToPlayer, type, false);
    }

    public static void sendUpdateMessage(@Nullable PlayerRef playerRef, boolean sendToPlayer, Type type, boolean requestAPI){
        List<String> recentVersions = new ArrayList<>();
        if(requestAPI){
            recentVersions = MarriageUpdateChecker.getVersionStrings();
            latestVersions = recentVersions;
        }else{
            recentVersions = latestVersions;
        }

        // Empty or not fetched yet: nothing to compare against. An exception here would
        // silently cancel the hourly scheduled task for good.
        if(recentVersions == null || recentVersions.isEmpty()){
            return;
        }

        boolean outDated = true;
        String latestVersion = recentVersions.getFirst();
        for(int i = 0; i < recentVersions.size(); i++){
            if(recentVersions.get(i).equalsIgnoreCase(Marriage.getVersion())){
                outDated = false;
            }
        }

        if(outDated){
            String translationId = "server.updateChecker.marriage.release.message";
            Message versionMessage = Message.translation(translationId).param("version", latestVersion);

            if(Type.PlayerReadyEvent.getValue() != type.getValue()) {
                Marriage.LOGGER.atInfo().log(versionMessage.getAnsiMessage());
            }

            if(sendToPlayer && playerRef != null) {
                if ((playerRef.hasPermission("*") || playerRef.hasPermission("marriage.admin")) && Marriage.getConfig().get().ifNewVersion()) {
                    playerRef.sendMessage(versionMessage.color(Color.RED).link("https://www.mermaids.dev/marriage/curseforge/"));
                }
            }
        }
    }

    public enum Type {
        StartUp(0),
        InfoCmd(1),
        PlayerReadyEvent(2),
        Default(3),
        MermaidsUI(4);

        private final int value;
        private Type(int value){
            this.value = value;
        }

        public int getValue(){
            return this.value;
        }
    }
}
