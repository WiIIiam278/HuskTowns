package me.william278.bungeetowny.command;

import de.themoep.minedown.MineDown;
import me.william278.bungeetowny.HuskTowns;
import me.william278.bungeetowny.MessageManager;
import me.william278.bungeetowny.object.cache.ClaimCache;
import me.william278.bungeetowny.object.chunk.ClaimedChunk;
import me.william278.bungeetowny.object.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class MapCommand extends CommandBase {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    public static String getMapAround(int chunkX, int chunkZ, String world) {
        ClaimCache cache = HuskTowns.getClaimCache();

        StringBuilder map = new StringBuilder();
        for (int currentChunkZ = (chunkZ - 5); currentChunkZ <= chunkZ + 5; currentChunkZ++) {
            for (int currentChunkX = (chunkX - 5); currentChunkX <= chunkX + 5; currentChunkX++) {
                ClaimedChunk chunk = cache.getChunkAt(currentChunkX, currentChunkZ, world);
                if (chunk == null) {
                    map.append("[⬜](#2e2e2e");
                } else {
                    String townName = chunk.getTown();
                    String colorCode = Town.getTownColor(townName);

                    map.append("[⬛](").append(colorCode)
                            .append(" show_text=")
                            .append("&")
                            .append(colorCode)
                            .append("&").append(townName).append("&r\n");

                    switch (chunk.getChunkType()) {
                        case FARM:
                            map.append("&r&#b0b0b0&Farming Chunk")
                                    .append("&r\n");
                            break;
                        case PLOT:
                            if (chunk.getPlotChunkOwner() != null) {
                                map.append("&r&#b0b0b0&")
                                        .append(HuskTowns.getPlayerCache().getUsername(chunk.getPlotChunkOwner()))
                                        .append("'s Plot")
                                        .append("&r\n");
                            } else {
                                map.append("&r&#b0b0b0&")
                                        .append("Unclaimed Plot")
                                        .append("&r\n");
                            }
                            break;
                    }
                    map.append("&r&#b0b0b0&Chunk: &").append(colorCode).append("&")
                            .append((currentChunkX * 16))
                            .append(", ")
                            .append((currentChunkX * 16))
                            .append("&r\n")
                            .append("&#b0b0b0&Claimed: &").append(colorCode).append("&")
                            .append(chunk.getFormattedTime());

                    if (chunk.getClaimerUUID() != null) {
                        String claimedBy = HuskTowns.getPlayerCache().getUsername(chunk.getClaimerUUID());
                        map.append("&r\n")
                                .append("&#b0b0b0&By: &").append(colorCode).append("&")
                                .append(claimedBy);
                    }
                    if (currentChunkX == chunkX && currentChunkZ == chunkZ) {
                        map.append("\n&#b0b0b0&Currently standing in");
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int chunkX = player.getLocation().getChunk().getX();
            int chunkZ = player.getLocation().getChunk().getZ();
            String world = player.getLocation().getWorld().getName();

            if (args.length == 2 || args.length == 3) {
                try {
                    chunkX = Integer.parseInt(args[0]);
                    chunkZ = Integer.parseInt(args[1]);
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
            }
            MessageManager.sendMessage(player, "claim_map_header");
            player.spigot().sendMessage(new MineDown(getMapAround(chunkX, chunkZ, world)).toComponent());
        });
    }
}
