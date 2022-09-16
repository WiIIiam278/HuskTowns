package net.william278.husktowns.command.subcommands;

import net.william278.husktowns.HuskTowns;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.StringJoiner;

public class ShortcutTownSubCommand extends TownSubCommand {

    public final String shortcutFor;

    public ShortcutTownSubCommand(String subCommand, String shortcutFor, String args, String... aliases) {
        super(subCommand, null, args, aliases);
        this.shortcutFor = shortcutFor;
    }

    @Override
    public void onExecute(Player player, String[] args) {
        final StringJoiner commandArgs = new StringJoiner(" ");
        for (String arg : args) {
            commandArgs.add(arg);
        }

        player.performCommand(shortcutFor + " " + commandArgs);
    }

    @Override
    public String getDescription() {
        final Command command = HuskTowns.getInstance().getCommand(shortcutFor);
        if (command != null) {
            return command.getDescription();
        }
        return super.getDescription();
    }
}
