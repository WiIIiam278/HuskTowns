package me.william278.husktowns.command;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.pluginmessage.PluginMessage;
import me.william278.husktowns.data.pluginmessage.PluginMessageType;
import me.william278.husktowns.object.cache.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class TownChatCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (HuskTowns.getSettings().doTownChat()) {
            PlayerCache playerCache = HuskTowns.getPlayerCache();
            String town = playerCache.getTown(player.getUniqueId());
            if (town != null) {
                if (args.length == 0) {
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
                    return;
                }
                StringBuilder message = new StringBuilder();
                for (int i = 1; i <= args.length; i++) {
                    message.append(args[i - 1]).append(" ");
                }
                if (message.toString().contains("💲")) {
                    MessageManager.sendMessage(player, "error_town_chat_invalid_characters");
                    return;
                }

                for (String playerName : playerCache.getPlayersInTown(town)) {
                    Player chatTarget = Bukkit.getPlayer(playerName);
                    if (chatTarget != null) {
                        MessageManager.sendMessage(chatTarget, "town_chat", town, player.getName(), message.toString());
                    } else {
                        if (HuskTowns.getSettings().doBungee()) {
                            new PluginMessage(playerName, PluginMessageType.TOWN_CHAT_MESSAGE, town, player.getName(), message.toString().replaceAll("\\$", "💲")).send(player);
                        }
                    }
                }
            } else {
                MessageManager.sendMessage(player, "error_not_in_town");
            }
        } else {
            MessageManager.sendMessage(player, "error_town_chat_disabled");
        }
    }
}
