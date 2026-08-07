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
import plugin.siren.Utils.Bond.BondManager;
import plugin.siren.Utils.Bond.BondPerk;
import plugin.siren.Utils.MarriageUtil;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;

import javax.annotation.Nonnull;
import java.awt.Color;

/**
 * {@code /marry bond} - how deep the bond is, and what it has unlocked.
 *
 * <p>The chat counterpart to the menu's Bond page, for players who would rather
 * not open a menu, and the only way to read the bond at all on a server that has
 * turned the menu off.</p>
 */
public class BondCmd extends AbstractPlayerCommand {
    public BondCmd() {
        super("bond", "server.commands.marry.bond.desc");

        this.setPermissionGroups("hytale:None");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        MarriageRecord record = MarriageManager.getRecord(playerRef.getUuid());
        if(record == null){
            playerRef.sendMessage(Message.translation("server.marriage.bond.unmarried"));
            return;
        }

        if(!Marriage.getConfig().get().ifBondEnabled()){
            playerRef.sendMessage(Message.translation("server.marriage.bond.disabled"));
        }else if(BondManager.isMaxLevel(record)){
            playerRef.sendMessage(Message.translation("server.marriage.bond.summaryMax")
                    .param("level", String.valueOf(record.getBondLevel()))
                    .color(Color.PINK));
        }else{
            playerRef.sendMessage(Message.translation("server.marriage.bond.summary")
                    .param("level", String.valueOf(record.getBondLevel()))
                    .param("xp", String.valueOf(Math.round(BondManager.xpIntoLevel(record))))
                    .param("needed", String.valueOf(Math.round(BondManager.xpForNextLevel(record))))
                    .color(Color.PINK));
        }

        playerRef.sendMessage(Message.translation("server.marriage.bond.together")
                .param("duration", MarriageUtil.formatDuration(record.getTogetherMinutes() * 60_000L)));

        int nextAnniversary = BondManager.nextAnniversaryDays(record);
        if(nextAnniversary > 0){
            playerRef.sendMessage(Message.translation("server.marriage.bond.nextAnniversary")
                    .param("days", String.valueOf(nextAnniversary - BondManager.daysMarried(record))));
        }

        for(BondPerk perk : BondPerk.values()){
            if(!perk.isEnabled()){
                continue;
            }
            boolean unlocked = BondManager.hasPerk(record, perk);
            playerRef.sendMessage(Message.translation(unlocked
                            ? "server.marriage.bond.perkLine.unlocked"
                            : "server.marriage.bond.perkLine.locked")
                    .param("perk", Message.translation(perk.getNameKey()))
                    .param("level", String.valueOf(BondManager.requiredLevelFor(record, perk))));
        }
    }
}
