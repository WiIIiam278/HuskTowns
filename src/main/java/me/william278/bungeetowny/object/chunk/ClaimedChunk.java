package me.william278.bungeetowny.object.chunk;

import me.william278.bungeetowny.HuskTowns;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.UUID;

public class ClaimedChunk {

    // Type of chunk
    private final ChunkType chunkType;

    // Location of the chunk on the Bungee network
    private final String server;

    // X and Z chunk position in the world
    private final String worldName;
    private final int chunkX;
    private final int chunkZ;

    // UUID of player who claimed the chunk
    private final UUID claimer;

    // Timestamp which the chunk was claimed on
    private static long claimTimestamp;

    // UUID of the chunk owner if this is a plot chunk; null if unclaimed
    private final UUID plotChunkOwner;

    public ClaimedChunk(String server, String worldName, int chunkX, int chunkZ, UUID claimerUUID, ChunkType chunkType, UUID plotChunkOwner) {
        this.chunkType = chunkType;
        this.server = server;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.claimer = claimerUUID;
        this.plotChunkOwner = plotChunkOwner;
        claimTimestamp = Instant.now().getEpochSecond();
    }

    public ClaimedChunk(String server, String worldName, int chunkX, int chunkZ, UUID claimerUUID, ChunkType chunkType) {
        this.chunkType = chunkType;
        this.server = server;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.claimer = claimerUUID;
        this.plotChunkOwner = null;
        claimTimestamp = Instant.now().getEpochSecond();
    }

    public ClaimedChunk(Player player) {
        this.chunkType = ChunkType.REGULAR;
        this.server = HuskTowns.getSettings().getServerID();
        this.worldName = player.getWorld().getName();
        this.chunkX = player.getLocation().getChunk().getX();
        this.chunkZ = player.getLocation().getChunk().getZ();
        this.claimer = player.getUniqueId();
        this.plotChunkOwner = null;
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

    public UUID getClaimerUUID() { return  claimer; }

    public UUID getPlotChunkOwner() { return  plotChunkOwner; }

}