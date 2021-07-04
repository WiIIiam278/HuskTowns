package me.william278.husktowns.commands;

import de.themoep.minedown.MineDown;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.util.PageChatList;
import me.william278.husktowns.util.UpdateChecker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HuskTownsCommand extends CommandBase {

    private static final HuskTowns plugin = HuskTowns.getInstance();
    private static final StringBuilder PLUGIN_INFORMATION = new StringBuilder()
            .append("[HuskTowns](#00fb9a bold) [| Version ").append(plugin.getDescription().getVersion()).append("](#00fb9a)\n")
            .append("[").append(plugin.getDescription().getDescription()).append("](gray)\n")
            .append("[• Author:](white) [William278](gray show_text=&7Click to pay a visit open_url=https://youtube.com/William27528)\n")
            .append("[• Help Wiki:](white) [[Link]](#00fb9a show_text=&7Click to open link open_url=https://github.com/WiIIiam278/HuskTownsDocs/wiki/)\n")
            .append("[• Report Issues:](white) [[Link]](#00fb9a show_text=&7Click to open link open_url=https://github.com/WiIIiam278/HuskTownsDocs/issues/)\n")
            .append("[• Support Discord:](white) [[Link]](#00fb9a show_text=&7Click to join open_url=https://discord.gg/tVYhJfyDWG)");

    // Show users a list of available commands
    public static void showHelpMenu(Player player, int pageNumber) {
        ArrayList<String> commandDisplay = new ArrayList<>();
        for (String command : plugin.getDescription().getCommands().keySet()) {
            if (HuskTowns.getSettings().hideCommandsFromHelpMenuWithoutPermission()) {
                if (!player.hasPermission((String) plugin.getDescription().getCommands().get(command).get("permission"))) {
                    continue;
                }
            }
            if (command.equals("husktowns") && HuskTowns.getSettings().hideHuskTownsCommandFromHelpMenu()) {
                continue;
            }
            String description = (String) plugin.getDescription().getCommands().get(command).get("description");
            String commandUsage = (String) plugin.getDescription().getCommands().get(command).get("usage");
            commandDisplay.add(MessageManager.getRawMessage("command_list_item", command, commandUsage, description));
        }

        MessageManager.sendMessage(player, "command_list_header");
        PageChatList helpList = new PageChatList(commandDisplay, 10, "/husktowns help");
        if (helpList.doesNotContainPage(pageNumber)) {
            MessageManager.sendMessage(player, "error_invalid_page_number");
            return;
        }
        player.spigot().sendMessage(helpList.getPage(pageNumber));
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length >= 1) {
            switch (args[0]) {
                case "help":
                    if (args.length == 2) {
                        try {
                            int pageNo = Integer.parseInt(args[1]);
                            showHelpMenu(player, pageNo);
                        } catch (NumberFormatException ex) {
                            MessageManager.sendMessage(player, "error_invalid_page_number");
                        }
                    } else {
                        showHelpMenu(player, 1);
                    }
                    break;
                case "about":
                case "info":
                    player.spigot().sendMessage(new MineDown(PLUGIN_INFORMATION.toString()).toComponent());
                    break;
                case "update":
                    if (player.hasPermission("husktowns.administrator")) {
                        UpdateChecker updateChecker = new UpdateChecker(plugin);
                        if (updateChecker.isUpToDate()) {
                            player.spigot().sendMessage(new MineDown("[HuskTowns](#00fb9a bold) [| Currently running the latest version: " + updateChecker.getLatestVersion() + "](#00fb9a)").toComponent());
                        } else {
                            player.spigot().sendMessage(new MineDown("[HuskTowns](#00fb9a bold) [| A new update is available: " + updateChecker.getLatestVersion() + " (Currently running: " + updateChecker.getCurrentVersion() + ")](#00fb9a)").toComponent());
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "reload":
                    if (player.hasPermission("husktowns.administrator")) {
                        plugin.reloadConfigFile();
                        MessageManager.loadMessages(HuskTowns.getSettings().getLanguage());
                        MessageManager.sendMessage(player, "reload_complete");
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                default:
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
                    break;
            }
        } else {
            showHelpMenu(player, 1);
        }
    }

    public static class HuskTownsTab implements TabCompleter {
        final static String[] COMMAND_TAB_ARGS = {"help", "about", "update", "reload"};

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            Player p = (Player) sender;
            if (command.getPermission() != null) {
                if (!p.hasPermission(command.getPermission())) {
                    return Collections.emptyList();
                }
            }
            if (args.length == 1) {
                final List<String> tabCompletions = new ArrayList<>();
                StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMAND_TAB_ARGS), tabCompletions);
                Collections.sort(tabCompletions);
                return tabCompletions;
            } else {
                return Collections.emptyList();
            }
        }
    }
}
