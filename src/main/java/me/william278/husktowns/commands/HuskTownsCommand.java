package me.william278.husktowns.commands;

import de.themoep.minedown.MineDown;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.object.cache.Cache;
import me.william278.husktowns.util.PageChatList;
import me.william278.husktowns.util.UpdateChecker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class HuskTownsCommand extends CommandBase {

    private static final HuskTowns plugin = HuskTowns.getInstance();
    private static final StringBuilder PLUGIN_INFORMATION = new StringBuilder()
            .append("[HuskTowns](#00fb9a bold) [| Version ").append(plugin.getDescription().getVersion()).append("](#00fb9a)\n")
            .append("[").append(plugin.getDescription().getDescription()).append("](gray)\n")
            .append("[• Author:](white) [William278](gray show_text=&7Click to pay a visit open_url=https://youtube.com/William27528)\n")
            .append("[• Help Wiki:](white) [[Link]](#00fb9a show_text=&7Click to open link open_url=https://github.com/WiIIiam278/HuskTownsDocs/wiki/)\n")
            .append("[• Report Issues:](white) [[Link]](#00fb9a show_text=&7Click to open link open_url=https://github.com/WiIIiam278/HuskTownsDocs/issues/)\n")
            .append("[• Support Discord:](white) [[Link]](#00fb9a show_text=&7Click to join open_url=https://discord.gg/tVYhJfyDWG)");

    public static void showCacheStatusMenu(Player player) {
        StringBuilder status = new StringBuilder()
                .append("[HuskTowns](#00fb9a bold) [| Current system statuses \\(v").append(plugin.getDescription().getVersion()).append("\\):](#00fb9a show_text=&#00fb9a&Displaying the current status of the system. Hover over them to view what they mean.)");
        final ArrayList<Cache> caches = new ArrayList<>();
        StringBuilder debugString = new StringBuilder().append("version:").append(plugin.getDescription().getVersion()).append(", ");
        caches.add(HuskTowns.getClaimCache());
        caches.add(HuskTowns.getPlayerCache());
        caches.add(HuskTowns.getTownMessageCache());
        caches.add(HuskTowns.getTownBonusesCache());

        for (Cache cache : caches) {
            switch (cache.getStatus()) {
                case UNINITIALIZED:
                    status.append("\n[• ").append(cache.getName()).append(" cache:](white) [uninitialized](#ff3300 show_text=&#ff3300&This cache has not been initialized from the database by the system yet; ").append(cache.getName().toLowerCase(Locale.ROOT)).append(" functions will not be available until it has been initialized.)");
                    break;
                case UPDATING:
                    status.append("\n[• ").append(cache.getName()).append(" cache:](white) [updating](#ff6b21 show_text=&#ff6b21&The system is currently initializing this cache and is loading data into it from the database; ").append(cache.getName().toLowerCase(Locale.ROOT)).append(" functions will not be available yet.\nProcess time: )").append(cache.getTimeSinceInitialization()).append(" sec");
                    break;
                case LOADED:
                    status.append("\n[• ").append(cache.getName()).append(" cache:](white) [loaded](#00ed2f show_text=&#00ed2f&This cache has been initialized and is actively loaded. Additional data will be onboarded as necessary)");
                    break;
            }
            debugString.append(cache.getName().toLowerCase(Locale.ROOT).replace(" ", "_")).append(":").append(cache.getStatus().toString().toLowerCase(Locale.ROOT)).append(", ");
        }

        status.append("\n\n[• Database:](white) [").append(HuskTowns.getSettings().getDatabaseType().toLowerCase(Locale.ROOT)).append("](gray show_text=&7The type of database you are using.)");
        debugString.append("database:").append(HuskTowns.getSettings().getDatabaseType().toLowerCase(Locale.ROOT)).append(", ");
        status.append("\n[• Bungee mode:](white) [").append(HuskTowns.getSettings().doBungee()).append("](gray show_text=&7If you are using bungee mode or not.)");
        debugString.append("bungee:").append(HuskTowns.getSettings().doBungee()).append(", ");
        status.append("\n[• Cache fallback:](white) [")
                .append(HuskTowns.getSettings().isFallbackOnDatabaseIfCacheFailed()).append("](gray show_text=&7Whether or not in the event a value fails to return from the cache should the system attempt to grab the required data from the").append(HuskTowns.getSettings().getDatabaseType().toLowerCase(Locale.ROOT)).append(" database as a fallback? This is off by default and can be enabled in the config.)");
        debugString.append("cache_fallback:").append(HuskTowns.getSettings().isFallbackOnDatabaseIfCacheFailed());

        status.append("\n[⎘ Click to get debug string](#00fb9a show_text=&#00fb9a&Click to suggest string into chat, then CTRL+A and CTRL+C to copy to clipboard. suggest_command=").append(debugString).append(")");

        player.spigot().sendMessage(new MineDown(status.toString()).toComponent());
    }

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
                case "status":
                    if (player.hasPermission("husktowns.administrator")) {
                        showCacheStatusMenu(player);
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
        final static String[] COMMAND_TAB_ARGS = {"help", "about", "update", "reload", "status"};

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
