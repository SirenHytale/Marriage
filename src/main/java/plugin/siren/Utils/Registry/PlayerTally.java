package plugin.siren.Utils.Registry;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

import javax.annotation.Nonnull;

/**
 * One player's lifetime marriage record, kept whether or not they are married
 * right now.
 *
 * <h2>Why this cannot live on the marriage record</h2>
 *
 * <p>The Records page asks questions a {@link MarriageRecord} structurally cannot
 * answer. "Most divorces" is a fact about somebody with no marriage at all — the
 * record that would have held it was deleted at the divorce. "Most gifts given"
 * should not reset because a player remarried. And "the longest marriage there
 * has ever been" outlives the marriage it describes by definition.</p>
 *
 * <p>{@link plugin.siren.Systems.MarriageSettingsComponent} already counts times
 * married and divorces per player, but only on the live entity — it cannot be
 * read for an offline player, let alone sorted across everyone. This is the
 * server-wide, offline-readable twin of those counters.</p>
 *
 * <p>Names are display caches refreshed on join, exactly like the marriage
 * record's; the UUID is the identity.</p>
 */
public class PlayerTally {

    public static final BuilderCodec<PlayerTally> CODEC = BuilderCodec.builder(PlayerTally.class, PlayerTally::new)
            .append(new KeyedCodec<String>("Uuid", Codec.STRING),
                    (tally, value) -> tally.uuid = value,
                    (tally) -> tally.uuid)
            .add()
            .append(new KeyedCodec<String>("Name", Codec.STRING),
                    (tally, value) -> tally.name = value,
                    (tally) -> tally.name)
            .add()
            .append(new KeyedCodec<Integer>("Marriages", Codec.INTEGER),
                    (tally, value) -> tally.marriages = value,
                    (tally) -> tally.marriages)
            .add()
            .append(new KeyedCodec<Integer>("Divorces", Codec.INTEGER),
                    (tally, value) -> tally.divorces = value,
                    (tally) -> tally.divorces)
            .add()
            .append(new KeyedCodec<Integer>("GiftsGiven", Codec.INTEGER),
                    (tally, value) -> tally.giftsGiven = value,
                    (tally) -> tally.giftsGiven)
            .add()
            .append(new KeyedCodec<Long>("LongestMarriageMillis", Codec.LONG),
                    (tally, value) -> tally.longestMarriageMillis = value,
                    (tally) -> tally.longestMarriageMillis)
            .add()
            .append(new KeyedCodec<ExPartner[]>("ExPartners", ArrayCodec.ofBuilderCodec(ExPartner.CODEC, ExPartner[]::new)),
                    (tally, value) -> tally.exPartners = value,
                    (tally) -> tally.exPartners)
            .add()
            .append(new KeyedCodec<Long>("RemarryCooldownUntilMillis", Codec.LONG),
                    (tally, value) -> tally.remarryCooldownUntilMillis = value,
                    (tally) -> tally.remarryCooldownUntilMillis)
            .add()
            .build();

    /** Newest first, and capped - a history nobody reads past is not worth the disk. */
    private static final int MAX_HISTORY = 10;

    private String uuid = "";
    private String name = "";
    private int marriages;
    private int divorces;
    private int giftsGiven;

    /** The longest a marriage of theirs has ever lasted, measured when it ended. */
    private long longestMarriageMillis;

    /** Marriages that ended, newest first. See {@link ExPartner}. */
    private ExPartner[] exPartners = new ExPartner[0];

    /** Epoch millis until which this player may not marry again. */
    private long remarryCooldownUntilMillis;

    public PlayerTally(){}

    public PlayerTally(@Nonnull String uuid, @Nonnull String name){
        this.uuid = uuid;
        this.name = name;
    }

    public String getUuid(){
        return this.uuid;
    }

    public String getName(){
        return this.name == null ? "" : this.name;
    }

    public void setName(@Nonnull String name){
        this.name = name;
    }

    public int getMarriages(){
        return this.marriages;
    }

    public void incrementMarriages(){
        this.marriages++;
    }

    public int getDivorces(){
        return this.divorces;
    }

    public void incrementDivorces(){
        this.divorces++;
    }

    public int getGiftsGiven(){
        return this.giftsGiven;
    }

    public void incrementGiftsGiven(){
        this.giftsGiven++;
    }

    public long getLongestMarriageMillis(){
        return this.longestMarriageMillis;
    }

    /** Records a finished marriage's duration if it beat their previous best. */
    public void recordMarriageLength(long millis){
        if(millis > this.longestMarriageMillis){
            this.longestMarriageMillis = millis;
        }
    }

    @Nonnull
    public ExPartner[] getExPartners(){
        return this.exPartners == null ? new ExPartner[0] : this.exPartners;
    }

    /** Pushes a finished marriage onto the front of the history, trimming the tail. */
    public void addExPartner(@Nonnull ExPartner ex){
        ExPartner[] existing = getExPartners();
        int keep = Math.min(existing.length, MAX_HISTORY - 1);
        ExPartner[] updated = new ExPartner[keep + 1];
        updated[0] = ex;
        System.arraycopy(existing, 0, updated, 1, keep);
        this.exPartners = updated;
    }

    /** Seconds until this player may marry again, 0 when they already may. */
    public long getRemarryCooldownRemainingSeconds(){
        long remaining = this.remarryCooldownUntilMillis - System.currentTimeMillis();
        return remaining <= 0L ? 0L : (remaining + 999L) / 1000L;
    }

    public void setRemarryCooldownUntilMillis(long millis){
        this.remarryCooldownUntilMillis = millis;
    }
}
