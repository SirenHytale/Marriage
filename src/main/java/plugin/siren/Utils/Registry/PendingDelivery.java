package plugin.siren.Utils.Registry;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Items owed to a player who was not online when they became theirs — today that
 * means their half of a Hope Chest split by a divorce.
 *
 * <p>This exists because a divorce must never be able to destroy items. The chest
 * belongs to the marriage record, and the record is deleted the moment the couple
 * separates; without somewhere to put the contents first, divorcing while your
 * partner was offline would quietly delete their share.</p>
 *
 * <p>The payload is a {@link SimpleItemContainer} rather than a list of stacks
 * purely to reuse the engine's own item codec — nothing ever opens it as a
 * container. It is drained on the owner's next join (see
 * {@code PlayerReadyEventM}).</p>
 */
public class PendingDelivery {

    public static final BuilderCodec<PendingDelivery> CODEC = BuilderCodec.builder(PendingDelivery.class, PendingDelivery::new)
            .append(new KeyedCodec<String>("Uuid", Codec.STRING),
                    (delivery, value) -> delivery.uuid = value,
                    (delivery) -> delivery.uuid)
            .add()
            .append(new KeyedCodec<String>("Reason", Codec.STRING),
                    (delivery, value) -> delivery.reason = value,
                    (delivery) -> delivery.reason)
            .add()
            .append(new KeyedCodec<SimpleItemContainer>("Items", SimpleItemContainer.CODEC),
                    (delivery, value) -> delivery.items = value,
                    (delivery) -> delivery.items)
            .add()
            .build();

    /** Lang key suffix for "your share of the Hope Chest". */
    public static final String REASON_HOPE_CHEST = "hopeChest";
    /** Lang key suffix for an anniversary gift. */
    public static final String REASON_ANNIVERSARY = "anniversary";

    private String uuid = "";
    private String reason = REASON_HOPE_CHEST;
    @Nullable
    private SimpleItemContainer items;

    public PendingDelivery(){}

    public PendingDelivery(@Nonnull String uuid, @Nonnull String reason, @Nonnull SimpleItemContainer items){
        this.uuid = uuid;
        this.reason = reason;
        this.items = items;
    }

    public String getUuid(){
        return this.uuid;
    }

    public String getReason(){
        return this.reason == null ? REASON_HOPE_CHEST : this.reason;
    }

    @Nullable
    public SimpleItemContainer getItems(){
        return this.items;
    }
}
