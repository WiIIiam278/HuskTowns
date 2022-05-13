package net.william278.husktowns.commands.subcommands.town;

import net.william278.husktowns.MessageManager;
import net.william278.husktowns.commands.subcommands.TownSubCommand;
import net.william278.husktowns.data.DataManager;
import net.william278.husktowns.town.TownRole;
import org.bukkit.entity.Player;

public class TownRenameSubCommand extends TownSubCommand {

    public TownRenameSubCommand() {
        super("rename", "husktowns.command.town.rename", "[new_name]",
                TownRole.getLowestRoleWithPermission(TownRole.RolePrivilege.RENAME), "error_insufficient_rename_privileges");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        if (args.length == 1) {
            final String townName = args[0];
            DataManager.renameTown(player, townName);
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", getUsage());
        }
    }
}
