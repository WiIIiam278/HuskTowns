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
        for (int currentChunkZ = chunkZ-8; currentChunkZ <= chunkZ+8; chunkZ++) {
            for (int currentChunkX = chunkX-8; currentChunkX <= chunkX+8; chunkX++) {
                ClaimedChunk chunk = cache.getChunkAt(currentChunkX, currentChunkZ, world);
                if (chunk == null) {
                    map.append("[■](#262626)");
                } else {
                    String townName = chunk.getTown();
                    String claimedBy = Bukkit.getOfflinePlayer(chunk.getClaimerUUID()).getName();
                    String claimedOn = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            .withLocale(Locale.getDefault())
                            .withZone(ZoneId.systemDefault())
                            .format(Instant.ofEpochSecond(chunk.getClaimTimestamp()));

                    Random random = new Random(getStringValue(townName));
                    int randomHex = random.nextInt(0xffffff + 1);
                    String colorCode = String.format("#%06x", randomHex);

                    map.append("[■](").append(colorCode)
                            .append("show_text=")
                            .append("&l&")
                            .append(colorCode)
                            .append("&\n")
                            .append("&r&#262626&Chunk: &").append(colorCode).append("&")
                            .append("(")
                            .append(currentChunkX)
                            .append(", ")
                            .append(currentChunkZ)
                            .append(")&r\n")
                            .append("&#262626&Claimed: &").append(colorCode).append("&")
                            .append(claimedOn)
                            .append("&r\n")
                            .append("&#262626&By: &").append(colorCode).append("&")
                            .append(claimedBy)
                            .append(")");
                }
            }
            map.append("\n");
        }
        return map.toString();
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            player.spigot().sendMessage(new MineDown(getMapAround(player.getLocation())).toComponent());
        });
    }
}
