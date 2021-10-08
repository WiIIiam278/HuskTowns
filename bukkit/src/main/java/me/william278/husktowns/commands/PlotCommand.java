package me.william278.husktowns.commands;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.cache.ClaimCache;
import me.william278.husktowns.cache.PlayerCache;
import me.william278.husktowns.chunk.ClaimedChunk;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class PlotCommand extends CommandBase {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        final ClaimCache claimCache = HuskTowns.getClaimCache();
        if (!claimCache.hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", claimCache.getName());
            return;
        }
        final Location playerLocation = player.getLocation();
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "set", "unset", "delete", "remove" -> {
                    if (!HuskTowns.getPlayerCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
                        return;
                    }
                    DataManager.changeToPlot(player, claimCache.getChunkAt(playerLocation.getChunk().getX(),
                            playerLocation.getChunk().getZ(), player.getWorld().getName()));
                }
                case "claim" -> {
                    if (!HuskTowns.getPlayerCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
                        return;
                    }
                    DataManager.claimPlot(player, claimCache.getChunkAt(playerLocation.getChunk().getX(),
                            playerLocation.getChunk().getZ(), player.getWorld().getName()));
                }
                case "unclaim", "abandon", "evict", "clear", "unassign" -> {
                    if (!HuskTowns.getPlayerCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
                        return;
                    }
                    DataManager.unClaimPlot(player, HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                            playerLocation.getChunk().getZ(), player.getWorld().getName()));
                }
                case "assign" -> {
                    if (args.length == 2) {
                        if (!HuskTowns.getPlayerCache().hasLoaded()) {
                            MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
                            return;
                        }
                        DataManager.assignPlotPlayer(player, args[1], claimCache.getChunkAt(playerLocation.getChunk().getX(),
                                playerLocation.getChunk().getZ(), player.getWorld().getName()));
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/plot assign <player>");
                    }
                }
                case "info" -> {
                    if (!HuskTowns.getPlayerCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
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
                        try (Connection connection = HuskTowns.getConnection()) {
                            ClaimedChunk chunk = DataManager.getClaimedChunk(server, world, chunkX, chunkZ, connection);
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
                }
                case "add", "addmember", "trust" -> {
                    if (!HuskTowns.getPlayerCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
                        return;
                    }
                    if (args.length == 2) {
                        final String playerToAdd = args[1];
                        ClaimedChunk chunk = HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(), playerLocation.getChunk().getZ(), player.getWorld().getName());
                        DataManager.addPlotMember(player, chunk, playerToAdd);
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/plot trust <player>");
                    }
                }
                case "removemember", "untrust" -> {
                    if (!HuskTowns.getPlayerCache().hasLoaded()) {
                        MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
                        return;
                    }
                    if (args.length == 2) {
                        final String playerToRemove = args[1];
                        ClaimedChunk chunk = HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(), playerLocation.getChunk().getZ(), player.getWorld().getName());
                        DataManager.removePlotMember(player, chunk, playerToRemove);
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/plot untrust <player>");
                    }
                }
                default -> MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
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
                    if (!playerCache.isPlayerInTown(p.getUniqueId())) {
                        return Collections.emptyList();
                    }
                    final String town = playerCache.getPlayerTown(p.getUniqueId());
                    final List<String> playerListTabCom = new ArrayList<>();
                    switch (args[0].toLowerCase()) {
                        case "assign":
                            final HashSet<String> playersInTown = playerCache.getPlayersInTown(town);
                            if (playersInTown == null) {
                                return Collections.emptyList();
                            }
                            if (playersInTown.isEmpty()) {
                                return Collections.emptyList();
                            }
                            StringUtil.copyPartialMatches(args[1], playersInTown, playerListTabCom);
                            Collections.sort(playerListTabCom);
                            return playerListTabCom;
                        case "trust":
                        case "add":
                        case "addmember":
                            final HashSet<String> onlinePlayers = new HashSet<>();
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                onlinePlayers.add(onlinePlayer.getName());
                            }
                            if (onlinePlayers.isEmpty()) {
                                return Collections.emptyList();
                            }
                            StringUtil.copyPartialMatches(args[1], onlinePlayers, playerListTabCom);
                            Collections.sort(playerListTabCom);
                            return playerListTabCom;
                        case "removemember":
                        case "untrust":
                            final ClaimCache claimCache = HuskTowns.getClaimCache();
                            if (claimCache.hasLoaded()) {
                                Player player = (Player) sender;
                                final HashSet<String> plotMembers = new HashSet<>();
                                final ClaimedChunk chunk = claimCache.getChunkAt(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ(), player.getWorld().getName());
                                if (chunk != null) {
                                    if (chunk.getChunkType() == ClaimedChunk.ChunkType.PLOT) {
                                        for (UUID plotMember : chunk.getPlotChunkMembers()) {
                                            plotMembers.add(playerCache.getPlayerUsername(plotMember));
                                        }
                                        if (plotMembers.isEmpty()) {
                                            return Collections.emptyList();
                                        }
                                        StringUtil.copyPartialMatches(args[1], plotMembers, playerListTabCom);
                                        Collections.sort(playerListTabCom);
                                        return playerListTabCom;
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