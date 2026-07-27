package plugin.siren.Utils.Registry;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * One marriage in the global registry. Keyed by both partners' UUIDs; usernames are
 * display-only caches refreshed on join, never used for lookups.
 * Codec keys are additive — new fields decode with their Java-initializer defaults
 * for records written by older versions.
 */
public class MarriageRecord {

    public static final BuilderCodec<MarriageRecord> CODEC = BuilderCodec.builder(MarriageRecord.class, MarriageRecord::new)
            .append(new KeyedCodec<String>("Player1Uuid", Codec.STRING),
                    (record, value) -> record.player1Uuid = value,
                    (record) -> record.player1Uuid)
            .add()
            .append(new KeyedCodec<String>("Player1Name", Codec.STRING),
                    (record, value) -> record.player1Name = value,
                    (record) -> record.player1Name)
            .add()
            .append(new KeyedCodec<String>("Player2Uuid", Codec.STRING),
                    (record, value) -> record.player2Uuid = value,
                    (record) -> record.player2Uuid)
            .add()
            .append(new KeyedCodec<String>("Player2Name", Codec.STRING),
                    (record, value) -> record.player2Name = value,
                    (record) -> record.player2Name)
            .add()
            .append(new KeyedCodec<Long>("MarriedAtMillis", Codec.LONG),
                    (record, value) -> record.marriedAtMillis = value,
                    (record) -> record.marriedAtMillis)
            .add()
            .append(new KeyedCodec<Integer>("GiftsByPlayer1", Codec.INTEGER),
                    (record, value) -> record.giftsByPlayer1 = value,
                    (record) -> record.giftsByPlayer1)
            .add()
            .append(new KeyedCodec<Integer>("GiftsByPlayer2", Codec.INTEGER),
                    (record, value) -> record.giftsByPlayer2 = value,
                    (record) -> record.giftsByPlayer2)
            .add()
            .append(new KeyedCodec<Integer>("TeleportsUsed", Codec.INTEGER),
                    (record, value) -> record.teleportsUsed = value,
                    (record) -> record.teleportsUsed)
            .add()
            .append(new KeyedCodec<Integer>("MessagesSent", Codec.INTEGER),
                    (record, value) -> record.messagesSent = value,
                    (record) -> record.messagesSent)
            .add()
            .append(new KeyedCodec<Boolean>("PvpProtection", Codec.BOOLEAN),
                    (record, value) -> record.pvpProtection = value,
                    (record) -> record.pvpProtection)
            .add()
            .build();

    private String player1Uuid = "";
    private String player1Name = "";
    private String player2Uuid = "";
    private String player2Name = "";
    private long marriedAtMillis;
    private int giftsByPlayer1;
    private int giftsByPlayer2;
    private int teleportsUsed;
    private int messagesSent;
    private boolean pvpProtection = true;

    public MarriageRecord(){}

    public MarriageRecord(UUID player1, String player1Name, UUID player2, String player2Name, long marriedAtMillis, boolean pvpProtection){
        this.player1Uuid = player1.toString();
        this.player1Name = player1Name;
        this.player2Uuid = player2.toString();
        this.player2Name = player2Name;
        this.marriedAtMillis = marriedAtMillis;
        this.pvpProtection = pvpProtection;
    }

    public boolean involves(UUID uuid){
        String s = uuid.toString();
        return s.equals(this.player1Uuid) || s.equals(this.player2Uuid);
    }

    @Nullable
    public UUID getPartnerUuid(UUID uuid){
        String s = uuid.toString();
        try{
            if(s.equals(this.player1Uuid)){
                return UUID.fromString(this.player2Uuid);
            }
            if(s.equals(this.player2Uuid)){
                return UUID.fromString(this.player1Uuid);
            }
        }catch(IllegalArgumentException ignored){
            // corrupt uuid string in a hand-edited file — treat as no partner
        }
        return null;
    }

    public String getPartnerName(UUID uuid){
        return uuid.toString().equals(this.player1Uuid) ? this.player2Name : this.player1Name;
    }

    public String getNameOf(UUID uuid){
        return uuid.toString().equals(this.player1Uuid) ? this.player1Name : this.player2Name;
    }

    public void refreshName(UUID uuid, String username){
        if(uuid.toString().equals(this.player1Uuid)){
            this.player1Name = username;
        }else if(uuid.toString().equals(this.player2Uuid)){
            this.player2Name = username;
        }
    }

    public void addGiftFrom(UUID uuid){
        if(uuid.toString().equals(this.player1Uuid)){
            this.giftsByPlayer1++;
        }else if(uuid.toString().equals(this.player2Uuid)){
            this.giftsByPlayer2++;
        }
    }

    public int getGiftsFrom(UUID uuid){
        return uuid.toString().equals(this.player1Uuid) ? this.giftsByPlayer1 : this.giftsByPlayer2;
    }

    public String getPlayer1Uuid(){
        return this.player1Uuid;
    }

    public String getPlayer1Name(){
        return this.player1Name;
    }

    public String getPlayer2Uuid(){
        return this.player2Uuid;
    }

    public String getPlayer2Name(){
        return this.player2Name;
    }

    public long getMarriedAtMillis(){
        return this.marriedAtMillis;
    }

    public int getGiftsByPlayer1(){
        return this.giftsByPlayer1;
    }

    public int getGiftsByPlayer2(){
        return this.giftsByPlayer2;
    }

    public int getTeleportsUsed(){
        return this.teleportsUsed;
    }

    public void incrementTeleportsUsed(){
        this.teleportsUsed++;
    }

    public int getMessagesSent(){
        return this.messagesSent;
    }

    public void incrementMessagesSent(){
        this.messagesSent++;
    }

    public boolean isPvpProtection(){
        return this.pvpProtection;
    }

    public void setPvpProtection(boolean pvpProtection){
        this.pvpProtection = pvpProtection;
    }
}
