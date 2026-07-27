package plugin.siren.Events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Systems.MarriageComponent;
import plugin.siren.Systems.MarriageSettingsComponent;
import plugin.siren.Utils.MarriageUpdateChecker;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;

import java.util.UUID;

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
                store.putComponent(ref, MarriageComponent.getComponentType(), new MarriageComponent());
            }

            MarriageSettingsComponent settings = store.getComponent(ref, MarriageSettingsComponent.getComponentType());
            if (settings == null) {
                settings = new MarriageSettingsComponent();
                store.putComponent(ref, MarriageSettingsComponent.getComponentType(), settings);
            }

            if (validPlayerRef) {
                reconcileWithRegistry(playerRef, settings);
            }

            if (validPlayerRef) {
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

    /**
     * Brings the persisted per-player settings component (the 1.x source of truth,
     * a cache since 2.0.0) in step with the global registry:
     * - pending divorce-sync marker -> clear stale married state, count the divorce;
     * - settings married but no registry record -> import the legacy 1.x marriage
     *   (unless the partner has since married someone else);
     * - registry married but settings stale -> update the settings cache;
     * - refresh cached display names and the accept-requests preference cache.
     */
    private static void reconcileWithRegistry(PlayerRef playerRef, MarriageSettingsComponent settings){
        UUID uuid = playerRef.getUuid();
        String username = playerRef.getUsername();

        MarriageManager.cacheAcceptsRequests(uuid, settings.isAcceptRequests());

        if (MarriageManager.consumePendingDivorceSync(uuid)) {
            if (settings.isMarried()) {
                String formerPartner = settings.getPartnerUsername();
                settings.setMarried(false);
                settings.clearPartnerUUID();
                settings.setPartnerUsername("");
                settings.incrementDivorces();
                playerRef.sendMessage(Message.translation("server.marriage.divorcedWhileAway").param("partnerUsername", formerPartner));
            }
            return;
        }

        MarriageRecord record = MarriageManager.getRecord(uuid);

        if (record == null) {
            if (settings.isMarried() && settings.getPartnerUUID() != null) {
                UUID partnerUuid = settings.getPartnerUUID();
                if (MarriageManager.isMarried(partnerUuid)) {
                    // The partner has since married someone else - this save data is stale.
                    Marriage.LOGGER.atInfo().log("Cleared stale marriage data for " + username + " - their former partner is married to someone else.");
                    settings.setMarried(false);
                    settings.clearPartnerUUID();
                    settings.setPartnerUsername("");
                } else if (MarriageManager.importLegacy(uuid, username, partnerUuid, settings.getPartnerUsername())) {
                    if (settings.getTimesMarried() == 0) {
                        settings.incrementTimesMarried();
                    }
                }
            }
            return;
        }

        // Registry is authoritative from here.
        UUID registryPartner = record.getPartnerUuid(uuid);
        if (!settings.isMarried()) {
            // Married while offline (API/companion mod) - adopt it.
            settings.setMarried(true);
            settings.setPartnerUUID(registryPartner);
            settings.setPartnerUsername(record.getPartnerName(uuid));
            settings.incrementTimesMarried();
        } else if (registryPartner != null && !registryPartner.equals(settings.getPartnerUUID())) {
            settings.setPartnerUUID(registryPartner);
            settings.setPartnerUsername(record.getPartnerName(uuid));
        } else {
            // Keep the partner's cached display name fresh.
            settings.setPartnerUsername(record.getPartnerName(uuid));
        }

        MarriageManager.refreshName(uuid, username);
    }
}
