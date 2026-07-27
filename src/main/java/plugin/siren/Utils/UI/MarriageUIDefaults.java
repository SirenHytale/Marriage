package plugin.siren.Utils.UI;

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
import plugin.siren.API.CustomUI.MarriageUICategory;
import plugin.siren.API.CustomUI.MarriageUIContext;
import plugin.siren.API.CustomUI.MarriageUILayout;
import plugin.siren.API.CustomUI.MarriageUIOption;
import plugin.siren.API.CustomUI.MarriageUIRegistry;
import plugin.siren.API.CustomUI.MarriageUISection;
import plugin.siren.API.MarriageInfo;
import plugin.siren.Marriage;
import plugin.siren.Systems.MarriageSettingsComponent;
import plugin.siren.Utils.GiftManager;
import plugin.siren.Utils.MarriageConfig;
import plugin.siren.Utils.MarriageUtil;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registers the four built-in Marriage menu categories - Overview, Married
 * Couples, Settings and Admin - through the same public
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
        registerCouples();
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

        options.add(MarriageUIOption.builder("marriage:divorce")
                .name(LANG + "overview.action.divorce.name")
                .detail(ctx -> isDivorceArmed(ctx.getUuid())
                        ? Message.translation(LANG + "overview.action.divorce.confirm")
                                .param("seconds", String.valueOf(Marriage.getConfig().get().getDivorceConfirmSeconds()))
                        : Message.translation(LANG + "overview.action.divorce.detail"))
                .onSelect(MarriageUIDefaults::doDivorce)
                .build());

        return options;
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
                        .param("partnerUsername", marriage.getPartnerName(uuid)));
        index = appendInfoRow(commandBuilder, index, LANG + "overview.info.marriedForLabel",
                MarriageUtil.formatDuration(marriage.getMarriedForMillis()));
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
            if (held == null || !MarriageUtil.RING_ITEM_ID.equalsIgnoreCase(held.getItemId())) {
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

    private static void doDivorce(@Nonnull MarriageUIContext context) {
        UUID uuid = context.getUuid();
        // Snapshot before the divorce - the record is gone once it succeeds.
        MarriageInfo snapshot = MarriageInfo.of(MarriageManager.getRecord(uuid));

        MarriageManager.DivorceResult result = MarriageManager.divorce(uuid);
        switch (result) {
            case NOT_MARRIED, CANCELLED -> DIVORCE_ARMED.remove(uuid);
            case CONFIRM_NEEDED -> DIVORCE_ARMED.put(uuid, Boolean.TRUE);
            case DIVORCED -> {
                DIVORCE_ARMED.remove(uuid);
                if (snapshot != null) {
                    MarriageUtil.finishDivorce(context.getPlayerRef(), snapshot);
                }
            }
        }
    }

    private static boolean isDivorceArmed(@Nonnull UUID uuid) {
        return DIVORCE_ARMED.getOrDefault(uuid, Boolean.FALSE);
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

        int index = 0;
        for (MarriageInfo marriage : marriages) {
            boolean oneOnline = Universe.get().getPlayer(marriage.getPlayer1Uuid()) != null;
            boolean twoOnline = Universe.get().getPlayer(marriage.getPlayer2Uuid()) != null;
            int onlineCount = (oneOnline ? 1 : 0) + (twoOnline ? 1 : 0);

            commandBuilder.append("#CoupleList", MarriageUIPage.COUPLE_ROW_DOC);
            String row = "#CoupleList[" + index + "]";

            // "&" rather than a heart glyph - Hytale's font coverage for U+2665 is
            // unverified, and a missing glyph would render as a tofu box.
            commandBuilder.set(row + " #Names.Text", marriage.getPlayer1Name() + " & " + marriage.getPlayer2Name());
            commandBuilder.set(row + " #Duration.TextSpans", Message.translation(LANG + "couples.row.duration")
                    .param("duration", MarriageUtil.formatDuration(marriage.getMarriedForMillis())));
            commandBuilder.set(row + " #Status.TextSpans", Message.translation(LANG + "couples.row.online")
                    .param("online", String.valueOf(onlineCount)));

            index++;
        }
    }

    private static void appendStat(@Nonnull UICommandBuilder commandBuilder, int index, @Nonnull String captionKey, int value) {
        commandBuilder.append("#StatsRow", MarriageUIPage.STAT_ROW_DOC);
        String cell = "#StatsRow[" + index + "]";
        commandBuilder.set(cell + " #Value.Text", String.valueOf(value));
        commandBuilder.set(cell + " #Caption.TextSpans", Message.translation(captionKey));
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
