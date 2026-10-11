package plugin.siren;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import plugin.siren.Commands.MarriageCmd;
import plugin.siren.Commands.MarryCmd;
import plugin.siren.Events.AllWorldsLoadedEventM;
import plugin.siren.Events.PlayerChatEventM;
import plugin.siren.Events.PlayerDisconnectEventM;
import plugin.siren.Events.PlayerReadyEventM;
import plugin.siren.Systems.MarriageBondOfPainSystem;
import plugin.siren.Systems.MarriageComponent;
import plugin.siren.Systems.MarriageDeathSystem;
import plugin.siren.Systems.MarriagePresenceSystem;
import plugin.siren.Systems.MarriagePvPSystem;
import plugin.siren.Systems.MarriageRegenSystem;
import plugin.siren.Systems.MarriageRespawnSystem;
import plugin.siren.Systems.MarriageSettingsComponent;
import plugin.siren.Utils.HStats;
import plugin.siren.Utils.MarriageConfig;
import plugin.siren.Utils.MarriageMarkers;
import plugin.siren.Utils.MarriageUpdateChecker;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriagesData;
import plugin.siren.Utils.UI.MarriageUIDefaults;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class Marriage extends JavaPlugin {
    /**
     * The manifest's version - the single source of truth, so this class can
     * never drift from what the server loaded. Set in the constructor, the
     * earliest point the manifest is available, before anything reads it.
     */
    private static String version = "0.0.0";
    private static final boolean DEBUG = false;

    private static Marriage plugin;
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Config<MarriageConfig> config;
    private final Config<MarriagesData> marriagesData;

    private ComponentType<EntityStore, MarriageComponent> marriageComponent;
    private ComponentType<EntityStore, MarriageSettingsComponent> marriageSettingsComponent;

    public Marriage(@Nonnull JavaPluginInit init){
        super(init);

        plugin = this;
        version = this.getManifest().getVersion().toString();
        this.config = this.withConfig("Marriage", MarriageConfig.CODEC);
        this.marriagesData = this.withConfig("Data/MarriagesData", MarriagesData.CODEC);

        // Off the boot thread: HStats' constructor makes two blocking HTTP calls, and
        // nothing here keeps a reference to it. (Vendored file - license forbids editing it.)
        final String statsVersion = version;
        CompletableFuture.runAsync(() -> new HStats("650607f7-6c8a-4d52-ab81-f8ca3a9e9db2", statsVersion));
    }

    @Override
    protected void setup(){
        LOGGER.atInfo().log("===---==---==---== MARRIAGE ==---==---==---===");
        LOGGER.atInfo().log("Marriage has began to load.");

        MarriageManager.init(this.marriagesData);

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
            LOGGER.atSevere().log("Failed to register Marriage Component.");
        }

        this.marriageSettingsComponent = this.getEntityStoreRegistry().registerComponent(MarriageSettingsComponent.class, "MarriageSettings", MarriageSettingsComponent.CODEC);
        if(this.marriageSettingsComponent != null) {
            LOGGER.atInfo().log("Registered Marriage Settings Component.");
        }else{
            LOGGER.atSevere().log("Failed to register Marriage Settings Component.");
        }

        this.getEntityStoreRegistry().registerSystem(new MarriagePvPSystem());
        LOGGER.atInfo().log("Registered Marriage PvP Protection System.");

        // After the PvP system, which it declares a dependency on - a blow already
        // cancelled by partner protection must never be split (see the class note).
        this.getEntityStoreRegistry().registerSystem(new MarriageBondOfPainSystem());
        LOGGER.atInfo().log("Registered Marriage Bond of Pain System.");

        this.getEntityStoreRegistry().registerSystem(new MarriageRegenSystem(
                (float) this.config.get().getRegenIntervalSeconds(), this.marriageComponent));
        LOGGER.atInfo().log("Registered Marriage Partner Regen System.");

        this.getEntityStoreRegistry().registerSystem(new MarriagePresenceSystem(this.marriageComponent));
        LOGGER.atInfo().log("Registered Marriage Presence and Bond System.");

        this.getEntityStoreRegistry().registerSystem(new MarriageDeathSystem());
        LOGGER.atInfo().log("Registered Marriage Death System.");

        this.getEntityStoreRegistry().registerSystem(new MarriageRespawnSystem());
        LOGGER.atInfo().log("Registered Marriage Respawn System.");

        // Per WORLD rather than once globally: the marker provider registry lives on
        // each world's own WorldMapManager, so a world added later would otherwise
        // never get it. Same reason every vanilla marker provider registers here.
        this.getEventRegistry().registerGlobal(AddWorldEvent.class, event ->
                event.getWorld().getWorldMapManager().addMarkerProvider(
                        MarriageMarkers.PROVIDER_KEY, MarriageMarkers.INSTANCE));
        LOGGER.atInfo().log("Registered Marriage world-map markers.");

        // registerGlobal, not register: PlayerChatEvent is IAsyncEvent<String> like
        // PlayerReadyEvent (keyed by chat channel), so it takes the "any key"
        // overload rather than PlayerDisconnectEvent's unkeyed one. LATE: Cultivation and
        // Siren's Commands call setFormatter without chaining, so Marriage has to run
        // after them to wrap their formatter rather than be overwritten by it.
        EventRegistration<String, PlayerChatEvent> chatRegistration =
                this.getEventRegistry().registerGlobal(EventPriority.LATE, PlayerChatEvent.class, PlayerChatEventM::onPlayerChatEvent);
        if(chatRegistration != null && chatRegistration.isRegistered()) {
            LOGGER.atInfo().log("Registered Player Chat Event.");
        }else{
            LOGGER.atSevere().log("Failed to register Player Chat Event.");
        }

        EventRegistration<Void, PlayerDisconnectEvent> disconnectRegistration =
                this.getEventRegistry().register(PlayerDisconnectEvent.class, PlayerDisconnectEventM::onPlayerDisconnect);
        if(disconnectRegistration != null && disconnectRegistration.isRegistered()) {
            LOGGER.atInfo().log("Registered Player Disconnect Event.");
        }else{
            LOGGER.atSevere().log("Failed to register Player Disconnect Event.");
        }

        MarriageUIDefaults.register();
        LOGGER.atInfo().log("Registered Marriage Menu pages.");

        config.save();
        LOGGER.atInfo().log("Loaded config settings.");
        boolean configUpdated = config.get().ifConfigUpdate();
        if(configUpdated){
            config.save();
            LOGGER.atInfo().log("Updated config to latest version.");
        }

        if(config.get().ifGrantMenuPermission() && !config.get().ifGrantedMenuPermission()){
            LOGGER.atInfo().log("Adding marriage permission to Adventure and default group.");
            PermissionsModule.get().addGroupPermission("hytale:None", Set.of("marriage"));
            PermissionsModule.get().addGroupPermission("hytale:Adventurer", Set.of("marriage"));

            config.get().setGrantedMenuPermission(true);
            config.save();
        }

        LOGGER.atInfo().log("Version " + version + " of Marriage has successfully loaded.");

        if(ifDebug()){
            LOGGER.atInfo().log("= =- -=- -=- -=- -=- -=- -=- -=- -= =");
            LOGGER.atInfo().log("Loaded Marriage in Debug mode.");
        }

        // Off the boot thread: the check is a blocking HTTP call.
        CompletableFuture.runAsync(() -> {
            try{
                MarriageUpdateChecker.sendUpdateMessage(MarriageUpdateChecker.Type.StartUp);
            }catch(Exception e){
                LOGGER.atWarning().log("Startup update check failed: " + e);
            }
        });

        LOGGER.atInfo().log("===---==---==---==---==---==---==---==---===");
    }

    @Override
    protected void shutdown(){
        LOGGER.atInfo().log("===---==---==---== MARRIAGE ==---==---==---===");
        LOGGER.atInfo().log("Marriage has began to shutdown.");
        LOGGER.atInfo().log("Saving any necessary data.");
        LOGGER.atInfo().log("Version " + version + " of Marriage has successfully shutdown.");
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
        return version;
    }

    public static Config<MarriageConfig> getConfig(){
        return plugin.config;
    }

    public static boolean ifDebug(){
        return DEBUG;
    }
}
