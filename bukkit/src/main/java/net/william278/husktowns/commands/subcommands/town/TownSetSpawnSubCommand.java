package net.william278.husktowns.commands.subcommands.town;

import net.william278.husktowns.commands.subcommands.TownSubCommand;
import net.william278.husktowns.data.DataManager;
import net.william278.husktowns.town.TownRole;
import org.bukkit.entity.Player;

public class TownSetSpawnSubCommand extends TownSubCommand {

    public TownSetSpawnSubCommand() {
        super("setspawn", "husktowns.command.town.spawn.set", "",
                TownRole.getLowestRoleWithPermission("setspawn"), "error_insufficient_set_spawn_privileges");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        DataManager.updateTownSpawn(player);
    }
}
