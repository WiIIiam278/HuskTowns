package net.william278.husktowns.commands.subcommands.town;

import net.william278.husktowns.commands.subcommands.TownSubCommand;
import net.william278.husktowns.data.DataManager;
import org.bukkit.entity.Player;

public class TownSettingsSubCommand extends TownSubCommand {

    public TownSettingsSubCommand() {
        super("settings", "husktowns.command.town.settings", "[town]", "config", "flags", "prefs", "preferences");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        if (args.length == 1) {
            DataManager.sendTownSettings(player, args[0]);
        } else {
            DataManager.sendTownSettings(player);
        }
    }
}
