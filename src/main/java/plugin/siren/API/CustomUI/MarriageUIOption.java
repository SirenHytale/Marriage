package plugin.siren.API.CustomUI;

import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * One selectable or actionable thing inside a {@link MarriageUICategory} - a
 * settings toggle, an action button (teleport / gift / divorce), or whatever a
 * third-party mod wants to add. Modelled on Mermaids' {@code CreatureUIOption}.
 *
 * <p>Build one with {@link #builder(String)}. The only required parts are an id
 * and a name; everything else has a sensible default.</p>
 *
 * <p>Ids are namespaced strings ({@code "marriage:divorce"}, {@code "myMod:x"})
 * and are what travels in the click event, so adding or removing options never
 * renumbers anything.</p>
 */
public final class MarriageUIOption {
    private final String id;
    private final Message name;
    private final Function<MarriageUIContext, Message> detail;
    private final boolean toggle;
    private final Predicate<MarriageUIContext> selected;
    private final Predicate<MarriageUIContext> visible;
    private final Predicate<MarriageUIContext> enabled;
    private final Function<MarriageUIContext, Message> disabledReason;
    private final Consumer<MarriageUIContext> action;

    private MarriageUIOption(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.detail = builder.detail;
        this.toggle = builder.toggle;
        this.selected = builder.selected;
        this.visible = builder.visible;
        this.enabled = builder.enabled;
        this.disabledReason = builder.disabledReason;
        this.action = builder.action;
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

    /**
     * Optional second line - a short blurb, or (when disabled) overridden by the
     * reason. Resolved per player so an option whose blurb depends on transient
     * per-player state (e.g. "click again to confirm") can reflect it.
     */
    @Nullable
    public Message getDetail(@Nonnull MarriageUIContext context) {
        return this.detail == null ? null : this.detail.apply(context);
    }

    /** True for options that render as a checkbox row rather than a button row. */
    public boolean isToggle() {
        return this.toggle;
    }

    public boolean isSelected(@Nonnull MarriageUIContext context) {
        return this.selected != null && this.selected.test(context);
    }

    public boolean isVisible(@Nonnull MarriageUIContext context) {
        return this.visible == null || this.visible.test(context);
    }

    public boolean isEnabled(@Nonnull MarriageUIContext context) {
        return this.enabled == null || this.enabled.test(context);
    }

    /** Shown under the name when {@link #isEnabled} is false. */
    @Nullable
    public Message getDisabledReason(@Nonnull MarriageUIContext context) {
        return this.disabledReason == null ? null : this.disabledReason.apply(context);
    }

    /**
     * Runs the option. Called on the world thread that owns the player, with the
     * page about to be rebuilt - safe to mutate a settings component directly, but
     * never put a component on the Store from here.
     */
    public void apply(@Nonnull MarriageUIContext context) {
        if (this.action != null) {
            this.action.accept(context);
        }
    }

    public static final class Builder {
        private final String id;
        private Message name;
        private Function<MarriageUIContext, Message> detail;
        private boolean toggle;
        private Predicate<MarriageUIContext> selected;
        private Predicate<MarriageUIContext> visible;
        private Predicate<MarriageUIContext> enabled;
        private Function<MarriageUIContext, Message> disabledReason;
        private Consumer<MarriageUIContext> action;

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
        public Builder detail(@Nonnull String translationKey) {
            Message message = Message.translation(translationKey);
            this.detail = context -> message;
            return this;
        }

        @Nonnull
        public Builder detail(@Nonnull Message detail) {
            this.detail = context -> detail;
            return this;
        }

        /** A blurb chosen per player, for an option whose second line depends on transient state. */
        @Nonnull
        public Builder detail(@Nonnull Function<MarriageUIContext, Message> detail) {
            this.detail = detail;
            return this;
        }

        /** Renders this option as a checkbox row instead of a button row. */
        @Nonnull
        public Builder toggle(boolean toggle) {
            this.toggle = toggle;
            return this;
        }

        @Nonnull
        public Builder selected(@Nonnull Predicate<MarriageUIContext> selected) {
            this.selected = selected;
            return this;
        }

        @Nonnull
        public Builder visible(@Nonnull Predicate<MarriageUIContext> visible) {
            this.visible = visible;
            return this;
        }

        @Nonnull
        public Builder enabled(@Nonnull Predicate<MarriageUIContext> enabled) {
            this.enabled = enabled;
            return this;
        }

        @Nonnull
        public Builder disabledReason(@Nonnull String translationKey) {
            Message message = Message.translation(translationKey);
            this.disabledReason = context -> message;
            return this;
        }

        @Nonnull
        public Builder disabledReason(@Nonnull Function<MarriageUIContext, Message> disabledReason) {
            this.disabledReason = disabledReason;
            return this;
        }

        @Nonnull
        public Builder onSelect(@Nonnull Consumer<MarriageUIContext> action) {
            this.action = action;
            return this;
        }

        @Nonnull
        public MarriageUIOption build() {
            return new MarriageUIOption(this);
        }
    }
}
