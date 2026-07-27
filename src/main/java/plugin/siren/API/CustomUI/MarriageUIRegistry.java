package plugin.siren.API.CustomUI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The open registry backing the Marriage menu (/marriage). A third-party mod can
 * add its own page by registering a {@link MarriageUICategory} here during its own
 * plugin setup - declare Marriage in your {@code OptionalDependencies} so the
 * classloader bridge exists, and register after Marriage has loaded.
 *
 * <p>Registration is not thread-safe and is not meant to be: do it once at boot,
 * from the setup thread, like every other asset registry in the server.</p>
 *
 * <p>Modelled on Mermaids' {@code CreatureUIRegistry}, simplified: Marriage has one
 * menu rather than one per creature, so this is a flat id -> category map rather
 * than a creature-keyed one.</p>
 */
public final class MarriageUIRegistry {
    /** Built-in category ids, so a mod can position relative to them or override one. */
    public static final String CATEGORY_OVERVIEW = "overview";
    public static final String CATEGORY_COUPLES = "couples";
    public static final String CATEGORY_SETTINGS = "settings";
    public static final String CATEGORY_ADMIN = "admin";

    private static final Map<String, MarriageUICategory> CATEGORIES = new LinkedHashMap<>();

    private MarriageUIRegistry() {
    }

    /**
     * Adds a category to the menu. Registering an id that already exists replaces
     * it, which is how a mod re-themes a built-in category rather than adding
     * beside it.
     */
    public static void registerCategory(@Nonnull MarriageUICategory category) {
        CATEGORIES.put(category.getId(), category);
    }

    @Nullable
    public static MarriageUICategory getCategory(@Nonnull String categoryId) {
        return CATEGORIES.get(categoryId);
    }

    /**
     * Every registered category, sorted by {@link MarriageUICategory#getSortOrder()}.
     * Built-in categories are spaced 100 apart so a mod can slot in between two of
     * them without renumbering anything. Includes categories that are not visible
     * to the current player - callers filter with
     * {@link MarriageUICategory#isVisible} themselves, since visibility is
     * per-player and this list is not.
     */
    @Nonnull
    public static List<MarriageUICategory> getCategories() {
        List<MarriageUICategory> categories = new ArrayList<>(CATEGORIES.values());
        categories.sort((left, right) -> Integer.compare(left.getSortOrder(), right.getSortOrder()));
        return categories;
    }
}
