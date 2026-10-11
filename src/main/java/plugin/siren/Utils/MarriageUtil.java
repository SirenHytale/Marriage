package plugin.siren.Utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.asset.type.item.config.metadata.ItemDisplayMetadata;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.API.MarriageInfo;
import plugin.siren.Marriage;
import plugin.siren.Utils.Bond.RingTier;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Shared bits of the marriage flows used by both the commands and the menu. */
public final class MarriageUtil {

    /**
     * The base band. Kept as a constant for the call sites that name it
     * directly; anything checking "is this a wedding ring" should ask
     * {@link RingTier#isRing} instead, so the Gold and Diamond bands count too.
     */
    public static final String RING_ITEM_ID = RingTier.COPPER.getItemId();

    private MarriageUtil(){}

    /**
     * Runs the celebration after MarriageManager.propose returned MARRIED: syncs
     * both settings components, consumes rings when configured, congratulates the
     * couple, and announces to the server when configured. Call on the accepter's
     * world thread with the accepter's store/ref.
     */
    public static void completeMarriage(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                                        @Nonnull PlayerRef accepter, @Nonnull PlayerRef requester){
        ComponentSync.syncMarried(accepter, requester.getUuid(), requester.getUsername());
        ComponentSync.syncMarried(requester, accepter.getUuid(), accepter.getUsername());

        // The band is read once, here, and recorded on the marriage - see RingTier
        // for why it cannot be re-checked later. The LOWER of the two rings counts:
        // a Diamond band is not worth anything unless your partner brought one too,
        // which is the only reading under which the tier belongs to the couple
        // rather than to whichever of them happened to be richer.
        RingTier ownTier = heldRingTier(store, ref);
        Ref<EntityStore> requesterEntity = requester.getReference();
        if(requesterEntity != null && requesterEntity.isValid() && requesterEntity.getStore() != store){
            // The requester is in another world: their hotbar may only be touched on
            // that world's thread (Store.getComponent asserts the thread), so the read
            // hops there and the rest of the wedding continues from it.
            // A failed read counts as "no ring" (Copper) rather than leaving the wedding
            // half-told; with onAccepterThread false the rest is safe from any thread.
            heldRingTierRemote(requester).exceptionally(failure -> null).thenAccept(theirTier ->
                    finishMarriage(store, ref, accepter, requester, lowerTierOf(ownTier, theirTier), false))
                    .exceptionally(failure -> {
                        Marriage.LOGGER.atWarning().log("Finishing the wedding of " + requester.getUsername()
                                + " and " + accepter.getUsername() + " failed: " + failure);
                        return null;
                    });
            return;
        }
        RingTier theirTier = requesterEntity == null || !requesterEntity.isValid()
                ? null : heldRingTier(store, requesterEntity);
        finishMarriage(store, ref, accepter, requester, lowerTierOf(ownTier, theirTier), true);
    }

    /**
     * The back half of {@link #completeMarriage}: records the tier, deals with the
     * rings, and tells everyone. {@code onAccepterThread} is false when the tier read
     * had to hop to the requester's world, in which case the accepter's ring work
     * hops back to theirs.
     */
    private static void finishMarriage(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                                       @Nonnull PlayerRef accepter, @Nonnull PlayerRef requester,
                                       @Nonnull RingTier tier, boolean onAccepterThread){
        MarriageConfig config = Marriage.getConfig().get();
        MarriageManager.setRingTier(accepter.getUuid(), tier);

        if(config.ifRequireRing() && config.ifConsumeRing()){
            // Each ring goes on its own owner's thread.
            if(onAccepterThread){
                consumeRingInline(store, ref);
            }else{
                consumeRingRemote(accepter);
            }
            consumeRingRemote(requester);
        }else if(config.ifEngraveRings()){
            // Kept rather than consumed - so it gets their names on it. This is the
            // whole trade-off of Consume-Ring-On-Marry: give the ring up, or wear it.
            if(onAccepterThread){
                engraveInline(store, ref, accepter.getUsername(), requester.getUsername());
            }else{
                engraveRemote(accepter, accepter.getUsername(), requester.getUsername());
            }
            engraveRemote(requester, requester.getUsername(), accepter.getUsername());
        }

        accepter.sendMessage(Message.translation("server.commands.marry.player.marry.player.msg").param("partnerUsername", requester.getUsername()).color(Color.PINK));
        requester.sendMessage(Message.translation("server.commands.marry.player.marry.player.msg").param("partnerUsername", accepter.getUsername()).color(Color.PINK));

        if(config.ifAnnounceMarriages()){
            broadcast(Message.translation("server.commands.marry.player.console.alert")
                    .param("usernameOne", requester.getUsername())
                    .param("usernameTwo", accepter.getUsername())
                    .color(Color.PINK));
        }

        Marriage.LOGGER.atInfo().log(requester.getUsername() + " and " + accepter.getUsername() + " just got Married!");
    }

    /**
     * The whole proposal flow - ring requirement, the handshake, and every reply -
     * shared by {@code /marry player} and the menu's Propose page.
     *
     * <p>One copy on purpose. The handshake has eight outcomes and a ring gate,
     * and a second implementation behind the menu button would be free to drift
     * out of step with the command until the two disagreed about who is allowed
     * to marry whom.</p>
     *
     * <p>Call on the proposer's own world thread with their own store/ref.</p>
     */
    public static void proposeTo(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                                 @Nonnull PlayerRef playerRef, @Nonnull PlayerRef targetPlayerRef){
        if(Marriage.getConfig().get().ifRequireRing()){
            InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
            ItemStack itemInHand = hotbar == null ? null : hotbar.getActiveItem();
            // Any tier of band satisfies the requirement - see RingTier.
            if(itemInHand == null || !RingTier.isRing(itemInHand.getItemId())){
                playerRef.sendMessage(Message.translation("server.commands.marry.player.missingRing"));
                return;
            }
        }

        MarriageManager.ProposeResult result = MarriageManager.propose(
                playerRef.getUuid(), playerRef.getUsername(),
                targetPlayerRef.getUuid(), targetPlayerRef.getUsername());

        switch(result){
            case SELF -> playerRef.sendMessage(Message.translation("server.commands.marry.player.self.player.msg"));
            case ALREADY_MARRIED -> playerRef.sendMessage(Message.translation("server.commands.marry.player.alreadyMarried"));
            case TARGET_MARRIED -> playerRef.sendMessage(Message.translation("server.commands.marry.player.targetMarried").param("username", targetPlayerRef.getUsername()));
            case TARGET_NOT_ACCEPTING -> playerRef.sendMessage(Message.translation("server.commands.marry.player.notAccepting").param("username", targetPlayerRef.getUsername()));
            case ALREADY_REQUESTED -> playerRef.sendMessage(Message.translation("server.commands.marry.player.alreadyRequested").param("username", targetPlayerRef.getUsername()));
            case CANCELLED -> playerRef.sendMessage(Message.translation("server.commands.marry.player.cancelled"));
            case COOLDOWN -> playerRef.sendMessage(Message.translation("server.marriage.remarry.cooldown")
                    .param("minutes", String.valueOf(Math.max(1L,
                            (MarriageManager.getRemarryCooldownRemaining(playerRef.getUuid()) + 59L) / 60L))));
            case TARGET_COOLDOWN -> playerRef.sendMessage(Message.translation("server.marriage.remarry.targetCooldown")
                    .param("username", targetPlayerRef.getUsername()));
            case REQUEST_SENT -> {
                playerRef.sendMessage(Message.translation("server.commands.marry.player.request.player.msg").param("username", targetPlayerRef.getUsername()));
                targetPlayerRef.sendMessage(Message.translation("server.commands.marry.player.receiveRequest.player.msg").param("username", playerRef.getUsername()));
            }
            case MARRIED -> completeMarriage(store, ref, playerRef, targetPlayerRef);
        }
    }

    /**
     * Sends a private note to the partner, the {@code /marry msg} flow shared with
     * the menu's compose page. Checks the feature toggle, marriage and presence
     * itself and sends all feedback.
     */
    public static void sendLoveNote(@Nonnull PlayerRef playerRef, @Nonnull String message){
        if(!Marriage.getConfig().get().ifMsgEnabled()){
            playerRef.sendMessage(Message.translation("server.commands.marry.message.disabled"));
            return;
        }
        if(message.isBlank()){
            playerRef.sendMessage(Message.translation("server.commands.marry.message.missingMsg.player.msg"));
            return;
        }

        UUID uuid = playerRef.getUuid();
        String partnerName = MarriageManager.getPartnerName(uuid);
        UUID partnerUuid = MarriageManager.getPartnerUuid(uuid);
        if(partnerUuid == null){
            playerRef.sendMessage(Message.translation("server.commands.marry.message.unmarried"));
            return;
        }

        PlayerRef partner = Universe.get().getPlayer(partnerUuid);
        if(partner == null || !partner.isValid()){
            playerRef.sendMessage(Message.translation("server.commands.marry.message.partnerOffline.player.msg"));
            return;
        }

        partner.sendMessage(Message.translation("server.marriage.note.received")
                .param("partnerUsername", playerRef.getUsername())
                .param("msg", message)
                .color(Color.PINK));
        playerRef.sendMessage(Message.translation("server.commands.marry.message.sentMsg")
                .param("partnerUsername", displayName(partnerName))
                .param("msg", message)
                .color(Color.PINK));

        MarriageManager.recordMessage(uuid);
    }

    /**
     * The {@code /marry renew} flow, shared with the Bond page's button. Checks
     * the toggle, marriage and timing itself and sends all feedback.
     *
     * <p>Reads the band in hand and passes it down so a couple who have crafted a
     * better one since the wedding are upgraded — the loop that makes an
     * anniversary's Gold Band worth something.</p>
     */
    public static void renewVows(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                                 @Nonnull PlayerRef playerRef){
        MarriageConfig config = Marriage.getConfig().get();
        if(!config.ifRenewEnabled()){
            playerRef.sendMessage(Message.translation("server.marriage.renew.disabled"));
            return;
        }

        UUID uuid = playerRef.getUuid();
        MarriageRecord record = MarriageManager.getRecord(uuid);
        if(record == null){
            playerRef.sendMessage(Message.translation("server.marriage.renew.unmarried"));
            return;
        }

        UUID partnerUuid = record.getPartnerUuid(uuid);
        PlayerRef partner = partnerUuid == null ? null : Universe.get().getPlayer(partnerUuid);
        if(partner == null || !partner.isValid()){
            playerRef.sendMessage(Message.translation("server.marriage.renew.partnerOffline")
                    .param("partnerUsername", displayName(record.getPartnerName(uuid))));
            return;
        }

        MarriageManager.RenewResult result = MarriageManager.renewVows(uuid, heldRingTier(store, ref));
        switch(result){
            case NOT_MARRIED -> playerRef.sendMessage(Message.translation("server.marriage.renew.unmarried"));
            case TOO_SOON -> playerRef.sendMessage(Message.translation("server.marriage.renew.tooSoon")
                    .param("days", String.valueOf(Math.round(config.getRenewMinDays()))));
            case OFFERED -> {
                playerRef.sendMessage(Message.translation("server.marriage.renew.offered")
                        .param("partnerUsername", partner.getUsername())
                        .param("seconds", String.valueOf(config.getRenewOfferSeconds())));
                partner.sendMessage(Message.translation("server.marriage.renew.invited")
                        .param("partnerUsername", playerRef.getUsername())
                        .color(Color.PINK));
            }
            case RENEWED -> {
                Message renewed = Message.translation("server.marriage.renew.done").color(Color.PINK);
                playerRef.sendMessage(renewed);
                partner.sendMessage(renewed);
                if(Marriage.getConfig().get().ifAnnounceMarriages()){
                    broadcast(Message.translation("server.marriage.renew.broadcast")
                            .param("usernameOne", playerRef.getUsername())
                            .param("usernameTwo", partner.getUsername())
                            .color(Color.PINK));
                }
                Marriage.LOGGER.atInfo().log(playerRef.getUsername() + " and " + partner.getUsername()
                        + " renewed their vows.");
            }
        }
    }

    /**
     * The {@code /marry divorce} flow, shared with the menu button: files a
     * divorce, completes one the partner already filed, or reports the wait.
     *
     * <p>Pass the snapshot-taking to the caller's own hands is not needed here -
     * this takes it itself, because the record is gone the moment a divorce
     * actually completes.</p>
     */
    public static void divorceFlow(@Nonnull PlayerRef playerRef){
        UUID uuid = playerRef.getUuid();
        MarriageInfo snapshot = MarriageInfo.of(MarriageManager.getRecord(uuid));
        MarriageRecord record = MarriageManager.getRecord(uuid);
        boolean partnerFiled = record != null && record.isDivorcePending() && !record.isDivorceFiledBy(uuid);

        MarriageManager.DivorceResult result = MarriageManager.fileDivorce(uuid);
        switch(result){
            case NOT_MARRIED -> playerRef.sendMessage(Message.translation("server.commands.marry.divorce.unmarried"));
            case CONFIRM_NEEDED -> playerRef.sendMessage(Message.translation("server.commands.marry.divorce.confirmation")
                    .param("seconds", String.valueOf(Marriage.getConfig().get().getDivorceConfirmSeconds())));
            case CANCELLED -> playerRef.sendMessage(Message.translation("server.commands.marry.divorce.cancelled"));
            case ALREADY_FILED -> {
                long seconds = record == null ? 0L : MarriageManager.getDivorceRemainingSeconds(record);
                playerRef.sendMessage(Message.translation("server.marriage.divorce.alreadyFiled")
                        .param("hours", String.valueOf(Math.max(1L, (seconds + 3599L) / 3600L))));
            }
            case FILED -> announceFiling(playerRef, snapshot);
            case DIVORCED -> {
                if(snapshot != null){
                    // A divorce the partner had already filed: this was consent,
                    // not a fresh act, and the message says so.
                    finishDivorce(playerRef, snapshot);
                    if(partnerFiled){
                        playerRef.sendMessage(Message.translation("server.marriage.divorce.agreed"));
                    }
                }
            }
        }
    }

    /** Tells both sides a divorce has been filed and when it matures. */
    private static void announceFiling(@Nonnull PlayerRef playerRef, @Nullable MarriageInfo marriage){
        MarriageRecord record = MarriageManager.getRecord(playerRef.getUuid());
        long seconds = record == null ? 0L : MarriageManager.getDivorceRemainingSeconds(record);
        long hours = Math.max(1L, (seconds + 3599L) / 3600L);

        playerRef.sendMessage(Message.translation("server.marriage.divorce.filed")
                .param("hours", String.valueOf(hours)));

        if(marriage == null){
            return;
        }
        UUID partnerUuid = marriage.getPartnerUuid(playerRef.getUuid());
        PlayerRef partner = partnerUuid == null ? null : Universe.get().getPlayer(partnerUuid);
        if(partner != null && partner.isValid()){
            partner.sendMessage(Message.translation("server.marriage.divorce.filedAgainstYou")
                    .param("partnerUsername", playerRef.getUsername())
                    .param("hours", String.valueOf(hours)));
        }
    }

    public static void broadcast(@Nonnull Message message){
        for(PlayerRef onlinePlayer : Universe.get().getPlayers()){
            if(onlinePlayer != null && onlinePlayer.isValid()){
                onlinePlayer.sendMessage(message);
            }
        }
    }

    /**
     * Runs the aftermath of MarriageManager.divorce returning DIVORCED: settings
     * sync for both sides (offline partner reconciles on next join), messages,
     * and the config-gated announcement. Pass the snapshot taken BEFORE divorcing.
     */
    public static void finishDivorce(@Nonnull PlayerRef initiator, @Nonnull MarriageInfo endedMarriage){
        String partnerName = endedMarriage.getPartnerName(initiator.getUuid());

        ComponentSync.syncDivorced(initiator);
        initiator.sendMessage(Message.translation("server.commands.marry.divorce.player.msg").param("partnerUsername", partnerName));

        UUID partnerUuid = endedMarriage.getPartnerUuid(initiator.getUuid());
        PlayerRef partnerPlayerRef = partnerUuid == null ? null : Universe.get().getPlayer(partnerUuid);
        if(partnerPlayerRef != null && partnerPlayerRef.isValid()){
            ComponentSync.syncDivorced(partnerPlayerRef);
            partnerPlayerRef.sendMessage(Message.translation("server.commands.marry.divorce.player.msg").param("partnerUsername", initiator.getUsername()));
        }

        if(Marriage.getConfig().get().ifAnnounceDivorces()){
            broadcast(Message.translation("server.commands.marry.divorce.console.alert")
                    .param("usernameOne", initiator.getUsername())
                    .param("usernameTwo", partnerName));
        }
    }

    /**
     * The /marry tp flow, shared by the command and the menu action. Checks the
     * feature toggle, marriage, cooldown, and partner presence itself and sends
     * all feedback messages.
     */
    public static void teleportToPartner(@Nonnull PlayerRef playerRef){
        if(!Marriage.getConfig().get().ifTpEnabled()){
            playerRef.sendMessage(Message.translation("server.commands.marry.tp.disabled"));
            return;
        }
        UUID partnerUuid = MarriageManager.getPartnerUuid(playerRef.getUuid());
        if(partnerUuid == null){
            playerRef.sendMessage(Message.translation("server.commands.marry.tp.unmarried"));
            return;
        }
        long cooldownRemaining = MarriageManager.getTpCooldownRemaining(playerRef.getUuid());
        if(cooldownRemaining > 0L){
            playerRef.sendMessage(Message.translation("server.commands.marry.tp.cooldown").param("seconds", String.valueOf(cooldownRemaining)));
            return;
        }
        PlayerRef partnerPlayerRef = Universe.get().getPlayer(partnerUuid);
        if(partnerPlayerRef == null || !partnerPlayerRef.isValid()){
            playerRef.sendMessage(Message.translation("server.commands.marry.tp.partnerOffline.player.msg"));
            return;
        }

        String consoleRunCommand = "tp " + playerRef.getUsername() + " " + partnerPlayerRef.getUsername();
        CommandManager.get().handleCommand(ConsoleSender.INSTANCE, consoleRunCommand);

        MarriageManager.markTpUsed(playerRef.getUuid());
        MarriageManager.recordTeleport(playerRef.getUuid());

        playerRef.sendMessage(Message.translation("server.commands.marry.tp.player.msg"));
        partnerPlayerRef.sendMessage(Message.translation("server.commands.marry.tp.partner.msg"));
    }

    /** The tier of the band this player is holding, or null if they hold no ring. */
    @Nullable
    public static RingTier heldRingTier(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref){
        InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        ItemStack held = hotbar == null ? null : hotbar.getActiveItem();
        return held == null ? null : RingTier.fromItemId(held.getItemId());
    }

    /**
     * The same, for a player on another world thread: the read runs on THEIR
     * world's thread and the future completes there.
     *
     * <p>Completes with null when they are unreachable, which the caller reads as
     * "no ring" - the wedding then records Copper. That is the safe direction to
     * fail: a couple never gets a better band than they can be proven to hold.</p>
     */
    @Nonnull
    private static CompletableFuture<RingTier> heldRingTierRemote(@Nonnull PlayerRef target){
        Ref<EntityStore> targetRef = target.getReference();
        if(targetRef == null || !targetRef.isValid()){
            return CompletableFuture.completedFuture(null);
        }
        Store<EntityStore> targetStore = targetRef.getStore();
        World targetWorld = targetStore.getExternalData().getWorld();
        return CompletableFuture.supplyAsync(() ->
                // Skip if they changed worlds mid-hop - see onTargetThread.
                targetRef.isValid() && targetRef.getStore() == targetStore
                        ? heldRingTier(targetStore, targetRef) : null, targetWorld);
    }

    /** The weaker of two bands; Copper when either side brought nothing. */
    @Nonnull
    private static RingTier lowerTierOf(@Nullable RingTier one, @Nullable RingTier two){
        if(one == null || two == null){
            return RingTier.COPPER;
        }
        return one.ordinal() <= two.ordinal() ? one : two;
    }

    private static void consumeRingInline(Store<EntityStore> store, Ref<EntityStore> ref){
        InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        if(hotbar == null){
            return;
        }
        // Removes whichever band they are actually holding, not always the base
        // one - otherwise marrying with a Gold ring consumed a Copper ring they
        // may not even own, and left the Gold one on their finger.
        ItemStack held = hotbar.getActiveItem();
        RingTier tier = held == null ? null : RingTier.fromItemId(held.getItemId());
        if(tier != null){
            hotbar.getInventory().removeItemStackFromSlot(hotbar.getActiveSlot(), held, 1);
        }
    }

    private static void consumeRingRemote(PlayerRef target){
        onTargetThread(target, MarriageUtil::consumeRingInline);
    }

    /**
     * Replaces the held band with an otherwise identical one whose display name
     * carries both partners' names.
     *
     * <p>{@link ItemStack#withMetadata} returns a copy rather than mutating, so
     * the slot is overwritten with the engraved stack. Only the display name is
     * touched - item id, durability and every other metadata key survive, so an
     * engraved ring is still a ring for every other purpose.</p>
     */
    private static void engraveInline(Store<EntityStore> store, Ref<EntityStore> ref, String selfName, String partnerName){
        InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        if(hotbar == null){
            return;
        }
        ItemStack held = hotbar.getActiveItem();
        if(held == null || !RingTier.isRing(held.getItemId())){
            return;
        }

        ItemDisplayMetadata engraving = new ItemDisplayMetadata(
                Message.translation("server.marriage.ring.engraved.name")
                        .param("usernameOne", selfName)
                        .param("usernameTwo", partnerName),
                Message.translation("server.marriage.ring.engraved.description")
                        .param("partnerUsername", partnerName));

        hotbar.getInventory().setItemStackForSlot(hotbar.getActiveSlot(),
                held.withMetadata(ItemDisplayMetadata.KEYED_CODEC, engraving));
    }

    private static void engraveRemote(PlayerRef target, String selfName, String partnerName){
        onTargetThread(target, (store, ref) -> engraveInline(store, ref, selfName, partnerName));
    }

    /** Runs a bit of inventory work on the target's own world thread, or skips it. */
    private static void onTargetThread(PlayerRef target, java.util.function.BiConsumer<Store<EntityStore>, Ref<EntityStore>> work){
        Ref<EntityStore> targetRef = target.getReference();
        if(targetRef == null || !targetRef.isValid()){
            return;
        }
        Store<EntityStore> targetStore = targetRef.getStore();
        World targetWorld = targetStore.getExternalData().getWorld();
        CompletableFuture.runAsync(() -> {
            // Skip if they changed worlds mid-hop - their components now belong
            // to another thread, and a missed ring beats a cross-thread touch.
            if(targetRef.isValid() && targetRef.getStore() == targetStore){
                work.accept(targetStore, targetRef);
            }
        }, targetWorld);
    }

    /**
     * A cached display name, or a stand-in when there isn't one.
     *
     * <p>Cached names can genuinely be empty: a marriage imported from 1.x save
     * data carries whatever {@code PartnerUsername} that save held, which for an
     * old or hand-edited file may be nothing at all, and a record written for a
     * player who has never joined since has never had its name refreshed. Every
     * such name reaches a label eventually, and a blank one renders as a hole in
     * the sentence — "You are married to  (offline)".</p>
     *
     * <p>Returns a {@link Message} rather than a String so the stand-in is
     * translatable; {@code param} accepts either.</p>
     */
    @Nonnull
    public static Message displayName(@Nullable String cached){
        return cached == null || cached.isBlank()
                ? Message.translation("server.marriage.unknownPartner")
                : Message.raw(cached);
    }

    /** The same stand-in as a plain string, for the few places that concatenate. */
    @Nonnull
    public static String displayNameRaw(@Nullable String cached){
        return cached == null || cached.isBlank() ? "?" : cached;
    }

    /** "3d 4h" style duration for married-since displays. */
    public static String formatDuration(long millis){
        long totalMinutes = Math.max(0L, millis) / 60000L;
        long days = totalMinutes / (60L * 24L);
        long hours = (totalMinutes / 60L) % 24L;
        long minutes = totalMinutes % 60L;
        if(days > 0L){
            return days + "d " + hours + "h";
        }
        if(hours > 0L){
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }
}
