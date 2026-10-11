package plugin.siren.Commands.Marriage;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import plugin.siren.Utils.Bond.BondManager;
import plugin.siren.Utils.HopeChestManager;
import plugin.siren.Utils.MarriageUtil;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;
import plugin.siren.Utils.Registry.PlayerTally;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * {@code /marriage force info <player>} — everything the server knows about one
 * player's marriage, for answering a support ticket without opening the JSON.
 *
 * <p>Read-only, and deliberately the most detailed surface in the mod: it reports
 * the marriage, the bond, the band, the home, the chest and the lifetime tallies,
 * including for a player who is not online and one who is not married at all.</p>
 */
public class ForceInfoCmd extends CommandBase {

    private final RequiredArg<String> playerArg =
            withRequiredArg("player", "server.commands.marriage.force.arg.player.desc", ArgTypes.STRING);

    public ForceInfoCmd(){
        super("info", "server.commands.marriage.force.info.desc");
        this.requirePermission(ForceCmd.PERMISSION);
        // empty (not null): never inherit a parent's group, which would grant this node to everyone
        this.setPermissionGroups();
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

        String name = MarriageManager.getKnownName(uuid);
        boolean online = Universe.get().getPlayer(uuid) != null;

        context.sendMessage(Message.translation("server.commands.marriage.force.info.header")
                .param("username", name)
                .param("uuid", uuid.toString())
                .param("status", Message.translation(online
                        ? "server.commands.marriage.force.info.online"
                        : "server.commands.marriage.force.info.offline")));

        MarriageRecord record = MarriageManager.getRecord(uuid);
        if(record == null){
            context.sendMessage(Message.translation("server.commands.marriage.force.info.unmarried"));
        }else{
            UUID partnerUuid = record.getPartnerUuid(uuid);

            context.sendMessage(Message.translation("server.commands.marriage.force.info.partner")
                    .param("username", record.getPartnerName(uuid))
                    .param("duration", MarriageUtil.formatDuration(
                            Math.max(0L, System.currentTimeMillis() - record.getMarriedAtMillis()))));

            context.sendMessage(Message.translation("server.commands.marriage.force.info.bond")
                    .param("level", String.valueOf(record.getBondLevel()))
                    .param("xp", String.valueOf(Math.round(record.getBondXp())))
                    .param("percent", String.valueOf(Math.round(BondManager.progress(record) * 100f)))
                    .param("ring", Message.translation(record.getRingTier().getNameKey())));

            context.sendMessage(Message.translation("server.commands.marriage.force.info.stats")
                    .param("gifts", String.valueOf(record.getGiftsFrom(uuid)))
                    .param("received", String.valueOf(partnerUuid == null ? 0 : record.getGiftsFrom(partnerUuid)))
                    .param("teleports", String.valueOf(record.getTeleportsUsed()))
                    .param("messages", String.valueOf(record.getMessagesSent()))
                    .param("together", MarriageUtil.formatDuration(record.getTogetherMinutes() * 60_000L)));

            context.sendMessage(Message.translation("server.commands.marriage.force.info.holdings")
                    .param("home", record.hasHome()
                            ? record.getHomeWorld() + " " + Math.round(record.getHomeX())
                                    + ", " + Math.round(record.getHomeY())
                                    + ", " + Math.round(record.getHomeZ())
                            : "-")
                    .param("chest", String.valueOf(HopeChestManager.usedSlots(record)))
                    .param("pvp", String.valueOf(record.isPvpProtection())));
        }

        PlayerTally tally = MarriageManager.getTally(uuid);
        if(tally == null){
            context.sendMessage(Message.translation("server.commands.marriage.force.info.noHistory"));
            return;
        }
        context.sendMessage(Message.translation("server.commands.marriage.force.info.lifetime")
                .param("marriages", String.valueOf(tally.getMarriages()))
                .param("divorces", String.valueOf(tally.getDivorces()))
                .param("gifts", String.valueOf(tally.getGiftsGiven()))
                .param("longest", tally.getLongestMarriageMillis() > 0L
                        ? MarriageUtil.formatDuration(tally.getLongestMarriageMillis())
                        : "-"));
    }
}
