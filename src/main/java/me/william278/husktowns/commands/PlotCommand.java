package me.william278.husktowns.commands;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.cache.ClaimCache;
import me.william278.husktowns.object.cache.PlayerCache;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class PlotCommand extends CommandBase {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (!HuskTowns.getClaimCache().hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", "Town Claim Data");
            return;
        }
        final Location playerLocation = player.getLocation();
        if (args.length >= 1) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "set":
                case "unset":
                case "delete":
                case "remove":
                    if (!HuskTowns.getPlayerCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
                        return;
                    }
                    DataManager.changeToPlot(player, HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                            playerLocation.getChunk().getZ(), player.getWorld().getName()));
                    return;
                case "claim":
                    if (!HuskTowns.getPlayerCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
                        return;
                    }
                    DataManager.claimPlot(player, HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                            playerLocation.getChunk().getZ(), player.getWorld().getName()));
                    return;
                case "unclaim":
                case "abandon":
                case "evict":
                case "clear":
                case "unassign":
                    if (!HuskTowns.getPlayerCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
                        return;
                    }
                    DataManager.unClaimPlot(player, HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                            playerLocation.getChunk().getZ(), player.getWorld().getName()));
                    return;
                case "assign":
                    if (args.length == 2) {
                        if (!HuskTowns.getPlayerCache().hasLoaded()) {
                            MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
                            return;
                        }
                        DataManager.assignPlotPlayer(player, args[1], HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                                playerLocation.getChunk().getZ(), player.getWorld().getName()));
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/plot assign <player>");
                    }
                    return;
                case "info":
                    if (!HuskTowns.getPlayerCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
                        return;
                    }
                    if (!HuskTowns.getClaimCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "Claim Data");
                        return;
                    }
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        int chunkX = player.getLocation().getChunk().getX();
                        int chunkZ = player.getLocation().getChunk().getZ();
                        String world = player.getWorld().getName();
                        String server = HuskTowns.getSettings().getServerID();

                        if (args.length == 2 || args.length == 3 || args.length == 4) {
                            try {
                                chunkX = Integer.parseInt(args[0]);
                                chunkZ = Integer.parseInt(args[1]);
                            } catch (NumberFormatException exception) {
                                MessageManager.sendMessage(player, "error_invalid_chunk_coords");
                                return;
                            }
                        }
                        if (args.length == 4) {
                            server = args[3];
                        }
                        if (args.length == 3) {
                            world = args[2];
                            if (Bukkit.getWorld(world) == null) {
                                MessageManager.sendMessage(player, "error_invalid_world");
                                return;
                            }
                        }
                        try {
                            ClaimedChunk chunk = DataManager.getClaimedChunk(server, world, chunkX, chunkZ, HuskTowns.getConnection());
                            if (chunk != null) {
                                if (chunk.getChunkType() == ClaimedChunk.ChunkType.PLOT) {
                                    ClaimCommand.showClaimInfo(player, chunk);
                                } else {
                                    MessageManager.sendMessage(player, "error_not_a_plot");
                                }
                            } else {
                                if (HuskTowns.getSettings().getUnClaimableWorlds().contains(world)) {
                                    MessageManager.sendMessage(player, "inspect_chunk_not_claimable");
                                } else {
                                    MessageManager.sendMessage(player, "inspect_chunk_not_claimed");
                                }
                            }
                        } catch (SQLException e) {
                            plugin.getLogger().log(Level.SEVERE, "An SQL exception has occurred", e);
                        }
                    });
                    return;
                case "add":
                case "addmember":
                case "trust":
                    if (!HuskTowns.getPlayerCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
                        return;
                    }
                    if (!HuskTowns.getClaimCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "Claim Data");
                        return;
                    }
                    if (args.length == 2) {
                        final String playerToAdd = args[1];
                        ClaimedChunk chunk = HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(), playerLocation.getChunk().getZ(), player.getWorld().getName());
                        DataManager.addPlotMember(player, chunk, playerToAdd);
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/plot trust <player>");
                    }
                    return;
                case "removemember":
                case "untrust":
                    if (!HuskTowns.getPlayerCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
                        return;
                    }
                    if (!HuskTowns.getClaimCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "Claim Data");
                        return;
                    }
                    if (args.length == 2) {
                        final String playerToRemove = args[1];
                        ClaimedChunk chunk = HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(), playerLocation.getChunk().getZ(), player.getWorld().getName());
                        DataManager.removePlotMember(player, chunk, playerToRemove);
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/plot untrust <player>");
                    }
                    return;
                default:
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
            }
        } else {
            DataManager.changeToPlot(player, HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                    playerLocation.getChunk().getZ(), player.getWorld().getName()));
        }
    }

    public static class PlotTab implements TabCompleter {

        final static String[] COMMAND_TAB_ARGS = {"set", "remove", "claim", "unclaim", "assign", "trust", "untrust"};

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
                    if (playerCache.getTown(p.getUniqueId()) == null) {
                        return Collections.emptyList();
                    }
                    switch (args[0].toLowerCase(Locale.ROOT)) {
                        case "assign":
                            final List<String> playerListTabCom = new ArrayList<>();
                            final String town = playerCache.getTown(p.getUniqueId());
                            if (town == null) {
                                return Collections.emptyList();
                            }
                            final HashSet<String> playersInTown = playerCache.getPlayersInTown(town);
                            if (playersInTown == null) {
                                return Collections.emptyList();
                            }
                            if (playersInTown.isEmpty()) {
                                return Collections.emptyList();
                            }
                            StringUtil.copyPartialMatches(args[0], playersInTown, playerListTabCom);
                            Collections.sort(playerListTabCom);
                            return playerListTabCom;
                        case "trust":
                        case "add":
                        case "addmember":
                            final List<String> onlinePlayerList = new ArrayList<>();
                            if (town == null) {
                                return Collections.emptyList();
                            }
                            final HashSet<String> onlinePlayers = playerCache.getPlayersInTown(town);
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                onlinePlayers.add(onlinePlayer.getName());
                            }
                            if (onlinePlayers.isEmpty()) {
                                return Collections.emptyList();
                            }
                            StringUtil.copyPartialMatches(args[0], onlinePlayers, onlinePlayerList);
                            Collections.sort(onlinePlayerList);
                            return onlinePlayerList;
                        case "removemember":
                        case "untrust":
                            final List<String> plotMemberList = new ArrayList<>();
                            if (town == null) {
                                return Collections.emptyList();
                            }
                            final PlayerCache untrustingPlayerCache = HuskTowns.getPlayerCache();
                            if (!untrustingPlayerCache.hasLoaded()) {
                                return Collections.emptyList();
                            }
                            final ClaimCache claimCache = HuskTowns.getClaimCache();
                            if (claimCache.hasLoaded()) {
                                Player player = (Player) sender;
                                final HashSet<String> plotMembers = new HashSet<>();
                                final ClaimedChunk chunk =claimCache.getChunkAt(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ(), player.getWorld().getName());
                                if (chunk != null) {
                                    if (chunk.getChunkType() == ClaimedChunk.ChunkType.PLOT) {
                                        for (UUID plotMember : chunk.getPlotChunkMembers()) {
                                            plotMembers.add(untrustingPlayerCache.getUsername(plotMember));
                                        }
                                        if (plotMembers.isEmpty()) {
                                            return Collections.emptyList();
                                        }
                                        StringUtil.copyPartialMatches(args[0], plotMembers, plotMemberList);
                                        Collections.sort(plotMemberList);
                                        return plotMemberList;
                                    }
                                }
                            }
                        default:
                            return Collections.emptyList();
                    }
                default:
                    return Collections.emptyList();
            }

        }
    }
}