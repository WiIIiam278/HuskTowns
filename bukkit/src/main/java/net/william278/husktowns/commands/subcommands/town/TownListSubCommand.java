package net.william278.husktowns.commands.subcommands.town;

import net.william278.husktowns.commands.subcommands.TownSubCommand;
import org.bukkit.entity.Player;

public class TownListSubCommand extends TownSubCommand {

    public TownListSubCommand() {
        super("list", "husktowns.command.town.list", "[sort by]");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        if (args.length == 1) {
            player.performCommand("townlist " + args[0]);
        } else {
            player.performCommand("townlist");
        }
    }
}
