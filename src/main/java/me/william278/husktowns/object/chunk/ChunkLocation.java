package me.william278.husktowns.object.chunk;

public class ChunkLocation {

    final int chunkX;
    final int chunkZ;
    final String world;
    final String server;

    public ChunkLocation(String server, String world, int x, int z) {
        this.chunkX = x;
        this.chunkZ = z;
        this.world = world;
        this.server = server;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public String getWorld() {
        return world;
    }

    public String getServer() {
        return server;
    }
}
