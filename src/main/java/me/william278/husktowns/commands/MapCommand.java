package me.william278.husktowns.commands;

import de.themoep.minedown.MineDown;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.object.cache.ClaimCache;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.object.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class MapCommand extends CommandBase {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    public static String getMapAround(int chunkX, int chunkZ, String world, String viewerTown, boolean doCurrentlyHere) {
        ClaimCache cache = HuskTowns.getClaimCache();

        StringBuilder map = new StringBuilder();
        for (int currentChunkZ = (chunkZ - 5); currentChunkZ <= chunkZ + 5; currentChunkZ++) {
            for (int currentChunkX = (chunkX - 5); currentChunkX <= chunkX + 5; currentChunkX++) {
                ClaimedChunk chunk = cache.getChunkAt(currentChunkX, currentChunkZ, world);
                if (chunk == null) {
                    map.append("[▒](#2e2e2e ");
                    map.append("show_text=").append(MessageManager.getRawMessage("wilderness"));
                    if (doCurrentlyHere && currentChunkX == chunkX && currentChunkZ == chunkZ) {
                        map.append("\n&#b0b0b0&▽ Currently here ▽ ");
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
                                    .append(" show_text=")
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
                        map.append("&r&")
                                .append(colorCode)
                                .append("&Ⓐ &r&#b0b0b0&Admin Claim")
                                .append("&r\n");
                    } else {
                        switch (chunk.getChunkType()) {
                            case FARM:
                                map.append("&r&")
                                        .append(colorCode)
                                        .append("&Ⓕ &r&#b0b0b0&Farming Chunk")
                                        .append("&r\n");
                                break;
                            case PLOT:
                                if (chunk.getPlotChunkOwner() != null) {
                                    map.append("&r&").append(colorCode).append("&Ⓟ&r &#b0b0b0&")
                                            .append(HuskTowns.getPlayerCache().getUsername(chunk.getPlotChunkOwner()))
                                            .append("'s Plot")
                                            .append("&r\n");
                                    if (!chunk.getPlotChunkMembers().isEmpty()) {
                                        map.append("&#b0b0b0&Plot Members: &").append(colorCode).append("&")
                                                .append(chunk.getPlotChunkMembers().size())
                                                .append("&r\n");
                                    }
                                } else {
                                    map.append("&r&")
                                            .append(colorCode)
                                            .append("&Ⓟ &r&#b0b0b0&Unclaimed Plot")
                                            .append("&r\n");
                                }
                                break;
                        }
                    }
                    map.append("&r&#b0b0b0&Chunk: &").append(colorCode).append("&")
                            .append((currentChunkX * 16))
                            .append(", ")
                            .append((currentChunkZ * 16))
                            .append("&r\n")
                            .append("&#b0b0b0&Claimed: &").append(colorCode).append("&")
                            .append(chunk.getFormattedTime());

                    if (chunk.getClaimerUUID() != null) {
                        String claimedBy = HuskTowns.getPlayerCache().getUsername(chunk.getClaimerUUID());
                        map.append("&r\n")
                                .append("&#b0b0b0&By: &").append(colorCode).append("&")
                                .append(claimedBy);
                    }
                    if (doCurrentlyHere && currentChunkX == chunkX && currentChunkZ == chunkZ) {
                        map.append("\n&#b0b0b0&▽ Currently here ▽");
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
            String world = player.getLocation().getWorld().getName();
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
                        HuskTowns.getPlayerCache().getTown(player.getUniqueId()), doCurrentlyHere)).toComponent());
            } else {
                player.spigot().sendMessage(new MineDown(getMapAround(chunkX, chunkZ, world, null, doCurrentlyHere)).toComponent());
            }
        });
    }
}
