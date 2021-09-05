package me.william278.husktowns.commands;

import de.themoep.minedown.MineDown;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.cache.ClaimCache;
import me.william278.husktowns.chunk.ClaimedChunk;
import me.william278.husktowns.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class MapCommand extends CommandBase {

    private static final HuskTowns plugin = HuskTowns.getInstance();
    private static final int mapRadius = HuskTowns.getSettings().getTownMapSquareRadius();

    public static String getMapAround(int chunkX, int chunkZ, String world, String viewerTown, boolean doCurrentlyHere) {
        ClaimCache cache = HuskTowns.getClaimCache();

        StringBuilder map = new StringBuilder();
        for (int currentChunkZ = (chunkZ - mapRadius); currentChunkZ <= chunkZ + mapRadius; currentChunkZ++) {
            for (int currentChunkX = (chunkX - mapRadius); currentChunkX <= chunkX + mapRadius; currentChunkX++) {
                ClaimedChunk chunk = cache.getChunkAt(currentChunkX, currentChunkZ, world);
                if (chunk == null) {
                    if (HuskTowns.getSettings().getUnClaimableWorlds().contains(world)) {
                        map.append("[▒](#780000 ");
                        map.append("show_text=").append(MessageManager.getRawMessage("map_square_unclaimable")).append(" ");
                    } else {
                        map.append("[▒](#2e2e2e ");
                        map.append("show_text=").append(MessageManager.getRawMessage("map_square_wilderness")).append(" ");
                    }

                    if (doCurrentlyHere && currentChunkX == chunkX && currentChunkZ == chunkZ) {
                        map.append("\n").append(MessageManager.getRawMessage("map_square_currently_here")).append(" ");
                    }
                    if (viewerTown != null) {
                        map.append("run_command=/claim ").append(currentChunkX).append(" ")
                                .append(currentChunkZ).append(" ").append(world);
                    }
                } else {
                    String townName = chunk.getTown();
                    String colorCode = Town.getTownColorHex(townName);

                    if (viewerTown != null) {
                        if (townName.equals(viewerTown)) {
                            map.append("[█](").append(colorCode)
                                    .append(" ")
                                    .append("&")
                                    .append(colorCode)
                                    .append("&").append(townName).append("&r\n");
                        } else {
                            map.append("[▓](").append(colorCode)
                                    .append(" show_text=")
                                    .append("&")
                                    .append(colorCode)
                                    .append("&").append(townName).append("&r\n");
                        }
                    } else {
                        map.append("[█](").append(colorCode)
                                .append(" show_text=")
                                .append("&")
                                .append(colorCode)
                                .append("&").append(townName).append("&r\n");
                    }

                    if (townName.equals(HuskTowns.getSettings().getAdminTownName())) {
                        map.append("&r").append(MessageManager.getRawMessage("map_square_admin_claim", colorCode))
                                .append("&r\n");
                    } else {
                        switch (chunk.getChunkType()) {
                            case FARM:
                                map.append("&r").append(MessageManager.getRawMessage("map_square_farm", colorCode))
                                        .append("&r\n");
                                break;
                            case PLOT:
                                if (chunk.getPlotChunkOwner() != null) {
                                    map.append("&r").append(MessageManager.getRawMessage("map_square_unclaimed_plot", colorCode, HuskTowns.getPlayerCache().getPlayerUsername(chunk.getPlotChunkOwner())))
                                            .append("&r\n");
                                    if (!chunk.getPlotChunkMembers().isEmpty()) {
                                        map.append(MessageManager.getRawMessage("map_square_plot_member_count", colorCode, Integer.toString(chunk.getPlotChunkMembers().size())))
                                                .append("&r\n");
                                    }
                                } else {
                                    map.append("&r").append(MessageManager.getRawMessage("map_square_unclaimed_plot", colorCode))
                                            .append("&r\n");
                                }
                                break;
                        }
                    }
                    map.append("&r")
                            .append(MessageManager.getRawMessage("map_square_chunk", colorCode, Integer.toString(currentChunkX * 16), Integer.toString(currentChunkZ * 16)))
                            .append("&r\n")
                            .append(MessageManager.getRawMessage("map_square_claimed_timestamp", colorCode, chunk.getFormattedClaimTime()));

                    if (chunk.getClaimerUUID() != null) {
                        final String claimedBy = HuskTowns.getPlayerCache().getPlayerUsername(chunk.getClaimerUUID());
                        map.append("&r\n")
                                .append(MessageManager.getRawMessage("map_square_claimed_by", colorCode, claimedBy));
                    }
                    if (doCurrentlyHere && currentChunkX == chunkX && currentChunkZ == chunkZ) {
                        map.append("\n").append(MessageManager.getRawMessage("map_square_currently_here"));
                    }
                    map.append(" run_command=/town view ").append(townName);
                }
                map.append(")");
            }
            map.append("\n");
        }
        return map.toString();
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        final ClaimCache claimCache = HuskTowns.getClaimCache();
        if (!claimCache.hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", claimCache.getName());
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int chunkX = player.getLocation().getChunk().getX();
            int chunkZ = player.getLocation().getChunk().getZ();
            String world = player.getWorld().getName();
            boolean doCurrentlyHere = true;

            if (args.length == 2 || args.length == 3) {
                try {
                    chunkX = Integer.parseInt(args[0]);
                    chunkZ = Integer.parseInt(args[1]);
                    doCurrentlyHere = (chunkX == player.getLocation().getChunk().getX()) && (chunkZ == player.getLocation().getChunk().getZ());
                } catch (NumberFormatException exception) {
                    MessageManager.sendMessage(player, "error_invalid_chunk_coords");
                    return;
                }
            }
            if (args.length == 3) {
                world = args[2];
                if (Bukkit.getWorld(world) == null) {
                    MessageManager.sendMessage(player, "error_invalid_world");
                    return;
                }
                doCurrentlyHere = world.equals(player.getWorld().getName());
            }
            MessageManager.sendMessage(player, "claim_map_header");
            if (HuskTowns.getPlayerCache().isPlayerInTown(player.getUniqueId())) {
                player.spigot().sendMessage(new MineDown(getMapAround(chunkX, chunkZ, world,
                        HuskTowns.getPlayerCache().getPlayerTown(player.getUniqueId()), doCurrentlyHere)).toComponent());
            } else {
                player.spigot().sendMessage(new MineDown(getMapAround(chunkX, chunkZ, world, null, doCurrentlyHere)).toComponent());
            }
        });
    }
}
