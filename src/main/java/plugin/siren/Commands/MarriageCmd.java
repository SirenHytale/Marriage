package plugin.siren.Commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Commands.Marriage.RequireRingCmd;
import plugin.siren.Marriage;
import plugin.siren.Utils.UI.MarriageUIPage;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * /marriage opens the Marriage menu (since 2.0.0); admin subcommands live under it.
 */
public class MarriageCmd extends AbstractCommand {
    public MarriageCmd(){
        super("marriage","server.commands.marriage.desc");

        this.addSubCommand(new RequireRingCmd());

        this.requirePermission("marriage");
        this.setPermissionGroups("hytale:None");
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        if(!context.isPlayer()){
            return CompletableFuture.completedFuture(null);
        }
        Ref<EntityStore> ref = context.senderAsPlayerRef();
        if(ref == null || !ref.isValid()){
            return CompletableFuture.completedFuture(null);
        }
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();
        return CompletableFuture.runAsync(() -> {
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            if(player == null || playerRef == null){
                return;
            }
            if(!Marriage.getConfig().get().ifMenuEnabled()){
                playerRef.sendMessage(Message.translation("server.marriage.menu.disabled"));
                return;
            }
            player.getPageManager().openCustomPage(ref, store, new MarriageUIPage(playerRef));
        }, world);
    }
}
