package me.william278.husktowns.chunk;

/**
 * An object representing a claimed chunk's position on a server running HuskTowns
 */
public class ChunkLocation {

    private final int chunkX;
    private final int chunkZ;
    private final String world;
    private final String server;

    /**
     * Creates a new ChunkLocation
     * @param server ID of the server on the proxy
     * @param world Name of the world on the server
     * @param x Chunk grid x position
     * @param z Chunk grid z position
     * @see ClaimedChunk
     */
    public ChunkLocation(String server, String world, int x, int z) {
        this.chunkX = x;
        this.chunkZ = z;
        this.world = world;
        this.server = server;
    }

    /**
     * Returns the X position on the chunk grid.
     * @return The X position
     */
    public int getChunkX() {
        return chunkX;
    }

    /**
     * Returns the Z position on the chunk grid.
     * @return The Z position
     */
    public int getChunkZ() {
        return chunkZ;
    }

    /**
     * Returns the name of the world on the server the chunk is on
     * @return The world name
     */
    public String getWorld() {
        return world;
    }

    /**
     * Returns the ID of the server on the proxy that this chunk is on
     * @return The server ID
     */
    public String getServer() {
        return server;
    }
}
