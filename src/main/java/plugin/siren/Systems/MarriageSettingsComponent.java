package plugin.siren.Systems;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Persisted per-player marriage data. Since 2.0.0 the global registry
 * (MarriageManager) is the source of truth for who is married; the married/partner
 * fields here are kept as a synced cache AND as the migration source for
 * marriages saved by 1.x. Codec keys "Married?", "PartnerUUID" and
 * "PartnerUsername" must never change spelling — they exist in player saves.
 */
public class MarriageSettingsComponent implements Component<EntityStore> {

    public static final BuilderCodec<MarriageSettingsComponent> CODEC = BuilderCodec.builder(MarriageSettingsComponent.class, MarriageSettingsComponent::new)
            .append(new KeyedCodec<Boolean>("Married?", Codec.BOOLEAN),
                    (marSettings, mBool) -> marSettings.married = mBool, // Setter
                    (marSettings) -> marSettings.married)                    // Getter
            .add()
            .append(new KeyedCodec<>("PartnerUUID", Codec.UUID_STRING),
                    (marSettings, pUUID) -> marSettings.partnerUUID = pUUID, // Setter
                    (marSettings) -> marSettings.partnerUUID)                    // Getter
            .add()
            .append(new KeyedCodec<String>("PartnerUsername", Codec.STRING),
                    (marSettings, puStr) -> marSettings.partnerUsername = puStr, // Setter
                    (marSettings) -> marSettings.partnerUsername)                    // Getter
            .add()
            .append(new KeyedCodec<Boolean>("AcceptRequests", Codec.BOOLEAN),
                    (marSettings, arBool) -> marSettings.acceptRequests = arBool, // Setter
                    (marSettings) -> marSettings.acceptRequests)                    // Getter
            .add()
            .append(new KeyedCodec<Integer>("TimesMarried", Codec.INTEGER),
                    (marSettings, tmInt) -> marSettings.timesMarried = tmInt, // Setter
                    (marSettings) -> marSettings.timesMarried)                    // Getter
            .add()
            .append(new KeyedCodec<Integer>("Divorces", Codec.INTEGER),
                    (marSettings, dInt) -> marSettings.divorces = dInt, // Setter
                    (marSettings) -> marSettings.divorces)                    // Getter
            .add()
            // 2.1.0 - additive keys, so a save written by 2.0.x decodes with the
            // Java initializers below rather than needing a migration.
            .append(new KeyedCodec<Boolean>("CoupleChannel", Codec.BOOLEAN),
                    (marSettings, ccBool) -> marSettings.coupleChannel = ccBool,
                    (marSettings) -> marSettings.coupleChannel)
            .add()
            .append(new KeyedCodec<Boolean>("PartnerHud", Codec.BOOLEAN),
                    (marSettings, phBool) -> marSettings.partnerHud = phBool,
                    (marSettings) -> marSettings.partnerHud)
            .add()
            .build();

    private boolean married;
    private UUID partnerUUID;
    private String partnerUsername;
    private boolean acceptRequests = true;
    private int timesMarried;
    private int divorces;

    /** When on, ordinary chat goes to the partner instead of the whole server. Off by default - a chat mode nobody asked for is a way to say something private out loud. */
    private boolean coupleChannel;
    /** When on, a small plate shows the partner's health and distance while they are near. */
    private boolean partnerHud = true;

    public static ComponentType<EntityStore, MarriageSettingsComponent> getComponentType(){
        return Marriage.get().getMarriageSettingsComponentType();
    }

    public MarriageSettingsComponent(){
        this.married = false;
        this.partnerUUID = null;
        this.partnerUsername = "";
    }

    public MarriageSettingsComponent(MarriageSettingsComponent other){
        this.married = other.married;
        this.partnerUUID = other.partnerUUID;
        this.partnerUsername = other.partnerUsername;
        this.acceptRequests = other.acceptRequests;
        this.timesMarried = other.timesMarried;
        this.divorces = other.divorces;
        this.coupleChannel = other.coupleChannel;
        this.partnerHud = other.partnerHud;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new MarriageSettingsComponent(this);
    }

    public boolean isMarried(){
        return this.married;
    }

    public void setMarried(boolean married){
        this.married = married;
    }

    public UUID getPartnerUUID(){
        return this.partnerUUID;
    }

    public void setPartnerUUID(UUID partnerUUID){
        this.partnerUUID = partnerUUID;
    }

    public void clearPartnerUUID(){
        this.partnerUUID = null;
    }

    public String getPartnerUsername(){
        return this.partnerUsername;
    }

    public void setPartnerUsername(String partnerUsername) {
        this.partnerUsername = partnerUsername;
    }

    public boolean isAcceptRequests(){
        return this.acceptRequests;
    }

    public void setAcceptRequests(boolean acceptRequests){
        this.acceptRequests = acceptRequests;
    }

    public int getTimesMarried(){
        return this.timesMarried;
    }

    public void incrementTimesMarried(){
        this.timesMarried++;
    }

    public int getDivorces(){
        return this.divorces;
    }

    public void incrementDivorces(){
        this.divorces++;
    }

    public boolean isCoupleChannel(){
        return this.coupleChannel;
    }

    public void setCoupleChannel(boolean coupleChannel){
        this.coupleChannel = coupleChannel;
    }

    public boolean isPartnerHud(){
        return this.partnerHud;
    }

    public void setPartnerHud(boolean partnerHud){
        this.partnerHud = partnerHud;
    }
}
