package me.william278.husktowns.commands;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.cache.PlayerCache;
import me.william278.husktowns.chunk.ClaimedChunk;
import me.william278.husktowns.town.TownRole;
import me.william278.husktowns.util.PageChatList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TownCommand extends CommandBase {

    private static HashMap<String, TownRole> commandRoles(String command, TownRole role) {
        HashMap<String, TownRole> commandRoles = new HashMap<>();
        commandRoles.put(command, role);
        return commandRoles;
    }
    private static final Map<HashMap<String, TownRole>,String> townCommands;
    static {
        townCommands = new HashMap<>();
        townCommands.put(commandRoles("town create", null), "Create a town");
        townCommands.put(commandRoles("town settings", TownRole.TRUSTED), "Set the town preferences");
        townCommands.put(commandRoles("town list", null), "View a list of towns");
        townCommands.put(commandRoles("town info", null), "View a town''s overview");
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
                case "create":
                case "found":
                    if (player.hasPermission("husktowns.create_town")) {
                        if (args.length == 2) {
                            String townName = args[1];
                            DataManager.createTown(player, townName);
                        } else {
                            MessageManager.sendMessage(player, "error_invalid_syntax", "/town create <name>");
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "deposit":
                    if (player.hasPermission("husktowns.command.town.deposit")) {
                        if (args.length == 2) {
                            try {
                                double amountToDeposit = Double.parseDouble(args[1]);
                                DataManager.depositMoney(player, amountToDeposit);
                            } catch (NumberFormatException ex) {
                                MessageManager.sendMessage(player, "error_invalid_amount");
                            }
                        } else {
                            MessageManager.sendMessage(player, "error_invalid_syntax", "/town deposit <amount>");
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "leave":
                    if (player.hasPermission("husktowns.command.town.leave")) {
                        DataManager.leaveTown(player);
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "rename":
                    if (!player.hasPermission("husktowns.command.town.rename")) {
                        MessageManager.sendMessage(player, "error_no_permission");
                        return;
                    }
                    if (args.length == 2) {
                        String townName = args[1];
                        DataManager.renameTown(player, townName);
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town rename <new name>");
                    }
                    break;
                case "setspawn":
                case "setwarp":
                    if (player.hasPermission("husktowns.command.town.spawn.set")) {
                        DataManager.updateTownSpawn(player);
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "spawn":
                case "warp":
                    if (player.hasPermission("husktowns.command.town.spawn")) {
                        if (args.length == 2) {
                            DataManager.teleportPlayerToOtherSpawn(player, args[1]);
                        } else {
                            DataManager.teleportPlayerToSpawn(player);
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "publicspawn":
                case "privatespawn":
                case "publicwarp":
                case "privatewarp":
                    if (player.hasPermission("husktowns.command.town.spawn.privacy")) {
                        DataManager.toggleTownPrivacy(player);
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "info":
                case "view":
                case "about":
                case "check":
                    if (args.length == 2) {
                        DataManager.sendTownInfoMenu(player, args[1]);
                    } else {
                        DataManager.sendTownInfoMenu(player);
                    }
                    break;
                case "greeting":
                    if (player.hasPermission("husktowns.command.town.greeting")) {
                        if (args.length >= 2) {
                            StringBuilder description = new StringBuilder();
                            for (int i = 2; i <= args.length; i++) {
                                description.append(args[i - 1]).append(" ");
                            }

                            DataManager.updateTownGreeting(player, description.toString().trim());
                        } else {
                            MessageManager.sendMessage(player, "error_invalid_syntax", "/town greeting <new message>");
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "farewell":
                    if (player.hasPermission("husktowns.command.town.farewell")) {
                        if (args.length >= 2) {
                            StringBuilder description = new StringBuilder();
                            for (int i = 2; i <= args.length; i++) {
                                description.append(args[i - 1]).append(" ");
                            }

                            DataManager.updateTownFarewell(player, description.toString().trim());
                        } else {
                            MessageManager.sendMessage(player, "error_invalid_syntax", "/town farewell <new message>");
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "bio":
                case "description":
                    if (player.hasPermission("husktowns.command.town.bio")) {
                        if (args.length >= 2) {
                            StringBuilder description = new StringBuilder();
                            for (int i = 2; i <= args.length; i++) {
                                description.append(args[i - 1]).append(" ");
                            }

                            DataManager.updateTownBio(player, description.toString().trim());
                        } else {
                            MessageManager.sendMessage(player, "error_invalid_syntax", "/town bio <new bio>");
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "chat":
                    if (args.length >= 2) {
                        StringBuilder description = new StringBuilder();
                        for (int i = 2; i <= args.length; i++) {
                            description.append(args[i - 1]).append(" ");
                        }

                        player.performCommand("townchat " + description.toString().trim());
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town chat <message>");
                    }
                    break;
                case "list":
                    if (args.length == 2) {
                        player.performCommand("townlist " + args[1]);
                    } else {
                        player.performCommand("townlist");
                    }
                    break;
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
                case "disband":
                case "delete":
                    if (args.length == 1) {
                        if (HuskTowns.getPlayerCache().isPlayerInTown(player.getUniqueId())) {
                            if (HuskTowns.getPlayerCache().getPlayerRole(player.getUniqueId()) == TownRole.MAYOR) {
                                MessageManager.sendMessage(player, "disband_town_confirm");
                            } else {
                                MessageManager.sendMessage(player, "error_insufficient_disband_privileges");
                            }
                        } else {
                            MessageManager.sendMessage(player, "error_not_in_town");
                        }
                    } else if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("confirm")) {
                            DataManager.disbandTown(player);
                        } else {
                            MessageManager.sendMessage(player, "error_invalid_syntax", "/town disband [confirm]");
                        }
                    }
                    break;
                case "settings":
                case "config":
                case "flags":
                case "prefs":
                case "preferences":
                    if (player.hasPermission("husktowns.command.town.settings")) {
                        if (args.length == 2) {
                            DataManager.sendTownSettings(player, args[1]);
                        } else {
                            DataManager.sendTownSettings(player);
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "flag":
                    String townName = null;
                    ClaimedChunk.ChunkType chunkType;
                    String flagIdentifier;
                    boolean value;
                    boolean showSettingsMenu = false;
                    if (args.length == 6) {
                        showSettingsMenu = (args[5].equals("-settings"));
                    }
                    int argIndexer = 1;
                    if (args.length == 5 || args.length == 6) {
                        townName = args[argIndexer];
                        argIndexer++;
                    }
                    if (args.length >= 5 && args.length <= 6) {
                        try {
                            chunkType = ClaimedChunk.ChunkType.valueOf(args[argIndexer].toUpperCase());
                            argIndexer++;
                            flagIdentifier = args[argIndexer];
                            argIndexer++;
                            value = Boolean.parseBoolean(args[argIndexer]);
                            if (townName == null) {
                                DataManager.setTownFlag(player, chunkType, flagIdentifier, value, showSettingsMenu);
                            } else {
                                DataManager.setTownFlag(player, townName, chunkType, flagIdentifier, value, showSettingsMenu);
                            }
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                            MessageManager.sendMessage(player, "error_invalid_chunk_type");
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town flag [town] <chunk_type> <flag> <value>");
                    }
                    break;
                case "help":
                    if (args.length == 2) {
                        try {
                            int pageNo = Integer.parseInt(args[1]);
                            showTownHelpMenu(player, pageNo);
                        } catch (NumberFormatException ex) {
                            MessageManager.sendMessage(player, "error_invalid_page_number");
                        }
                    } else {
                        showTownHelpMenu(player, 1);
                    }
                    return;
                default:
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
                    break;
            }
        } else {
            DataManager.sendTownInfoMenu(player);
        }
    }

    // Show users a list of available town subcommands
    public static void showTownHelpMenu(Player player, int pageNumber) {
        final ArrayList<String> commandDisplay = new ArrayList<>();
        final PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (!playerCache.hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", playerCache.getName());
            return;
        }
        for (HashMap<String, TownRole> commandRoles : townCommands.keySet()) {
            final String commandDescription = townCommands.get(commandRoles);
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

    public static class TownTab implements TabCompleter {

        final static String[] COMMAND_TAB_ARGS = {"create", "deposit", "leave", "rename", "setspawn", "publicspawn",
                "privatespawn", "spawn", "info", "greeting", "farewell", "kick", "claims", "invite", "promote", "settings",
                "demote",  "claim", "unclaim", "plot", "farm", "map", "transfer", "disband", "list", "help", "bio", "flag"};

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
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
                        case "invite":
                            if (playerCache.getPlayerTown(p.getUniqueId()) == null) {
                                return Collections.emptyList();
                            }
                            final List<String> inviteTabCom = new ArrayList<>();
                            final ArrayList<String> players = new ArrayList<>();
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                players.add(player.getName());
                            }
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