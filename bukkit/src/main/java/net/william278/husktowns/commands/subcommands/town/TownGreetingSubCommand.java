package net.william278.husktowns.commands.subcommands.town;

import net.william278.husktowns.MessageManager;
import net.william278.husktowns.commands.subcommands.TownSubCommand;
import net.william278.husktowns.data.DataManager;
import net.william278.husktowns.town.TownRole;
import org.bukkit.entity.Player;

import java.util.StringJoiner;

public class TownGreetingSubCommand extends TownSubCommand {

    public TownGreetingSubCommand() {
        super("greeting", "husktowns.command.town.message.greeting", "<message>", TownRole.TRUSTED, "error_insufficient_message_privileges");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        if (args.length >= 1) {
            StringJoiner description = new StringJoiner(" ");
            for (String arg : args) {
                description.add(arg);
            }

            DataManager.updateTownGreeting(player, description.toString());
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", getUsage());
        }
    }
}
