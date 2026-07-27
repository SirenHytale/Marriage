package plugin.siren.Utils.Registry;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

/**
 * On-disk container for the global marriage registry (Data/MarriagesData.json).
 * The empty-array initializers ARE the empty-registry default for a missing file.
 */
public class MarriagesData {

    public static final BuilderCodec<MarriagesData> CODEC = BuilderCodec.builder(MarriagesData.class, MarriagesData::new)
            .append(new KeyedCodec<MarriageRecord[]>("Marriages", ArrayCodec.ofBuilderCodec(MarriageRecord.CODEC, MarriageRecord[]::new)),
                    (data, value) -> data.Marriages = value,
                    (data) -> data.Marriages)
            .add()
            .append(new KeyedCodec<Integer>("TotalMarriagesEver", Codec.INTEGER),
                    (data, value) -> data.TotalMarriagesEver = value,
                    (data) -> data.TotalMarriagesEver)
            .add()
            .append(new KeyedCodec<Integer>("TotalDivorces", Codec.INTEGER),
                    (data, value) -> data.TotalDivorces = value,
                    (data) -> data.TotalDivorces)
            .add()
            .append(new KeyedCodec<String[]>("PendingDivorceSync", Codec.STRING_ARRAY),
                    (data, value) -> data.PendingDivorceSync = value,
                    (data) -> data.PendingDivorceSync)
            .add()
            .build();

    private MarriageRecord[] Marriages = new MarriageRecord[0];
    private int TotalMarriagesEver;
    private int TotalDivorces;

    /**
     * UUIDs whose per-player settings component still says married although their
     * marriage ended while they were offline. Consumed (and their settings cleared)
     * on next join - this is what stops stale 1.x save data re-importing an ended
     * marriage.
     */
    private String[] PendingDivorceSync = new String[0];

    public MarriagesData(){}

    public MarriageRecord[] getMarriages(){
        return this.Marriages;
    }

    public void setMarriages(MarriageRecord[] marriages){
        this.Marriages = marriages;
    }

    public int getTotalMarriagesEver(){
        return this.TotalMarriagesEver;
    }

    public void incrementTotalMarriagesEver(){
        this.TotalMarriagesEver++;
    }

    public int getTotalDivorces(){
        return this.TotalDivorces;
    }

    public void incrementTotalDivorces(){
        this.TotalDivorces++;
    }

    public String[] getPendingDivorceSync(){
        return this.PendingDivorceSync;
    }

    public void setPendingDivorceSync(String[] pendingDivorceSync){
        this.PendingDivorceSync = pendingDivorceSync;
    }
}
