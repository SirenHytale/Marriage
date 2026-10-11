package plugin.siren.Commands.Marriage;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import plugin.siren.API.MarriageAPI;
import plugin.siren.Marriage;
import plugin.siren.Utils.ComponentSync;
import plugin.siren.Utils.MarriageUtil;
import plugin.siren.Utils.Registry.MarriageManager;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.UUID;

/**
 * {@code /marriage force marry <playerOne> <playerTwo>} — marries two players
 * with no proposal, no ring, and no consent.
 *
 * <p>Routed through {@link MarriageAPI#marry}, so the cancellable
 * {@code PreMarryEvent} still fires and every companion mod sees the marriage
 * appear the same way it would have otherwise.</p>
 *
 * <p>Unlike force-divorce this needs both players ONLINE. A marriage has to write
 * a settings component on each of them, and doing that for an absent player means
 * either touching another thread's entity or inventing a pending-marry queue for
 * a case an admin can trivially avoid by waiting until they log in.</p>
 */
public class ForceMarryCmd extends CommandBase {

    private final RequiredArg<PlayerRef> firstArg =
            withRequiredArg("playerOne", "server.commands.marriage.force.arg.playerOne.desc", ArgTypes.PLAYER_REF);
    private final RequiredArg<PlayerRef> secondArg =
            withRequiredArg("playerTwo", "server.commands.marriage.force.arg.playerTwo.desc", ArgTypes.PLAYER_REF);

    public ForceMarryCmd(){
        super("marry", "server.commands.marriage.force.marry.desc");
        this.requirePermission(ForceCmd.PERMISSION);
        // empty (not null): never inherit a parent's group, which would grant this node to everyone
        this.setPermissionGroups();
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context){
        PlayerRef one = this.firstArg.get(context);
        PlayerRef two = this.secondArg.get(context);

        if(one == null || !one.isValid() || two == null || !two.isValid()){
            context.sendMessage(Message.translation("server.commands.marriage.force.marry.needsOnline"));
            return;
        }
        if(one.getUuid().equals(two.getUuid())){
            context.sendMessage(Message.translation("server.commands.marriage.force.marry.self"));
            return;
        }

        UUID oneUuid = one.getUuid();
        UUID twoUuid = two.getUuid();

        if(MarriageAPI.isMarried(oneUuid)){
            context.sendMessage(Message.translation("server.commands.marriage.force.alreadyMarried")
                    .param("username", one.getUsername()));
            return;
        }
        if(MarriageAPI.isMarried(twoUuid)){
            context.sendMessage(Message.translation("server.commands.marriage.force.alreadyMarried")
                    .param("username", two.getUsername()));
            return;
        }

        if(!MarriageAPI.marry(oneUuid, one.getUsername(), twoUuid, two.getUsername())){
            context.sendMessage(Message.translation("server.commands.marriage.force.marry.cancelled"));
            return;
        }

        // The registry now holds the marriage; both players still need their own
        // cached settings component brought into line.
        ComponentSync.syncMarried(one, twoUuid, two.getUsername());
        ComponentSync.syncMarried(two, oneUuid, one.getUsername());

        one.sendMessage(Message.translation("server.marriage.force.married")
                .param("partnerUsername", two.getUsername()).color(Color.PINK));
        two.sendMessage(Message.translation("server.marriage.force.married")
                .param("partnerUsername", one.getUsername()).color(Color.PINK));

        if(Marriage.getConfig().get().ifAnnounceMarriages()){
            MarriageUtil.broadcast(Message.translation("server.commands.marry.player.console.alert")
                    .param("usernameOne", one.getUsername())
                    .param("usernameTwo", two.getUsername())
                    .color(Color.PINK));
        }

        context.sendMessage(Message.translation("server.commands.marriage.force.marry.done")
                .param("usernameOne", one.getUsername())
                .param("usernameTwo", two.getUsername()));
        Marriage.LOGGER.atInfo().log("An admin force-married " + one.getUsername() + " and " + two.getUsername() + ".");
    }
}
