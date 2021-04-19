package me.william278.bungeetowny.object.chunk;

import me.william278.bungeetowny.HuskTowns;
import org.bukkit.entity.Player;

import java.time.Instant;

public class ClaimedChunk {

    // Location of the chunk on the Bungee network
    private final String server;

    // X and Z chunk position in the world
    private final String worldName;
    private final int chunkX;
    private final int chunkZ;

    // Timestamp which the chunk was claimed on
    private static long claimTimestamp;

    public ClaimedChunk(String server, String worldName, int chunkX, int chunkZ) {
        this.server = server;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        claimTimestamp = Instant.now().getEpochSecond();
    }

    public ClaimedChunk(Player player) {
        this.server = HuskTowns.getSettings().getServerID();
        this.worldName = player.getWorld().getName();
        this.chunkX = player.getLocation().getChunk().getX();
        this.chunkZ = player.getLocation().getChunk().getZ();
    }

    public String getServer() {
        return server;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public long getClaimTimestamp() {
        return claimTimestamp;
    }
}
