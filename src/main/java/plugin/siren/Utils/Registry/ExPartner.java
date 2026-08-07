package plugin.siren.Utils.Registry;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

/**
 * One marriage that ended, remembered on the player it ended for.
 *
 * <p>Kept because a divorce currently erases the entire fact that a marriage
 * happened: the record is deleted, and all that survives is a count. A count
 * cannot tell you who, or how long, or when — which is exactly what makes a
 * divorce weightless.</p>
 *
 * <p>The name is a display cache, as everywhere else in this registry; the UUID
 * is the identity, and is what lets a re-marriage to the same person be
 * recognized rather than looking like a new stranger.</p>
 */
public class ExPartner {

    public static final BuilderCodec<ExPartner> CODEC = BuilderCodec.builder(ExPartner.class, ExPartner::new)
            .append(new KeyedCodec<String>("Uuid", Codec.STRING),
                    (ex, value) -> ex.uuid = value,
                    (ex) -> ex.uuid)
            .add()
            .append(new KeyedCodec<String>("Name", Codec.STRING),
                    (ex, value) -> ex.name = value,
                    (ex) -> ex.name)
            .add()
            .append(new KeyedCodec<Long>("EndedAtMillis", Codec.LONG),
                    (ex, value) -> ex.endedAtMillis = value,
                    (ex) -> ex.endedAtMillis)
            .add()
            .append(new KeyedCodec<Long>("LastedMillis", Codec.LONG),
                    (ex, value) -> ex.lastedMillis = value,
                    (ex) -> ex.lastedMillis)
            .add()
            .build();

    private String uuid = "";
    private String name = "";
    private long endedAtMillis;
    private long lastedMillis;

    public ExPartner(){}

    public ExPartner(@Nonnull String uuid, @Nonnull String name, long endedAtMillis, long lastedMillis){
        this.uuid = uuid;
        this.name = name;
        this.endedAtMillis = endedAtMillis;
        this.lastedMillis = lastedMillis;
    }

    public String getUuid(){
        return this.uuid;
    }

    public String getName(){
        return this.name == null ? "" : this.name;
    }

    public long getEndedAtMillis(){
        return this.endedAtMillis;
    }

    public long getLastedMillis(){
        return this.lastedMillis;
    }
}
