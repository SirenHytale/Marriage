package plugin.siren.Utils.UI;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Utils.LoveLetter;
import plugin.siren.Utils.MarriageUtil;
import plugin.siren.Utils.Registry.MarriageManager;

import javax.annotation.Nonnull;
import java.awt.Color;

/**
 * Writing a Love Letter, and reading one.
 *
 * <p>One page for both, because they are the same panel with different halves
 * shown — see {@code MarriageLetterUIPage.ui}. Which half appears is decided by
 * whether the letter in the player's hand has words on it yet.</p>
 *
 * <h2>The TextField rules this page obeys</h2>
 *
 * <ul>
 *   <li>Content is read and written through {@code .Value}. A {@code .Text} push
 *       at a TextField silently does nothing, and the binding lint rejects it.</li>
 *   <li>{@code .Value} is set on every build even when empty — a TextField that
 *       has never been given one renders but refuses to accept typing.</li>
 *   <li>The submit binding is {@code Activating} on the button, carrying the
 *       field's value as an {@code '@'}-prefixed selector so the client resolves
 *       it to whatever has been typed rather than echoing the selector back.</li>
 * </ul>
 *
 * <h2>Why the stack is re-read on submit</h2>
 *
 * <p>The page holds no reference to the letter it opened. Between opening and
 * sealing, the player can have moved it, dropped it, or swapped hotbar slots, so
 * the active slot is read again and re-checked at submit time. A letter that is
 * no longer there simply does not get written.</p>
 */
public class MarriageLetterUIPage extends InteractiveCustomUIPage<MarriageLetterUIPage.LetterEventData> {

    private static final String PAGE = "Pages/SirensMarriage/MarriageLetterUIPage.ui";
    private static final String LANG = "server.customUI.marriage.letterui.";

    /**
     * What this page is composing.
     *
     * <p>{@link #NOTE} reuses the letter's write half wholesale, because a love
     * note is the same act with a different destination — type words, press the
     * button. It needs no markup of its own, only different wording and a
     * different thing done with the text on submit.</p>
     */
    public enum Mode {
        /** A Love Letter: written onto the item in hand, to be gifted and kept. */
        LETTER,
        /** A love note: sent to the partner now, and gone. */
        NOTE
    }

    private final Mode mode;

    public MarriageLetterUIPage(@Nonnull PlayerRef playerRef) {
        this(playerRef, Mode.LETTER);
    }

    public MarriageLetterUIPage(@Nonnull PlayerRef playerRef, @Nonnull Mode mode) {
        super(playerRef, CustomPageLifetime.CanDismiss, LetterEventData.CODEC);
        this.mode = mode;
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            @Nonnull Store<EntityStore> store
    ) {
        commandBuilder.append(PAGE);
        commandBuilder.set("#CloseButton.TextSpans", Message.translation(LANG + "close"));

        if (this.mode == Mode.NOTE) {
            renderNote(commandBuilder, eventBuilder);
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton",
                    EventData.of(LetterEventData.KEY_ACTION, LetterEventData.ACTION_CLOSE));
            return;
        }

        ItemStack held = heldLetter(store, ref);
        LoveLetter written = LoveLetter.read(held);

        if (held == null) {
            // They opened the page and then lost the letter - say so rather than
            // showing an empty panel that does nothing when pressed.
            commandBuilder.set("#LetterTitle.TextSpans", Message.translation(LANG + "gone"));
            commandBuilder.set("#WriteBlock.Visible", false);
            commandBuilder.set("#ReadBlock.Visible", false);
            commandBuilder.set("#SealButton.Visible", false);
        } else if (written != null) {
            renderRead(written, commandBuilder);
        } else {
            renderWrite(commandBuilder, eventBuilder);
        }

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton",
                EventData.of(LetterEventData.KEY_ACTION, LetterEventData.ACTION_CLOSE));
    }

    private void renderWrite(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
        commandBuilder.set("#WriteBlock.Visible", true);
        commandBuilder.set("#ReadBlock.Visible", false);
        commandBuilder.set("#SealButton.Visible", true);

        commandBuilder.set("#LetterTitle.TextSpans", Message.translation(LANG + "write.title"));
        commandBuilder.set("#WriteHint.TextSpans", Message.translation(LANG + "write.hint"));
        commandBuilder.set("#SealButton.TextSpans", Message.translation(LANG + "write.seal"));

        // Set even though it is empty: a TextField that has never been given a
        // .Value renders but will not accept typing.
        commandBuilder.set("#Input.Value", "");
        commandBuilder.set("#Input.MaxLength", Marriage.getConfig().get().getLoveLetterMaxLength());

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SealButton",
                new EventData()
                        .append(LetterEventData.KEY_ACTION, LetterEventData.ACTION_SEAL)
                        .append(LetterEventData.KEY_TEXT, "#Input.Value"));
    }

    /**
     * Note mode: the letter's write half, worded for a message that is sent
     * rather than sealed.
     */
    private void renderNote(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
        commandBuilder.set("#WriteBlock.Visible", true);
        commandBuilder.set("#ReadBlock.Visible", false);
        commandBuilder.set("#SealButton.Visible", true);

        String partnerName = MarriageManager.getPartnerName(this.playerRef.getUuid());
        commandBuilder.set("#LetterTitle.TextSpans", Message.translation(LANG + "note.title"));
        commandBuilder.set("#WriteHint.TextSpans", Message.translation(LANG + "note.hint")
                .param("partnerUsername", MarriageUtil.displayName(partnerName)));
        commandBuilder.set("#SealButton.TextSpans", Message.translation(LANG + "note.send"));

        // Set even though it is empty: a TextField that has never been given a
        // .Value renders but will not accept typing.
        commandBuilder.set("#Input.Value", "");
        commandBuilder.set("#Input.MaxLength", Marriage.getConfig().get().getLoveLetterMaxLength());

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SealButton",
                new EventData()
                        .append(LetterEventData.KEY_ACTION, LetterEventData.ACTION_SEND)
                        .append(LetterEventData.KEY_TEXT, "#Input.Value"));
    }

    private void renderRead(@Nonnull LoveLetter letter, @Nonnull UICommandBuilder commandBuilder) {
        commandBuilder.set("#WriteBlock.Visible", false);
        commandBuilder.set("#ReadBlock.Visible", true);
        // Hidden rather than disabled: a sealed letter cannot be rewritten, so a
        // greyed-out Seal would only invite the click it refuses.
        commandBuilder.set("#SealButton.Visible", false);

        commandBuilder.set("#LetterTitle.TextSpans", Message.translation(LANG + "read.title")
                .param("username", letter.getAuthor()));
        // .Text, not .TextSpans: this is the player's own typed prose, which must
        // be rendered as the literal string they wrote rather than run through
        // translation-key resolution.
        commandBuilder.set("#ReadText.Text", letter.getText());
        commandBuilder.set("#ReadMeta.TextSpans", Message.translation(LANG + "read.meta")
                .param("username", letter.getAuthor())
                .param("ago", MarriageUtil.formatDuration(
                        Math.max(0L, System.currentTimeMillis() - letter.getWrittenAtMillis()))));
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull LetterEventData data
    ) {
        if (LetterEventData.ACTION_SEAL.equals(data.action)) {
            seal(store, ref, data.text);
        } else if (LetterEventData.ACTION_SEND.equals(data.action)) {
            // Trimmed and length-capped exactly as a sealed letter is - the field's
            // MaxLength should already have stopped it, and a client that sends
            // more anyway is not trusted.
            String text = data.text == null ? "" : data.text.strip();
            int max = Marriage.getConfig().get().getLoveLetterMaxLength();
            if (text.length() > max) {
                text = text.substring(0, max);
            }
            MarriageUtil.sendLoveNote(this.playerRef, text);
        }

        // Page.None is how the engine closes a custom page - there is no
        // closeCustomPage; vanilla's own ClearRespawnUI does exactly this.
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.getPageManager().setPage(ref, store, Page.None);
        }
    }

    /** Writes the typed words onto the letter in the player's hand. */
    private void seal(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, String text) {
        if (text == null || text.isBlank()) {
            this.playerRef.sendMessage(Message.translation(LANG + "write.empty"));
            return;
        }

        InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        if (hotbar == null) {
            return;
        }
        // Re-read rather than trusting what was in hand when the page opened.
        ItemStack held = hotbar.getActiveItem();
        if (!LoveLetter.isLetter(held) || LoveLetter.read(held) != null) {
            this.playerRef.sendMessage(Message.translation(LANG + "gone"));
            return;
        }

        int max = Marriage.getConfig().get().getLoveLetterMaxLength();
        String trimmed = text.strip();
        if (trimmed.length() > max) {
            // The field's MaxLength should already have stopped this; a client that
            // sends more anyway gets trimmed rather than trusted.
            trimmed = trimmed.substring(0, max);
        }

        hotbar.getInventory().setItemStackForSlot(hotbar.getActiveSlot(),
                LoveLetter.write(held, this.playerRef.getUsername(), trimmed));

        this.playerRef.sendMessage(Message.translation(LANG + "write.sealed").color(Color.PINK));
    }

    /** The Love Letter in the player's active hotbar slot, or null. */
    private static ItemStack heldLetter(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref) {
        InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        ItemStack held = hotbar == null ? null : hotbar.getActiveItem();
        return LoveLetter.isLetter(held) ? held : null;
    }

    public static class LetterEventData {
        static final String KEY_ACTION = "Action";
        /** '@'-prefixed: selector-resolved, so the client sends the typed text rather than the selector. */
        static final String KEY_TEXT = "@Text";

        static final String ACTION_SEAL = "seal";
        static final String ACTION_SEND = "send";
        static final String ACTION_CLOSE = "close";

        public static final BuilderCodec<LetterEventData> CODEC =
                BuilderCodec.builder(LetterEventData.class, LetterEventData::new)
                        .addField(new KeyedCodec<>(KEY_ACTION, Codec.STRING),
                                (entry, value) -> entry.action = value, entry -> entry.action)
                        .addField(new KeyedCodec<>(KEY_TEXT, Codec.STRING),
                                (entry, value) -> entry.text = value, entry -> entry.text)
                        .build();

        private String action;
        private String text;

        public LetterEventData() {
        }
    }
}
