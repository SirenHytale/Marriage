package plugin.siren;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import plugin.siren.Commands.MarriageCmd;
import plugin.siren.Commands.MarryCmd;
import plugin.siren.Events.AllWorldsLoadedEventM;
import plugin.siren.Events.PlayerReadyEventM;
import plugin.siren.Systems.MarriageComponent;
import plugin.siren.Systems.MarriageSettingsComponent;
import plugin.siren.Utils.HStats;
import plugin.siren.Utils.MarriageConfig;
import plugin.siren.Utils.MarriageUpdateChecker;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class Marriage extends JavaPlugin {
    private static final String VERSION = "1.2.4";
    private static final boolean DEBUG = false;

    private static Marriage plugin;
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Config<MarriageConfig> config;

    private ComponentType<EntityStore, MarriageComponent> marriageComponent;
    private ComponentType<EntityStore, MarriageSettingsComponent> marriageSettingsComponent;

    public Marriage(@Nonnull JavaPluginInit init){
        super(init);

        plugin = this;
        this.config = this.withConfig("Marriage", MarriageConfig.CODEC);

        new HStats("650607f7-6c8a-4d52-ab81-f8ca3a9e9db2", VERSION);
    }

    @Override
    protected void setup(){
        LOGGER.atInfo().log("===---==---==---== MARRIAGE ==---==---==---===");
        LOGGER.atInfo().log("Marriage has began to load.");

        EventRegistration<String, PlayerReadyEvent> playerReadyEventRegistration = this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerReadyEventM::onPlayerReadyEvent);
        if(playerReadyEventRegistration != null && playerReadyEventRegistration.isRegistered()) {
            LOGGER.atInfo().log("Registered Player Ready Event.");
        }else{
            LOGGER.atSevere().log("Failed to register Player Ready Event.");
        }

        EventRegistration<Void, AllWorldsLoadedEvent> allWorldsLoadedEventRegistration = this.getEventRegistry().registerGlobal(AllWorldsLoadedEvent.class, AllWorldsLoadedEventM::onAllWorldsLoaded);
        if(allWorldsLoadedEventRegistration != null && allWorldsLoadedEventRegistration.isRegistered()) {
            LOGGER.atInfo().log("Registered All Worlds Loaded Event.");
        }else{
            LOGGER.atSevere().log("Failed to register All Worlds Loaded Event.");
        }

        CommandRegistration marriageCmdRegistration = this.getCommandRegistry().registerCommand(new MarriageCmd());
        if(marriageCmdRegistration != null && marriageCmdRegistration.isRegistered()) {
            LOGGER.atInfo().log("Registered Marriage Command.");
        }else{
            LOGGER.atSevere().log("Failed to register Marriage Command.");
        }

        CommandRegistration marryCmdRegistration = this.getCommandRegistry().registerCommand(new MarryCmd());
        if(marryCmdRegistration != null && marryCmdRegistration.isRegistered()) {
            LOGGER.atInfo().log("Registered Marry Command.");
        }else{
            LOGGER.atSevere().log("Failed to register Marry Command.");
        }

        this.marriageComponent = this.getEntityStoreRegistry().registerComponent(MarriageComponent.class, MarriageComponent::new);
        if(this.marriageComponent != null) {
            LOGGER.atInfo().log("Registered Marriage Component.");
        }else{
            LOGGER.atInfo().log("Failed to register Marriage Component.");
        }

        this.marriageSettingsComponent = this.getEntityStoreRegistry().registerComponent(MarriageSettingsComponent.class, "MarriageSettings", MarriageSettingsComponent.CODEC);
        if(this.marriageSettingsComponent != null) {
            LOGGER.atInfo().log("Registered Marriage Settings Component.");
        }else{
            LOGGER.atInfo().log("Failed to register Marriage Settings Component.");
        }

        config.save();
        LOGGER.atInfo().log("Loaded config settings.");
        boolean configUpdated = config.get().ifConfigUpdate();
        if(configUpdated){
            config.save();
            LOGGER.atInfo().log("Updated config to latest version.");
        }

        LOGGER.atInfo().log("Version " + VERSION + " of Marriage has successfully loaded.");

        if(ifDebug()){
            LOGGER.atInfo().log("= =- -=- -=- -=- -=- -=- -=- -=- -= =");
            LOGGER.atInfo().log("Loaded Marriage in Debug mode.");
        }

        MarriageUpdateChecker.sendUpdateMessage(MarriageUpdateChecker.Type.StartUp);

        LOGGER.atInfo().log("===---==---==---==---==---==---==---==---===");
    }

    @Override
    protected void shutdown(){
        LOGGER.atInfo().log("===---==---==---== MARRIAGE ==---==---==---===");
        LOGGER.atInfo().log("Marriage has began to shutdown.");
        LOGGER.atInfo().log("Saving any necessary data.");
        LOGGER.atInfo().log("Version " + VERSION + " of Marriage has successfully shutdown.");
        LOGGER.atInfo().log("===---==---==---==---==---==---==---==---===");
    }

    public ComponentType<EntityStore, MarriageComponent> getMarriageComponentType(){
        return marriageComponent;
    }

    public ComponentType<EntityStore, MarriageSettingsComponent> getMarriageSettingsComponentType(){
        return marriageSettingsComponent;
    }

    public static Marriage get(){
        return plugin;
    }

    public static String getVersion(){
        return VERSION;
    }

    public static Config<MarriageConfig> getConfig(){
        return plugin.config;
    }

    public static boolean ifDebug(){
        return plugin.DEBUG;
    }
}
