package plugin.siren.API.CustomUI;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * One entry in the Marriage menu's left-hand list, and the content it shows.
 * Modelled on Mermaids' {@code CreatureUICategory}, with two additions this menu
 * needed that Mermaids' single-creature-per-page model did not:
 *
 * <ul>
 *   <li>{@link #isVisible} - a category-level gate (the Admin category only
 *       exists for players with the admin permission), evaluated fresh on every
 *       build and every click so a revoked permission takes effect immediately.</li>
 *   <li>{@link #isPageOpener} - a category that redirects to a completely
 *       different page (the Admin config editor) instead of rendering inline.
 *       Clicking it never marks it "selected"; it is dispatched before the normal
 *       category-select handling, exactly like the Mythical Creatures hub's
 *       {@code menu} cards.</li>
 * </ul>
 */
public final class MarriageUICategory {
    /** Produces a category's content for one player, at the moment it is opened. */
    public interface Provider extends Function<MarriageUIContext, List<MarriageUISection>> {
    }

    /**
     * Fills in a {@link MarriageUILayout#DOCUMENT} category's dynamic content
     * after its static {@code .ui} fragment has been appended into
     * {@code #CategoryDocument}. Given the event builder too (unlike Mermaids'
     * page-level {@code decorateDocument} hook) because Marriage's document
     * categories are interactive - pending-request accept/decline, the
     * teleport/gift/divorce actions.
     */
    public interface DocumentRenderer {
        void render(
                @Nonnull MarriageUIContext context,
                @Nonnull UICommandBuilder commandBuilder,
                @Nonnull UIEventBuilder eventBuilder
        );
    }

    private final String id;
    private final Message name;
    private final Message hint;
    private final MarriageUILayout layout;
    private final String document;
    private final DocumentRenderer documentRenderer;
    private final Provider provider;
    private final int sortOrder;
    private final Predicate<MarriageUIContext> visible;
    private final Consumer<MarriageUIContext> pageOpener;

    private MarriageUICategory(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.hint = builder.hint;
        this.layout = builder.layout;
        this.document = builder.document;
        this.documentRenderer = builder.documentRenderer;
        this.provider = builder.provider;
        this.sortOrder = builder.sortOrder;
        this.visible = builder.visible;
        this.pageOpener = builder.pageOpener;
    }

    @Nonnull
    public static Builder builder(@Nonnull String id) {
        return new Builder(id);
    }

    @Nonnull
    public String getId() {
        return this.id;
    }

    @Nonnull
    public Message getName() {
        return this.name;
    }

    /** Optional one-liner shown under the category heading. */
    @Nullable
    public Message getHint() {
        return this.hint;
    }

    @Nonnull
    public MarriageUILayout getLayout() {
        return this.layout;
    }

    /** The {@code .ui} path appended for {@link MarriageUILayout#DOCUMENT}. */
    @Nullable
    public String getDocument() {
        return this.document;
    }

    /**
     * Where this category sits in the list. Built-in categories are spaced 100
     * apart so a mod can slot in between two of them without renumbering.
     */
    public int getSortOrder() {
        return this.sortOrder;
    }

    /** Whether this category exists for this player at all - re-checked on every build and click. */
    public boolean isVisible(@Nonnull MarriageUIContext context) {
        return this.visible == null || this.visible.test(context);
    }

    /** True for a category that redirects to another page instead of rendering inline. */
    public boolean isPageOpener() {
        return this.pageOpener != null;
    }

    /** Runs the redirect. Callers should re-check {@link #isVisible} immediately before calling this. */
    public void openPage(@Nonnull MarriageUIContext context) {
        if (this.pageOpener != null) {
            this.pageOpener.accept(context);
        }
    }

    /** Fills in a DOCUMENT category's dynamic content; a no-op if none was registered. */
    public void decorate(
            @Nonnull MarriageUIContext context,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        if (this.documentRenderer != null) {
            this.documentRenderer.render(context, commandBuilder, eventBuilder);
        }
    }

    /**
     * The sections to render for this player. Never returns null, and drops
     * sections that ended up with no visible options so an empty family does not
     * leave a dangling caption behind. Also used by DOCUMENT categories that
     * register {@link #options} purely for click dispatch (their content is drawn
     * by hand in {@link #decorate}, but the click still resolves through this).
     */
    @Nonnull
    public List<MarriageUISection> getSections(@Nonnull MarriageUIContext context) {
        if (this.provider == null) {
            return Collections.emptyList();
        }

        List<MarriageUISection> produced = this.provider.apply(context);
        if (produced == null) {
            return Collections.emptyList();
        }

        List<MarriageUISection> populated = new ArrayList<>();
        for (MarriageUISection section : produced) {
            if (section != null && !section.getVisibleOptions(context).isEmpty()) {
                populated.add(section);
            }
        }
        return populated;
    }

    public static final class Builder {
        private final String id;
        private Message name;
        private Message hint;
        private MarriageUILayout layout = MarriageUILayout.GRID;
        private String document;
        private DocumentRenderer documentRenderer;
        private Provider provider;
        private int sortOrder = 1000;
        private Predicate<MarriageUIContext> visible;
        private Consumer<MarriageUIContext> pageOpener;

        private Builder(@Nonnull String id) {
            this.id = id;
            this.name = Message.raw(id);
        }

        @Nonnull
        public Builder name(@Nonnull String translationKey) {
            this.name = Message.translation(translationKey);
            return this;
        }

        @Nonnull
        public Builder name(@Nonnull Message name) {
            this.name = name;
            return this;
        }

        @Nonnull
        public Builder hint(@Nonnull String translationKey) {
            this.hint = Message.translation(translationKey);
            return this;
        }

        @Nonnull
        public Builder hint(@Nonnull Message hint) {
            this.hint = hint;
            return this;
        }

        @Nonnull
        public Builder layout(@Nonnull MarriageUILayout layout) {
            this.layout = layout;
            return this;
        }

        /** Sets {@link MarriageUILayout#DOCUMENT} with a purely static document. */
        @Nonnull
        public Builder document(@Nonnull String uiPath) {
            this.layout = MarriageUILayout.DOCUMENT;
            this.document = uiPath;
            return this;
        }

        /** Sets {@link MarriageUILayout#DOCUMENT} with a document plus a dynamic-content hook. */
        @Nonnull
        public Builder document(@Nonnull String uiPath, @Nonnull DocumentRenderer renderer) {
            this.layout = MarriageUILayout.DOCUMENT;
            this.document = uiPath;
            this.documentRenderer = renderer;
            return this;
        }

        @Nonnull
        public Builder sortOrder(int sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        /** Content as a single unlabelled grid/row list. */
        @Nonnull
        public Builder options(@Nonnull Function<MarriageUIContext, List<MarriageUIOption>> options) {
            this.provider = context -> Collections.singletonList(MarriageUISection.of(options.apply(context)));
            return this;
        }

        /** Content as captioned sections. */
        @Nonnull
        public Builder sections(@Nonnull Provider provider) {
            this.provider = provider;
            return this;
        }

        @Nonnull
        public Builder visible(@Nonnull Predicate<MarriageUIContext> visible) {
            this.visible = visible;
            return this;
        }

        /** Marks this category as a redirect: clicking it opens another page instead of selecting it. */
        @Nonnull
        public Builder opensPage(@Nonnull Consumer<MarriageUIContext> pageOpener) {
            this.pageOpener = pageOpener;
            return this;
        }

        @Nonnull
        public MarriageUICategory build() {
            return new MarriageUICategory(this);
        }
    }
}
