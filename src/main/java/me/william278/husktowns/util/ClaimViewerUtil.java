package me.william278.husktowns.util;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.object.cache.ClaimCache;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.util.claimviewer.ClaimViewer;
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
                    String ownerName = HuskTowns.getPlayerCache().getUsername(plotOwner);
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
}