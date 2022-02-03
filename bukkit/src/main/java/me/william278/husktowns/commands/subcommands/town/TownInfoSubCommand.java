package me.william278.husktowns.commands.subcommands.town;

import me.william278.husktowns.commands.subcommands.TownSubCommand;
import me.william278.husktowns.data.DataManager;
import org.bukkit.entity.Player;

public class TownInfoSubCommand extends TownSubCommand {

    public TownInfoSubCommand() {
        super("info", "husktowns.command.town.info", "", "view", "about", "check");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        if (args.length == 1) {
            DataManager.sendTownInfoMenu(player, args[0]);
        } else {
            DataManager.sendTownInfoMenu(player);
        }
    }
}
