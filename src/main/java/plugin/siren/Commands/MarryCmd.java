package plugin.siren.Commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import plugin.siren.Commands.Marry.*;

public class MarryCmd extends AbstractCommandCollection {
    public MarryCmd(){
        super("marry","server.commands.marry.desc");

        this.addSubCommand(new MarryPlayerCmd());
        this.addSubCommand(new PartnerCmd());
        this.addSubCommand(new TPCmd());
        this.addSubCommand(new MessageCmd());
        this.addSubCommand(new GiftCmd());
        this.addSubCommand(new DivorceCmd());
        this.addSubCommand(new MarriedListCmd());

        // 2.1.0 - the bond and the perks it unlocks.
        this.addSubCommand(new BondCmd());
        this.addSubCommand(new HomeCmd());
        this.addSubCommand(new SetHomeCmd());
        this.addSubCommand(new ClearHomeCmd());
        this.addSubCommand(new ChestCmd());
        this.addSubCommand(new ChannelCmd());
        this.addSubCommand(new LetterCmd());
        this.addSubCommand(new RenewCmd());

        // One verb per emote rather than an "emote <kind>" argument - see EmoteCmd.
        for(plugin.siren.Utils.EmoteManager.Emote emote : plugin.siren.Utils.EmoteManager.Emote.values()){
            this.addSubCommand(new EmoteCmd(emote));
        }

        this.setPermissionGroups("hytale:None");
    }
}
