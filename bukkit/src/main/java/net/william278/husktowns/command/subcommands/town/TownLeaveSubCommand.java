package net.william278.husktowns.command.subcommands.town;

import net.william278.husktowns.command.subcommands.TownSubCommand;
import net.william278.husktowns.data.DataManager;
import net.william278.husktowns.town.TownRole;
import org.bukkit.entity.Player;

public class TownLeaveSubCommand extends TownSubCommand {

    public TownLeaveSubCommand() {
        super("leave", "husktowns.command.town.leave", "", TownRole.getDefaultRole(), "error_not_in_town");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        DataManager.leaveTown(player);
    }
}
