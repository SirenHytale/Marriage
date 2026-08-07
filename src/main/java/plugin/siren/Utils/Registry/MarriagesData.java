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
            .append(new KeyedCodec<PendingDelivery[]>("PendingDeliveries", ArrayCodec.ofBuilderCodec(PendingDelivery.CODEC, PendingDelivery[]::new)),
                    (data, value) -> data.PendingDeliveries = value,
                    (data) -> data.PendingDeliveries)
            .add()
            .append(new KeyedCodec<PlayerTally[]>("PlayerTallies", ArrayCodec.ofBuilderCodec(PlayerTally.CODEC, PlayerTally[]::new)),
                    (data, value) -> data.PlayerTallies = value,
                    (data) -> data.PlayerTallies)
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

    /**
     * Items owed to players who were offline when a divorce split their Hope Chest,
     * handed over on their next join. See {@link PendingDelivery}.
     */
    private PendingDelivery[] PendingDeliveries = new PendingDelivery[0];

    /**
     * Lifetime per-player counters that outlive any single marriage - what the
     * Records page is built from. See {@link PlayerTally}.
     */
    private PlayerTally[] PlayerTallies = new PlayerTally[0];

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

    public PendingDelivery[] getPendingDeliveries(){
        return this.PendingDeliveries == null ? new PendingDelivery[0] : this.PendingDeliveries;
    }

    public void setPendingDeliveries(PendingDelivery[] pendingDeliveries){
        this.PendingDeliveries = pendingDeliveries;
    }

    public PlayerTally[] getPlayerTallies(){
        return this.PlayerTallies == null ? new PlayerTally[0] : this.PlayerTallies;
    }

    public void setPlayerTallies(PlayerTally[] playerTallies){
        this.PlayerTallies = playerTallies;
    }
}
