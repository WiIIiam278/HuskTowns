package me.william278.husktowns.commands.subcommands.town;

import me.william278.husktowns.MessageManager;
import me.william278.husktowns.commands.subcommands.TownSubCommand;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.town.TownRole;
import org.bukkit.entity.Player;

import java.util.StringJoiner;

public class TownFarewellSubCommand extends TownSubCommand {

    public TownFarewellSubCommand() {
        super("farewell", "husktowns.command.town.message.farewell", "<message>", TownRole.TRUSTED, "error_insufficient_message_privileges");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        if (args.length >= 1) {
            StringJoiner description = new StringJoiner(" ");
            for (String arg : args) {
                description.add(arg);
            }

            DataManager.updateTownFarewell(player, description.toString());
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", getUsage());
        }
    }
}
