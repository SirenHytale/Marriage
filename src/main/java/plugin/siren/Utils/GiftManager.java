package plugin.siren.Utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.API.MarriageEvents;
import plugin.siren.API.MarriageInfo;
import plugin.siren.Marriage;
import plugin.siren.Utils.Registry.MarriageManager;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Hands the item stack the giver is holding to their married partner.
 * Runs on the giver's world thread; the add happens on the partner's world
 * thread via a CompletableFuture hop (Cultivation's proven DuelManager pattern).
 * Remove-then-hop means a refund path is mandatory: if the partner vanishes
 * mid-hop, the stack hops back to the giver, and any overflow on either side is
 * dropped at that player's feet (vanilla GiveItemEffect behavior) - never lost.
 */
public final class GiftManager {

    private GiftManager(){}

    /**
     * Attempts the gift. Must be called on the giver's world thread with the
     * giver's own store/ref. Sends all feedback messages itself.
     */
    public static void gift(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef){
        MarriageConfig config = Marriage.getConfig().get();
        if(!config.ifGiftEnabled()){
            playerRef.sendMessage(Message.translation("server.marriage.gift.disabled"));
            return;
        }

        UUID giverUuid = playerRef.getUuid();
        MarriageInfo marriage = MarriageInfo.of(MarriageManager.getRecord(giverUuid));
        if(marriage == null){
            playerRef.sendMessage(Message.translation("server.marriage.gift.unmarried"));
            return;
        }

        long cooldownRemaining = MarriageManager.getGiftCooldownRemaining(giverUuid);
        if(cooldownRemaining > 0L){
            playerRef.sendMessage(Message.translation("server.marriage.gift.cooldown").param("seconds", String.valueOf(cooldownRemaining)));
            return;
        }

        InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        if(hotbar == null){
            Marriage.LOGGER.atInfo().log("Failed to get Hotbar component : GiftManager");
            return;
        }
        ItemStack held = hotbar.getActiveItem();
        if(held == null){
            playerRef.sendMessage(Message.translation("server.marriage.gift.emptyHand"));
            return;
        }

        UUID partnerUuid = marriage.getPartnerUuid(giverUuid);
        String partnerName = marriage.getPartnerName(giverUuid);
        PlayerRef partner = partnerUuid == null ? null : Universe.get().getPlayer(partnerUuid);
        if(partner == null || !partner.isValid()){
            playerRef.sendMessage(Message.translation("server.marriage.gift.partnerOffline").param("partnerUsername", partnerName));
            return;
        }
        Ref<EntityStore> partnerRef = partner.getReference();
        if(partnerRef == null || !partnerRef.isValid()){
            playerRef.sendMessage(Message.translation("server.marriage.gift.partnerOffline").param("partnerUsername", partnerName));
            return;
        }

        World giverWorld = store.getExternalData().getWorld();
        World partnerWorld = partnerRef.getStore().getExternalData().getWorld();

        double range = config.getGiftRangeBlocks();
        if(range > 0.0){
            // Store identity, not world equality - same store means the partner's
            // components are safely readable from this (the giver's) world thread.
            if(partnerRef.getStore() != store){
                playerRef.sendMessage(Message.translation("server.marriage.gift.tooFar").param("partnerUsername", partnerName));
                return;
            }
            TransformComponent giverTransform = store.getComponent(ref, TransformComponent.getComponentType());
            TransformComponent partnerTransform = store.getComponent(partnerRef, TransformComponent.getComponentType());
            if(giverTransform == null || partnerTransform == null
                    || giverTransform.getPosition().distanceSquared(partnerTransform.getPosition()) > range * range){
                playerRef.sendMessage(Message.translation("server.marriage.gift.tooFar").param("partnerUsername", partnerName));
                return;
            }
        }

        if(!MarriageEvents.firePreGift(new MarriageEvents.PreGiftEvent(marriage, giverUuid, held.getItemId(), held.getQuantity()))){
            return;
        }

        // Remove from the giver first - we are on the giver's world thread. The
        // ItemStack-matching overload fails if the slot content changed under us.
        ItemStackSlotTransaction removed = hotbar.getInventory()
                .removeItemStackFromSlot(hotbar.getActiveSlot(), held, held.getQuantity());
        if(!removed.succeeded()){
            playerRef.sendMessage(Message.translation("server.marriage.gift.emptyHand"));
            return;
        }

        MarriageManager.markGiftUsed(giverUuid);

        String itemId = held.getItemId();
        int quantity = held.getQuantity();
        String giverName = playerRef.getUsername();

        CompletableFuture.runAsync(() -> {
            // Now on the partner's world thread - but the partner may have changed
            // worlds mid-hop, in which case their components belong to another
            // thread and the only safe move is a refund.
            if(!partner.isValid() || !partnerRef.isValid()
                    || partnerRef.getStore().getExternalData().getWorld() != partnerWorld){
                refund(giverWorld, store, ref, playerRef, held);
                return;
            }
            ItemStackTransaction given = Player.giveItem(held, partnerRef, partnerRef.getStore());
            ItemStack remainder = given.getRemainder();
            if(!ItemStack.isEmpty(remainder)){
                // Vanilla GiveItemEffect behavior: overflow lands at the receiver's feet.
                ItemUtils.dropItem(partnerRef, remainder, partnerRef.getStore());
            }

            MarriageManager.recordGift(giverUuid);
            MarriageInfo updated = MarriageInfo.of(MarriageManager.getRecord(giverUuid));
            MarriageEvents.fireGift(new MarriageEvents.GiftEvent(updated != null ? updated : marriage, giverUuid, itemId, quantity));

            partner.sendMessage(Message.translation("server.marriage.gift.received")
                    .param("partnerUsername", giverName)
                    .param("item", itemId)
                    .param("quantity", String.valueOf(quantity))
                    .color(Color.PINK));
            playerRef.sendMessage(Message.translation("server.marriage.gift.sent")
                    .param("partnerUsername", partnerName)
                    .param("item", itemId)
                    .param("quantity", String.valueOf(quantity))
                    .color(Color.PINK));
        }, partnerWorld).exceptionally(throwable -> {
            Marriage.LOGGER.atSevere().log("Gift transfer failed, refunding " + giverName + ": " + throwable);
            refund(giverWorld, store, ref, playerRef, held);
            return null;
        });
    }

    /** Hops back to the giver's world thread and returns the stack, dropping overflow at their feet. */
    private static void refund(World giverWorld, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, ItemStack stack){
        CompletableFuture.runAsync(() -> {
            if(!ref.isValid()){
                Marriage.LOGGER.atSevere().log("Could not refund a gift to " + playerRef.getUsername() + " - they left the world.");
                return;
            }
            ItemStackTransaction refund = Player.giveItem(stack, ref, store);
            ItemStack remainder = refund.getRemainder();
            if(!ItemStack.isEmpty(remainder)){
                ItemUtils.dropItem(ref, remainder, store);
            }
            if(playerRef.isValid()){
                playerRef.sendMessage(Message.translation("server.marriage.gift.refunded"));
            }
        }, giverWorld);
    }
}
