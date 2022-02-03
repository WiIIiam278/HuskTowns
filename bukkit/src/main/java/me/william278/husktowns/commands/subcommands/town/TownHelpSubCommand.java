package me.william278.husktowns.commands.subcommands.town;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.cache.PlayerCache;
import me.william278.husktowns.commands.TownCommand;
import me.william278.husktowns.commands.subcommands.TownSubCommand;
import me.william278.husktowns.town.TownRole;
import me.william278.husktowns.util.PageChatList;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class TownHelpSubCommand extends TownSubCommand {

    public TownHelpSubCommand() {
        super("help", "husktowns.command.town.help", "[page]", "?");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        if (args.length == 1) {
            try {
                int pageNo = Integer.parseInt(args[0]);
                showTownHelpMenu(player, pageNo);
            } catch (NumberFormatException ex) {
                MessageManager.sendMessage(player, "error_invalid_page_number");
            }
        } else {
            showTownHelpMenu(player, 1);
        }
    }

    // Shows users a list of usable commands
    private void showTownHelpMenu(Player player, int pageNumber) {
        final ArrayList<String> commandDisplay = new ArrayList<>();
        final PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (!playerCache.hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", playerCache.getName());
            return;
        }
        for (HashMap<String, TownRole> commandRoles : TownCommand.townCommands.keySet()) {
            final String commandDescription = TownCommand.townCommands.get(commandRoles);
            for (String command : commandRoles.keySet()) {
                final TownRole role = commandRoles.get(command);
                if (role != null) {
                    if (!playerCache.isPlayerInTown(player.getUniqueId())) {
                        break;
                    }
                    if (role == TownRole.TRUSTED) {
                        if (playerCache.getPlayerRole(player.getUniqueId()) == TownRole.RESIDENT) {
                            break;
                        }
                    }
                    if (role == TownRole.MAYOR) {
                        if (playerCache.getPlayerRole(player.getUniqueId()) != TownRole.MAYOR) {
                            break;
                        }
                    }
                }
                commandDisplay.add(MessageManager.getRawMessage("town_subcommand_list_item", command, commandDescription));
                break;
            }
        }

        MessageManager.sendMessage(player, "town_subcommand_list_header");
        PageChatList townHelpList = new PageChatList(commandDisplay, 10, "/town help");
        if (townHelpList.doesNotContainPage(pageNumber)) {
            MessageManager.sendMessage(player, "error_invalid_page_number");
            return;
        }
        player.spigot().sendMessage(townHelpList.getPage(pageNumber));
    }
}
