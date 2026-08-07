package plugin.siren.Utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerWindow;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Utils.Bond.BondManager;
import plugin.siren.Utils.Bond.BondPerk;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;
import plugin.siren.Utils.Registry.PendingDelivery;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Hope Chest: one container both partners can open from anywhere.
 *
 * <h2>How it is opened</h2>
 *
 * <p>A plain {@link ContainerWindow} over the marriage's own
 * {@link SimpleItemContainer}, shown through
 * {@code PageManager.setPageWithWindows(..., Page.Bench, ...)} — the same call
 * vanilla's {@code /inventory see} uses to show one player another's container, and
 * the same one a chest block's {@code OpenContainerInteraction} uses. Nothing here
 * is a custom UI; the client renders it as the chest window it already knows.</p>
 *
 * <h2>Two people, one chest, two world threads</h2>
 *
 * <p>Both partners can have it open at once, from different worlds. That is safe
 * because {@code SimpleItemContainer} guards its slots with a
 * {@code ReentrantReadWriteLock} internally — the engine's own container is
 * already the concurrent one, and handing the same instance to two windows is
 * exactly what an {@code ItemContainerBlock} does when two players open one
 * chest.</p>
 *
 * <p>What is <em>not</em> free is persistence: the container mutates in place with
 * no hook of its own, so the write to disk is hung off the window's close event.
 * A crash with the chest open can therefore lose the last session's moves, which
 * is the same exposure every vanilla chest has.</p>
 */
public final class HopeChestManager {

    private HopeChestManager(){}

    /**
     * Opens the couple's chest. Must be called on the player's own world thread
     * with their own store/ref. Sends all feedback messages itself.
     */
    public static void open(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                            @Nonnull PlayerRef playerRef){
        if(!Marriage.getConfig().get().ifHopeChestEnabled()){
            playerRef.sendMessage(Message.translation("server.marriage.chest.disabled"));
            return;
        }

        UUID uuid = playerRef.getUuid();
        MarriageRecord record = MarriageManager.getRecord(uuid);
        if(record == null){
            playerRef.sendMessage(Message.translation("server.marriage.chest.unmarried"));
            return;
        }
        if(!BondManager.hasPerk(record, BondPerk.HOPE_CHEST)){
            playerRef.sendMessage(Message.translation("server.marriage.perk.locked")
                    .param("perk", Message.translation(BondPerk.HOPE_CHEST.getNameKey()))
                    .param("level", String.valueOf(BondManager.requiredLevelFor(record, BondPerk.HOPE_CHEST))));
            return;
        }

        SimpleItemContainer chest = MarriageManager.getOrCreateHopeChest(uuid);
        if(chest == null){
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if(player == null){
            Marriage.LOGGER.atInfo().log("Failed to get Player component : HopeChestManager");
            return;
        }

        ContainerWindow window = new ContainerWindow(chest);
        if(!player.getPageManager().setPageWithWindows(ref, store, Page.Bench, true, window)){
            playerRef.sendMessage(Message.translation("server.marriage.chest.busy"));
            return;
        }

        // The container has no change hook of its own, so closing is where the
        // contents reach disk. Registered after the window is known to have opened -
        // a failed open has nothing to save.
        window.registerCloseEvent(event -> MarriageManager.saveHopeChest());
    }

    /**
     * Hands a player everything owed to them — today, their half of a Hope Chest a
     * divorce split while they were offline. Must be called on the player's own
     * world thread.
     *
     * <p>Overflow lands at their feet rather than being dropped from existence,
     * matching what the gift path does and what vanilla's own
     * {@code GiveItemEffect} does. The queue entry is consumed first, so a player
     * whose inventory is completely full still ends up with the items on the
     * ground rather than the queue retrying forever.</p>
     */
    public static void deliverPending(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                                      @Nonnull PlayerRef playerRef){
        List<PendingDelivery> deliveries = MarriageManager.consumeDeliveries(playerRef.getUuid());
        if(deliveries.isEmpty()){
            return;
        }

        // Counted per reason, so an anniversary gift is not announced as somebody's
        // half of a divorced chest.
        Map<String, Integer> byReason = new LinkedHashMap<>();
        for(PendingDelivery delivery : deliveries){
            SimpleItemContainer items = delivery.getItems();
            if(items == null){
                continue;
            }
            int delivered = 0;
            for(short slot = 0; slot < items.getCapacity(); slot++){
                ItemStack stack = items.getItemStack(slot);
                if(ItemStack.isEmpty(stack)){
                    continue;
                }
                give(store, ref, stack);
                delivered++;
            }
            if(delivered > 0){
                byReason.merge(delivery.getReason(), delivered, Integer::sum);
            }
        }

        for(Map.Entry<String, Integer> entry : byReason.entrySet()){
            playerRef.sendMessage(Message.translation("server.marriage.delivery." + entry.getKey())
                    .param("count", String.valueOf(entry.getValue()))
                    .color(Color.PINK));
        }
    }

    private static void give(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull ItemStack stack){
        ItemStackTransaction given = Player.giveItem(stack, ref, store);
        ItemStack remainder = given.getRemainder();
        if(!ItemStack.isEmpty(remainder)){
            ItemUtils.dropItem(ref, remainder, store);
        }
    }

    /** How many of the chest's slots currently hold something, for the menu. */
    public static int usedSlots(@Nullable MarriageRecord record){
        if(record == null){
            return 0;
        }
        SimpleItemContainer chest = record.getHopeChest();
        if(chest == null){
            return 0;
        }
        int used = 0;
        for(short slot = 0; slot < chest.getCapacity(); slot++){
            if(!ItemStack.isEmpty(chest.getItemStack(slot))){
                used++;
            }
        }
        return used;
    }
}
