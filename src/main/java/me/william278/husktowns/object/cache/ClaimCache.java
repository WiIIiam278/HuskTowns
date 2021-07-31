package me.william278.husktowns.object.cache;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.chunk.ChunkLocation;
import me.william278.husktowns.object.chunk.ClaimedChunk;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class manages a cache of all claimed chunks on the server for high-performance checking
 * without pulling data from SQL every time a player mines a block.
 *
 * It is updated when a player makes or removes a claim on the server.
 * It is also updated when a player disbands a town. If this has been done cross-server, plugin messages will alert the plugin
 */
public class ClaimCache extends Cache {

    private final HashMap<ChunkLocation,ClaimedChunk> claims;

    /**
     * Initialize the claim cache by loading all claims onto it
     */
    public ClaimCache() {
        super("Town Claims");
        claims = new HashMap<>();
        reload();
    }

    /**
     * Reload the claim cache
     */
    public void reload() {
        claims.clear();
        if (HuskTowns.getSettings().doMapIntegration()) {
            HuskTowns.getMap().clearMarkers();
        }
        DataManager.updateClaimedChunkCache();
    }

    public void renameReload(String oldName, String newName) throws CacheNotLoadedException {
        HashMap<ChunkLocation, ClaimedChunk> chunksToUpdate = getChunksToReload(oldName);
        for (ChunkLocation chunkLocation : chunksToUpdate.keySet()) {
            claims.remove(chunkLocation);
            ClaimedChunk chunk = chunksToUpdate.get(chunkLocation);
            chunk.updateTownName(newName);
            claims.put(chunkLocation, chunk);
        }
        if (HuskTowns.getSettings().doMapIntegration()) {
            HuskTowns.getMap().addMarkers(new HashSet<>(chunksToUpdate.values()));
        }
    }

    public void removeAllClaims(String disbandingTown) throws CacheNotLoadedException {
        HashMap<ChunkLocation, ClaimedChunk> chunksToRemove = getChunksToReload(disbandingTown);
        for (ChunkLocation chunkLocation : chunksToRemove.keySet()) {
            claims.remove(chunkLocation);
        }
        if (HuskTowns.getSettings().doMapIntegration()) {
            HuskTowns.getMap().removeMarkers(new HashSet<>(chunksToRemove.values()));
        }
    }

    private HashMap<ChunkLocation, ClaimedChunk> getChunksToReload(String oldName) {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        HashMap<ChunkLocation, ClaimedChunk> chunksToUpdate = new HashMap<>();
        for (ChunkLocation cl : claims.keySet()) {
            if (claims.get(cl).getTown().equals(oldName)) {
                chunksToUpdate.put(cl, claims.get(cl));
            }
        }
        return chunksToUpdate;
    }

    /**
     * Add a chunk to the cache
     * @param chunk the {@link ClaimedChunk} to add
     */
    public void add(ClaimedChunk chunk) {
        claims.put(chunk, chunk);
        if (HuskTowns.getSettings().doMapIntegration()) {
            HuskTowns.getMap().addMarker(chunk);
        }
        incrementItemsLoaded();
    }

    /**
     * Returns the ClaimedChunk at the given position
     * @param chunkX chunk X position
     * @param chunkZ chunk Z position
     * @param world chunk world name
     * @return the {@link ClaimedChunk}; null if there is not one
     */
    public ClaimedChunk getChunkAt(int chunkX, int chunkZ, String world) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        try {
            final HashMap<ChunkLocation,ClaimedChunk> currentClaims = claims;
            for (ChunkLocation chunkLocation : currentClaims.keySet()) {
                final ClaimedChunk chunk = currentClaims.get(chunkLocation);
                if (chunkX == chunk.getChunkX() && chunkZ == chunk.getChunkZ() && world.equals(chunk.getWorld()) && chunk.getServer().equals(HuskTowns.getSettings().getServerID())) {
                    return chunk;
                }
            }
        } catch (ConcurrentModificationException e) {
            return null; // Catches a rare exception where claims are being modified as stuff occurs
        }
        return null;
    }

    /**
     * Returns every claimed chunk in the cache
     * @return all {@link ClaimedChunk}s currently cached
     */
    public Collection<ClaimedChunk> getAllChunks() throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        return claims.values();
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
            if (HuskTowns.getSettings().doMapIntegration()) {
                HuskTowns.getMap().removeMarker(chunkToRemove);
            }
            claims.remove(chunkToRemove);
        }
    }
}
