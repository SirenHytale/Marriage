package plugin.siren.Commands.Marry;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Utils.EmoteManager;

import javax.annotation.Nonnull;

/**
 * {@code /marry hug}, {@code /marry kiss}, {@code /marry dance}.
 *
 * <p>One class, three registrations - each emote is its own verb rather than
 * {@code /marry emote <kind>}, because a kind argument is the flag-shaped thing
 * this mod does not do, and because "hug" reads better than "emote hug".</p>
 */
public class EmoteCmd extends AbstractPlayerCommand {

    private final EmoteManager.Emote emote;

    public EmoteCmd(@Nonnull EmoteManager.Emote emote) {
        super(emote.getId(), "server.commands.marry." + emote.getId() + ".desc");
        this.emote = emote;

        this.setPermissionGroups("hytale:None");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        EmoteManager.perform(store, ref, playerRef, this.emote);
    }
}
