package me.william278.husktowns.util;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.object.cache.ClaimCache;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.object.town.Town;
import org.bukkit.*;
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
            case REGULAR -> MessageManager.sendMessage(player, "regular_chunk_claimed_by", chunk.getTown(), Integer.toString(chunkToInspect.getX()),
                    Integer.toString(chunkToInspect.getZ()), chunkToInspect.getWorld().getName());
            case FARM -> MessageManager.sendMessage(player, "farm_chunk_claimed_by", chunk.getTown(), Integer.toString(chunkToInspect.getX()),
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
        final int centreChunkX = location.getChunk().getX();
        final int centreChunkZ = location.getChunk().getZ();

        final HashSet<ClaimedChunk> chunksToShow = new HashSet<>();
        for (int currentChunkX = (centreChunkX - 5); currentChunkX < (centreChunkX + 5); currentChunkX++) {
            for (int currentChunkZ = (centreChunkZ - 5); currentChunkZ < (centreChunkZ + 5); currentChunkZ++) {
                ClaimedChunk chunk = cache.getChunkAt(currentChunkX, currentChunkZ, world.getName());
                if (chunk != null) {
                    chunksToShow.add(chunk);
                }
            }
        }

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
        private static final double PARTICLE_SPACING = 0.2D;
        private static final double PARTICLE_HOVER = 0.1D;

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
                    Bukkit.getScheduler().runTask(plugin, () -> particleLocations.forEach(particleLocation -> player.spawnParticle(BORDER_PARTICLE, particleLocation,1, dustOptions)));
                }
            });
        }

        // Returns a HashSet of the particle display locations for this chunk
        private HashSet<Location> getParticleLocations(ClaimedChunk claimedChunk) {
            final HashSet<Location> particleLocations = new HashSet<>();

            // Don't return particle locations from unloaded chunks / worlds
            World world = Bukkit.getWorld(claimedChunk.getWorld());
            if (world == null) {
                return particleLocations;
            }
            Chunk chunk = world.getChunkAt(claimedChunk.getChunkX(), claimedChunk.getChunkZ());
            if (!chunk.isLoaded()) {
                return particleLocations;
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
                    particleLocations.add(new Location(subChunkBlockLocation.getWorld(),
                            (subChunkBlockLocation.getX() + subPosX),
                            chunkSnapshot.getHighestBlockYAt(subChunkX, subChunkZ) + PARTICLE_HOVER,
                            (subChunkBlockLocation.getZ())));
                    particleLocations.add(new Location(parallelSubChunkBlockLocation.getWorld(),
                            (parallelSubChunkBlockLocation.getX() + subPosX),
                            chunkSnapshot.getHighestBlockYAt(subChunkX, 15) + PARTICLE_HOVER,
                            (parallelSubChunkBlockLocation.getZ() + 1)));
                }
            }
            for (subChunkZ = 0; subChunkZ < 16; subChunkZ++) {
                Location subChunkBlockLocation = chunk.getBlock(15, subChunkY, subChunkZ).getLocation();
                Location parallelSubChunkBlockLocation = chunk.getBlock(0, subChunkY, subChunkZ).getLocation();
                for (double subPosZ = 0; subPosZ <= 1; subPosZ = subPosZ + PARTICLE_SPACING) {
                    particleLocations.add(new Location(subChunkBlockLocation.getWorld(),
                            (subChunkBlockLocation.getX() + 1),
                            chunkSnapshot.getHighestBlockYAt(15, subChunkZ) + PARTICLE_HOVER,
                            (subChunkBlockLocation.getZ() + subPosZ)));
                    particleLocations.add(new Location(parallelSubChunkBlockLocation.getWorld(),
                            (parallelSubChunkBlockLocation.getX()),
                            chunkSnapshot.getHighestBlockYAt(0, subChunkZ) + PARTICLE_HOVER,
                            (parallelSubChunkBlockLocation.getZ() + subPosZ)));
                }
            }
            return particleLocations;
        }
    }
}