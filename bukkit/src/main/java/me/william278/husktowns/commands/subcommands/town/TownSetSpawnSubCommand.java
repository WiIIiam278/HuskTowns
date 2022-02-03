package me.william278.husktowns.commands.subcommands.town;

import me.william278.husktowns.commands.subcommands.TownSubCommand;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.town.TownRole;
import org.bukkit.entity.Player;

public class TownSetSpawnSubCommand extends TownSubCommand {

    public TownSetSpawnSubCommand() {
        super("setspawn", "husktowns.command.town.spawn.set", "", TownRole.TRUSTED, "error_insufficient_set_spawn_privileges");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        DataManager.updateTownSpawn(player);
    }
}
