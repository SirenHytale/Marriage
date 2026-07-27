package plugin.siren.API.CustomUI;

/**
 * How a {@link MarriageUICategory} renders its content.
 *
 * <p>The layout decides which region of the page the category writes into, so a
 * category never has to know anything about the page markup. Modelled on
 * Mermaids' {@code CreatureUILayout}.</p>
 */
public enum MarriageUILayout {
    /**
     * A wrapping grid of tiles - for content that is a set of choices to pick one
     * of. Not used by any built-in Marriage category, but kept so a third-party
     * category can use it.
     */
    GRID,

    /**
     * Full-width rows, one per option. Options whose
     * {@link MarriageUIOption#isToggle()} is true render as a checkbox row, the
     * rest as a labelled action button. Used by the Settings category.
     */
    ROWS,

    /**
     * A hand-authored {@code .ui} document appended verbatim, decorated by the
     * category's own {@link MarriageUICategory.DocumentRenderer}. Used by the
     * Overview and Couples categories, whose content (info lines, pending
     * requests, the couples list) does not fit the options model.
     */
    DOCUMENT
}
