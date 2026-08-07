package plugin.siren.Commands.Marriage;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import plugin.siren.API.MarriageAPI;
import plugin.siren.API.MarriageInfo;
import plugin.siren.Marriage;
import plugin.siren.Utils.ComponentSync;
import plugin.siren.Utils.Registry.MarriageManager;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * {@code /marriage force divorce <player>} — ends a marriage outright.
 *
 * <p>No confirmation window: this is the admin path, and an admin typing it has
 * already decided. It goes through {@link MarriageAPI#divorce}, so the same
 * cancellable {@code PreDivorceEvent} fires and any companion mod that was
 * watching still gets its say — an admin override that bypassed the event would
 * leave other mods holding stale state.</p>
 *
 * <p>Takes a name rather than an online player: the marriage that most needs
 * ending administratively is usually one where somebody has stopped logging in.</p>
 */
public class ForceDivorceCmd extends CommandBase {

    private final RequiredArg<String> playerArg =
            withRequiredArg("player", "server.commands.marriage.force.arg.player.desc", ArgTypes.STRING);

    public ForceDivorceCmd(){
        super("divorce", "server.commands.marriage.force.divorce.desc");
        this.requirePermission(ForceCmd.PERMISSION);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context){
        String username = this.playerArg.get(context);
        UUID uuid = MarriageManager.findPlayerUuid(username);
        if(uuid == null){
            context.sendMessage(Message.translation("server.commands.marriage.force.unknownPlayer")
                    .param("username", username));
            return;
        }

        MarriageInfo marriage = MarriageAPI.getMarriage(uuid);
        if(marriage == null){
            context.sendMessage(Message.translation("server.commands.marriage.force.notMarried")
                    .param("username", MarriageManager.getKnownName(uuid)));
            return;
        }

        UUID partnerUuid = marriage.getPartnerUuid(uuid);
        String selfName = marriage.getNameOf(uuid);
        String partnerName = marriage.getPartnerName(uuid);

        if(!MarriageAPI.divorce(uuid)){
            // Only reachable when a listener cancelled the PreDivorceEvent.
            context.sendMessage(Message.translation("server.commands.marriage.force.divorce.cancelled")
                    .param("username", selfName));
            return;
        }

        // Both halves reconcile their settings component; whoever is offline does
        // it on next join through the pending divorce-sync marker.
        syncAndTell(uuid, partnerName);
        if(partnerUuid != null){
            syncAndTell(partnerUuid, selfName);
        }

        context.sendMessage(Message.translation("server.commands.marriage.force.divorce.done")
                .param("usernameOne", selfName)
                .param("usernameTwo", partnerName));
        Marriage.LOGGER.atInfo().log("An admin force-divorced " + selfName + " and " + partnerName + ".");
    }

    /** Clears one side's cached married state and tells them, if they are here. */
    private static void syncAndTell(@Nonnull UUID uuid, @Nonnull String formerPartnerName){
        PlayerRef playerRef = Universe.get().getPlayer(uuid);
        if(playerRef == null || !playerRef.isValid()){
            return;
        }
        ComponentSync.syncDivorced(playerRef);
        playerRef.sendMessage(Message.translation("server.marriage.force.divorced")
                .param("partnerUsername", formerPartnerName));
    }
}
