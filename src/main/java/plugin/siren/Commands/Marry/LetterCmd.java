package plugin.siren.Commands.Marry;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Utils.LoveLetter;
import plugin.siren.Utils.UI.MarriageLetterUIPage;

import javax.annotation.Nonnull;

/**
 * {@code /marry letter} - opens the Love Letter in your hand.
 *
 * <p>One command for both halves of the feature: a blank letter opens for
 * writing, a written one opens for reading. Which it is depends on the item, not
 * on an argument the player has to remember - and this mod does not use
 * flag-style arguments.</p>
 */
public class LetterCmd extends AbstractPlayerCommand {
    public LetterCmd() {
        super("letter", "server.commands.marry.letter.desc");

        this.setPermissionGroups("hytale:None");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if(!Marriage.getConfig().get().ifLoveLettersEnabled()){
            playerRef.sendMessage(Message.translation("server.marriage.letter.disabled"));
            return;
        }

        InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        ItemStack held = hotbar == null ? null : hotbar.getActiveItem();
        if(!LoveLetter.isLetter(held)){
            playerRef.sendMessage(Message.translation("server.marriage.letter.notHolding"));
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if(player == null){
            return;
        }
        player.getPageManager().openCustomPage(ref, store, new MarriageLetterUIPage(playerRef));
    }
}
