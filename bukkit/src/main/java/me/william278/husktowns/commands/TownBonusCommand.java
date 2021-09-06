package me.william278.husktowns.commands;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.town.TownBonus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;

public class TownBonusCommand extends CommandBase implements TabCompleter {

    // Cached bonus list handling
    private static final HashMap<UUID, HashMap<String, ArrayList<TownBonus>>> cachedBonusLists = new HashMap<>();

    public static ArrayList<TownBonus> getPlayerCachedBonusLists(UUID uuid, String townName) {
        return cachedBonusLists.get(uuid).get(townName);
    }

    public static boolean cachedBonusListContains(UUID uuid, String townName) {
        if (cachedBonusLists.containsKey(uuid)) {
            return cachedBonusLists.get(uuid).containsKey(townName);
        }
        return false;
    }

    public static void addCachedBonusList(UUID uuid, String townName, ArrayList<TownBonus> townBonuses) {
        if (!cachedBonusLists.containsKey(uuid)) {
            cachedBonusLists.put(uuid, new HashMap<>());
        }
        cachedBonusLists.get(uuid).put(townName, townBonuses);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length >= 2) {
            switch (args[0].toLowerCase()) {
                case "add" -> {
                    if (args.length != 4) {
                        MessageManager.sendMessage(sender, "error_invalid_syntax", "/townbonus add <town/player> <bonus claims> <bonus members>");
                        return true;
                    }
                    try {
                        String targetName = args[1];
                        int extraClaims = Integer.parseInt(args[2]);
                        int extraMembers = Integer.parseInt(args[3]);
                        UUID applierUUID;
                        if (sender instanceof Player) {
                            applierUUID = ((Player) sender).getUniqueId();
                        } else {
                            applierUUID = null;
                        }
                        TownBonus bonus = new TownBonus(applierUUID, extraClaims,
                                extraMembers, Instant.now().getEpochSecond());
                        DataManager.addTownBonus(sender, targetName, bonus);
                    } catch (NumberFormatException exception) {
                        MessageManager.sendMessage(sender, "error_invalid_syntax", "/townbonus add <town/player> <bonus claims> <bonus members>");
                    }
                    return true;
                }
                case "clear" -> {
                    DataManager.clearTownBonuses(sender, args[1]);
                    return true;
                }
                case "view", "list" -> {
                    String townName = args[1];
                    boolean useCache = false;
                    int pageNumber = 1;
                    int argIndex = 2;
                    if (args.length >= 3) {
                        if (args[argIndex].equalsIgnoreCase("-c") && sender instanceof Player) {
                            useCache = true;
                            argIndex++;
                        }
                        if (args.length >= (argIndex + 1)) {
                            try {
                                pageNumber = Integer.parseInt(args[argIndex]);
                            } catch (NumberFormatException exception) {
                                MessageManager.sendMessage(sender, "error_invalid_syntax", "/townbonus view <town/player> <page number>");
                            }
                        }
                    }
                    if (useCache) {
                        Player player = (Player) sender;
                        if (!cachedBonusListContains(player.getUniqueId(), townName)) {
                            useCache = false;
                        }
                    }
                    DataManager.sendTownBonusesList(sender, args[1], pageNumber, useCache);
                    return true;
                }
                default -> {
                    MessageManager.sendMessage(sender, "error_invalid_syntax", command.getUsage());
                    return true;
                }
            }
        } else {
            MessageManager.sendMessage(sender, "error_invalid_syntax", command.getUsage());
        }
        return true;
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
    }

    final static String[] COMMAND_TAB_ARGS = {"add", "clear", "view"};

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
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
        } else if (args.length == 2) {
            if (HuskTowns.getPlayerCache().getPlayerTown(p.getUniqueId()) == null) {
                return Collections.emptyList();
            }
            final List<String> arg1TabComp = new ArrayList<>();
            if (HuskTowns.getPlayerCache().getTowns().isEmpty()) {
                return Collections.emptyList();
            }
            StringUtil.copyPartialMatches(args[1], HuskTowns.getPlayerCache().getTowns(), arg1TabComp);
            Collections.sort(arg1TabComp);
            return arg1TabComp;
        } else {
            return Collections.emptyList();
        }
    }
}
