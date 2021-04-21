package me.william278.bungeetowny.object;

import me.william278.bungeetowny.data.DataManager;
import me.william278.bungeetowny.object.chunk.ChunkLocation;
import me.william278.bungeetowny.object.chunk.ClaimedChunk;

import java.util.HashMap;

/**
 * This class manages a cache of all claimed chunks on the server for high-performance checking
 * without pulling data from SQL every time a player mines a block.
 *
 * It is updated when a player makes or removes a claim on the server.
 * It is also updated when a player disbands a town. If this has been done cross-server, plugin messages will alert the plugin
 */
public class ClaimCache {

    private final HashMap<ChunkLocation,ClaimedChunk> claims;

    /**
     * Initialize the claim cache by loading all claims onto it
     */
    public ClaimCache() {
        claims = new HashMap<>();
        reload();
    }

    /**
     * Reload the claim cache
     */
    public void reload() {
        claims.clear();
        DataManager.updateClaimedChunkCache();
    }

    /**
     * Add a chunk to the cache
     * @param chunk the ClaimedChunk to add
     */
    public void add(ClaimedChunk chunk) {
        claims.put(chunk, chunk);
    }

    /**
     * Returns the ClaimedChunk at the given position
     * @param chunkX chunk X position to remove from cache
     * @param chunkZ chunk Z position to remove from cache
     * @param world chunk world name to remove from cache
     * @return the ClaimedChunk; null if there is not one
     */
    public ClaimedChunk getChunkAt(int chunkX, int chunkZ, String world) {
        for (ChunkLocation chunkLocation : claims.keySet()) {
            ClaimedChunk chunk = claims.get(chunkLocation);
            if (chunkX == chunk.getChunkX() && chunkZ == chunk.getChunkZ() && world.equals(chunk.getWorld())) {
                return chunk;
            }
        }
        return null;
    }

    /**
     * Remove a ClaimedChunk at given X, Z and World
     * @param chunkX chunk X position to remove from cache
     * @param chunkZ chunk Z position to remove from cache
     * @param world chunk world name to remove from cache
     */
    public void remove(int chunkX, int chunkZ, String world) {
        ClaimedChunk chunkToRemove = null;
        for (ChunkLocation chunkLocation : claims.keySet()) {
            ClaimedChunk chunk = claims.get(chunkLocation);
            if (chunkX == chunk.getChunkX() && chunkZ == chunk.getChunkZ() && world.equals(chunk.getWorld())) {
                chunkToRemove = chunk;
            }
        }
        if (chunkToRemove != null) {
            claims.remove(chunkToRemove);
        }
    }
}
