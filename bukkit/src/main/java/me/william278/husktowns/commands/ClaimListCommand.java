package me.william278.husktowns.commands;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.town.Town;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class ClaimListCommand extends CommandBase {

    // Cached claim list handling
    private static final HashMap<UUID,HashMap<String, Town>> cachedClaimLists = new HashMap<>();
    public static Town getPlayerCachedClaimLists(UUID uuid, String townName) {
        return cachedClaimLists.get(uuid).get(townName);
    }
    public static boolean cachedClaimListContains(UUID uuid, String townName) {
        if (cachedClaimLists.containsKey(uuid)) {
            return cachedClaimLists.get(uuid).containsKey(townName);
        }
        return false;
    }
    public static void addCachedClaimList(UUID uuid, String townName, Town town) {
        if (!cachedClaimLists.containsKey(uuid)) {
            cachedClaimLists.put(uuid, new HashMap<>());
        }
        cachedClaimLists.get(uuid).put(townName, town);
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        int pageNumber = 1;
        boolean useCache = false;
        if (args.length < 1) {
            DataManager.showClaimList(player, pageNumber);
            return;
        }
        String townName = args[0];
        if (args.length >= 2) {
            int argIndex = 1;
            if (args[1].equalsIgnoreCase("-c")) {
                useCache = true;
                argIndex++;
            }
            if (args.length >= argIndex + 1) {
                try {
                    pageNumber = Integer.parseInt(args[argIndex]);
                } catch (NumberFormatException ex) {
                    MessageManager.sendMessage(player, "error_invalid_page_number");
                }
            }
        }
        if (useCache) {
            if (!cachedClaimListContains(player.getUniqueId(), townName)) {
                useCache = false;
            }
        }
        DataManager.showClaimList(player, townName, pageNumber, useCache);
    }

    public static class TownListTab implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            Player p = (Player) sender;
            if (command.getPermission() != null) {
                if (!p.hasPermission(command.getPermission())) {
                    return Collections.emptyList();
                }
            }
            if (args.length == 1) {
                if (!HuskTowns.getPlayerCache().hasLoaded()) {
                    return Collections.emptyList();
                }
                if (HuskTowns.getPlayerCache().getPlayerTown(p.getUniqueId()) == null) {
                    return Collections.emptyList();
                }
                final List<String> arg1TabComp = new ArrayList<>();
                final HashSet<String> towns = HuskTowns.getPlayerCache().getTowns();
                if (towns == null) {
                    return Collections.emptyList();
                }
                if (towns.isEmpty()) {
                    return Collections.emptyList();
                }
                StringUtil.copyPartialMatches(args[0], towns, arg1TabComp);
                Collections.sort(arg1TabComp);
                return arg1TabComp;
            } else {
                return Collections.emptyList();
            }
        }

    }
}
