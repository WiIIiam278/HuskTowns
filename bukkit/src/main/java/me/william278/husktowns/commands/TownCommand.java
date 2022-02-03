package me.william278.husktowns.commands;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.cache.PlayerCache;
import me.william278.husktowns.chunk.ClaimedChunk;
import me.william278.husktowns.commands.CommandBase;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.town.TownRole;
import me.william278.husktowns.util.NameAutoCompleter;
import me.william278.husktowns.util.PageChatList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class TownCommand extends CommandBase {

    private static HashMap<String, TownRole> commandRoles(String command, TownRole role) {
        HashMap<String, TownRole> commandRoles = new HashMap<>();
        commandRoles.put(command, role);
        return commandRoles;
    }

    public static final Map<HashMap<String, TownRole>, String> townCommands;

    static {
        townCommands = new HashMap<>();
        townCommands.put(commandRoles("town create", null), "Create a town");
        townCommands.put(commandRoles("town settings", TownRole.TRUSTED), "Set the town preferences");
        townCommands.put(commandRoles("town list", null), "View a list of towns");
        townCommands.put(commandRoles("town info", null), "View a town''s overview");
        townCommands.put(commandRoles("town player", null), "View info about a player");
        townCommands.put(commandRoles("town chat", TownRole.RESIDENT), "Send a message to town members");
        townCommands.put(commandRoles("town map", null), "View a map of nearby towns");
        townCommands.put(commandRoles("town claim", TownRole.TRUSTED), "Claim land in your town");
        townCommands.put(commandRoles("town unclaim", TownRole.TRUSTED), "Unclaim town land");
        townCommands.put(commandRoles("town invite", TownRole.TRUSTED), "Invite someone to join your town");
        townCommands.put(commandRoles("town promote", TownRole.MAYOR), "Make a resident a Trusted citizen");
        townCommands.put(commandRoles("town demote", TownRole.MAYOR), "Demote a Trusted citizen");
        townCommands.put(commandRoles("town kick", TownRole.TRUSTED), "Kick a member from your town");
        townCommands.put(commandRoles("town spawn", TownRole.RESIDENT), "Teleport to your town spawn");
        townCommands.put(commandRoles("town setspawn", TownRole.TRUSTED), "Set your town spawn point");
        townCommands.put(commandRoles("town deposit", TownRole.RESIDENT), "Deposit money into the town coffers");
        townCommands.put(commandRoles("town leave", TownRole.RESIDENT), "Leave a town as a member");
        townCommands.put(commandRoles("town rename", TownRole.MAYOR), "Rename your town");
        townCommands.put(commandRoles("town claims", null), "View a list of town claims");
        townCommands.put(commandRoles("town plot", TownRole.TRUSTED), "Make the claim you are in a plot");
        townCommands.put(commandRoles("town farm", TownRole.TRUSTED), "Make the claim you are in a farm");
        townCommands.put(commandRoles("town disband", TownRole.MAYOR), "Disband your town");
        townCommands.put(commandRoles("town greeting", TownRole.TRUSTED), "Change the town greeting message");
        townCommands.put(commandRoles("town farewell", TownRole.TRUSTED), "Change the town farewell message");
        townCommands.put(commandRoles("town transfer", TownRole.MAYOR), "Transfer ownership of a town");
        townCommands.put(commandRoles("town bio", TownRole.TRUSTED), "Change the town bio");
        townCommands.put(commandRoles("town publicspawn", TownRole.TRUSTED), "Toggle town spawn privacy");
        townCommands.put(commandRoles("town flag", TownRole.TRUSTED), "Set flags for town claims");
        townCommands.put(commandRoles("town help", null), "View the town help menu");
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "kick":
                    if (args.length == 2) {
                        player.performCommand("evict " + args[1]);
                    } else {
                        player.performCommand("evict");
                    }
                    break;
                case "claims":
                case "autoclaim":
                case "claimlist":
                case "claimslist":
                case "invite":
                case "map":
                case "evict":
                case "promote":
                case "demote":
                case "trust":
                case "untrust":
                case "claim":
                case "unclaim":
                case "delclaim":
                case "abandonclaim":
                case "plot":
                case "farm":
                case "transfer":
                    StringBuilder commandArgs = new StringBuilder();
                    for (String arg : args) {
                        commandArgs.append(arg).append(" ");
                    }
                    player.performCommand(commandArgs.toString());
                    break;
                default:
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
                    break;
            }
        } else {
            DataManager.sendTownInfoMenu(player);
        }
    }



    public static class TownTab implements TabCompleter {

        final static String[] COMMAND_TAB_ARGS = {"create", "deposit", "leave", "rename", "setspawn", "publicspawn", "player",
                "privatespawn", "spawn", "info", "greeting", "farewell", "kick", "claims", "invite", "promote", "settings",
                "demote", "claim", "unclaim", "plot", "farm", "map", "transfer", "disband", "list", "help", "bio", "flag"};

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            Player p = (Player) sender;
            if (command.getPermission() != null) {
                if (!p.hasPermission(command.getPermission())) {
                    return Collections.emptyList();
                }
            }
            switch (args.length) {
                case 1:
                    final List<String> tabCompletions = new ArrayList<>();
                    StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMAND_TAB_ARGS), tabCompletions);
                    Collections.sort(tabCompletions);
                    return tabCompletions;
                case 2:
                    final PlayerCache playerCache = HuskTowns.getPlayerCache();
                    if (!playerCache.hasLoaded()) {
                        return Collections.emptyList();
                    }
                    switch (args[0].toLowerCase()) {
                        case "kick":
                        case "evict":
                        case "promote":
                        case "demote":
                        case "trust":
                        case "untrust":
                        case "transfer":
                            if (playerCache.getPlayerTown(p.getUniqueId()) == null) {
                                return Collections.emptyList();
                            }
                            final List<String> playerListTabCom = new ArrayList<>();
                            HashSet<String> playersInTown = playerCache.getPlayersInTown(playerCache.getPlayerTown(p.getUniqueId()));
                            if (playersInTown.isEmpty()) {
                                return Collections.emptyList();
                            }
                            StringUtil.copyPartialMatches(args[1], playersInTown, playerListTabCom);
                            Collections.sort(playerListTabCom);
                            return playerListTabCom;
                        case "info":
                        case "about":
                        case "view":
                        case "check":
                            if (playerCache.getPlayerTown(p.getUniqueId()) == null) {
                                return Collections.emptyList();
                            }
                            final List<String> townListTabCom = new ArrayList<>();
                            if (playerCache.getTowns().isEmpty()) {
                                return Collections.emptyList();
                            }
                            StringUtil.copyPartialMatches(args[1], HuskTowns.getPlayerCache().getTowns(), townListTabCom);
                            Collections.sort(townListTabCom);
                            return townListTabCom;
                        case "warp":
                        case "spawn":
                            final List<String> publicTownSpawnTabList = new ArrayList<>();
                            if (playerCache.getTowns().isEmpty()) {
                                return Collections.emptyList();
                            }
                            StringUtil.copyPartialMatches(args[1], HuskTowns.getTownDataCache().getPublicSpawnTowns(), publicTownSpawnTabList);
                            Collections.sort(publicTownSpawnTabList);
                            return publicTownSpawnTabList;
                        case "who":
                        case "player":
                            final List<String> playerLookupList = new ArrayList<>();
                            final ArrayList<String> onlinePlayers = new ArrayList<>(HuskTowns.getPlayerList().getPlayers());
                            if (onlinePlayers.isEmpty()) {
                                return Collections.emptyList();
                            }
                            StringUtil.copyPartialMatches(args[1], onlinePlayers, playerLookupList);
                            Collections.sort(playerLookupList);
                            return playerLookupList;
                        case "invite":
                            if (playerCache.getPlayerTown(p.getUniqueId()) == null) {
                                return Collections.emptyList();
                            }
                            final List<String> inviteTabCom = new ArrayList<>();
                            final ArrayList<String> players = new ArrayList<>(HuskTowns.getPlayerList().getPlayers());
                            if (players.isEmpty()) {
                                return Collections.emptyList();
                            }
                            StringUtil.copyPartialMatches(args[1], players, inviteTabCom);
                            Collections.sort(inviteTabCom);
                            return inviteTabCom;
                        case "list":
                            final String[] townListCom = {"oldest", "newest", "name", "level"};
                            final List<String> townListCompletions = new ArrayList<>();
                            StringUtil.copyPartialMatches(args[1], Arrays.asList(townListCom), townListCompletions);
                            Collections.sort(townListCompletions);
                            return townListCompletions;
                        default:
                            return Collections.emptyList();
                    }
                default:
                    return Collections.emptyList();
            }

        }

    }
}