package net.william278.husktowns.commands.subcommands.town;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.MessageManager;
import net.william278.husktowns.cache.PlayerCache;
import net.william278.husktowns.commands.TownCommand;
import net.william278.husktowns.commands.subcommands.TownSubCommand;
import net.william278.husktowns.town.TownRole;
import net.william278.husktowns.util.PageChatList;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class TownHelpSubCommand extends TownSubCommand {

    public TownHelpSubCommand() {
        super("help", "husktowns.command.town.help",
                "[page]", "?");
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
        for (TownSubCommand townSubCommand : TownCommand.subCommands) {
            final String commandDescription = townSubCommand.getDescription();
            final TownRole role = townSubCommand.requiredRole;
            if (role != null) {
                if (!playerCache.isPlayerInTown(player.getUniqueId())) {
                    continue;
                }
                if (role.weight() > playerCache.getPlayerRole(player.getUniqueId()).weight()) {
                    continue;
                }
            }
            commandDisplay.add(MessageManager.getRawMessage("town_subcommand_list_item", townSubCommand.subCommand, commandDescription));
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
