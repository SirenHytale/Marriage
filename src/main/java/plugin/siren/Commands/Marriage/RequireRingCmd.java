package plugin.siren.Commands.Marriage;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;

import javax.annotation.Nonnull;

public class RequireRingCmd extends AbstractPlayerCommand {
    public RequireRingCmd() {
        super("requirering", "server.commands.marriage.requireRing.desc");

        this.requirePermission("marriage.admin.requirering");
    }

    RequiredArg<Boolean> msgRequireRingArg = this.withRequiredArg("Require Ring", "server.commands.marriage.requireRing.arg.boolean.desc", ArgTypes.BOOLEAN);

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        boolean requireRing = msgRequireRingArg.get(commandContext);

        Marriage.getConfig().get().setRequireRing(requireRing);
        Marriage.getConfig().save();

        String playerTranslationId = "";
        String consoleTranslationId = "";
        if (requireRing) {
            playerTranslationId = "server.commands.marriage.requireRing.player.msg.enabled";
            consoleTranslationId = "server.commands.marriage.requireRing.console.msg.enabled";
        } else {
            playerTranslationId = "server.commands.marriage.requireRing.player.msg.disabled";
            consoleTranslationId = "server.commands.marriage.requireRing.console.msg.disabled";
        }
        playerRef.sendMessage(Message.translation(playerTranslationId));

        Marriage.LOGGER.atInfo().log(Message.translation(consoleTranslationId).param("username",playerRef.getUsername()).getAnsiMessage());
    }
}