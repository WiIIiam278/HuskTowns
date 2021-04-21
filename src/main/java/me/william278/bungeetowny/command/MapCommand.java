package me.william278.bungeetowny.command;

import de.themoep.minedown.MineDown;
import me.william278.bungeetowny.HuskTowns;
import me.william278.bungeetowny.MessageManager;
import me.william278.bungeetowny.object.ClaimCache;
import me.william278.bungeetowny.object.chunk.ClaimedChunk;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Random;

public class MapCommand extends CommandBase {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    // Converts a string into an integer value
    public long getStringValue(String string) {
        long value = 0;
        for (String c : string.split("")) {
            value++;
            int charint = c.charAt(0);
            value = value * (long) charint;
        }
        return value;
    }

    public String getMapAround(Location location) {
        String world = location.getWorld().getName();
        int chunkX = location.getChunk().getX();
        int chunkZ = location.getChunk().getZ();
        ClaimCache cache = HuskTowns.getClaimCache();

        StringBuilder map = new StringBuilder();
        for (int currentChunkZ = (chunkZ-5); currentChunkZ <= chunkZ+5; currentChunkZ++) {
            for (int currentChunkX = (chunkX-5); currentChunkX <= chunkX+5; currentChunkX++) {
                ClaimedChunk chunk = cache.getChunkAt(currentChunkX, currentChunkZ, world);
                if (chunk == null) {
                    map.append("[⬜](#2e2e2e");
                    if (currentChunkX == chunkX && currentChunkZ == chunkZ) {
                        map.append(" show_text=&#b0b0b0&Currently standing in)");
                    } else {
                        map.append(")");
                    }
                } else {
                    String townName = chunk.getTown();
                    String claimedOn = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            .withLocale(Locale.getDefault())
                            .withZone(ZoneId.systemDefault())
                            .format(Instant.ofEpochSecond(chunk.getClaimTimestamp()));

                    // Generates a random color code to color a town, seeded based on the town name
                    Random random = new Random(getStringValue(townName));
                    int randomHex = random.nextInt(0xffffff + 1);
                    String colorCode = String.format("#%06x", randomHex);

                    map.append("[⬛](").append(colorCode)
                            .append(" show_text=")
                            .append("&")
                            .append(colorCode)
                            .append("&").append(townName).append("&r\n")
                            .append("&r&#b0b0b0&Chunk: &").append(colorCode).append("&")
                            .append(currentChunkX)
                            .append(", ")
                            .append(currentChunkZ)
                            .append("&r\n")
                            .append("&#b0b0b0&Claimed: &").append(colorCode).append("&")
                            .append(claimedOn);

                    if (chunk.getClaimerUUID() != null) {
                        String claimedBy = Bukkit.getOfflinePlayer(chunk.getClaimerUUID()).getName();
                        map.append("&r\n")
                                .append("&#b0b0b0&By: &").append(colorCode).append("&")
                                .append(claimedBy);
                    }
                    if (currentChunkX == chunkX && currentChunkZ == chunkZ) {
                        map.append("\n&#b0b0b0&Currently standing in");
                    }
                    map.append("&r)");
                }

            }
            map.append("\n");
        }
        return map.toString();
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            MessageManager.sendMessage(player, "claim_map_header");
            player.spigot().sendMessage(new MineDown(getMapAround(player.getLocation())).toComponent());
        });
    }
}
