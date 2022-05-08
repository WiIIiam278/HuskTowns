package net.william278.husktowns.commands.subcommands.town;

import net.william278.husktowns.MessageManager;
import net.william278.husktowns.commands.subcommands.TownSubCommand;
import net.william278.husktowns.data.DataManager;
import org.bukkit.entity.Player;

public class TownCreateSubCommand extends TownSubCommand {

    public TownCreateSubCommand() {
        super("create", "husktowns.command.town.create", "<name>", "found");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        if (args.length == 1) {
            String townName = args[0];
            DataManager.createTown(player, townName);
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", getUsage());
        }
    }
}
