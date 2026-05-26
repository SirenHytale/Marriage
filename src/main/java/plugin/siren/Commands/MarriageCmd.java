package plugin.siren.Commands;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import plugin.siren.Commands.Marriage.*;

public class MarriageCmd extends AbstractCommandCollection {
    public MarriageCmd(){
        super("marriage","server.commands.marriage.desc");

        this.addSubCommand(new RequireRingCmd());

        this.requirePermission("marriage.admin");
    }
}
