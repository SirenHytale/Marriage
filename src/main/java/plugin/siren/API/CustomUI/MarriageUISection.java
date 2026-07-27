package plugin.siren.API.CustomUI;

import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A captioned group of options inside one category. Modelled on Mermaids'
 * {@code CreatureUISection}.
 *
 * <p>A section with no caption renders as a plain unlabelled grid or row list,
 * which is what Marriage's own categories want.</p>
 */
public final class MarriageUISection {
    private final Message caption;
    private final List<MarriageUIOption> options;

    public MarriageUISection(@Nullable Message caption, @Nonnull List<MarriageUIOption> options) {
        this.caption = caption;
        this.options = options;
    }

    @Nonnull
    public static MarriageUISection of(@Nonnull List<MarriageUIOption> options) {
        return new MarriageUISection(null, options);
    }

    @Nonnull
    public static MarriageUISection of(@Nonnull String captionKey, @Nonnull List<MarriageUIOption> options) {
        return new MarriageUISection(Message.translation(captionKey), options);
    }

    @Nullable
    public Message getCaption() {
        return this.caption;
    }

    @Nonnull
    public List<MarriageUIOption> getOptions() {
        return Collections.unmodifiableList(this.options);
    }

    /**
     * The options this player can actually see, in order. Called once per build so
     * a {@code visible} predicate that consults permissions or another mod's state
     * is evaluated fresh every time the menu opens.
     */
    @Nonnull
    public List<MarriageUIOption> getVisibleOptions(@Nonnull MarriageUIContext context) {
        List<MarriageUIOption> visible = new ArrayList<>();
        for (MarriageUIOption option : this.options) {
            if (option.isVisible(context)) {
                visible.add(option);
            }
        }
        return visible;
    }
}
