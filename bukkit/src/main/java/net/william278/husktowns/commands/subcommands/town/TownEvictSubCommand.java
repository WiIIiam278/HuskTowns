package net.william278.husktowns.commands.subcommands.town;

import net.william278.husktowns.MessageManager;
import net.william278.husktowns.commands.subcommands.TownSubCommand;
import net.william278.husktowns.data.DataManager;
import net.william278.husktowns.town.TownRole;
import org.bukkit.entity.Player;

public class TownEvictSubCommand extends TownSubCommand {

    public TownEvictSubCommand() {
        super("evict", "husktowns.command.town.kick", "<player>", TownRole.TRUSTED, "error_insufficient_evict_privileges", "kick");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        if (args.length == 1) {
            String playerToEvict = args[0];
            DataManager.evictPlayerFromTown(player, playerToEvict);
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", getUsage());
        }
    }
}
