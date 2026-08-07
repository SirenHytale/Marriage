package plugin.siren.Utils.UI;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * A small plate showing your partner's health and how far off they are, while
 * they are near enough to matter.
 *
 * <h2>Why the meter is a vanilla ProgressBar</h2>
 *
 * <p>{@code HudComponent} is a closed enum — a mod cannot add a meter to the
 * vanilla HUD — so anything of this shape has to be a {@link CustomUIHud} with
 * the vanilla {@code ProgressBar} element inside it, driven by a plain 0..1
 * {@code .Value} from Java. That is the same construction Cultivation's ritual
 * meter and Mermaids' drying-out meter use.</p>
 *
 * <h2>Shown and hidden, not just refreshed</h2>
 *
 * <p>The plate is added when the partner comes into range and removed when they
 * leave it, rather than being left on screen empty. {@code addCustomHud} is
 * idempotent per key, but re-adding re-appends the document, so
 * {@link #isShown} is checked first — otherwise a couple standing together would
 * stack a new copy of the plate every presence tick.</p>
 */
public class PartnerHud extends CustomUIHud {

    public static final String KEY = "plugin.siren:marriage_partner_hud";
    private static final String DOCUMENT_PATH = "Hud/Marriage/PartnerHud.ui";
    private static final String LANG = "server.customUI.marriage.hud.";

    public PartnerHud(@Nonnull PlayerRef playerRef){
        super(playerRef, KEY);
    }

    @Override
    protected void build(@Nonnull UICommandBuilder commandBuilder){
        commandBuilder.append(DOCUMENT_PATH);
    }

    /**
     * Pushes the partner's name, health fraction and distance.
     *
     * @param healthFraction 0..1, already clamped by the caller.
     * @param distance       blocks between the two of them.
     */
    public void refresh(@Nonnull String partnerName, float healthFraction, double distance){
        UICommandBuilder commandBuilder = new UICommandBuilder();

        commandBuilder.set("#PartnerHud #PartnerName.Text", partnerName);
        commandBuilder.set("#PartnerHud #PartnerHealthBar.Value", healthFraction);
        commandBuilder.set("#PartnerHud #PartnerDistance.TextSpans",
                Message.translation(LANG + "distance").param("blocks", String.valueOf(Math.round(distance))));

        // clear=false: only .set() calls against the document build() already
        // appended. Clearing would drop the document and leave an empty plate.
        this.update(false, commandBuilder);
    }

    // === show / hide ===

    /** Whether this player currently has the plate on screen. */
    public static boolean isShown(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref){
        Player player = store.getComponent(ref, Player.getComponentType());
        return player != null && player.getHudManager().getCustomHud(KEY) != null;
    }

    /**
     * The player's plate, adding it first if they do not have one.
     * Null when their Player component is unavailable.
     */
    public static PartnerHud showOrGet(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                                       @Nonnull PlayerRef playerRef){
        Player player = store.getComponent(ref, Player.getComponentType());
        if(player == null){
            return null;
        }

        CustomUIHud existing = player.getHudManager().getCustomHud(KEY);
        if(existing instanceof PartnerHud hud){
            return hud;
        }

        PartnerHud hud = new PartnerHud(playerRef);
        player.getHudManager().addCustomHud(playerRef, hud);
        return hud;
    }

    /** Takes the plate off screen. A no-op when it is not there. */
    public static void remove(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                              @Nonnull PlayerRef playerRef){
        Player player = store.getComponent(ref, Player.getComponentType());
        if(player != null && player.getHudManager().getCustomHud(KEY) != null){
            player.getHudManager().removeCustomHud(playerRef, KEY);
        }
    }
}
