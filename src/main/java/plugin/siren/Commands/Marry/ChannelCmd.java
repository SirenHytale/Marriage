package plugin.siren.Commands.Marry;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Systems.MarriageSettingsComponent;
import plugin.siren.Utils.Registry.MarriageManager;

import javax.annotation.Nonnull;
import java.awt.Color;

/**
 * {@code /marry channel} - toggles the couple chat channel.
 *
 * <p>A toggle rather than a per-line prefix, which is the whole point: it exists
 * so {@code /marry msg} does not have to be retyped every line.</p>
 */
public class ChannelCmd extends AbstractPlayerCommand {
    public ChannelCmd() {
        super("channel", "server.commands.marry.channel.desc");

        if(Marriage.getConfig().get().ifCmdPermission()){
            this.requirePermission("marriage.msg");
        }else{
            this.setPermissionGroups("hytale:None");
        }
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if(!Marriage.getConfig().get().ifCoupleChannelEnabled()){
            playerRef.sendMessage(Message.translation("server.marriage.channel.disabled"));
            return;
        }
        if(!MarriageManager.isMarried(playerRef.getUuid())){
            playerRef.sendMessage(Message.translation("server.marriage.channel.unmarried"));
            return;
        }

        MarriageSettingsComponent settings = store.getComponent(ref, MarriageSettingsComponent.getComponentType());
        if(settings == null){
            return;
        }

        boolean on = !settings.isCoupleChannel();
        settings.setCoupleChannel(on);
        MarriageManager.setCoupleChannelOn(playerRef.getUuid(), on);

        playerRef.sendMessage(Message.translation(on
                ? "server.marriage.channel.on"
                : "server.marriage.channel.off").color(Color.PINK));
    }
}
