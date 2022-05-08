package net.william278.husktowns.commands.subcommands.town;

import net.william278.husktowns.commands.subcommands.TownSubCommand;
import net.william278.husktowns.data.DataManager;
import org.bukkit.entity.Player;

public class TownSpawnSubCommand extends TownSubCommand {

    public TownSpawnSubCommand() {
        super("spawn", "husktowns.command.town.spawn", "[town]", "warp");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        if (args.length == 1) {
            DataManager.teleportPlayerToOtherSpawn(player, args[0]);
        } else {
            DataManager.teleportPlayerToSpawn(player);
        }
    }
}
