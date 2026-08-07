package plugin.siren.Utils.UI;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.API.CustomUI.MarriageUICategory;
import plugin.siren.API.CustomUI.MarriageUIContext;
import plugin.siren.API.CustomUI.MarriageUILayout;
import plugin.siren.API.CustomUI.MarriageUIOption;
import plugin.siren.API.CustomUI.MarriageUIRegistry;
import plugin.siren.API.CustomUI.MarriageUISection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The /marriage menu.
 *
 * <p>Adapted from Mermaids' {@code CreatureUIPage} engine, collapsed into a single
 * concrete class since Marriage has one menu rather than one per creature - there is
 * no per-subclass dispatch to keep abstract. Content comes from
 * {@link MarriageUIRegistry}, exactly the same public door a third-party mod would
 * use to add its own page.</p>
 *
 * <p>The same three details that are load-bearing in Mermaids' engine apply here:</p>
 * <ul>
 *   <li><b>Clicks carry ids, not indices</b> - a stale click from a menu that has
 *       since changed resolves to nothing rather than to the wrong thing.</li>
 *   <li><b>Selection state is drawn with {@code .Visible} and {@code .Disabled}</b> -
 *       rewriting a {@code TextButton}'s {@code .Style} at runtime disconnects the
 *       client.</li>
 *   <li><b>Color pushes are solid {@code "#RRGGBB"}</b> - the {@code (alpha)} suffix
 *       is a document-parser feature that does not survive the protocol.</li>
 * </ul>
 *
 * <p>One thing Mermaids' engine did not need: the Admin category redirects to a
 * whole different page ({@link MarriageAdminUIPage}) instead of rendering inline,
 * exactly like the Mythical Creatures hub's "menu" cards - see
 * {@link MarriageUICategory#isPageOpener()}.</p>
 */
public class MarriageUIPage extends InteractiveCustomUIPage<MarriageUIPage.MarriageUIEventData> {
    private static final String BASE = "Pages/SirensMarriage/";
    static final String PAGE = BASE + "MarriageUIPage.ui";
    static final String TILE_DOC = BASE + "Parts/OptionTile.ui";
    static final String CATEGORY_ENTRY_DOC = BASE + "Parts/CategoryEntry.ui";
    static final String CHECK_ROW_DOC = BASE + "Parts/CheckRow.ui";
    static final String ACTION_ROW_DOC = BASE + "Parts/ActionRow.ui";
    static final String INFO_ROW_DOC = BASE + "Parts/InfoRow.ui";
    static final String STAT_ROW_DOC = BASE + "Parts/StatRow.ui";
    static final String COUPLE_ROW_DOC = BASE + "Parts/CoupleRow.ui";
    static final String REQUEST_ROW_DOC = BASE + "Parts/RequestRow.ui";

    private static final int SECTION_SLOTS = 6;

    /** The one bright-pink accent, reserved for "this is your current selection" - must match @RPink in MarriageTheme.ui. */
    static final int RING_COLOR = 0xFF5C9C;

    private String selectedCategoryId;

    public MarriageUIPage(@Nonnull PlayerRef playerRef) {
        this(playerRef, null);
    }

    public MarriageUIPage(@Nonnull PlayerRef playerRef, @Nullable String categoryId) {
        super(playerRef, CustomPageLifetime.CanDismiss, MarriageUIEventData.CODEC);
        this.selectedCategoryId = categoryId;
    }

    // --- Lifecycle -------------------------------------------------------------
    // No onDismiss override - there is no parent hub to return to, so the default
    // (a no-op, per CustomUIPage) is exactly right: dismissing just goes back to
    // the game.

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            @Nonnull Store<EntityStore> store
    ) {
        commandBuilder.append(PAGE);
        MarriageUIContext context = new MarriageUIContext(this.playerRef, ref, store);
        render(context, commandBuilder, eventBuilder);
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull MarriageUIEventData data
    ) {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        MarriageUIContext context = new MarriageUIContext(this.playerRef, ref, store);

        // Checked first, with an immediate return on success - exactly like the
        // Mythical Creatures hub's "menu" cards. A page-opener category never
        // becomes the selected category, so this must not fall into the same
        // branch as a normal category-select click.
        if (data.openCategory != null) {
            MarriageUICategory category = findCategory(context, data.openCategory);
            if (category != null && category.isPageOpener() && category.isVisible(context)) {
                category.openPage(context);
                return;
            }
            // Permission was revoked or the category vanished since the sidebar was
            // drawn - fall through to a normal rebuild so the sidebar catches up.
        } else if (data.category != null) {
            this.selectedCategoryId = data.category;
        } else if (data.option != null && data.optionCategory != null) {
            applyOption(context, data.optionCategory, data.option);
        } else if (data.requestAction != null && data.requestUuid != null) {
            MarriageUIDefaults.handleRequestAction(context, data.requestAction, data.requestUuid);
        } else if (data.coupleAction != null && data.coupleUuid != null) {
            MarriageUIDefaults.handleCoupleAction(context, data.coupleAction, data.coupleUuid);
        }

        // The page is redrawn wholesale after every click rather than patched - an
        // option can change what the sidebar or header say, and these menus are
        // small enough that a rebuild is cheap.
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.getPageManager().openCustomPage(ref, store, new MarriageUIPage(this.playerRef, this.selectedCategoryId));
            return;
        }

        render(context, commandBuilder, eventBuilder);
        this.sendUpdate(commandBuilder, eventBuilder, false);
    }

    // --- Category lookups --------------------------------------------------

    @Nonnull
    private List<MarriageUICategory> getVisibleCategories(@Nonnull MarriageUIContext context) {
        List<MarriageUICategory> visible = new ArrayList<>();
        for (MarriageUICategory category : MarriageUIRegistry.getCategories()) {
            if (category.isVisible(context)) {
                visible.add(category);
            }
        }
        return visible;
    }

    @Nullable
    private MarriageUICategory findCategory(@Nonnull MarriageUIContext context, @Nonnull String id) {
        for (MarriageUICategory category : getVisibleCategories(context)) {
            if (category.getId().equals(id)) {
                return category;
            }
        }
        return null;
    }

    /**
     * The category to show. Keeps the one the player was on when it is still in
     * the (visible, non-redirecting) list, and otherwise falls to the first
     * selectable one. A page-opener category (Admin) is never selectable - it
     * always redirects instead, so it must never come back as "the open category".
     */
    @Nullable
    private MarriageUICategory resolveSelectedCategory(@Nonnull List<MarriageUICategory> categories) {
        List<MarriageUICategory> selectable = new ArrayList<>();
        for (MarriageUICategory category : categories) {
            if (!category.isPageOpener()) {
                selectable.add(category);
            }
        }
        if (selectable.isEmpty()) {
            return null;
        }

        if (this.selectedCategoryId != null) {
            for (MarriageUICategory category : selectable) {
                if (category.getId().equals(this.selectedCategoryId)) {
                    return category;
                }
            }
        }

        return selectable.get(0);
    }

    // --- Dispatch ------------------------------------------------------------

    /**
     * Runs the clicked option, if it is still there. Both the category and the
     * option are re-resolved from the registry rather than trusted, since the
     * click was sent against the menu as it looked a moment ago.
     */
    private void applyOption(@Nonnull MarriageUIContext context, @Nonnull String categoryId, @Nonnull String optionId) {
        MarriageUICategory category = findCategory(context, categoryId);
        if (category == null) {
            return;
        }

        for (MarriageUISection section : category.getSections(context)) {
            for (MarriageUIOption option : section.getVisibleOptions(context)) {
                if (!option.getId().equals(optionId)) {
                    continue;
                }

                if (!option.isEnabled(context)) {
                    Message reason = option.getDisabledReason(context);
                    if (reason != null) {
                        context.getPlayerRef().sendMessage(reason);
                    }
                    return;
                }

                option.apply(context);
                return;
            }
        }
    }

    // --- Rendering -------------------------------------------------------------

    private void render(
            @Nonnull MarriageUIContext context,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        List<MarriageUICategory> categories = getVisibleCategories(context);
        MarriageUICategory selected = resolveSelectedCategory(categories);
        this.selectedCategoryId = selected == null ? null : selected.getId();

        renderCategoryList(categories, selected, commandBuilder, eventBuilder);
        renderContent(context, selected, commandBuilder, eventBuilder);
    }

    private void renderCategoryList(
            @Nonnull List<MarriageUICategory> categories,
            @Nullable MarriageUICategory selected,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        boolean showSidebar = categories.size() > 1;
        commandBuilder.set("#CategoryListPanel.Visible", showSidebar);
        commandBuilder.set("#CategoryListGap.Visible", showSidebar);
        commandBuilder.clear("#CategoryList");

        if (!showSidebar) {
            return;
        }

        for (int index = 0; index < categories.size(); index++) {
            MarriageUICategory category = categories.get(index);
            boolean isSelected = !category.isPageOpener() && selected != null && selected.getId().equals(category.getId());

            commandBuilder.append("#CategoryList", CATEGORY_ENTRY_DOC);
            String entry = "#CategoryList[" + index + "]";

            commandBuilder.set(entry + " #Button.TextSpans", category.getName());
            commandBuilder.set(entry + " #Button.Disabled", isSelected);
            commandBuilder.set(entry + " #Spine.Visible", isSelected);

            if (category.isPageOpener()) {
                eventBuilder.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        entry + " #Button",
                        EventData.of(MarriageUIEventData.KEY_OPEN_CATEGORY, category.getId()));
            } else if (!isSelected) {
                eventBuilder.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        entry + " #Button",
                        EventData.of(MarriageUIEventData.KEY_CATEGORY, category.getId()));
            }
        }
    }

    private void renderContent(
            @Nonnull MarriageUIContext context,
            @Nullable MarriageUICategory category,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        for (int slot = 0; slot < SECTION_SLOTS; slot++) {
            commandBuilder.set("#Sec" + slot + ".Visible", false);
            commandBuilder.clear("#SecGrid" + slot);
        }
        commandBuilder.set("#RowList.Visible", false);
        commandBuilder.clear("#RowList");
        commandBuilder.set("#CategoryDocument.Visible", false);
        commandBuilder.clear("#CategoryDocument");
        commandBuilder.set("#EmptyState.Visible", false);

        if (category == null) {
            commandBuilder.set("#CategoryTitle.TextSpans", Message.empty());
            commandBuilder.set("#CategoryHint.Visible", false);
            commandBuilder.set("#EmptyState.Visible", true);
            return;
        }

        commandBuilder.set("#CategoryTitle.TextSpans", category.getName());
        if (category.getHint() != null) {
            commandBuilder.set("#CategoryHint.TextSpans", category.getHint());
            commandBuilder.set("#CategoryHint.Visible", true);
        } else {
            commandBuilder.set("#CategoryHint.Visible", false);
        }

        if (category.getLayout() == MarriageUILayout.DOCUMENT) {
            String document = category.getDocument();
            if (document == null) {
                commandBuilder.set("#EmptyState.Visible", true);
                return;
            }

            commandBuilder.append("#CategoryDocument", document);
            commandBuilder.set("#CategoryDocument.Visible", true);
            category.decorate(context, commandBuilder, eventBuilder);
            return;
        }

        List<MarriageUISection> sections = category.getSections(context);
        if (sections.isEmpty()) {
            commandBuilder.set("#EmptyState.Visible", true);
            return;
        }

        if (category.getLayout() == MarriageUILayout.ROWS) {
            renderRows(context, category, sections, "#RowList", commandBuilder, eventBuilder);
        } else {
            renderGrid(context, category, sections, commandBuilder, eventBuilder);
        }
    }

    private void renderGrid(
            @Nonnull MarriageUIContext context,
            @Nonnull MarriageUICategory category,
            @Nonnull List<MarriageUISection> sections,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        int slot = 0;
        int tileIndex = 0;

        for (MarriageUISection section : sections) {
            String gridSelector = "#SecGrid" + slot;
            boolean firstSectionInSlot = tileIndex == 0;

            commandBuilder.set("#Sec" + slot + ".Visible", true);

            if (firstSectionInSlot) {
                if (section.getCaption() != null) {
                    commandBuilder.set("#SecLabel" + slot + ".TextSpans", section.getCaption());
                    commandBuilder.set("#SecLabel" + slot + ".Visible", true);
                } else {
                    commandBuilder.set("#SecLabel" + slot + ".Visible", false);
                }
            }

            for (MarriageUIOption option : section.getVisibleOptions(context)) {
                commandBuilder.append(gridSelector, TILE_DOC);
                renderTile(context, category, option, gridSelector + "[" + tileIndex + "]", commandBuilder, eventBuilder);
                tileIndex++;
            }

            if (slot < SECTION_SLOTS - 1) {
                slot++;
                tileIndex = 0;
            }
        }
    }

    private void renderTile(
            @Nonnull MarriageUIContext context,
            @Nonnull MarriageUICategory category,
            @Nonnull MarriageUIOption option,
            @Nonnull String tile,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        boolean selected = option.isSelected(context);
        boolean enabled = option.isEnabled(context);

        commandBuilder.set(tile + " #Label.TextSpans", option.getName());
        commandBuilder.set(tile + " #Ring.Visible", selected);
        commandBuilder.set(tile + " #Button.Disabled", selected);

        if (selected) {
            commandBuilder.setObject(tile + " #Ring.Background", solid(RING_COLOR));
        }

        Message detail = option.getDetail(context);
        if (!enabled) {
            Message reason = option.getDisabledReason(context);
            if (reason != null) {
                detail = reason;
            }
        }
        if (detail != null) {
            commandBuilder.set(tile + " #Detail.TextSpans", detail);
            commandBuilder.set(tile + " #Detail.Visible", true);
        }

        if (!selected) {
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, tile + " #Button", optionEvent(category.getId(), option));
        }
    }

    /**
     * Renders a category's options as full-width rows into {@code rowListSelector}
     * (normally {@code "#RowList"}, but also used by the Overview category's
     * DOCUMENT content to render its teleport/gift/divorce actions into its own
     * {@code #ActionList}). Package-private so {@link MarriageUIDefaults} can reuse
     * it rather than re-implementing row rendering by hand.
     */
    static void renderRows(
            @Nonnull MarriageUIContext context,
            @Nonnull MarriageUICategory category,
            @Nonnull List<MarriageUISection> sections,
            @Nonnull String rowListSelector,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        int rowIndex = 0;
        for (MarriageUISection section : sections) {
            for (MarriageUIOption option : section.getVisibleOptions(context)) {
                boolean toggle = option.isToggle();
                commandBuilder.append(rowListSelector, toggle ? CHECK_ROW_DOC : ACTION_ROW_DOC);
                String row = rowListSelector + "[" + rowIndex + "]";

                if (toggle) {
                    renderCheckRow(context, category, option, row, commandBuilder, eventBuilder);
                } else {
                    renderActionRow(context, category, option, row, commandBuilder, eventBuilder);
                }

                rowIndex++;
            }
        }
    }

    private static void renderCheckRow(
            @Nonnull MarriageUIContext context,
            @Nonnull MarriageUICategory category,
            @Nonnull MarriageUIOption option,
            @Nonnull String row,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        boolean enabled = option.isEnabled(context);

        commandBuilder.set(row + " #Label.TextSpans", option.getName());

        Message detail = option.getDetail(context);
        if (!enabled) {
            Message reason = option.getDisabledReason(context);
            if (reason != null) {
                detail = reason;
            }
        }
        if (detail != null) {
            commandBuilder.set(row + " #Detail.TextSpans", detail);
            commandBuilder.set(row + " #Detail.Visible", true);
        } else {
            commandBuilder.set(row + " #Detail.Visible", false);
        }

        commandBuilder.set(row + " #CheckBox.Value", option.isSelected(context));
        commandBuilder.set(row + " #CheckBox.Disabled", !enabled);

        if (enabled) {
            eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, row + " #CheckBox", optionEvent(category.getId(), option));
        }
    }

    /**
     * A themed action-button row: the button's own caption is the option's name
     * (unlike Mermaids' engine, which shows a generic "Apply" - Marriage's actions
     * are few and each names a distinct verb, so the button says it directly).
     * {@code #Detail} carries an optional blurb, replaced by the disabled reason.
     */
    static void renderActionRow(
            @Nonnull MarriageUIContext context,
            @Nonnull MarriageUICategory category,
            @Nonnull MarriageUIOption option,
            @Nonnull String row,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        boolean enabled = option.isEnabled(context);
        boolean destructive = isDestructive(option);

        Message detail = option.getDetail(context);
        if (!enabled) {
            Message reason = option.getDisabledReason(context);
            if (reason != null) {
                detail = reason;
            }
        }
        if (detail != null) {
            commandBuilder.set(row + " #Detail.TextSpans", detail);
            commandBuilder.set(row + " #Detail.Visible", true);
        } else {
            commandBuilder.set(row + " #Detail.Visible", false);
        }

        String shown = destructive ? " #Danger" : " #Button";
        String hidden = destructive ? " #Button" : " #Danger";

        commandBuilder.set(row + shown + ".Visible", true);
        commandBuilder.set(row + shown + ".TextSpans", option.getName());
        commandBuilder.set(row + shown + ".Disabled", !enabled);
        commandBuilder.set(row + hidden + ".Visible", false);

        if (enabled) {
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, row + shown, optionEvent(category.getId(), option));
        }
    }

    /** Actions that undo something get the destructive button - just divorce, today. */
    private static boolean isDestructive(@Nonnull MarriageUIOption option) {
        return option.getId().contains("divorce");
    }

    @Nonnull
    static EventData optionEvent(@Nonnull String categoryId, @Nonnull MarriageUIOption option) {
        return new EventData()
                .append(MarriageUIEventData.KEY_OPTION_CATEGORY, categoryId)
                .append(MarriageUIEventData.KEY_OPTION, option.getId());
    }

    // --- Color helpers -----------------------------------------------------

    /** A flat-color background for a Java-pushed element - solid "#RRGGBB" only. */
    @Nonnull
    static PatchStyle solid(int rgb) {
        return new PatchStyle().setColor(Value.of(String.format("#%06X", rgb & 0xFFFFFF)));
    }

    // --- Event payload -----------------------------------------------------

    public static class MarriageUIEventData {
        static final String KEY_CATEGORY = "Category";
        static final String KEY_OPEN_CATEGORY = "OpenCategory";
        static final String KEY_OPTION_CATEGORY = "OptionCategory";
        static final String KEY_OPTION = "Option";
        static final String KEY_REQUEST_ACTION = "RequestAction";
        static final String KEY_REQUEST_UUID = "RequestUuid";
        public static final String KEY_COUPLE_ACTION = "CoupleAction";
        public static final String KEY_COUPLE_UUID = "CoupleUuid";

        public static final BuilderCodec<MarriageUIEventData> CODEC =
                BuilderCodec.builder(MarriageUIEventData.class, MarriageUIEventData::new)
                        .addField(new KeyedCodec<>(KEY_CATEGORY, Codec.STRING),
                                (entry, value) -> entry.category = value, entry -> entry.category)
                        .addField(new KeyedCodec<>(KEY_OPEN_CATEGORY, Codec.STRING),
                                (entry, value) -> entry.openCategory = value, entry -> entry.openCategory)
                        .addField(new KeyedCodec<>(KEY_OPTION_CATEGORY, Codec.STRING),
                                (entry, value) -> entry.optionCategory = value, entry -> entry.optionCategory)
                        .addField(new KeyedCodec<>(KEY_OPTION, Codec.STRING),
                                (entry, value) -> entry.option = value, entry -> entry.option)
                        .addField(new KeyedCodec<>(KEY_REQUEST_ACTION, Codec.STRING),
                                (entry, value) -> entry.requestAction = value, entry -> entry.requestAction)
                        .addField(new KeyedCodec<>(KEY_REQUEST_UUID, Codec.STRING),
                                (entry, value) -> entry.requestUuid = value, entry -> entry.requestUuid)
                        .addField(new KeyedCodec<>(KEY_COUPLE_ACTION, Codec.STRING),
                                (entry, value) -> entry.coupleAction = value, entry -> entry.coupleAction)
                        .addField(new KeyedCodec<>(KEY_COUPLE_UUID, Codec.STRING),
                                (entry, value) -> entry.coupleUuid = value, entry -> entry.coupleUuid)
                        .build();

        private String category;
        private String openCategory;
        private String optionCategory;
        private String option;
        private String requestAction;
        private String requestUuid;
        private String coupleAction;
        private String coupleUuid;

        public MarriageUIEventData() {
        }
    }
}
