package plugin.siren.Utils.UI;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Compatibility.CultivationBridge;
import plugin.siren.Systems.MarriagePresenceSystem;
import plugin.siren.API.CustomUI.MarriageUICategory;
import plugin.siren.API.CustomUI.MarriageUIContext;
import plugin.siren.API.CustomUI.MarriageUILayout;
import plugin.siren.API.CustomUI.MarriageUIOption;
import plugin.siren.API.CustomUI.MarriageUIRegistry;
import plugin.siren.API.CustomUI.MarriageUISection;
import plugin.siren.API.MarriageAPI;
import plugin.siren.API.MarriageInfo;
import plugin.siren.Marriage;
import plugin.siren.Utils.ComponentSync;
import plugin.siren.Systems.MarriageSettingsComponent;
import plugin.siren.Utils.Bond.BondManager;
import plugin.siren.Utils.Bond.BondPerk;
import plugin.siren.Utils.Bond.BondTitle;
import plugin.siren.Utils.Bond.RingTier;
import plugin.siren.Utils.Bond.Vow;
import plugin.siren.Utils.EmoteManager;
import plugin.siren.Utils.GiftManager;
import plugin.siren.Utils.HomeManager;
import plugin.siren.Utils.LoveLetter;
import plugin.siren.Utils.HopeChestManager;
import plugin.siren.Utils.MarriageConfig;
import plugin.siren.Utils.MarriageUtil;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;
import plugin.siren.Utils.Registry.PlayerTally;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Registers the built-in Marriage menu categories - Overview, Our Bond, Married
 * Couples, Records, Settings and Admin - through the same public
 * {@link MarriageUIRegistry} door a third-party mod would use.
 *
 * <p>Called once from {@link Marriage#setup()}.</p>
 */
public final class MarriageUIDefaults {
    private static final String LANG = "server.customUI.marriage.marriageui.";
    private static final String CATEGORY_BASE = "Pages/SirensMarriage/Categories/";
    private static final String OVERVIEW_DOC = CATEGORY_BASE + "OverviewContent.ui";
    private static final String COUPLES_DOC = CATEGORY_BASE + "CouplesContent.ui";

    /**
     * Whether a player's next Divorce click will actually divorce, per
     * {@link MarriageManager#divorce}'s CONFIRM_NEEDED/DIVORCED handshake. Purely a
     * UI-layer mirror (drives the button's blurb text) - the real confirmation
     * window is owned and timed by MarriageManager; this only ever affects what the
     * page says, never what a click does. Never cleaned up on logout, same as
     * MarriageManager's own transient cooldown maps - a stray boolean per
     * once-online player is negligible.
     */
    private static final Map<UUID, Boolean> DIVORCE_ARMED = new ConcurrentHashMap<>();

    private MarriageUIDefaults() {
    }

    public static void register() {
        registerOverview();
        registerPropose();
        registerBond();
        registerCouples();
        registerDualCultivation();
        registerRecords();
        registerSettings();
        registerAdmin();
    }

    // --- Overview --------------------------------------------------------------

    private static void registerOverview() {
        MarriageUIRegistry.registerCategory(MarriageUICategory.builder(MarriageUIRegistry.CATEGORY_OVERVIEW)
                .name(LANG + "category.overview")
                .hint(LANG + "category.overview.hint")
                .sortOrder(100)
                // The three action buttons dispatch through the ordinary options
                // mechanism (same optionCategory/option event keys as a ROWS category)
                // even though this category's layout is DOCUMENT - decorate() below
                // renders them by hand into "#ActionList" instead of the generic
                // "#RowList", reusing MarriageUIPage's row-rendering helpers.
                .options(MarriageUIDefaults::overviewActions)
                .document(OVERVIEW_DOC, MarriageUIDefaults::renderOverview)
                .build());
    }

    @Nonnull
    private static List<MarriageUIOption> overviewActions(@Nonnull MarriageUIContext context) {
        if (!MarriageManager.isMarried(context.getUuid())) {
            return List.of();
        }

        List<MarriageUIOption> options = new ArrayList<>();

        options.add(MarriageUIOption.builder("marriage:teleport")
                .name(LANG + "overview.action.teleport.name")
                .detail(LANG + "overview.action.teleport.detail")
                .enabled(ctx -> Marriage.getConfig().get().ifTpEnabled())
                .disabledReason(LANG + "overview.action.teleport.disabled")
                .onSelect(ctx -> MarriageUtil.teleportToPartner(ctx.getPlayerRef()))
                .build());

        options.add(MarriageUIOption.builder("marriage:gift")
                .name(LANG + "overview.action.gift.name")
                .detail(LANG + "overview.action.gift.detail")
                .enabled(ctx -> Marriage.getConfig().get().ifGiftEnabled())
                .disabledReason(LANG + "overview.action.gift.disabled")
                .onSelect(ctx -> GiftManager.gift(ctx.getStore(), ctx.getRef(), ctx.getPlayerRef()))
                .build());

        options.add(MarriageUIOption.builder("marriage:note")
                .name(LANG + "overview.action.note.name")
                .detail(LANG + "overview.action.note.detail")
                .enabled(ctx -> {
                    if (!Marriage.getConfig().get().ifMsgEnabled()) {
                        return false;
                    }
                    UUID partnerUuid = MarriageManager.getPartnerUuid(ctx.getUuid());
                    return partnerUuid != null && Universe.get().getPlayer(partnerUuid) != null;
                })
                .disabledReason(ctx -> Marriage.getConfig().get().ifMsgEnabled()
                        ? Message.translation(LANG + "overview.action.note.offline")
                        : Message.translation(LANG + "overview.action.note.disabled"))
                .onSelect(MarriageUIDefaults::openNoteComposer)
                .build());

        // One row per emote, so the menu carries them the same way the commands do.
        for (EmoteManager.Emote emote : EmoteManager.Emote.values()) {
            options.add(MarriageUIOption.builder("marriage:emote_" + emote.getId())
                    .name(emote.getNameKey())
                    .detail(LANG + "overview.action.emote.detail")
                    .visible(ctx -> Marriage.getConfig().get().ifEmotesEnabled())
                    .onSelect(ctx -> EmoteManager.perform(ctx.getStore(), ctx.getRef(), ctx.getPlayerRef(), emote))
                    .build());
        }

        options.add(MarriageUIOption.builder("marriage:letter")
                .name(LANG + "overview.action.letter.name")
                .detail(LANG + "overview.action.letter.detail")
                .visible(ctx -> Marriage.getConfig().get().ifLoveLettersEnabled())
                // Disabled rather than hidden when they are not holding one: the
                // feature is real and craftable, and a row that vanishes teaches
                // nobody that Love Letters exist.
                .enabled(ctx -> LoveLetter.isLetter(heldItem(ctx)))
                .disabledReason(LANG + "overview.action.letter.notHolding")
                .onSelect(MarriageUIDefaults::openLetter)
                .build());

        options.add(MarriageUIOption.builder("marriage:home")
                .name(LANG + "overview.action.home.name")
                .detail(ctx -> {
                    MarriageRecord record = MarriageManager.getRecord(ctx.getUuid());
                    return Message.translation(record != null && record.hasHome()
                            ? LANG + "overview.action.home.detail"
                            : LANG + "overview.action.home.unset");
                })
                // Hidden rather than disabled when the bond has not reached it: a
                // perk the couple has never had is not a button that stopped working.
                .visible(ctx -> BondManager.hasPerk(MarriageManager.getRecord(ctx.getUuid()), BondPerk.SHARED_HOME))
                .enabled(ctx -> {
                    MarriageRecord record = MarriageManager.getRecord(ctx.getUuid());
                    return record != null && record.hasHome();
                })
                .disabledReason(LANG + "overview.action.home.unset")
                .onSelect(ctx -> HomeManager.travelHome(ctx.getStore(), ctx.getRef(), ctx.getPlayerRef()))
                .build());

        options.add(MarriageUIOption.builder("marriage:sethome")
                .name(LANG + "overview.action.setHome.name")
                // The name stays put and the blurb carries the difference: the
                // option builder resolves a detail per player but a name once, and
                // widening that is a public-API change this does not need.
                .detail(ctx -> {
                    MarriageRecord record = MarriageManager.getRecord(ctx.getUuid());
                    return Message.translation(record != null && record.hasHome()
                            ? LANG + "overview.action.setHome.move"
                            : LANG + "overview.action.setHome.detail");
                })
                .visible(ctx -> BondManager.hasPerk(MarriageManager.getRecord(ctx.getUuid()), BondPerk.SHARED_HOME))
                .onSelect(ctx -> HomeManager.setHome(ctx.getStore(), ctx.getRef(), ctx.getPlayerRef(),
                        ctx.getStore().getExternalData().getWorld()))
                .build());

        options.add(MarriageUIOption.builder("marriage:clearhome")
                .name(LANG + "overview.action.clearHome.name")
                .detail(LANG + "overview.action.clearHome.detail")
                // Only when there is one to clear - otherwise it is a button whose
                // only outcome is being told there was nothing to do.
                .visible(ctx -> {
                    MarriageRecord record = MarriageManager.getRecord(ctx.getUuid());
                    return record != null && record.hasHome()
                            && BondManager.hasPerk(record, BondPerk.SHARED_HOME);
                })
                .onSelect(ctx -> HomeManager.clearHome(ctx.getPlayerRef()))
                .build());

        options.add(MarriageUIOption.builder("marriage:chest")
                .name(LANG + "overview.action.chest.name")
                .detail(ctx -> Message.translation(LANG + "overview.action.chest.detail")
                        .param("used", String.valueOf(HopeChestManager.usedSlots(MarriageManager.getRecord(ctx.getUuid()))))
                        .param("slots", String.valueOf(Marriage.getConfig().get().getHopeChestSlots())))
                .visible(ctx -> BondManager.hasPerk(MarriageManager.getRecord(ctx.getUuid()), BondPerk.HOPE_CHEST))
                .onSelect(ctx -> HopeChestManager.open(ctx.getStore(), ctx.getRef(), ctx.getPlayerRef()))
                .build());

        // Answering a divorce somebody else filed: agree, or object and make them
        // serve the full period. Only shown to the partner who did NOT file.
        options.add(MarriageUIOption.builder("marriage:divorce_accept")
                .name(LANG + "overview.action.divorceAccept.name")
                .detail(LANG + "overview.action.divorceAccept.detail")
                .visible(ctx -> {
                    MarriageRecord record = MarriageManager.getRecord(ctx.getUuid());
                    return record != null && record.isDivorcePending() && !record.isDivorceFiledBy(ctx.getUuid());
                })
                .onSelect(ctx -> MarriageUtil.divorceFlow(ctx.getPlayerRef()))
                .build());

        options.add(MarriageUIOption.builder("marriage:divorce_contest")
                .name(LANG + "overview.action.divorceContest.name")
                .detail(LANG + "overview.action.divorceContest.detail")
                .visible(ctx -> {
                    MarriageRecord record = MarriageManager.getRecord(ctx.getUuid());
                    return record != null && record.isDivorcePending()
                            && !record.isDivorceFiledBy(ctx.getUuid()) && !record.isDivorceContested();
                })
                .onSelect(MarriageUIDefaults::doContestDivorce)
                .build());

        options.add(MarriageUIOption.builder("marriage:divorce_withdraw")
                .name(LANG + "overview.action.divorceWithdraw.name")
                .detail(LANG + "overview.action.divorceWithdraw.detail")
                .visible(ctx -> {
                    MarriageRecord record = MarriageManager.getRecord(ctx.getUuid());
                    return record != null && record.isDivorcePending() && record.isDivorceFiledBy(ctx.getUuid());
                })
                .onSelect(MarriageUIDefaults::doWithdrawDivorce)
                .build());

        options.add(MarriageUIOption.builder("marriage:divorce")
                .name(LANG + "overview.action.divorce.name")
                // Hidden once a filing is in flight - the three rows above are what
                // you do then, and a fourth Divorce button would be a no-op.
                .visible(ctx -> {
                    MarriageRecord record = MarriageManager.getRecord(ctx.getUuid());
                    return record == null || !record.isDivorcePending();
                })
                .detail(ctx -> isDivorceArmed(ctx.getUuid())
                        ? Message.translation(LANG + "overview.action.divorce.confirm")
                                .param("seconds", String.valueOf(Marriage.getConfig().get().getDivorceConfirmSeconds()))
                        : Message.translation(LANG + "overview.action.divorce.detail"))
                .onSelect(MarriageUIDefaults::doDivorce)
                .build());

        return options;
    }

    private static void doContestDivorce(@Nonnull MarriageUIContext context) {
        if (!MarriageManager.contestDivorce(context.getUuid())) {
            return;
        }
        MarriageRecord record = MarriageManager.getRecord(context.getUuid());
        context.getPlayerRef().sendMessage(Message.translation("server.marriage.divorce.contested"));
        if (record != null) {
            UUID filerUuid = record.getPartnerUuid(context.getUuid());
            PlayerRef filer = filerUuid == null ? null : Universe.get().getPlayer(filerUuid);
            if (filer != null && filer.isValid()) {
                filer.sendMessage(Message.translation("server.marriage.divorce.contestedByPartner")
                        .param("partnerUsername", context.getPlayerRef().getUsername()));
            }
        }
    }

    private static void doWithdrawDivorce(@Nonnull MarriageUIContext context) {
        if (!MarriageManager.withdrawDivorce(context.getUuid())) {
            return;
        }
        MarriageRecord record = MarriageManager.getRecord(context.getUuid());
        context.getPlayerRef().sendMessage(Message.translation("server.marriage.divorce.withdrawn"));
        if (record != null) {
            UUID partnerUuid = record.getPartnerUuid(context.getUuid());
            PlayerRef partner = partnerUuid == null ? null : Universe.get().getPlayer(partnerUuid);
            if (partner != null && partner.isValid()) {
                partner.sendMessage(Message.translation("server.marriage.divorce.withdrawnByPartner")
                        .param("partnerUsername", context.getPlayerRef().getUsername()));
            }
        }
    }

    private static void renderOverview(
            @Nonnull MarriageUIContext context,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        MarriageInfo marriage = MarriageInfo.of(MarriageManager.getRecord(context.getUuid()));
        boolean married = marriage != null;

        commandBuilder.set("#MarriedBlock.Visible", married);
        commandBuilder.set("#UnmarriedBlock.Visible", !married);

        if (married) {
            renderInfoRows(context, marriage, commandBuilder);
            renderActionRows(context, commandBuilder, eventBuilder);
        } else {
            renderRequestRows(context, commandBuilder, eventBuilder);
        }
    }

    private static void renderInfoRows(
            @Nonnull MarriageUIContext context,
            @Nonnull MarriageInfo marriage,
            @Nonnull UICommandBuilder commandBuilder
    ) {
        commandBuilder.clear("#InfoList");

        UUID uuid = context.getUuid();
        UUID partnerUuid = marriage.getPartnerUuid(uuid);
        boolean partnerOnline = partnerUuid != null && Universe.get().getPlayer(partnerUuid) != null;

        int index = appendInfoRowMessage(commandBuilder, 0, LANG + "overview.info.partnerLabel",
                Message.translation(partnerOnline ? LANG + "overview.info.partnerOnline" : LANG + "overview.info.partnerOffline")
                        .param("partnerUsername", MarriageUtil.displayName(marriage.getPartnerName(uuid))));
        index = appendInfoRow(commandBuilder, index, LANG + "overview.info.marriedForLabel",
                MarriageUtil.formatDuration(marriage.getMarriedForMillis()));

        MarriageRecord record = MarriageManager.getRecord(uuid);
        if (record != null && Marriage.getConfig().get().ifBondEnabled()) {
            index = appendInfoRowMessage(commandBuilder, index, LANG + "overview.info.bondLabel",
                    BondManager.isMaxLevel(record)
                            ? Message.translation(LANG + "overview.info.bondMax")
                                    .param("level", String.valueOf(record.getBondLevel()))
                            : Message.translation(LANG + "overview.info.bondValue")
                                    .param("level", String.valueOf(record.getBondLevel()))
                                    .param("xp", String.valueOf(Math.round(BondManager.xpIntoLevel(record))))
                                    .param("needed", String.valueOf(Math.round(BondManager.xpForNextLevel(record)))));

            index = appendInfoRowMessage(commandBuilder, index, LANG + "overview.info.titleLabel",
                    Message.translation(BondTitle.of(record).getNameKey()));
        }

        // Situational rows last, so the fixed ones never move under the reader.
        if (record != null && BondManager.isHoneymoon(record)) {
            index = appendInfoRowMessage(commandBuilder, index, LANG + "overview.info.honeymoonLabel",
                    Message.translation(LANG + "overview.info.honeymoonValue")
                            .param("multiplier", formatMultiplier((float) Marriage.getConfig().get().getHoneymoonXpMultiplier()))
                            .param("minutes", String.valueOf(BondManager.honeymoonMinutesLeft(record))));
        }

        if (record != null && record.isDivorcePending()) {
            long seconds = MarriageManager.getDivorceRemainingSeconds(record);
            index = appendInfoRowMessage(commandBuilder, index, LANG + "overview.info.divorceLabel",
                    Message.translation(record.isDivorceFiledBy(uuid)
                            ? LANG + "overview.info.divorceYours"
                            : LANG + "overview.info.divorceTheirs")
                            .param("hours", String.valueOf(Math.max(1L, (seconds + 3599L) / 3600L))));
        }
        index = appendInfoRow(commandBuilder, index, LANG + "overview.info.giftsGivenLabel",
                String.valueOf(marriage.getGiftsFrom(uuid)));
        index = appendInfoRow(commandBuilder, index, LANG + "overview.info.giftsReceivedLabel",
                String.valueOf(partnerUuid == null ? 0 : marriage.getGiftsFrom(partnerUuid)));
        index = appendInfoRow(commandBuilder, index, LANG + "overview.info.teleportsLabel",
                String.valueOf(marriage.getTeleportsUsed()));
        appendInfoRow(commandBuilder, index, LANG + "overview.info.messagesLabel",
                String.valueOf(marriage.getMessagesSent()));
    }

    private static int appendInfoRow(@Nonnull UICommandBuilder commandBuilder, int index, @Nonnull String labelKey, @Nonnull String plainValue) {
        commandBuilder.append("#InfoList", MarriageUIPage.INFO_ROW_DOC);
        String row = "#InfoList[" + index + "]";
        commandBuilder.set(row + " #Label.TextSpans", Message.translation(labelKey));
        commandBuilder.set(row + " #Value.Text", plainValue);
        return index + 1;
    }

    private static int appendInfoRowMessage(@Nonnull UICommandBuilder commandBuilder, int index, @Nonnull String labelKey, @Nonnull Message value) {
        commandBuilder.append("#InfoList", MarriageUIPage.INFO_ROW_DOC);
        String row = "#InfoList[" + index + "]";
        commandBuilder.set(row + " #Label.TextSpans", Message.translation(labelKey));
        commandBuilder.set(row + " #Value.TextSpans", value);
        return index + 1;
    }

    private static void renderActionRows(
            @Nonnull MarriageUIContext context,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        commandBuilder.clear("#ActionList");
        MarriageUICategory category = MarriageUIRegistry.getCategory(MarriageUIRegistry.CATEGORY_OVERVIEW);
        if (category == null) {
            return;
        }
        List<MarriageUISection> sections = category.getSections(context);
        MarriageUIPage.renderRows(context, category, sections, "#ActionList", commandBuilder, eventBuilder);
    }

    private static void renderRequestRows(
            @Nonnull MarriageUIContext context,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        commandBuilder.set("#UnmarriedHint.TextSpans", Message.translation(LANG + "overview.unmarried.tip"));

        Map<UUID, MarriageManager.PendingRequest> requests = MarriageManager.getPendingRequests(context.getUuid());
        commandBuilder.clear("#RequestList");
        commandBuilder.set("#NoRequestsLabel.Visible", requests.isEmpty());

        long now = System.currentTimeMillis();
        int index = 0;
        for (Map.Entry<UUID, MarriageManager.PendingRequest> entry : requests.entrySet()) {
            UUID requesterUuid = entry.getKey();
            MarriageManager.PendingRequest pending = entry.getValue();
            long secondsRemaining = Math.max(0L, (pending.expiryMillis() - now + 999L) / 1000L);

            commandBuilder.append("#RequestList", MarriageUIPage.REQUEST_ROW_DOC);
            String row = "#RequestList[" + index + "]";

            commandBuilder.set(row + " #Name.Text", pending.requesterName());
            commandBuilder.set(row + " #Expiry.TextSpans",
                    Message.translation(LANG + "overview.request.expiresIn").param("seconds", String.valueOf(secondsRemaining)));
            commandBuilder.set(row + " #Accept.TextSpans", Message.translation(LANG + "overview.request.accept"));
            commandBuilder.set(row + " #Decline.TextSpans", Message.translation(LANG + "overview.request.decline"));

            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, row + " #Accept", requestEvent("accept", requesterUuid));
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, row + " #Decline", requestEvent("decline", requesterUuid));

            index++;
        }
    }

    @Nonnull
    private static EventData requestEvent(@Nonnull String action, @Nonnull UUID requesterUuid) {
        return new EventData()
                .append(MarriageUIPage.MarriageUIEventData.KEY_REQUEST_ACTION, action)
                .append(MarriageUIPage.MarriageUIEventData.KEY_REQUEST_UUID, requesterUuid.toString());
    }

    /** Called by {@link MarriageUIPage#handleDataEvent} for a RequestRow Accept/Decline click. */
    static void handleRequestAction(@Nonnull MarriageUIContext context, @Nonnull String action, @Nonnull String requestUuidStr) {
        UUID requesterUuid;
        try {
            requesterUuid = UUID.fromString(requestUuidStr);
        } catch (IllegalArgumentException invalidUuid) {
            return;
        }
        UUID targetUuid = context.getUuid();

        if ("decline".equals(action)) {
            MarriageManager.declineRequest(targetUuid, requesterUuid);
            return;
        }
        if (!"accept".equals(action)) {
            return;
        }

        MarriageManager.PendingRequest pendingRequest = MarriageManager.getPendingRequests(targetUuid).get(requesterUuid);
        if (pendingRequest == null) {
            // Expired, declined, or already accepted by the time this click arrived -
            // the row is simply gone on the next render.
            return;
        }
        String requesterName = pendingRequest.requesterName();

        MarriageConfig config = Marriage.getConfig().get();
        if (config.ifRequireRing()) {
            InventoryComponent.Hotbar hotbar = context.getComponent(InventoryComponent.Hotbar.getComponentType());
            ItemStack held = hotbar == null ? null : hotbar.getActiveItem();
            // Any tier of band satisfies the requirement - see RingTier.
            if (held == null || !RingTier.isRing(held.getItemId())) {
                context.getPlayerRef().sendMessage(Message.translation("server.commands.marry.player.missingRing"));
                return;
            }
        }

        MarriageManager.ProposeResult result = MarriageManager.acceptRequest(targetUuid, context.getPlayerRef().getUsername(), requesterUuid);

        switch (result) {
            case ALREADY_MARRIED ->
                    context.getPlayerRef().sendMessage(Message.translation("server.commands.marry.player.alreadyMarried"));
            case TARGET_MARRIED ->
                    context.getPlayerRef().sendMessage(Message.translation("server.commands.marry.player.targetMarried")
                            .param("username", requesterName));
            case CANCELLED ->
                    context.getPlayerRef().sendMessage(Message.translation("server.commands.marry.player.cancelled"));
            case COOLDOWN -> context.getPlayerRef().sendMessage(
                    Message.translation("server.marriage.remarry.cooldown")
                            .param("minutes", String.valueOf(Math.max(1L,
                                    (MarriageManager.getRemarryCooldownRemaining(targetUuid) + 59L) / 60L))));
            case TARGET_COOLDOWN -> context.getPlayerRef().sendMessage(
                    Message.translation("server.marriage.remarry.targetCooldown").param("username", requesterName));
            case SELF, TARGET_NOT_ACCEPTING, REQUEST_SENT, ALREADY_REQUESTED -> {
                // Not reachable from a genuine accept click: SELF cannot happen (the
                // requester and the accepter already differ), and the other three are
                // propose()'s fresh-proposal branches - superseded here because a
                // pending request from this exact requester was just confirmed to
                // exist above. Kept only so the switch stays exhaustive.
            }
            case MARRIED -> {
                PlayerRef requesterRef = Universe.get().getPlayer(requesterUuid);
                if (requesterRef == null || !requesterRef.isValid()) {
                    context.getPlayerRef().sendMessage(Message.translation(LANG + "overview.request.requesterOffline")
                            .param("username", requesterName));
                    return;
                }
                MarriageUtil.completeMarriage(context.getStore(), context.getRef(), context.getPlayerRef(), requesterRef);
            }
        }
    }

    /** The stack in the player's active hotbar slot, or null. */
    @Nullable
    private static ItemStack heldItem(@Nonnull MarriageUIContext context) {
        InventoryComponent.Hotbar hotbar = context.getComponent(InventoryComponent.Hotbar.getComponentType());
        return hotbar == null ? null : hotbar.getActiveItem();
    }

    /** Opens the compose page in note mode - the menu's {@code /marry msg}. */
    private static void openNoteComposer(@Nonnull MarriageUIContext context) {
        Player player = context.getComponent(Player.getComponentType());
        if (player != null) {
            player.getPageManager().openCustomPage(context.getRef(), context.getStore(),
                    new MarriageLetterUIPage(context.getPlayerRef(), MarriageLetterUIPage.Mode.NOTE));
        }
    }

    /** Opens the Love Letter in hand, to write or to read. */
    private static void openLetter(@Nonnull MarriageUIContext context) {
        Player player = context.getComponent(Player.getComponentType());
        if (player != null) {
            player.getPageManager().openCustomPage(context.getRef(), context.getStore(),
                    new MarriageLetterUIPage(context.getPlayerRef(), MarriageLetterUIPage.Mode.LETTER));
        }
    }

    /**
     * The Divorce button. Mirrors the click-twice arming for the blurb, then hands
     * off to the same flow the command uses - which now files rather than ends it
     * outright when a waiting period is configured.
     */
    private static void doDivorce(@Nonnull MarriageUIContext context) {
        UUID uuid = context.getUuid();
        MarriageRecord before = MarriageManager.getRecord(uuid);
        boolean wasArmed = isDivorceArmed(uuid);

        MarriageUtil.divorceFlow(context.getPlayerRef());

        // Armed if the flow asked for a confirmation, cleared once it acted.
        MarriageRecord after = MarriageManager.getRecord(uuid);
        boolean confirmed = wasArmed || before == null || after == null
                || after.isDivorcePending();
        if (confirmed) {
            DIVORCE_ARMED.remove(uuid);
        } else {
            DIVORCE_ARMED.put(uuid, Boolean.TRUE);
        }
    }

    private static boolean isDivorceArmed(@Nonnull UUID uuid) {
        return DIVORCE_ARMED.getOrDefault(uuid, Boolean.FALSE);
    }

    // --- Bond ------------------------------------------------------------------

    /**
     * The Bond page: how deep this marriage runs, and the ladder of perks that
     * depth unlocks.
     *
     * <p>A ROWS category rather than its own document, so every perk renders through
     * the same row fragments the Settings page uses — the perk list is genuinely a
     * list, and giving it bespoke markup would buy nothing but another .ui file to
     * keep in step with the theme.</p>
     *
     * <p>Hidden entirely for an unmarried player and for a server that has turned
     * the bond off: an empty page is worse than no page.</p>
     */
    private static void registerBond() {
        MarriageUIRegistry.registerCategory(MarriageUICategory.builder(MarriageUIRegistry.CATEGORY_BOND)
                .name(LANG + "category.bond")
                .hint(LANG + "category.bond.hint")
                .layout(MarriageUILayout.ROWS)
                .sortOrder(150)
                .visible(context -> Marriage.getConfig().get().ifBondEnabled()
                        && MarriageManager.isMarried(context.getUuid()))
                .options(MarriageUIDefaults::bondOptions)
                .build());
    }

    @Nonnull
    private static List<MarriageUIOption> bondOptions(@Nonnull MarriageUIContext context) {
        MarriageRecord record = MarriageManager.getRecord(context.getUuid());
        if (record == null) {
            return List.of();
        }

        List<MarriageUIOption> options = new ArrayList<>();

        // Read-only summary rows. They are options with no onSelect rather than a
        // separate widget because a ROWS category renders options and nothing else -
        // clicking one is a no-op that redraws the page, which is harmless.
        options.add(MarriageUIOption.builder("marriage:bond_level")
                .name(BondManager.isMaxLevel(record)
                        ? Message.translation(LANG + "bond.level.max").param("level", String.valueOf(record.getBondLevel()))
                        : Message.translation(LANG + "bond.level").param("level", String.valueOf(record.getBondLevel())))
                .detail(ctx -> BondManager.isMaxLevel(record)
                        ? Message.translation(LANG + "bond.level.maxDetail")
                        : Message.translation(LANG + "bond.level.detail")
                                .param("xp", String.valueOf(Math.round(BondManager.xpIntoLevel(record))))
                                .param("needed", String.valueOf(Math.round(BondManager.xpForNextLevel(record))))
                                .param("percent", String.valueOf(Math.round(BondManager.progress(record) * 100f))))
                .build());

        options.add(MarriageUIOption.builder("marriage:bond_together")
                .name(LANG + "bond.together.name")
                .detail(Message.translation(LANG + "bond.together.detail")
                        .param("duration", MarriageUtil.formatDuration(record.getTogetherMinutes() * 60_000L)))
                .build());

        int nextAnniversary = BondManager.nextAnniversaryDays(record);
        options.add(MarriageUIOption.builder("marriage:bond_anniversary")
                .name(LANG + "bond.anniversary.name")
                .detail(nextAnniversary > 0
                        ? Message.translation(LANG + "bond.anniversary.detail")
                                .param("days", String.valueOf(nextAnniversary - BondManager.daysMarried(record)))
                                .param("milestone", String.valueOf(nextAnniversary))
                        : Message.translation(LANG + "bond.anniversary.allReached"))
                .visible(ctx -> Marriage.getConfig().get().ifAnniversariesEnabled())
                .build());

        // The vow: the choice itself while it is open, otherwise what they swore.
        if (Marriage.getConfig().get().ifVowsEnabled()) {
            if (record.isVowChosen()) {
                Vow sworn = record.getVow();
                options.add(record("marriage:vow_sworn", LANG + "bond.vow.sworn",
                        Message.translation(LANG + "bond.vow.sworn.detail")
                                .param("vow", Message.translation(sworn.getNameKey()))
                                .param("detail", Message.translation(sworn.getDetailKey()))));
            } else {
                options.add(record("marriage:vow_prompt", LANG + "bond.vow.choose",
                        Message.translation(LANG + "bond.vow.choose.detail")));
                for (Vow choice : Vow.choices()) {
                    options.add(vowRow(choice));
                }
            }
        }

        if (Marriage.getConfig().get().ifRenewEnabled()) {
            options.add(MarriageUIOption.builder("marriage:renew")
                    .name(LANG + "bond.renew.name")
                    .detail(LANG + "bond.renew.detail")
                    .onSelect(ctx -> MarriageUtil.renewVows(ctx.getStore(), ctx.getRef(), ctx.getPlayerRef()))
                    .build());
        }

        for (BondPerk perk : BondPerk.values()) {
            options.add(perkRow(record, perk));
        }

        return options;
    }

    /** One swearable vow, with what it favors spelled out before they commit. */
    @Nonnull
    private static MarriageUIOption vowRow(@Nonnull Vow vow) {
        return MarriageUIOption.builder("marriage:vow_" + vow.getId())
                .name(vow.getNameKey())
                .detail(vow.getDetailKey())
                .onSelect(ctx -> {
                    if (MarriageManager.setVow(ctx.getUuid(), vow)) {
                        MarriageRecord record = MarriageManager.getRecord(ctx.getUuid());
                        if (record != null) {
                            // Both of them: a vow is not one person's decision to
                            // be told about later.
                            BondManager.sendToCouple(record, Message.translation("server.marriage.vow.sworn")
                                    .param("vow", Message.translation(vow.getNameKey()))
                                    .color(java.awt.Color.PINK));
                        }
                    }
                })
                .build();
    }

    /**
     * One perk, rendered as a checkbox row whose tick IS the unlock state.
     *
     * <p>A toggle the player cannot toggle is the right shape here: the checkbox is
     * the clearest "have I got this yet" the row fragments offer, and every row is
     * disabled so a click never pretends to be a setting.</p>
     */
    @Nonnull
    private static MarriageUIOption perkRow(@Nonnull MarriageRecord record, @Nonnull BondPerk perk) {
        return MarriageUIOption.builder("marriage:perk_" + perk.getId())
                .name(perk.getNameKey())
                .toggle(true)
                .selected(ctx -> BondManager.hasPerk(MarriageManager.getRecord(ctx.getUuid()), perk))
                .visible(ctx -> perk.isEnabled())
                .enabled(ctx -> false)
                .disabledReason(ctx -> BondManager.hasPerk(MarriageManager.getRecord(ctx.getUuid()), perk)
                        ? Message.translation(perk.getDetailKey())
                        : Message.translation(LANG + "bond.perk.locked")
                                .param("level", String.valueOf(
                                        BondManager.requiredLevelFor(MarriageManager.getRecord(ctx.getUuid()), perk))))
                .build();
    }

    // --- Married Couples ---------------------------------------------------

    private static void registerCouples() {
        MarriageUIRegistry.registerCategory(MarriageUICategory.builder(MarriageUIRegistry.CATEGORY_COUPLES)
                .name(LANG + "category.couples")
                .hint(LANG + "category.couples.hint")
                .sortOrder(200)
                .document(COUPLES_DOC, MarriageUIDefaults::renderCouples)
                .build());
    }

    private static void renderCouples(
            @Nonnull MarriageUIContext context,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        commandBuilder.clear("#StatsRow");
        appendStat(commandBuilder, 0, LANG + "couples.stat.couples", MarriageManager.getMarriedCoupleCount());
        appendStat(commandBuilder, 1, LANG + "couples.stat.marriages", MarriageManager.getTotalMarriagesEver());
        appendStat(commandBuilder, 2, LANG + "couples.stat.divorces", MarriageManager.getTotalDivorces());

        List<MarriageInfo> marriages = MarriageManager.getAllMarriages();
        commandBuilder.clear("#CoupleList");
        commandBuilder.set("#CouplesEmptyState.Visible", marriages.isEmpty());

        // Admins get a Divorce control on every row - the per-couple action list
        // that matches what /marriage force divorce can do from the console.
        // Re-checked per render, not cached: a revoked permission takes effect on
        // the next redraw rather than the next restart.
        boolean admin = context.hasPermission(MarriageAdminUIPage.ADMIN_PERMISSION);

        int index = 0;
        for (MarriageInfo marriage : marriages) {
            boolean oneOnline = Universe.get().getPlayer(marriage.getPlayer1Uuid()) != null;
            boolean twoOnline = Universe.get().getPlayer(marriage.getPlayer2Uuid()) != null;
            int onlineCount = (oneOnline ? 1 : 0) + (twoOnline ? 1 : 0);

            commandBuilder.append("#CoupleList", MarriageUIPage.COUPLE_ROW_DOC);
            String row = "#CoupleList[" + index + "]";

            // "&" rather than a heart glyph - Hytale's font coverage for U+2665 is
            // unverified, and a missing glyph would render as a tofu box.
            commandBuilder.set(row + " #Names.Text", MarriageUtil.displayNameRaw(marriage.getPlayer1Name())
                    + " & " + MarriageUtil.displayNameRaw(marriage.getPlayer2Name()));
            commandBuilder.set(row + " #Duration.TextSpans", Message.translation(LANG + "couples.row.duration")
                    .param("duration", MarriageUtil.formatDuration(marriage.getMarriedForMillis())));
            commandBuilder.set(row + " #Status.TextSpans", Message.translation(LANG + "couples.row.online")
                    .param("online", String.valueOf(onlineCount))
                    .param("bond", String.valueOf(marriage.getBondLevel())));

            commandBuilder.set(row + " #Action.Visible", admin);
            if (admin) {
                boolean armed = isCoupleDivorceArmed(context.getUuid(), marriage.getPlayer1Uuid());
                commandBuilder.set(row + " #Action.TextSpans", Message.translation(armed
                        ? LANG + "couples.row.divorce.confirm"
                        : LANG + "couples.row.divorce"));
                eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, row + " #Action",
                        new EventData()
                                .append(MarriageUIPage.MarriageUIEventData.KEY_COUPLE_ACTION, "divorce")
                                .append(MarriageUIPage.MarriageUIEventData.KEY_COUPLE_UUID,
                                        marriage.getPlayer1Uuid().toString()));
            }

            index++;
        }
    }

    /**
     * Whether this admin's next click on a given couple actually divorces them.
     *
     * <p>Click-twice-to-confirm, keyed by admin AND by couple, so arming one row
     * cannot fire on another. Purely UI state - unlike a player divorcing
     * themselves, there is no server-side window to mirror here, because
     * {@code MarriageAPI.divorce} is immediate by design.</p>
     */
    private static final Map<UUID, UUID> COUPLE_DIVORCE_ARMED = new ConcurrentHashMap<>();

    private static boolean isCoupleDivorceArmed(@Nonnull UUID admin, @Nonnull UUID coupleKey) {
        return coupleKey.equals(COUPLE_DIVORCE_ARMED.get(admin));
    }

    /** Called by {@link MarriageUIPage#handleDataEvent} for a couple-row action click. */
    static void handleCoupleAction(@Nonnull MarriageUIContext context, @Nonnull String action, @Nonnull String coupleUuidStr) {
        if (!context.hasPermission(MarriageAdminUIPage.ADMIN_PERMISSION) || !"divorce".equals(action)) {
            return;
        }

        UUID coupleKey;
        try {
            coupleKey = UUID.fromString(coupleUuidStr);
        } catch (IllegalArgumentException invalidUuid) {
            return;
        }

        UUID admin = context.getUuid();
        if (!isCoupleDivorceArmed(admin, coupleKey)) {
            COUPLE_DIVORCE_ARMED.put(admin, coupleKey);
            return;
        }
        COUPLE_DIVORCE_ARMED.remove(admin);

        MarriageInfo marriage = MarriageAPI.getMarriage(coupleKey);
        if (marriage == null) {
            // Divorced by somebody else between arming and confirming - the row is
            // simply gone on the next render.
            return;
        }

        String one = marriage.getPlayer1Name();
        String two = marriage.getPlayer2Name();
        if (!MarriageAPI.divorce(coupleKey)) {
            context.getPlayerRef().sendMessage(Message.translation("server.commands.marriage.force.divorce.cancelled")
                    .param("username", one));
            return;
        }

        syncDivorcedSide(marriage.getPlayer1Uuid(), two);
        syncDivorcedSide(marriage.getPlayer2Uuid(), one);

        context.getPlayerRef().sendMessage(Message.translation("server.commands.marriage.force.divorce.done")
                .param("usernameOne", one)
                .param("usernameTwo", two));
        Marriage.LOGGER.atInfo().log(context.getPlayerRef().getUsername()
                + " force-divorced " + one + " and " + two + " from the menu.");
    }

    /** Clears one side's cached married state and tells them, if they are online. */
    private static void syncDivorcedSide(@Nonnull UUID uuid, @Nonnull String formerPartnerName) {
        PlayerRef playerRef = Universe.get().getPlayer(uuid);
        if (playerRef == null || !playerRef.isValid()) {
            return;
        }
        ComponentSync.syncDivorced(playerRef);
        playerRef.sendMessage(Message.translation("server.marriage.force.divorced")
                .param("partnerUsername", formerPartnerName));
    }

    private static void appendStat(@Nonnull UICommandBuilder commandBuilder, int index, @Nonnull String captionKey, int value) {
        commandBuilder.append("#StatsRow", MarriageUIPage.STAT_ROW_DOC);
        String cell = "#StatsRow[" + index + "]";
        commandBuilder.set(cell + " #Value.Text", String.valueOf(value));
        commandBuilder.set(cell + " #Caption.TextSpans", Message.translation(captionKey));
    }

    // --- Propose ---------------------------------------------------------------

    /**
     * Proposing from the menu, so a player never has to type a command to marry.
     *
     * <p>Only ever shown to an unmarried player: for a married one it would be a
     * tab offering something the registry would refuse anyway. The Overview's own
     * unmarried view keeps the incoming proposals, because answering one is a
     * different act from making one.</p>
     */
    private static void registerPropose() {
        MarriageUIRegistry.registerCategory(MarriageUICategory.builder(MarriageUIRegistry.CATEGORY_PROPOSE)
                .name(LANG + "category.propose")
                .hint(LANG + "category.propose.hint")
                .layout(MarriageUILayout.ROWS)
                .sortOrder(120)
                .visible(context -> !MarriageManager.isMarried(context.getUuid()))
                .options(MarriageUIDefaults::proposeOptions)
                .build());
    }

    /**
     * Everyone this player could propose to right now, plus whoever they have
     * already asked.
     *
     * <p>Built fresh on every render <em>and</em> re-resolved on click (see
     * {@code MarriageUIPage.applyOption}), so a candidate who logs off or marries
     * someone else between the two simply is not there any more and the click
     * resolves to nothing. That is the whole reason the target is captured in the
     * row's closure rather than parsed out of a stale id.</p>
     */
    @Nonnull
    private static List<MarriageUIOption> proposeOptions(@Nonnull MarriageUIContext context) {
        UUID self = context.getUuid();
        if (MarriageManager.isMarried(self)) {
            return List.of();
        }

        List<MarriageUIOption> options = new ArrayList<>();
        MarriageConfig config = Marriage.getConfig().get();

        // Outgoing first: "did my proposal land" is the question somebody opening
        // this page a second time actually has.
        long now = System.currentTimeMillis();
        for (PlayerRef candidate : Universe.get().getPlayers()) {
            if (candidate == null || !candidate.isValid() || candidate.getUuid().equals(self)) {
                continue;
            }
            MarriageManager.PendingRequest sent = MarriageManager.getPendingRequests(candidate.getUuid()).get(self);
            if (sent == null) {
                continue;
            }
            long secondsLeft = Math.max(0L, (sent.expiryMillis() - now + 999L) / 1000L);
            Message waiting = Message.translation(LANG + "propose.pending.detail")
                    .param("seconds", String.valueOf(secondsLeft));
            options.add(MarriageUIOption.builder("marriage:proposed:" + candidate.getUuid())
                    .name(Message.translation(LANG + "propose.pending.name").param("username", candidate.getUsername()))
                    .detail(waiting)
                    .enabled(ctx -> false)
                    .disabledReason(ctx -> waiting)
                    .build());
        }

        int candidates = 0;
        for (PlayerRef candidate : Universe.get().getPlayers()) {
            if (candidate == null || !candidate.isValid()) {
                continue;
            }
            UUID targetUuid = candidate.getUuid();
            if (targetUuid.equals(self) || MarriageManager.isMarried(targetUuid)) {
                continue;
            }
            // Already asked - they are listed above instead.
            if (MarriageManager.getPendingRequests(targetUuid).containsKey(self)) {
                continue;
            }

            candidates++;
            String name = candidate.getUsername();
            boolean accepting = MarriageManager.acceptsRequests(targetUuid);
            // A proposal they sent US: accepting it here completes the marriage,
            // which is exactly what MarriageManager.propose does with a mutual pair.
            boolean theyAsked = MarriageManager.getPendingRequests(self).containsKey(targetUuid);

            options.add(MarriageUIOption.builder("marriage:propose:" + targetUuid)
                    .name(Message.translation(theyAsked ? LANG + "propose.accept.name" : LANG + "propose.name")
                            .param("username", name))
                    .detail(Message.translation(theyAsked ? LANG + "propose.accept.detail" : LANG + "propose.detail")
                            .param("username", name))
                    .enabled(ctx -> accepting || theyAsked)
                    .disabledReason(ctx -> Message.translation(LANG + "propose.notAccepting").param("username", name))
                    .onSelect(ctx -> {
                        PlayerRef target = Universe.get().getPlayer(targetUuid);
                        if (target == null || !target.isValid()) {
                            ctx.getPlayerRef().sendMessage(Message.translation(LANG + "propose.gone").param("username", name));
                            return;
                        }
                        MarriageUtil.proposeTo(ctx.getStore(), ctx.getRef(), ctx.getPlayerRef(), target);
                    })
                    .build());
        }

        if (candidates == 0) {
            options.add(record("marriage:propose_empty", LANG + "propose.empty",
                    Message.translation(LANG + "propose.empty.detail")));
        }

        // The ring rule stated where it bites, rather than only as a refusal after
        // the click.
        if (config.ifRequireRing()) {
            options.add(record("marriage:propose_ring", LANG + "propose.ring",
                    Message.translation(config.ifConsumeRing()
                            ? LANG + "propose.ring.consumed"
                            : LANG + "propose.ring.kept")));
        }

        return options;
    }

    // --- Dual Cultivation (Cultivation compat) ---------------------------------

    /**
     * What this marriage is worth to a pair of cultivators.
     *
     * <p>Cultivation's Partnered Cultivation (双修) has read Marriage's API since
     * its 0.6.0, but only from its own side - a married couple got a faster Qi
     * rate with nothing in either mod's UI to say so. This is the reverse link:
     * the pairing, stated where the marriage lives.</p>
     *
     * <p>Registered unconditionally but visible only when
     * {@link CultivationBridge#isAvailable()} says Cultivation is actually there
     * and the feature is on. Registration happens in {@code setup()}, which may
     * run before Cultivation has loaded - which is exactly why visibility is a
     * per-render predicate rather than a decision made here.</p>
     */
    private static void registerDualCultivation() {
        MarriageUIRegistry.registerCategory(MarriageUICategory.builder(MarriageUIRegistry.CATEGORY_DUAL_CULTIVATION)
                .name(LANG + "category.dualCultivation")
                .hint(LANG + "category.dualCultivation.hint")
                .layout(MarriageUILayout.ROWS)
                .sortOrder(175)
                .visible(context -> MarriageManager.isMarried(context.getUuid())
                        && CultivationBridge.isAvailable()
                        && CultivationBridge.isPartneredCultivationEnabled())
                .options(MarriageUIDefaults::dualCultivationOptions)
                .build());
    }

    @Nonnull
    private static List<MarriageUIOption> dualCultivationOptions(@Nonnull MarriageUIContext context) {
        MarriageRecord record = MarriageManager.getRecord(context.getUuid());
        if (record == null || !CultivationBridge.isAvailable()) {
            return List.of();
        }

        List<MarriageUIOption> options = new ArrayList<>();
        UUID uuid = context.getUuid();

        boolean qualifies = CultivationBridge.meetsRealmRequirement(context.getStore(), context.getRef());
        String requiredRealmKey = CultivationBridge.getRequiredRealmKey();

        // Status first: the one row that answers "is this working right now".
        options.add(record("marriage:dual_status", LANG + "dual.status", statusOf(context, record, uuid, qualifies)));

        options.add(record("marriage:dual_multiplier", LANG + "dual.multiplier",
                Message.translation(LANG + "dual.multiplier.value")
                        .param("multiplier", formatMultiplier(CultivationBridge.getQiMultiplier()))));

        options.add(record("marriage:dual_range", LANG + "dual.range",
                Message.translation(LANG + "dual.range.value")
                        .param("blocks", String.valueOf(Math.round(CultivationBridge.getRadiusBlocks())))));

        // The realm gate, and only when the server actually sets one.
        if (requiredRealmKey != null) {
            options.add(record("marriage:dual_realm", LANG + "dual.realm",
                    Message.translation(qualifies ? LANG + "dual.realm.met" : LANG + "dual.realm.unmet")
                            .param("realm", Message.translation(requiredRealmKey))));
        }

        if (CultivationBridge.isConvergenceEnabled()) {
            options.add(record("marriage:dual_convergence", LANG + "dual.convergence",
                    Message.translation(LANG + "dual.convergence.value")));
        }

        return options;
    }

    /**
     * How the pairing stands for this player right now, in the order that answers
     * their question soonest: blocked, then not-yet-meditating, then live.
     */
    @Nonnull
    private static Message statusOf(@Nonnull MarriageUIContext context, @Nonnull MarriageRecord record,
                                    @Nonnull UUID uuid, boolean qualifies) {
        if (!qualifies) {
            String realmKey = CultivationBridge.getRequiredRealmKey();
            return realmKey == null
                    ? Message.translation(LANG + "dual.status.noCultivation")
                    : Message.translation(LANG + "dual.status.locked").param("realm", Message.translation(realmKey));
        }

        UUID partnerUuid = record.getPartnerUuid(uuid);
        PlayerRef partner = partnerUuid == null ? null : Universe.get().getPlayer(partnerUuid);
        if (partner == null || !partner.isValid()) {
            return Message.translation(LANG + "dual.status.partnerOffline")
                    .param("partnerUsername", MarriageUtil.displayName(record.getPartnerName(uuid)));
        }

        Ref<EntityStore> partnerRef = partner.getReference();
        // Same store, exactly as Cultivation's own pairing test requires - a
        // partner in another world is not sitting with you, and their components
        // could not be read from here anyway.
        if (partnerRef == null || !partnerRef.isValid() || partnerRef.getStore() != context.getStore()) {
            return Message.translation(LANG + "dual.status.otherWorld")
                    .param("partnerUsername", MarriageUtil.displayName(record.getPartnerName(uuid)));
        }

        boolean weMeditate = CultivationBridge.isMeditating(context.getStore(), context.getRef());
        boolean theyMeditate = CultivationBridge.isMeditating(context.getStore(), partnerRef);
        boolean inRange = MarriagePresenceSystem.isWithinBondRange(context.getStore(), context.getRef(), partnerRef,
                CultivationBridge.getRadiusBlocks());

        if (weMeditate && theyMeditate && inRange) {
            return Message.translation(LANG + "dual.status.active")
                    .param("multiplier", formatMultiplier(CultivationBridge.getQiMultiplier()));
        }
        if (!inRange) {
            return Message.translation(LANG + "dual.status.tooFar")
                    .param("partnerUsername", MarriageUtil.displayName(record.getPartnerName(uuid)));
        }
        return Message.translation(LANG + "dual.status.ready")
                .param("partnerUsername", MarriageUtil.displayName(record.getPartnerName(uuid)));
    }

    /** "1.25" rather than "1.25000005" - a multiplier read off a float. */
    @Nonnull
    private static String formatMultiplier(float multiplier) {
        return String.valueOf(Math.round(multiplier * 100f) / 100f);
    }

    // --- Records ---------------------------------------------------------------

    /**
     * The hall of records: who has been married longest, who gives the most, and
     * — because a marriage mod should be able to laugh at itself — who has been
     * divorced the most.
     *
     * <p>Built from {@link plugin.siren.Utils.Registry.PlayerTally}, not from the
     * live marriage records, because two of the three questions are about people
     * who may not be married at all right now.</p>
     */
    private static void registerRecords() {
        MarriageUIRegistry.registerCategory(MarriageUICategory.builder(MarriageUIRegistry.CATEGORY_RECORDS)
                .name(LANG + "category.records")
                .hint(LANG + "category.records.hint")
                .layout(MarriageUILayout.ROWS)
                .sortOrder(250)
                .visible(context -> Marriage.getConfig().get().ifRecordsEnabled())
                .options(MarriageUIDefaults::recordsOptions)
                .build());
    }

    @Nonnull
    private static List<MarriageUIOption> recordsOptions(@Nonnull MarriageUIContext context) {
        List<MarriageUIOption> options = new ArrayList<>();

        // Longest marriage still standing, from the live records.
        MarriageInfo longest = MarriageManager.getAllMarriages().stream()
                .min(Comparator.comparingLong(MarriageInfo::getMarriedAtMillis))
                .orElse(null);
        options.add(record("marriage:record_longest_current", LANG + "records.longestCurrent",
                longest == null
                        ? Message.translation(LANG + "records.none")
                        : Message.translation(LANG + "records.longestCurrent.value")
                                .param("usernameOne", MarriageUtil.displayName(longest.getPlayer1Name()))
                                .param("usernameTwo", MarriageUtil.displayName(longest.getPlayer2Name()))
                                .param("duration", MarriageUtil.formatDuration(longest.getMarriedForMillis()))));

        List<PlayerTally> tallies = MarriageManager.getAllTallies();

        // Longest that has ever ENDED. Deliberately separate from the one above:
        // a marriage that lasted a year and then broke is a different record from
        // one that is still going, and folding them together would let a divorce
        // quietly take the crown off a couple who are still together.
        PlayerTally longestEver = best(tallies, Comparator.comparingLong(PlayerTally::getLongestMarriageMillis),
                tally -> tally.getLongestMarriageMillis() > 0L);
        options.add(record("marriage:record_longest_ever", LANG + "records.longestEver",
                longestEver == null
                        ? Message.translation(LANG + "records.none")
                        : Message.translation(LANG + "records.longestEver.value")
                                .param("username", MarriageUtil.displayName(longestEver.getName()))
                                .param("duration", MarriageUtil.formatDuration(longestEver.getLongestMarriageMillis()))));

        PlayerTally mostGifts = best(tallies, Comparator.comparingInt(PlayerTally::getGiftsGiven),
                tally -> tally.getGiftsGiven() > 0);
        options.add(record("marriage:record_gifts", LANG + "records.mostGifts",
                mostGifts == null
                        ? Message.translation(LANG + "records.none")
                        : Message.translation(LANG + "records.mostGifts.value")
                                .param("username", MarriageUtil.displayName(mostGifts.getName()))
                                .param("count", String.valueOf(mostGifts.getGiftsGiven()))));

        PlayerTally mostDivorces = best(tallies, Comparator.comparingInt(PlayerTally::getDivorces),
                tally -> tally.getDivorces() > 0);
        options.add(record("marriage:record_divorces", LANG + "records.mostDivorces",
                mostDivorces == null
                        ? Message.translation(LANG + "records.noneDivorces")
                        : Message.translation(LANG + "records.mostDivorces.value")
                                .param("username", MarriageUtil.displayName(mostDivorces.getName()))
                                .param("count", String.valueOf(mostDivorces.getDivorces()))));

        PlayerTally mostMarriages = best(tallies, Comparator.comparingInt(PlayerTally::getMarriages),
                tally -> tally.getMarriages() > 0);
        options.add(record("marriage:record_marriages", LANG + "records.mostMarriages",
                mostMarriages == null
                        ? Message.translation(LANG + "records.none")
                        : Message.translation(LANG + "records.mostMarriages.value")
                                .param("username", MarriageUtil.displayName(mostMarriages.getName()))
                                .param("count", String.valueOf(mostMarriages.getMarriages()))));

        return options;
    }

    /** The highest-scoring tally that passes {@code eligible}, or null when none does. */
    @Nullable
    private static PlayerTally best(@Nonnull List<PlayerTally> tallies,
                                    @Nonnull Comparator<PlayerTally> by,
                                    @Nonnull Predicate<PlayerTally> eligible) {
        return tallies.stream().filter(eligible).max(by).orElse(null);
    }

    /** A read-only records row: a heading and the answer, never clickable. */
    @Nonnull
    private static MarriageUIOption record(@Nonnull String id, @Nonnull String nameKey, @Nonnull Message value) {
        return MarriageUIOption.builder(id)
                .name(nameKey)
                .detail(value)
                .enabled(ctx -> false)
                .disabledReason(ctx -> value)
                .build();
    }

    // --- Settings ------------------------------------------------------------

    private static void registerSettings() {
        MarriageUIRegistry.registerCategory(MarriageUICategory.builder(MarriageUIRegistry.CATEGORY_SETTINGS)
                .name(LANG + "category.settings")
                .hint(LANG + "category.settings.hint")
                .layout(MarriageUILayout.ROWS)
                .sortOrder(300)
                .options(MarriageUIDefaults::settingsOptions)
                .build());
    }

    @Nonnull
    private static List<MarriageUIOption> settingsOptions(@Nonnull MarriageUIContext context) {
        List<MarriageUIOption> options = new ArrayList<>();

        options.add(MarriageUIOption.builder("marriage:accept_requests")
                .name(LANG + "settings.acceptRequests.name")
                .detail(LANG + "settings.acceptRequests.detail")
                .toggle(true)
                .selected(ctx -> {
                    MarriageSettingsComponent settings = ctx.getComponent(MarriageSettingsComponent.getComponentType());
                    return settings != null && settings.isAcceptRequests();
                })
                .onSelect(MarriageUIDefaults::toggleAcceptRequests)
                .build());

        options.add(MarriageUIOption.builder("marriage:couple_channel")
                .name(LANG + "settings.coupleChannel.name")
                .detail(LANG + "settings.coupleChannel.detail")
                .toggle(true)
                .visible(ctx -> MarriageManager.isMarried(ctx.getUuid()))
                .selected(ctx -> {
                    MarriageSettingsComponent settings = ctx.getComponent(MarriageSettingsComponent.getComponentType());
                    return settings != null && settings.isCoupleChannel();
                })
                .enabled(ctx -> Marriage.getConfig().get().ifCoupleChannelEnabled())
                .disabledReason(LANG + "settings.coupleChannel.disabled")
                .onSelect(MarriageUIDefaults::toggleCoupleChannel)
                .build());

        options.add(MarriageUIOption.builder("marriage:partner_hud")
                .name(LANG + "settings.partnerHud.name")
                .detail(LANG + "settings.partnerHud.detail")
                .toggle(true)
                .visible(ctx -> MarriageManager.isMarried(ctx.getUuid()))
                .selected(ctx -> {
                    MarriageSettingsComponent settings = ctx.getComponent(MarriageSettingsComponent.getComponentType());
                    return settings != null && settings.isPartnerHud();
                })
                .enabled(ctx -> Marriage.getConfig().get().ifPartnerHudEnabled())
                .disabledReason(LANG + "settings.partnerHud.disabled")
                .onSelect(MarriageUIDefaults::togglePartnerHud)
                .build());

        options.add(MarriageUIOption.builder("marriage:pvp_protection")
                .name(LANG + "settings.pvpProtection.name")
                .detail(LANG + "settings.pvpProtection.detail")
                .toggle(true)
                // Only meaningful once married - the couple's own PvP flag lives on the
                // shared marriage record, not on either player alone.
                .visible(ctx -> MarriageManager.isMarried(ctx.getUuid()))
                .selected(ctx -> {
                    MarriageRecord record = MarriageManager.getRecord(ctx.getUuid());
                    return record != null && record.isPvpProtection();
                })
                .enabled(ctx -> Marriage.getConfig().get().ifPartnerPvpProtection())
                .disabledReason(LANG + "settings.pvpProtection.disabled")
                .onSelect(MarriageUIDefaults::togglePvpProtection)
                .build());

        return options;
    }

    private static void toggleAcceptRequests(@Nonnull MarriageUIContext context) {
        MarriageSettingsComponent settings = context.getComponent(MarriageSettingsComponent.getComponentType());
        if (settings == null) {
            return;
        }
        boolean newValue = !settings.isAcceptRequests();
        settings.setAcceptRequests(newValue);
        MarriageManager.cacheAcceptsRequests(context.getUuid(), newValue);
    }

    private static void toggleCoupleChannel(@Nonnull MarriageUIContext context) {
        MarriageSettingsComponent settings = context.getComponent(MarriageSettingsComponent.getComponentType());
        if (settings == null) {
            return;
        }
        boolean newValue = !settings.isCoupleChannel();
        settings.setCoupleChannel(newValue);
        // Mirrored into the registry's cache, which is what the asynchronous chat
        // event reads - it cannot safely touch this component.
        MarriageManager.setCoupleChannelOn(context.getUuid(), newValue);
    }

    private static void togglePartnerHud(@Nonnull MarriageUIContext context) {
        MarriageSettingsComponent settings = context.getComponent(MarriageSettingsComponent.getComponentType());
        if (settings == null) {
            return;
        }
        boolean newValue = !settings.isPartnerHud();
        settings.setPartnerHud(newValue);
        if (!newValue) {
            // Take it off screen now rather than at the next presence tick, so the
            // toggle looks like it did something.
            PartnerHud.remove(context.getStore(), context.getRef(), context.getPlayerRef());
        }
    }

    private static void togglePvpProtection(@Nonnull MarriageUIContext context) {
        UUID uuid = context.getUuid();
        MarriageRecord record = MarriageManager.getRecord(uuid);
        if (record == null) {
            return;
        }
        MarriageManager.setPvpProtection(uuid, !record.isPvpProtection());
    }

    // --- Admin -----------------------------------------------------------------

    private static void registerAdmin() {
        MarriageUIRegistry.registerCategory(MarriageUICategory.builder(MarriageUIRegistry.CATEGORY_ADMIN)
                .name(LANG + "category.admin")
                .sortOrder(900)
                .visible(context -> context.hasPermission(MarriageAdminUIPage.ADMIN_PERMISSION))
                .opensPage(MarriageUIDefaults::openAdminMenu)
                .build());
    }

    private static void openAdminMenu(@Nonnull MarriageUIContext context) {
        Player player = context.getComponent(Player.getComponentType());
        // Re-checked rather than trusted: the category is only listed for admins,
        // but a click carries an id and ids can be replayed.
        if (player != null && context.hasPermission(MarriageAdminUIPage.ADMIN_PERMISSION)) {
            player.getPageManager().openCustomPage(context.getRef(), context.getStore(), new MarriageAdminUIPage(context.getPlayerRef()));
        }
    }
}
