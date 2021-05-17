package me.william278.husktowns.command;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.Locale;

public class TownBonusCommand extends CommandBase {

    // /townbonus <set|clear> <town> <claims|members> <new maximum>

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length == 4) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "set":
                    if (!HuskTowns.getPlayerCache().getTowns().contains(args[1])) {
                        MessageManager.sendMessage(player, "error_invalid_town");
                        return;
                    }
                    return;
                case "clear":
                    if (!HuskTowns.getPlayerCache().getTowns().contains(args[1])) {
                        MessageManager.sendMessage(player, "error_invalid_town");
                        return;
                    }

                    return;
                default:
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
                    return;
            }
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
        }
    }
}
