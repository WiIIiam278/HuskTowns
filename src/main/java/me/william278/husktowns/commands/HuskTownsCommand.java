package me.william278.husktowns.commands;

import de.themoep.minedown.MineDown;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.object.cache.Cache;
import me.william278.husktowns.util.PageChatList;
import me.william278.husktowns.util.UpdateChecker;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class HuskTownsCommand extends CommandBase {

    private static final HuskTowns plugin = HuskTowns.getInstance();
    private static final StringBuilder PLUGIN_INFORMATION = new StringBuilder()
            .append("[HuskTowns](#00fb9a bold) [| Version ").append(plugin.getDescription().getVersion()).append("](#00fb9a)\n")
            .append("[").append(plugin.getDescription().getDescription()).append("](gray)\n")
            .append("[• Author:](white) [William278](gray show_text=&7Click to pay a visit open_url=https://youtube.com/William27528)\n")
            .append("[• Help Wiki:](white) [[Link]](#00fb9a show_text=&7Click to open link open_url=https://github.com/WiIIiam278/HuskTownsDocs/wiki/)\n")
            .append("[• Report Issues:](white) [[Link]](#00fb9a show_text=&7Click to open link open_url=https://github.com/WiIIiam278/HuskTownsDocs/issues/)\n")
            .append("[• Support Discord:](white) [[Link]](#00fb9a show_text=&7Click to join open_url=https://discord.gg/tVYhJfyDWG)");

    private static StringBuilder getSystemStats() {
        return new StringBuilder()
                .append("[HuskTowns](#00fb9a bold) [| Statistics](#00fb9a)\n")
                .append("[Stats about HuskTowns on your server or network](gray)\n")
                .append("[• Towns:](white) &7").append(HuskTowns.getPlayerCache().getTowns().size()).append(" [[List]](#00fb9a show_text=&7Click to view a list of towns run_command=/townlist)\n")
                .append("[• Residents:](white) &7").append(HuskTowns.getPlayerCache().getResidentCount()).append("\n")
                .append("[• Claims:](white) &7").append(HuskTowns.getClaimCache().getAllChunks().size()).append("\n")
                .append("[• Town Bonuses:](white) &7").append(HuskTowns.getTownBonusesCache().getItemsLoaded());
    }

    private static void showCacheStatusMenu(Player player) {
        StringBuilder status = new StringBuilder()
                .append("[HuskTowns](#00fb9a bold) [| Current system statuses \\(v").append(plugin.getDescription().getVersion()).append("\\):](#00fb9a show_text=&#00fb9a&Displaying the current status of the system. Hover over them to view what they mean.)");
        final ArrayList<Cache> caches = new ArrayList<>();
        StringBuilder debugString = new StringBuilder().append("version:").append(plugin.getDescription().getVersion()).append(", ");
        caches.add(HuskTowns.getClaimCache());
        caches.add(HuskTowns.getPlayerCache());
        caches.add(HuskTowns.getTownDataCache());
        caches.add(HuskTowns.getTownBonusesCache());

        for (Cache cache : caches) {
            switch (cache.getStatus()) {
                case UNINITIALIZED -> status.append("\n[• ").append(cache.getName()).append(" cache:](white) [uninitialized ✖](#ff3300 show_text=&#ff3300&This cache has not been initialized from the database by the system yet; ").append(cache.getName().toLowerCase()).append(" functions will not be available until it has been initialized.\n&7").append(cache.getItemsLoaded()).append(" item\\(s\\) loaded)");
                case UPDATING -> status.append("\n[• ").append(cache.getName()).append(" cache:](white) [updating ♦](#ff6b21 show_text=&#ff6b21&The system is currently initializing this cache and is loading data into it from the database; ").append(cache.getName().toLowerCase()).append(" functions will not be available yet.\n&7").append(cache.getItemsLoaded()).append(" item\\(s\\) loaded) [(⌚ ").append(cache.getTimeSinceInitialization()).append(" sec)](gray show_text=&7How long this cache has been processing for in seconds.)");
                case LOADED -> status.append("\n[• ").append(cache.getName()).append(" cache:](white) [loaded ✔](#00ed2f show_text=&#00ed2f&This cache has been initialized and is actively loaded. Additional data will be onboarded as necessary\n&7").append(cache.getItemsLoaded()).append(" item\\(s\\) loaded)");
                default -> status.append("\n[• ").append(cache.getName()).append(" cache:](white) [error ✖](#ff3300 show_text=&#00ed2f&This cache failed to initialize due to an error; check console logs for details\n&7").append(cache.getItemsLoaded()).append(" item\\(s\\) loaded)");
            }
            debugString.append(cache.getName().toLowerCase().replace(" ", "_")).append(":").append(cache.getStatus().toString().toLowerCase()).append(", ");
        }

        status.append("\n\n[• Database:](white) [").append(HuskTowns.getSettings().getDatabaseType().toLowerCase()).append("](gray show_text=&7The type of database you are using.)");
        debugString.append("database:").append(HuskTowns.getSettings().getDatabaseType().toLowerCase()).append(", ");
        status.append("\n[• Bungee mode:](white) [").append(HuskTowns.getSettings().doBungee()).append("](gray show_text=&7If you are using bungee mode or not.)");
        debugString.append("bungee:").append(HuskTowns.getSettings().doBungee()).append(", ");
        status.append("\n[• Cache fallback:](white) [")
                .append(HuskTowns.getSettings().isFallbackOnDatabaseIfCacheFailed()).append("](gray show_text=&7Whether or not to fallback on the ").append(HuskTowns.getSettings().getDatabaseType().toLowerCase()).append(" database if a cache fails. Off by default.)");
        debugString.append("cache_fallback:").append(HuskTowns.getSettings().isFallbackOnDatabaseIfCacheFailed());

        status.append("\n\n[•](#262626) [[⚡ Click to reload caches]](#00fb9a show_text=&#00fb9a&Click to reload cache data. This may take some time and certain functions may be unavailable while data is processed run_command=/husktowns cache reload)");
        status.append("\n[•](#262626) [[❄ Click to get debug string]](#00fb9a show_text=&#00fb9a&Click to suggest string into chat, then CTRL+A and CTRL+C to copy to clipboard. suggest_command=").append(debugString).append(")");

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
                case "stats":
                    if (!HuskTowns.getClaimCache().hasLoaded() || !HuskTowns.getPlayerCache().hasLoaded() || !HuskTowns.getTownDataCache().hasLoaded() || !HuskTowns.getTownBonusesCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "all cached");
                        return;
                    }
                    player.spigot().sendMessage(new MineDown(getSystemStats().toString()).toComponent());
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
                case "verbose":
                    if (player.hasPermission("husktowns.administrator")) {
                        if (MessageManager.isPlayerRecievingVerbatimMessages(player)) {
                            MessageManager.removeVerbatimRecipient(player);
                            MessageManager.sendMessage(player, "verbose_mode_toggle_off");
                        } else {
                            ChatMessageType type = ChatMessageType.CHAT;
                            if (args.length == 2) {
                                try {
                                    type = ChatMessageType.valueOf(args[1].toUpperCase());
                                } catch (IllegalArgumentException e) {
                                    MessageManager.sendMessage(player, "error_invalid_chat_type");
                                    return;
                                }
                            }
                            MessageManager.addVerbatimRecipient(player, type);
                            MessageManager.sendMessage(player, "verbose_mode_toggle_on");
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "status":
                case "cache":
                case "caches":
                    if (player.hasPermission("husktowns.administrator")) {
                        if (args.length == 2) {
                            if (args[1].equalsIgnoreCase("reload")) {
                                player.spigot().sendMessage(new MineDown("[HuskTowns](#00fb9a bold) [| Reloading the system caches. This may take awhile and system functions may be restricted.](#00fb9a) [(View status...)](gray show_text=&7View the status of the caches run_command=/husktowns cache)").toComponent());
                                HuskTowns.initializeCaches();
                            } else {
                                MessageManager.sendMessage(player, "error_invalid_syntax", "/husktowns cache [reload]");
                            }
                        } else {
                            showCacheStatusMenu(player);
                        }
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

    public static class HuskTownsCommandTab extends SimpleTab {
        public HuskTownsCommandTab() {
            commandTabArgs = new String[]{"help", "about", "update", "reload", "status", "stats", "verbose"};
        }
    }
}
