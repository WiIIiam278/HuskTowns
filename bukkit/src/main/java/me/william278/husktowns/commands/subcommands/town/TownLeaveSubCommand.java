package me.william278.husktowns.commands.subcommands.town;

import me.william278.husktowns.commands.subcommands.TownSubCommand;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.town.TownRole;
import org.bukkit.entity.Player;

public class TownLeaveSubCommand extends TownSubCommand {

    public TownLeaveSubCommand() {
        super("leave", "husktowns.command.town.leave", "", TownRole.RESIDENT, "error_not_in_town");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        DataManager.leaveTown(player);
    }
}
