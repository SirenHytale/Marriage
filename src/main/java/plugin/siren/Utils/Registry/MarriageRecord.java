package plugin.siren.Utils.Registry;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import plugin.siren.Utils.Bond.RingTier;
import plugin.siren.Utils.Bond.Vow;

import javax.annotation.Nonnull;
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

            // === bond (2.1.0) ===
            .append(new KeyedCodec<Double>("BondXp", Codec.DOUBLE),
                    (record, value) -> record.bondXp = value,
                    (record) -> record.bondXp)
            .add()
            .append(new KeyedCodec<Integer>("BondLevel", Codec.INTEGER),
                    (record, value) -> record.bondLevel = value,
                    (record) -> record.bondLevel)
            .add()
            .append(new KeyedCodec<Long>("TogetherMinutes", Codec.LONG),
                    (record, value) -> record.togetherMinutes = value,
                    (record) -> record.togetherMinutes)
            .add()
            .append(new KeyedCodec<Long>("LastBondTickMillis", Codec.LONG),
                    (record, value) -> record.lastBondTickMillis = value,
                    (record) -> record.lastBondTickMillis)
            .add()
            .append(new KeyedCodec<Long>("LastTogetherMillis", Codec.LONG),
                    (record, value) -> record.lastTogetherMillis = value,
                    (record) -> record.lastTogetherMillis)
            .add()
            .append(new KeyedCodec<Integer>("AnniversariesAwarded", Codec.INTEGER),
                    (record, value) -> record.anniversariesAwarded = value,
                    (record) -> record.anniversariesAwarded)
            .add()
            // The band they married with. Absent for a 2.0.x record, which decodes
            // to 0 - RingTier.COPPER, exactly the right answer for a wedding held
            // before tiers existed.
            .append(new KeyedCodec<Integer>("RingTier", Codec.INTEGER),
                    (record, value) -> record.ringTier = value,
                    (record) -> record.ringTier)
            .add()

            // === vows and renewal ===
            // Absent for a pre-vow record, which decodes to 0 - Vow.NONE, and
            // VowChosen false, so those couples are simply offered the choice.
            .append(new KeyedCodec<Integer>("Vow", Codec.INTEGER),
                    (record, value) -> record.vow = value,
                    (record) -> record.vow)
            .add()
            .append(new KeyedCodec<Boolean>("VowChosen", Codec.BOOLEAN),
                    (record, value) -> record.vowChosen = value,
                    (record) -> record.vowChosen)
            .add()
            .append(new KeyedCodec<Long>("LastRenewedAtMillis", Codec.LONG),
                    (record, value) -> record.lastRenewedAtMillis = value,
                    (record) -> record.lastRenewedAtMillis)
            .add()

            // === a divorce in progress ===
            // The marriage still exists while one is pending, so its own record is
            // where this belongs - no second store to keep in step, and it is
            // deleted with the marriage whichever way the filing ends.
            .append(new KeyedCodec<Long>("DivorceFiledAtMillis", Codec.LONG),
                    (record, value) -> record.divorceFiledAtMillis = value,
                    (record) -> record.divorceFiledAtMillis)
            .add()
            .append(new KeyedCodec<String>("DivorceFiledBy", Codec.STRING),
                    (record, value) -> record.divorceFiledBy = value,
                    (record) -> record.divorceFiledBy)
            .add()
            .append(new KeyedCodec<Boolean>("DivorceContested", Codec.BOOLEAN),
                    (record, value) -> record.divorceContested = value,
                    (record) -> record.divorceContested)
            .add()

            // === shared home (2.1.0) ===
            .append(new KeyedCodec<String>("HomeWorld", Codec.STRING),
                    (record, value) -> record.homeWorld = value,
                    (record) -> record.homeWorld)
            .add()
            .append(new KeyedCodec<Double>("HomeX", Codec.DOUBLE),
                    (record, value) -> record.homeX = value,
                    (record) -> record.homeX)
            .add()
            .append(new KeyedCodec<Double>("HomeY", Codec.DOUBLE),
                    (record, value) -> record.homeY = value,
                    (record) -> record.homeY)
            .add()
            .append(new KeyedCodec<Double>("HomeZ", Codec.DOUBLE),
                    (record, value) -> record.homeZ = value,
                    (record) -> record.homeZ)
            .add()
            .append(new KeyedCodec<Float>("HomeYaw", Codec.FLOAT),
                    (record, value) -> record.homeYaw = value,
                    (record) -> record.homeYaw)
            .add()
            .append(new KeyedCodec<Float>("HomePitch", Codec.FLOAT),
                    (record, value) -> record.homePitch = value,
                    (record) -> record.homePitch)
            .add()

            // === hope chest (2.1.0) ===
            // Nested BuilderCodec rather than a flattened item list: SimpleItemContainer
            // already ships one, and reusing it means slot layout and stack encoding stay
            // whatever the engine says they are rather than a second opinion of ours.
            .append(new KeyedCodec<SimpleItemContainer>("HopeChest", SimpleItemContainer.CODEC),
                    (record, value) -> record.hopeChest = value,
                    (record) -> record.hopeChest)
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

    private double bondXp;
    private int bondLevel;
    private long togetherMinutes;
    private long lastBondTickMillis;
    private long lastTogetherMillis;
    private int anniversariesAwarded;
    private int ringTier;

    private int vow;
    private boolean vowChosen;
    private long lastRenewedAtMillis;

    /** Zero when no divorce is pending; otherwise when it was filed. */
    private long divorceFiledAtMillis;
    private String divorceFiledBy = "";
    private boolean divorceContested;

    /** Blank until the couple sets a shared home. */
    private String homeWorld = "";
    private double homeX;
    private double homeY;
    private double homeZ;
    private float homeYaw;
    private float homePitch;

    /** Null until the couple first opens the chest — an unused chest costs no disk. */
    @Nullable
    private SimpleItemContainer hopeChest;

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

    // === bond ===

    public double getBondXp(){
        return this.bondXp;
    }

    public void setBondXp(double bondXp){
        this.bondXp = Math.max(0.0, bondXp);
    }

    public int getBondLevel(){
        return this.bondLevel;
    }

    public void setBondLevel(int bondLevel){
        this.bondLevel = Math.max(0, bondLevel);
    }

    public long getTogetherMinutes(){
        return this.togetherMinutes;
    }

    public void addTogetherMinutes(long minutes){
        this.togetherMinutes += Math.max(0L, minutes);
    }

    /**
     * When the bond system last consumed elapsed time for this couple. Zero for a
     * record written before 2.1.0 — the tick treats that as "start the clock now"
     * rather than back-paying every hour since the wedding.
     */
    public long getLastBondTickMillis(){
        return this.lastBondTickMillis;
    }

    public void setLastBondTickMillis(long millis){
        this.lastBondTickMillis = millis;
    }

    /** Epoch millis the partners were last within bond range of each other. */
    public long getLastTogetherMillis(){
        return this.lastTogetherMillis;
    }

    public void setLastTogetherMillis(long millis){
        this.lastTogetherMillis = millis;
    }

    /** How many anniversary milestones this couple has already been paid. */
    public int getAnniversariesAwarded(){
        return this.anniversariesAwarded;
    }

    public void setAnniversariesAwarded(int awarded){
        this.anniversariesAwarded = Math.max(0, awarded);
    }

    /** The band this couple married with. Copper for anything from before 2.1.0. */
    @Nonnull
    public RingTier getRingTier(){
        return RingTier.fromOrdinal(this.ringTier);
    }

    public void setRingTier(@Nonnull RingTier tier){
        this.ringTier = tier.ordinal();
    }

    // === vows ===

    @Nonnull
    public Vow getVow(){
        return Vow.fromOrdinal(this.vow);
    }

    public void setVow(@Nonnull Vow vow){
        this.vow = vow.ordinal();
        this.vowChosen = vow != Vow.NONE;
    }

    /**
     * Whether this couple has already sworn. A vow is chosen once and then held:
     * swapping it freely would make it a loadout rather than a promise. Renewing
     * their vows clears this and lets them choose again.
     */
    public boolean isVowChosen(){
        return this.vowChosen;
    }

    /** Reopens the choice, as a renewal does. Keeps the current vow until a new one is sworn. */
    public void reopenVow(){
        this.vowChosen = false;
    }

    public long getLastRenewedAtMillis(){
        return this.lastRenewedAtMillis;
    }

    public void setLastRenewedAtMillis(long millis){
        this.lastRenewedAtMillis = millis;
    }

    // === pending divorce ===

    public boolean isDivorcePending(){
        return this.divorceFiledAtMillis > 0L;
    }

    public long getDivorceFiledAtMillis(){
        return this.divorceFiledAtMillis;
    }

    @Nonnull
    public String getDivorceFiledBy(){
        return this.divorceFiledBy == null ? "" : this.divorceFiledBy;
    }

    /** True when {@code uuid} is the one who filed. False for the other partner. */
    public boolean isDivorceFiledBy(@Nonnull UUID uuid){
        return uuid.toString().equals(this.divorceFiledBy);
    }

    public boolean isDivorceContested(){
        return this.divorceContested;
    }

    public void fileDivorce(@Nonnull UUID by, long atMillis){
        this.divorceFiledBy = by.toString();
        this.divorceFiledAtMillis = atMillis;
        this.divorceContested = false;
    }

    public void contestDivorce(){
        this.divorceContested = true;
    }

    public void clearPendingDivorce(){
        this.divorceFiledAtMillis = 0L;
        this.divorceFiledBy = "";
        this.divorceContested = false;
    }

    // === shared home ===

    public boolean hasHome(){
        return this.homeWorld != null && !this.homeWorld.isBlank();
    }

    public String getHomeWorld(){
        return this.homeWorld == null ? "" : this.homeWorld;
    }

    public double getHomeX(){
        return this.homeX;
    }

    public double getHomeY(){
        return this.homeY;
    }

    public double getHomeZ(){
        return this.homeZ;
    }

    public float getHomeYaw(){
        return this.homeYaw;
    }

    public float getHomePitch(){
        return this.homePitch;
    }

    public void setHome(@Nonnull String world, double x, double y, double z, float yaw, float pitch){
        this.homeWorld = world;
        this.homeX = x;
        this.homeY = y;
        this.homeZ = z;
        this.homeYaw = yaw;
        this.homePitch = pitch;
    }

    public void clearHome(){
        this.homeWorld = "";
        this.homeX = 0.0;
        this.homeY = 0.0;
        this.homeZ = 0.0;
        this.homeYaw = 0f;
        this.homePitch = 0f;
    }

    // === hope chest ===

    @Nullable
    public SimpleItemContainer getHopeChest(){
        return this.hopeChest;
    }

    /**
     * The couple's chest, created on first use at {@code capacity} slots.
     *
     * <p>An existing chest is only ever grown, never shrunk: a server owner who
     * lowers Hope-Chest-Slots would otherwise silently delete whatever sat in the
     * slots that vanished. Growing copies the stacks across because
     * {@code SimpleItemContainer}'s capacity is fixed at construction.</p>
     */
    @Nonnull
    public SimpleItemContainer getOrCreateHopeChest(short capacity){
        short slots = (short) Math.max(1, capacity);
        if(this.hopeChest == null){
            this.hopeChest = new SimpleItemContainer(slots);
        }else if(this.hopeChest.getCapacity() < slots){
            SimpleItemContainer grown = new SimpleItemContainer(slots);
            for(short slot = 0; slot < this.hopeChest.getCapacity(); slot++){
                grown.setItemStackForSlot(slot, this.hopeChest.getItemStack(slot), false);
            }
            this.hopeChest = grown;
        }
        return this.hopeChest;
    }

    public void clearHopeChest(){
        this.hopeChest = null;
    }
}
