package me.william278.bungeetowny.objects.chunk;

import java.util.UUID;

/**
 * A chunk within a town owned by a certain resident
 */
public class PlotChunk extends ClaimedChunk {

    // UUID of the plot's owner
    UUID plotOwner;

    // Whether or not the plot has been claimed
    boolean claimed;

    public PlotChunk(String server, String worldName, int chunkX, int chunkZ, UUID claimantUUID) {
        super(server, worldName, chunkX, chunkZ);
        this.plotOwner = claimantUUID;
        this.claimed = true;
    }

    public PlotChunk(String server, String worldName, int chunkX, int chunkZ) {
        super(server, worldName, chunkX, chunkZ);
        this.plotOwner = null;
        this.claimed = false;
    }

    // Returns null if owner does not exist
    public UUID getPlotOwner() {
        return plotOwner;
    }

    // Returns if the plot is claimed or not
    public boolean isClaimed() {
        return claimed;
    }
}
