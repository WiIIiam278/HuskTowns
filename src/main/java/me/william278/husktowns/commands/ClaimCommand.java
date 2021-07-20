package me.william278.husktowns.commands;

import de.themoep.minedown.MineDown;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.cache.PlayerCache;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class ClaimCommand extends CommandBase {

    private static final int MAXIMUM_CLAIM_DISTANCE = 256;
    private static final HuskTowns plugin = HuskTowns.getInstance();

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length > 0) {
            int argumentIndexer = 0;
            if (args[0].equalsIgnoreCase("info")) {
                argumentIndexer = 1;
            }
            String targetServer = HuskTowns.getSettings().getServerID();
            if (args.length == (argumentIndexer+4)) {
                targetServer = args[argumentIndexer+3];
            }
            World targetWorld = player.getWorld();
            if (args.length == (argumentIndexer+3)) {
                targetWorld = Bukkit.getWorld(args[argumentIndexer+2]);
                if (targetWorld == null) {
                    MessageManager.sendMessage(player, "claim_chunk_other_world");
                    return;
                }
                if (!player.getWorld().getName().equalsIgnoreCase(targetWorld.getName())) {
                    MessageManager.sendMessage(player, "claim_chunk_other_world");
                    return;
                }
            }
            if (args.length >= (argumentIndexer+2)) {
                try {
                    final int targetX = Integer.parseInt(args[argumentIndexer]);
                    final int targetZ = Integer.parseInt(args[argumentIndexer+1]);
                    if (argumentIndexer == 0) {
                        Location location = new Location(targetWorld, targetX*16, 64, targetZ*16);

                        if (location.distanceSquared(player.getLocation()) > MAXIMUM_CLAIM_DISTANCE) {
                            MessageManager.sendMessage(player, "claim_chunk_too_far");
                            return;
                        }
                        DataManager.claimChunk(player, location);
                    } else {
                        final String worldName = targetWorld.getName();
                        final String serverName = targetServer;
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            try {
                                DataManager.getClaimedChunk(serverName, worldName, targetX, targetZ, HuskTowns.getConnection());
                                showClaimInfo(player, HuskTowns.getClaimCache().getChunkAt(targetX, targetZ, worldName));
                            } catch (SQLException e) {
                                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred", e);
                            }
                        });
                    }
                } catch (NumberFormatException e) {
                    MessageManager.sendMessage(player, "error_invalid_chunk_coords");
                }
            }
        } else {
            DataManager.claimChunk(player, player.getLocation());
        }
    }

    public static void showClaimInfo(Player player, ClaimedChunk chunk) {
        PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (chunk != null) {

            MessageManager.sendMessage(player, "claim_details", Integer.toString(chunk.getChunkX()*16),
                    Integer.toString(chunk.getChunkZ()*16), chunk.getWorld(), chunk.getTown());
            MessageManager.sendMessage(player, "claim_details_claimed_by", playerCache.getUsername(chunk.getClaimerUUID()));
            MessageManager.sendMessage(player, "claim_details_timestamp", chunk.getFormattedTime());
            MessageManager.sendMessage(player, "claim_details_type", chunk.getChunkType().toString().toLowerCase(Locale.ROOT));

            if (chunk.getChunkType() == ClaimedChunk.ChunkType.PLOT) {
                if (chunk.getPlotChunkOwner() == null) {
                    if (playerCache.isPlayerInTown(player.getUniqueId())) {
                        if (playerCache.getTown(player.getUniqueId()).equalsIgnoreCase(chunk.getTown()) && HuskTowns.getClaimCache().getChunkAt(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ(), player.getWorld().getName()) == chunk) {
                            MessageManager.sendMessage(player, "claim_details_plot_vacant_claimable", Integer.toString(chunk.getChunkX()), Integer.toString(chunk.getChunkZ()), chunk.getWorld());
                        } else {
                            MessageManager.sendMessage(player, "claim_details_plot_vacant");
                        }
                    }
                } else {
                    MessageManager.sendMessage(player, "claim_details_plot_owner", playerCache.getUsername(chunk.getPlotChunkOwner()));
                    // Send a list of the town members
                    if (!chunk.getPlotChunkMembers().isEmpty()) {
                        StringJoiner townMembers = new StringJoiner("[,](gray) ");
                        for (UUID uuid : chunk.getPlotChunkMembers()) {
                            townMembers.add("[" + MineDown.escape(playerCache.getUsername(uuid)) + "](white show_text=&7UUID: " + MineDown.escape(uuid.toString()) + ")");
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