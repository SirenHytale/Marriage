package plugin.siren.API;

import plugin.siren.Utils.Registry.MarriageRecord;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Immutable snapshot of one marriage, safe to hand to other mods.
 * Usernames are display caches from the registry (refreshed on join) — key on UUIDs.
 */
public final class MarriageInfo {

    private final UUID player1Uuid;
    private final String player1Name;
    private final UUID player2Uuid;
    private final String player2Name;
    private final long marriedAtMillis;
    private final int giftsByPlayer1;
    private final int giftsByPlayer2;
    private final int teleportsUsed;
    private final int messagesSent;
    private final boolean pvpProtection;
    private final double bondXp;
    private final int bondLevel;
    private final long togetherMinutes;
    private final int anniversariesReached;
    private final boolean hasHome;

    private MarriageInfo(UUID player1Uuid, String player1Name, UUID player2Uuid, String player2Name,
                         long marriedAtMillis, int giftsByPlayer1, int giftsByPlayer2,
                         int teleportsUsed, int messagesSent, boolean pvpProtection,
                         double bondXp, int bondLevel, long togetherMinutes,
                         int anniversariesReached, boolean hasHome){
        this.player1Uuid = player1Uuid;
        this.player1Name = player1Name;
        this.player2Uuid = player2Uuid;
        this.player2Name = player2Name;
        this.marriedAtMillis = marriedAtMillis;
        this.giftsByPlayer1 = giftsByPlayer1;
        this.giftsByPlayer2 = giftsByPlayer2;
        this.teleportsUsed = teleportsUsed;
        this.messagesSent = messagesSent;
        this.pvpProtection = pvpProtection;
        this.bondXp = bondXp;
        this.bondLevel = bondLevel;
        this.togetherMinutes = togetherMinutes;
        this.anniversariesReached = anniversariesReached;
        this.hasHome = hasHome;
    }

    @Nullable
    public static MarriageInfo of(@Nullable MarriageRecord record){
        if(record == null){
            return null;
        }
        UUID uuid1;
        UUID uuid2;
        try{
            uuid1 = UUID.fromString(record.getPlayer1Uuid());
            uuid2 = UUID.fromString(record.getPlayer2Uuid());
        }catch(IllegalArgumentException e){
            return null;
        }
        return new MarriageInfo(uuid1, record.getPlayer1Name(), uuid2, record.getPlayer2Name(),
                record.getMarriedAtMillis(), record.getGiftsByPlayer1(), record.getGiftsByPlayer2(),
                record.getTeleportsUsed(), record.getMessagesSent(), record.isPvpProtection(),
                record.getBondXp(), record.getBondLevel(), record.getTogetherMinutes(),
                record.getAnniversariesAwarded(), record.hasHome());
    }

    public UUID getPlayer1Uuid(){
        return this.player1Uuid;
    }

    public String getPlayer1Name(){
        return this.player1Name;
    }

    public UUID getPlayer2Uuid(){
        return this.player2Uuid;
    }

    public String getPlayer2Name(){
        return this.player2Name;
    }

    public boolean involves(UUID uuid){
        return this.player1Uuid.equals(uuid) || this.player2Uuid.equals(uuid);
    }

    @Nullable
    public UUID getPartnerUuid(UUID uuid){
        if(this.player1Uuid.equals(uuid)){
            return this.player2Uuid;
        }
        if(this.player2Uuid.equals(uuid)){
            return this.player1Uuid;
        }
        return null;
    }

    public String getPartnerName(UUID uuid){
        return this.player1Uuid.equals(uuid) ? this.player2Name : this.player1Name;
    }

    public String getNameOf(UUID uuid){
        return this.player1Uuid.equals(uuid) ? this.player1Name : this.player2Name;
    }

    /** Epoch millis when the couple married (import time for marriages migrated from 1.x saves). */
    public long getMarriedAtMillis(){
        return this.marriedAtMillis;
    }

    public long getMarriedForMillis(){
        return Math.max(0L, System.currentTimeMillis() - this.marriedAtMillis);
    }

    public int getGiftsByPlayer1(){
        return this.giftsByPlayer1;
    }

    public int getGiftsByPlayer2(){
        return this.giftsByPlayer2;
    }

    public int getGiftsFrom(UUID uuid){
        return this.player1Uuid.equals(uuid) ? this.giftsByPlayer1 : this.giftsByPlayer2;
    }

    public int getTotalGifts(){
        return this.giftsByPlayer1 + this.giftsByPlayer2;
    }

    public int getTeleportsUsed(){
        return this.teleportsUsed;
    }

    public int getMessagesSent(){
        return this.messagesSent;
    }

    public boolean isPvpProtection(){
        return this.pvpProtection;
    }

    // === bond (2.1.0) ===

    /** Total bond XP the couple has banked. */
    public double getBondXp(){
        return this.bondXp;
    }

    /** The couple's bond level. 0 for a marriage that has not begun to deepen. */
    public int getBondLevel(){
        return this.bondLevel;
    }

    /** Minutes the partners have spent within bond range of each other. */
    public long getTogetherMinutes(){
        return this.togetherMinutes;
    }

    /** How many anniversary milestones the couple has passed (0-4). */
    public int getAnniversariesReached(){
        return this.anniversariesReached;
    }

    /** Whether the couple has a shared home set. Its coordinates are deliberately not exposed. */
    public boolean hasHome(){
        return this.hasHome;
    }
}
