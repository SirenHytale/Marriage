package plugin.siren.Commands.Marriage;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

/**
 * {@code /marriage force …} — the admin operations {@code MarriageAPI} has always
 * exposed, finally reachable from a console or a chat box.
 *
 * <p>A collection rather than a {@code force} command taking a mode argument,
 * because this mod does not use flag-style arguments: the tier above the flag is
 * promoted to a real subcommand, so it is {@code /marriage force divorce Bob}
 * rather than {@code /marriage force --action=divorce}.</p>
 *
 * <p>Every child re-checks {@code marriage.admin.force} for itself. Subcommands
 * do inherit their parent's permission, but these three each end a marriage or
 * create one, and a permission that has to be revoked in one place should not be
 * revocable in only one place.</p>
 */
public class ForceCmd extends AbstractCommandCollection {

    public static final String PERMISSION = "marriage.admin.force";

    public ForceCmd(){
        super("force", "server.commands.marriage.force.desc");

        this.addSubCommand(new ForceMarryCmd());
        this.addSubCommand(new ForceDivorceCmd());
        this.addSubCommand(new ForceInfoCmd());

        this.requirePermission(PERMISSION);
        // empty (not null): do not inherit MarriageCmd's hytale:None group, which would grant this node to everyone
        this.setPermissionGroups();
    }
}
