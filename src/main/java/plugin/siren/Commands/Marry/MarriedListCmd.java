package plugin.siren.Commands.Marry;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.API.MarriageInfo;
import plugin.siren.Marriage;
import plugin.siren.Utils.MarriageUtil;
import plugin.siren.Utils.Registry.MarriageManager;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Since 2.0.0 this lists EVERY married couple on the server from the global
 * registry - offline couples included - not just whoever happens to be online.
 */
public class MarriedListCmd extends AbstractPlayerCommand {
    public MarriedListCmd() {
        super("list", "server.commands.marry.list.desc");

        this.setPermissionGroups("hytale:None");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        List<MarriageInfo> marriages = MarriageManager.getAllMarriages();

        if(marriages.isEmpty()){
            playerRef.sendMessage(Message.translation("server.commands.marry.list.empty"));
        }else{
            playerRef.sendMessage(Message.translation("server.commands.marry.list.header").param("count", String.valueOf(marriages.size())));
            for(MarriageInfo marriage : marriages){
                boolean oneOnline = Universe.get().getPlayer(marriage.getPlayer1Uuid()) != null;
                boolean twoOnline = Universe.get().getPlayer(marriage.getPlayer2Uuid()) != null;
                String onlineCount = (oneOnline && twoOnline) ? "2" : (oneOnline || twoOnline) ? "1" : "0";

                playerRef.sendMessage(Message.translation("server.commands.marry.list.married")
                        .param("usernameOne", marriage.getPlayer1Name())
                        .param("usernameTwo", marriage.getPlayer2Name())
                        .param("duration", MarriageUtil.formatDuration(marriage.getMarriedForMillis()))
                        .param("online", onlineCount));
            }
        }

        if(Marriage.ifDebug()) {
            Marriage.LOGGER.atInfo().log(Message.translation("server.commands.marry.list.success").param("username",playerRef.getUsername()).getAnsiMessage());
        }
    }
}
