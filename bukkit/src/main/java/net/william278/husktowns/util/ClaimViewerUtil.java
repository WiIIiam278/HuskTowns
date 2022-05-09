package net.william278.husktowns.util;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.MessageManager;
import net.william278.husktowns.cache.Cache;
import net.william278.husktowns.cache.ClaimCache;
import net.william278.husktowns.chunk.ClaimedChunk;
import net.william278.husktowns.town.Town;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class provides methods to show particles around chunks to a player
 */
public class ClaimViewerUtil {

    private static final HashMap<UUID, ClaimViewer> claimViewers = new HashMap<>();
    private static final HuskTowns plugin = HuskTowns.getInstance();

    public static void inspectChunk(Player player, Location locationToInspect) {
        Chunk chunkToInspect = locationToInspect.getChunk();
        ClaimCache cache = HuskTowns.getClaimCache();
        if (!cache.hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", cache.getName());
            return;
        }

        ClaimedChunk chunk = cache.getChunkAt(chunkToInspect.getX(), chunkToInspect.getZ(), chunkToInspect.getWorld().getName());

        for (String restrictedWorld : HuskTowns.getSettings().getUnClaimableWorlds()) {
            if (chunkToInspect.getWorld().getName().equals(restrictedWorld)) {
                MessageManager.sendMessage(player, "inspect_chunk_not_claimable");
                return;
            }
        }
        if (chunk == null) {
            MessageManager.sendMessage(player, "inspect_chunk_not_claimed");
            return;
        }
        switch (chunk.getChunkType()) {
            case REGULAR ->
                    MessageManager.sendMessage(player, "regular_chunk_claimed_by", chunk.getTown(), Integer.toString(chunkToInspect.getX()),
                            Integer.toString(chunkToInspect.getZ()), chunkToInspect.getWorld().getName());
            case FARM ->
                    MessageManager.sendMessage(player, "farm_chunk_claimed_by", chunk.getTown(), Integer.toString(chunkToInspect.getX()),
                            Integer.toString(chunkToInspect.getZ()), chunkToInspect.getWorld().getName());
            case PLOT -> {
                UUID plotOwner = chunk.getPlotChunkOwner();
                if (plotOwner != null) {
                    String ownerName = HuskTowns.getPlayerCache().getPlayerUsername(plotOwner);
                    MessageManager.sendMessage(player, "assigned_plot_chunk_claimed_by", chunk.getTown(), ownerName, Integer.toString(chunkToInspect.getX()),
                            Integer.toString(chunkToInspect.getZ()), chunkToInspect.getWorld().getName());
                } else {
                    MessageManager.sendMessage(player, "unassigned_plot_chunk_claimed_by", chunk.getTown(), Integer.toString(chunkToInspect.getX()),
                            Integer.toString(chunkToInspect.getZ()), chunkToInspect.getWorld().getName());
                }
            }
        }
        showParticles(player, 5, chunk);
    }

    // Returns a HashSet of chunks near a point
    public static HashSet<ClaimedChunk> getChunksNear(Location centerPoint, int chunkRadius) {
        final ClaimCache cache = HuskTowns.getClaimCache();
        if (!cache.hasLoaded()) {
            return new HashSet<>();
        }
        if (centerPoint.getWorld() == null) {
            return new HashSet<>();
        }
        final HashSet<ClaimedChunk> nearbyChunks = new HashSet<>();
        final int centreChunkX = centerPoint.getChunk().getX();
        final int centreChunkZ = centerPoint.getChunk().getZ();

        for (int currentChunkX = (centreChunkX - chunkRadius); currentChunkX < (centreChunkX + chunkRadius); currentChunkX++) {
            for (int currentChunkZ = (centreChunkZ - chunkRadius); currentChunkZ < (centreChunkZ + chunkRadius); currentChunkZ++) {
                ClaimedChunk chunk = cache.getChunkAt(currentChunkX, currentChunkZ, centerPoint.getWorld().getName());
                if (chunk != null) {
                    nearbyChunks.add(chunk);
                }
            }
        }
        return nearbyChunks;
    }

    public static void inspectNearbyChunks(Player player, Location location) {
        final ClaimCache cache = HuskTowns.getClaimCache();
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        if (!cache.hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", cache.getName());
            return;
        }
        final HashSet<ClaimedChunk> chunksToShow = getChunksNear(location, 5);

        if (chunksToShow.isEmpty()) {
            MessageManager.sendMessage(player, "no_nearby_claims");
        } else {
            MessageManager.sendMessage(player, "showing_nearby_chunks", Integer.toString(chunksToShow.size()));
            showParticles(player, 5, chunksToShow.toArray(ClaimedChunk[]::new));
        }
    }

    public static void showParticles(Player player, int duration, ClaimedChunk... chunksToView) {
        final ClaimViewer currentClaimViewer = new ClaimViewer(chunksToView);
        final UUID playerUUID = player.getUniqueId();
        claimViewers.put(playerUUID, currentClaimViewer);

        final AtomicInteger repeats = new AtomicInteger();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                if (!claimViewers.containsKey(playerUUID)) {
                    cancel();
                    return;
                }
                if (claimViewers.get(playerUUID) != currentClaimViewer) {
                    cancel();
                    return;
                }
                currentClaimViewer.showChunkBorders(player);
                repeats.getAndIncrement();
                if (repeats.get() >= (duration) * 2) {
                    claimViewers.remove(playerUUID);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    public static class ClaimViewer {
        private static final Particle BORDER_PARTICLE = Particle.REDSTONE;
        private static final double PARTICLES_BETWEEN = 5;
        private static final double PARTICLE_HOVER = 1.1D;

        private final HashMap<String, HashSet<Location>> particleLocations = new HashMap<>();
        private volatile boolean isDone = false;

        // Create a new ClaimViewer and calculate the particles
        public ClaimViewer(ClaimedChunk... chunksToView) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                // Get all the particle locations
                for (ClaimedChunk claimedChunk : chunksToView) {
                    final String townName = claimedChunk.getTown();
                    if (!particleLocations.containsKey(townName)) {
                        particleLocations.put(townName, new HashSet<>());
                    }
                    particleLocations.get(townName).addAll(getParticleLocations(claimedChunk));
                }
                isDone = true;
            });
        }

        // Using this ClaimViewer, show the chunk border particles to the player
        public void showChunkBorders(Player player) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                while (!isDone) Thread.onSpinWait();
                for (String townName : particleLocations.keySet()) {
                    final java.awt.Color townColor = Town.getTownColor(townName);
                    final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromBGR(townColor.getBlue(), townColor.getGreen(), townColor.getRed()), 1);
                    final Iterable<Location> particleLocations = this.particleLocations.get(townName);
                    Bukkit.getScheduler().runTask(plugin, () -> particleLocations.forEach(particleLocation -> player.spawnParticle(BORDER_PARTICLE, particleLocation, 1, dustOptions)));
                }
            });
        }

        // Returns a HashSet of the particle display locations for this chunk
        private ArrayList<Location> getParticleLocations(ClaimedChunk claimedChunk) {
            final ArrayList<Location> particleLocations = new ArrayList<>();

            // Don't return particle locations from unloaded chunks / worlds
            World world = Bukkit.getWorld(claimedChunk.getWorld());
            if (world == null) {
                return particleLocations;
            }
            Chunk chunk = world.getChunkAt(claimedChunk.getChunkX(), claimedChunk.getChunkZ());
            Chunk chunkX1 = world.getChunkAt(claimedChunk.getChunkX(), claimedChunk.getChunkZ());
            Chunk chunkZ1 = world.getChunkAt(claimedChunk.getChunkX(), claimedChunk.getChunkZ());

            if (!chunk.isLoaded() || !chunkX1.isLoaded() || !chunkZ1.isLoaded()) {
                return particleLocations;
            }

            // Calculate particle locations
            ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();

            // Determine cornerstone locations
            final ArrayList<Location> blockLocations = new ArrayList<>();

            int subChunkX;
            int subChunkZ;

            // Add starting block
            blockLocations.add(chunk.getBlock(0, chunkSnapshot.getHighestBlockYAt(0, 0),
                    0).getLocation().add(0, PARTICLE_HOVER, 0));

            // Iterate along edges of chunk, add blocks
            for (subChunkX = 0; subChunkX < 16; subChunkX++) {
                blockLocations.add(chunk.getBlock(subChunkX, chunkSnapshot.getHighestBlockYAt(subChunkX, 0),
                        0).getLocation().add(1, PARTICLE_HOVER, 0));
            }
            for (subChunkZ = 0; subChunkZ < 16; subChunkZ++) {
                blockLocations.add(chunk.getBlock(15, chunkSnapshot.getHighestBlockYAt(15, subChunkZ),
                        subChunkZ).getLocation().add(1, PARTICLE_HOVER, 1));
            }
            for (subChunkX = 15; subChunkX >= 0; subChunkX--) {
                blockLocations.add(chunk.getBlock(subChunkX, chunkSnapshot.getHighestBlockYAt(subChunkX, 15),
                        15).getLocation().add(1, PARTICLE_HOVER, 1));
            }
            for (subChunkZ = 15; subChunkZ >= 0; subChunkZ--) {
                blockLocations.add(chunk.getBlock(0, chunkSnapshot.getHighestBlockYAt(0, subChunkZ),
                        subChunkZ).getLocation().add(0, PARTICLE_HOVER, 1));
            }

            // Add final block
            blockLocations.add(chunk.getBlock(0, chunkSnapshot.getHighestBlockYAt(0, 0),
                    0).getLocation().add(0, PARTICLE_HOVER, 0));


            // Interpolate points
            for (int i = 1; i < blockLocations.size(); i++) {
                final Location lastLocation = blockLocations.get(i - 1);
                final Location targetLocation = blockLocations.get(i);


                final double xStep = ((targetLocation.getX() - lastLocation.getX()) / PARTICLES_BETWEEN);
                final double yStep = ((targetLocation.getY() - lastLocation.getY()) / PARTICLES_BETWEEN);
                final double zStep = ((targetLocation.getZ() - lastLocation.getZ()) / PARTICLES_BETWEEN);
                for (int j = 0; j <= PARTICLES_BETWEEN; j++) {
                    particleLocations.add(new Location(lastLocation.getWorld(),
                            lastLocation.getX() + (xStep * j),
                            lastLocation.getY() + (yStep * j),
                            lastLocation.getZ() + (zStep * j)));
                }
            }

            return particleLocations;
        }
    }
}