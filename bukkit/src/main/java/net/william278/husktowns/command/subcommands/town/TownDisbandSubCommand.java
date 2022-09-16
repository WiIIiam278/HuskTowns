package net.william278.husktowns.command.subcommands.town;

import net.william278.husktowns.MessageManager;
import net.william278.husktowns.command.subcommands.TownSubCommand;
import net.william278.husktowns.data.DataManager;
import net.william278.husktowns.town.TownRole;
import org.bukkit.entity.Player;

public class TownDisbandSubCommand extends TownSubCommand {

    public TownDisbandSubCommand() {
        super("disband", "husktowns.command.town.disband", "[confirm]",
                TownRole.getMayorRole(), "error_insufficient_disband_privileges", "delete");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        if (args.length == 0) {
            MessageManager.sendMessage(player, "disband_town_confirm");
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("confirm")) {
                DataManager.disbandTown(player);
            } else {
                MessageManager.sendMessage(player, "error_invalid_syntax", getUsage());
            }
        }
    }
}
