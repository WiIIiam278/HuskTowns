package me.william278.husktowns.command;

import de.themoep.minedown.MineDown;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.util.PageChatList;
import me.william278.husktowns.util.UpdateChecker;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class HuskTownsCommand extends CommandBase {

    private static final HuskTowns plugin = HuskTowns.getInstance();
    private static final StringBuilder pluginInformation = new StringBuilder()
            .append("[HuskTowns](#00fb9a bold) [| Version ").append(plugin.getDescription().getVersion()).append("](#00fb9a)\n")
            .append("[").append(plugin.getDescription().getDescription()).append("](gray)\n")
            .append("[• Author:](white) [William278](gray show_text=&7Click to pay a visit open_url=https://youtube.com/William27528)\n")
            .append("[• Help Wiki:](white) [[Link]](#00fb9a show_text=&7Click to open link open_url=https://github.com/WiIIiam278/HuskTowns/wiki/)\n")
            .append("[• Report Issues:](white) [[Link]](#00fb9a show_text=&7Click to open link open_url=https://github.com/WiIIiam278/HuskTowns/issues/)\n")
            .append("[• Support Discord:](white) [[Link]](#00fb9a show_text=&7Click to join open_url=https://discord.gg/tVYhJfyDWG)");

    // Show users a list of available commands
    private void showHelpMenu(Player player, int pageNumber) {
        ArrayList<String> commandDisplay = new ArrayList<>();
        for (String command : plugin.getDescription().getCommands().keySet()) {
            String description = (String) plugin.getDescription().getCommands().get(command).get("description");
            String commandUsage = (String) plugin.getDescription().getCommands().get(command).get("usage");
            commandDisplay.add("[" + command + "](#00fb9a show_text=&#00fb9a&" + commandUsage + " suggest_command=/"  + command + ") [•](white) [" + description + "](gray)");
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
                    player.spigot().sendMessage(new MineDown(pluginInformation.toString()).toComponent());
                    break;
                case "update":
                    UpdateChecker updateChecker = new UpdateChecker(plugin);
                    if (updateChecker.isUpToDate()) {
                        player.spigot().sendMessage(new MineDown("[HuskHomes](#00fb9a bold) [| Currently running the latest version: " + updateChecker.getLatestVersion() + "](#00fb9a)").toComponent());
                    } else {
                        player.spigot().sendMessage(new MineDown("[HuskHomes](#00fb9a bold) [| A new update is available: " + updateChecker.getLatestVersion() + " (Currently running: " + updateChecker.getCurrentVersion() + ")](#00fb9a)").toComponent());
                    }
                    break;
                case "reload":
                    plugin.reloadConfigFile();
                    MessageManager.loadMessages(HuskTowns.getSettings().getLanguage());
                    MessageManager.sendMessage(player, "reload_complete");
                    break;
                default:
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
                    break;
            }
        } else {
            showHelpMenu(player, 1);
        }
    }
}
