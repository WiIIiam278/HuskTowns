package me.william278.husktowns.commands;

import de.themoep.minedown.MineDown;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.cache.PlayerCache;
import me.william278.husktowns.chunk.ClaimedChunk;
import me.william278.husktowns.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class ClaimCommand extends CommandBase {

    private static final double maximumClaimDistance = HuskTowns.getSettings().getTownMapSquareRadius() * 26;
    private static final HuskTowns plugin = HuskTowns.getInstance();

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (!HuskTowns.getTownDataCache().hasLoaded() || !HuskTowns.getClaimCache().hasLoaded() || !HuskTowns.getPlayerCache().hasLoaded() || !HuskTowns.getTownBonusesCache().hasLoaded()) {
            MessageManager.sendMessage(player,"error_cache_updating", "system");
            return;
        }
        if (args.length > 0) {
            int argumentIndexer = 0;
            if (args[0].equalsIgnoreCase("info")) {
                argumentIndexer = 1;
            }
            String targetServer = HuskTowns.getSettings().getServerID();
            if (args.length == (argumentIndexer + 4)) {
                if (HuskTowns.getSettings().doBungee()) {
                    targetServer = args[argumentIndexer + 3];
                    if (!targetServer.equalsIgnoreCase(HuskTowns.getSettings().getServerID())) {
                        MessageManager.sendMessage(player, "claim_chunk_other_server");
                        return;
                    }
                }
            }

            String targetWorldName = player.getWorld().getName();
            if (args.length == (argumentIndexer + 3)) {
                targetWorldName = args[argumentIndexer + 2];
            }

            int targetX = player.getLocation().getChunk().getX();
            int targetZ = player.getLocation().getChunk().getZ();
            if (args.length >= (argumentIndexer + 2)) {
                try {
                    targetX = Integer.parseInt(args[argumentIndexer]);
                    targetZ = Integer.parseInt(args[argumentIndexer + 1]);
                } catch (NumberFormatException e) {
                    MessageManager.sendMessage(player, "error_invalid_chunk_coords");
                }
            }

            if (argumentIndexer == 0) {
                // Claim a chunk
                World targetWorld = Bukkit.getWorld(targetWorldName);
                if (targetWorld == null) {
                    MessageManager.sendMessage(player, "claim_chunk_other_world");
                    return;
                }
                if (!player.getWorld().getName().equalsIgnoreCase(targetWorldName)) {
                    MessageManager.sendMessage(player, "claim_chunk_other_world");
                    return;
                }
                if (!targetServer.equalsIgnoreCase(HuskTowns.getSettings().getServerID())) {
                    MessageManager.sendMessage(player, "claim_chunk_other_server");
                    return;
                }
                Location location = new Location(targetWorld, (targetX * 16), player.getLocation().getY(), (targetZ * 16));
                if (location.distance(player.getLocation()) > maximumClaimDistance) {
                    MessageManager.sendMessage(player, "claim_chunk_too_far");
                    return;
                }
                DataManager.claimChunk(player, location, true);
            } else {
                // Check for information about a chunk
                if (!HuskTowns.getSettings().doBungee()) {
                    World targetWorld = Bukkit.getWorld(targetWorldName);
                    if (targetWorld == null) {
                        MessageManager.sendMessage(player, "claim_chunk_other_world");
                        return;
                    }
                    if (!player.getWorld().getName().equalsIgnoreCase(targetWorldName)) {
                        MessageManager.sendMessage(player, "claim_chunk_other_world");
                        return;
                    }
                }

                final String worldName = targetWorldName;
                final String serverName = targetServer;
                final int chunkXPos = targetX;
                final int chunkZPos = targetZ;
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try (Connection connection = HuskTowns.getConnection()) {
                        DataManager.getClaimedChunk(serverName, worldName, chunkXPos, chunkZPos, connection);
                        showClaimInfo(player, HuskTowns.getClaimCache().getChunkAt(chunkXPos, chunkZPos, worldName));
                    } catch (SQLException e) {
                        plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred", e);
                    }
                });
            }
        } else {
            DataManager.claimChunk(player, player.getLocation(), false);
        }
    }

    public static void showClaimInfo(Player player, ClaimedChunk chunk) {
        PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (chunk != null) {

            MessageManager.sendMessage(player, "claim_details", Integer.toString(chunk.getChunkX() * 16),
                    Integer.toString(chunk.getChunkZ() * 16), chunk.getWorld(), chunk.getTown());
            if (HuskTowns.getSettings().doBungee()) {
                MessageManager.sendMessage(player, "claim_details_server", chunk.getServer());
            }
            MessageManager.sendMessage(player, "claim_details_claimed_by", playerCache.getPlayerUsername(chunk.getClaimerUUID()));
            MessageManager.sendMessage(player, "claim_details_timestamp", chunk.getFormattedClaimTime());
            MessageManager.sendMessage(player, "claim_details_type", chunk.getChunkType().toString().toLowerCase(Locale.ROOT));

            if (chunk.getChunkType() == ClaimedChunk.ChunkType.PLOT) {
                if (chunk.getPlotChunkOwner() == null) {
                    if (playerCache.isPlayerInTown(player.getUniqueId())) {
                        if (playerCache.getPlayerTown(player.getUniqueId()).equalsIgnoreCase(chunk.getTown()) && HuskTowns.getClaimCache().getChunkAt(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ(), player.getWorld().getName()) == chunk) {
                            MessageManager.sendMessage(player, "claim_details_plot_vacant_claimable", Integer.toString(chunk.getChunkX()), Integer.toString(chunk.getChunkZ()), chunk.getWorld());
                        } else {
                            MessageManager.sendMessage(player, "claim_details_plot_vacant");
                        }
                    }
                } else {
                    MessageManager.sendMessage(player, "claim_details_plot_owner", playerCache.getPlayerUsername(chunk.getPlotChunkOwner()));
                    // Send a list of the town members
                    if (!chunk.getPlotChunkMembers().isEmpty()) {
                        StringJoiner townMembers = new StringJoiner("[,](gray) ");
                        for (UUID uuid : chunk.getPlotChunkMembers()) {
                            townMembers.add("[" + MineDown.escape(playerCache.getPlayerUsername(uuid)) + "](white show_text=&7UUID: " + MineDown.escape(uuid.toString()) + ")");
                        }
                        MessageManager.sendMessage(player, "claim_details_plot_members", townMembers.toString());
                    }
                }
            }
            if (HuskTowns.getSettings().getServerID().equalsIgnoreCase(chunk.getServer())) {
                MessageManager.sendMessage(player, "claim_details_view_on_map", Integer.toString(chunk.getChunkX()), Integer.toString(chunk.getChunkZ()), chunk.getWorld());
            }
        } else {
            MessageManager.sendMessage(player, "inspect_chunk_not_claimed");
        }
    }

    public static class ClaimTab implements TabCompleter {

        final static String[] COMMAND_TAB_ARGS = {"info"};

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
            }
            return Collections.emptyList();

        }
    }

}