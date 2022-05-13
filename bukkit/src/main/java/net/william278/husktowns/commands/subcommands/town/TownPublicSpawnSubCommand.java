package net.william278.husktowns.commands.subcommands.town;

import net.william278.husktowns.commands.subcommands.TownSubCommand;
import net.william278.husktowns.data.DataManager;
import net.william278.husktowns.town.TownRole;
import org.bukkit.entity.Player;

public class TownPublicSpawnSubCommand extends TownSubCommand {

    public TownPublicSpawnSubCommand() {
        super("publicspawn", "husktowns.command.town.spawn.privacy", "",
                TownRole.getLowestRoleWithPermission(TownRole.RolePrivilege.PUBLICSPAWN), "error_insufficient_spawn_privacy_privileges", "privatespawn", "publicwarp", "privatewarp");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        DataManager.toggleTownPrivacy(player);
    }
}
