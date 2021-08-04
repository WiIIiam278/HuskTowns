package me.william278.husktowns.util.claimviewer;

import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.object.town.Town;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;

public class ClaimViewer {
    private static final Particle BORDER_PARTICLE = Particle.REDSTONE;
    private static final double PARTICLE_SPACING = 0.2D;
    private static final double PARTICLE_HOVER = 0.1D;

    private final HashMap<String, Iterable<ParticleLocation>> iterableParticleLocations = new HashMap<>();

    // Create a new ClaimViewer and calculate the particles
    public ClaimViewer(ClaimedChunk... chunksToView) {
        // Get all the particle locations
        final HashMap<String, HashSet<ParticleLocation>> particleLocations = new HashMap<>();
        for (ClaimedChunk chunk : chunksToView) {
            final String townName = chunk.getTown();
            if (!particleLocations.containsKey(townName)) {
                particleLocations.put(townName, new HashSet<>());
            }
            particleLocations.get(townName).addAll(getParticleLocations(chunk));
        }

        // Graham scan the particle locations to only show the outline of each town claim set
        for (String townName : particleLocations.keySet()) {
            //iterableParticleLocations.put(townName, new GrahamScan(particleLocations.get(townName).toArray(ParticleLocation[]::new)).hull());
            iterableParticleLocations.put(townName, particleLocations.get(townName));
        }
    }

    // Using this ClaimViewer, show the chunk border particles to the player
    public void showChunkBorders(Player player) {
        for (String townName : iterableParticleLocations.keySet()) {
            final java.awt.Color townColor = Town.getTownColor(townName);
            final Particle.DustOptions dustOptions = new Particle.DustOptions(org.bukkit.Color.fromBGR(townColor.getBlue(), townColor.getGreen(), townColor.getRed()), 1);
            final Iterable<ParticleLocation> particleLocations = iterableParticleLocations.get(townName);
            particleLocations.forEach(particleLocation -> player.spawnParticle(BORDER_PARTICLE, particleLocation,1, dustOptions));
        }
    }

    private HashSet<ParticleLocation> getParticleLocations(ClaimedChunk claimedChunk) {
        final HashSet<ParticleLocation> particleLocations = new HashSet<>();

        // Don't return particle locations from unloaded chunks / worlds
        World world = Bukkit.getWorld(claimedChunk.getWorld());
        if (world == null) {
            return new HashSet<>();
        }
        Chunk chunk = world.getChunkAt(claimedChunk.getChunkX(), claimedChunk.getChunkZ());
        if (!chunk.isLoaded()) {
            return new HashSet<>();
        }

        // Calculate particle locations
        ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();
        int subChunkX;
        int subChunkZ = 0;
        int subChunkY = 64;

        for (subChunkX = 0; subChunkX < 16; subChunkX++) {
            Location subChunkBlockLocation = chunk.getBlock(subChunkX, subChunkY, 0).getLocation();
            Location parallelSubChunkBlockLocation = chunk.getBlock(subChunkX, subChunkY, 15).getLocation();
            for (double subPosX = 0; subPosX <= 1; subPosX = subPosX + PARTICLE_SPACING) {
                particleLocations.add(new ParticleLocation(subChunkBlockLocation.getWorld(),
                        (subChunkBlockLocation.getX() + subPosX),
                        chunkSnapshot.getHighestBlockYAt(subChunkX, subChunkZ) + PARTICLE_HOVER,
                        (subChunkBlockLocation.getZ())));
                particleLocations.add(new ParticleLocation(parallelSubChunkBlockLocation.getWorld(),
                        (parallelSubChunkBlockLocation.getX() + subPosX),
                        chunkSnapshot.getHighestBlockYAt(subChunkX, 15) + PARTICLE_HOVER,
                        (parallelSubChunkBlockLocation.getZ() + 1)));
            }
        }
        for (subChunkZ = 0; subChunkZ < 16; subChunkZ++) {
            Location subChunkBlockLocation = chunk.getBlock(15, subChunkY, subChunkZ).getLocation();
            Location parallelSubChunkBlockLocation = chunk.getBlock(0, subChunkY, subChunkZ).getLocation();
            for (double subPosZ = 0; subPosZ <= 1; subPosZ = subPosZ + PARTICLE_SPACING) {
                particleLocations.add(new ParticleLocation(subChunkBlockLocation.getWorld(),
                        (subChunkBlockLocation.getX() + 1),
                        chunkSnapshot.getHighestBlockYAt(15, subChunkZ) + PARTICLE_HOVER,
                        (subChunkBlockLocation.getZ() + subPosZ)));
                particleLocations.add(new ParticleLocation(parallelSubChunkBlockLocation.getWorld(),
                        (parallelSubChunkBlockLocation.getX()),
                        chunkSnapshot.getHighestBlockYAt(0, subChunkZ) + PARTICLE_HOVER,
                        (parallelSubChunkBlockLocation.getZ() + subPosZ)));
            }
        }
        return particleLocations;
    }
}
