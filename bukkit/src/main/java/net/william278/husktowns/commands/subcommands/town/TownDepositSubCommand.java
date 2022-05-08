package net.william278.husktowns.commands.subcommands.town;

import net.william278.husktowns.MessageManager;
import net.william278.husktowns.commands.subcommands.TownSubCommand;
import net.william278.husktowns.data.DataManager;
import net.william278.husktowns.town.TownRole;
import org.bukkit.entity.Player;

public class TownDepositSubCommand extends TownSubCommand {

    public TownDepositSubCommand() {
        super("deposit", "husktowns.command.town.deposit", "<amount>", TownRole.RESIDENT, "error_not_in_town");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        if (args.length == 1) {
            try {
                double amountToDeposit = Double.parseDouble(args[0]);
                DataManager.depositMoney(player, amountToDeposit);
            } catch (NumberFormatException ex) {
                MessageManager.sendMessage(player, "error_invalid_amount");
            }
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", getUsage());
        }
    }
}
