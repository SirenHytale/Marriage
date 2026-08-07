package plugin.siren.Utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.item.config.metadata.ItemDisplayMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * What is written inside a Love Letter, carried on the item stack itself.
 *
 * <h2>Where the words live</h2>
 *
 * <p>In the stack's own metadata, under {@link #KEY}, via
 * {@link ItemStack#withMetadata}. That is what makes a letter a real object: it
 * survives being dropped, stored in a chest, handed over by
 * {@code GiftManager}, and the server restarting, because item metadata is part
 * of the stack the engine already persists. Nothing here needs a registry of its
 * own, and a letter can never be separated from the item that is the letter.</p>
 *
 * <p>Because {@code withMetadata} returns a copy, a sealed letter is a
 * <em>different stack</em> from the blank one - which also means blank letters
 * still stack with each other while written ones never merge with anything,
 * exactly as you would want.</p>
 */
public final class LoveLetter {

    /** Metadata key on the item stack. Namespaced so it cannot collide with vanilla's. */
    public static final String KEY = "MarriageLoveLetter";

    public static final String ITEM_ID = "Marriage_LoveLetter";

    public static final BuilderCodec<LoveLetter> CODEC = BuilderCodec.builder(LoveLetter.class, LoveLetter::new)
            .append(new KeyedCodec<String>("Author", Codec.STRING),
                    (letter, value) -> letter.author = value,
                    (letter) -> letter.author)
            .add()
            .append(new KeyedCodec<String>("Text", Codec.STRING),
                    (letter, value) -> letter.text = value,
                    (letter) -> letter.text)
            .add()
            .append(new KeyedCodec<Long>("WrittenAtMillis", Codec.LONG),
                    (letter, value) -> letter.writtenAtMillis = value,
                    (letter) -> letter.writtenAtMillis)
            .add()
            .build();

    private static final KeyedCodec<LoveLetter> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    private String author = "";
    private String text = "";
    private long writtenAtMillis;

    public LoveLetter(){}

    public LoveLetter(@Nonnull String author, @Nonnull String text, long writtenAtMillis){
        this.author = author;
        this.text = text;
        this.writtenAtMillis = writtenAtMillis;
    }

    @Nonnull
    public String getAuthor(){
        return this.author == null ? "" : this.author;
    }

    @Nonnull
    public String getText(){
        return this.text == null ? "" : this.text;
    }

    public long getWrittenAtMillis(){
        return this.writtenAtMillis;
    }

    // === stack plumbing ===

    /** Whether this stack is a Love Letter item at all, written or blank. */
    public static boolean isLetter(@Nullable ItemStack stack){
        return stack != null && ITEM_ID.equalsIgnoreCase(stack.getItemId());
    }

    /** The letter written on a stack, or null if it is blank (or not a letter). */
    @Nullable
    public static LoveLetter read(@Nullable ItemStack stack){
        if(!isLetter(stack)){
            return null;
        }
        LoveLetter letter = stack.getFromMetadataOrNull(KEYED_CODEC);
        return letter == null || letter.getText().isBlank() ? null : letter;
    }

    /**
     * A copy of this stack with the letter written on it, and its display name
     * changed to say who it is from - so a chest full of letters can be told
     * apart without opening any of them.
     */
    @Nonnull
    public static ItemStack write(@Nonnull ItemStack blank, @Nonnull String author, @Nonnull String text){
        LoveLetter letter = new LoveLetter(author, text, System.currentTimeMillis());

        ItemDisplayMetadata display = new ItemDisplayMetadata(
                Message.translation("server.items.MarriageLoveLetter.sealed.name").param("username", author),
                Message.translation("server.items.MarriageLoveLetter.sealed.description"));

        return blank
                .withMetadata(KEYED_CODEC, letter)
                .withMetadata(ItemDisplayMetadata.KEYED_CODEC, display);
    }
}
