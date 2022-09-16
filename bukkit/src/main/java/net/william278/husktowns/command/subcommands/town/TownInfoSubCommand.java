package net.william278.husktowns.command.subcommands.town;

import net.william278.husktowns.command.subcommands.TownSubCommand;
import net.william278.husktowns.data.DataManager;
import org.bukkit.entity.Player;

public class TownInfoSubCommand extends TownSubCommand {

    public TownInfoSubCommand() {
        super("info", "husktowns.command.town.info",
                "", "view", "about", "check");
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
