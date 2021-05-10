package me.william278.husktowns.util;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.object.town.Town;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class provides methods to show particles around chunks to a player
 */
public class ClaimViewerUtil {

    private static final Particle BORDER_PARTICLE = Particle.REDSTONE;
    private static final HashMap<UUID,ClaimedChunk> highlightedChunks = new HashMap<>();
    private static final HuskTowns plugin = HuskTowns.getInstance();

    public static void inspectChunk(Player player, Location locationToInspect) {
        Chunk chunkToInspect = locationToInspect.getChunk();
        ClaimedChunk chunk = HuskTowns.getClaimCache().getChunkAt(chunkToInspect.getX(), chunkToInspect.getZ(), chunkToInspect.getWorld().getName());

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
            case REGULAR:
                MessageManager.sendMessage(player,"regular_chunk_claimed_by", chunk.getTown(), Integer.toString(chunkToInspect.getX()),
                        Integer.toString(chunkToInspect.getZ()), chunkToInspect.getWorld().getName());
                break;
            case FARM:
                MessageManager.sendMessage(player,"farm_chunk_claimed_by", chunk.getTown(), Integer.toString(chunkToInspect.getX()),
                        Integer.toString(chunkToInspect.getZ()), chunkToInspect.getWorld().getName());
                break;
            case PLOT:
                UUID plotOwner = chunk.getPlotChunkOwner();
                if (plotOwner != null) {
                    String ownerName = HuskTowns.getPlayerCache().getUsername(plotOwner);
                    MessageManager.sendMessage(player,"assigned_plot_chunk_claimed_by", chunk.getTown(), ownerName, Integer.toString(chunkToInspect.getX()),
                            Integer.toString(chunkToInspect.getZ()), chunkToInspect.getWorld().getName());
                } else {
                    MessageManager.sendMessage(player,"unassigned_plot_chunk_claimed_by", chunk.getTown(), Integer.toString(chunkToInspect.getX()),
                            Integer.toString(chunkToInspect.getZ()), chunkToInspect.getWorld().getName());
                }
                break;
        }
        showParticles(player, chunk, 5);
    }

    public static void showParticles(Player player, ClaimedChunk chunk, int duration) {
        if (Bukkit.getWorld(chunk.getWorld()) == null) {
            return;
        }
        showParticles(player, chunk.getChunkX(), chunk.getChunkZ(), Bukkit.getWorld(chunk.getWorld()), duration);
    }

    private static void showParticles(Player player, int chunkX, int chunkZ, World world, int duration) {
        ArrayList<Location> chunkHighlightParticleLocations = new ArrayList<>();
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        if (!chunk.isLoaded()) {
            return;
        }
        ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int subChunkX;
            int subChunkZ = 0;
            int subChunkY = 64;

            for (subChunkX = 0; subChunkX < 16; subChunkX++) {
                Location subChunkBlockLocation = chunk.getBlock(subChunkX, subChunkY, 0).getLocation();
                Location parallelSubChunkBlockLocation = chunk.getBlock(subChunkX, subChunkY, 15).getLocation();
                for (double subPosX = 0; subPosX <= 1; subPosX = subPosX + 0.2D) {
                    chunkHighlightParticleLocations.add(new Location(subChunkBlockLocation.getWorld(),
                            (subChunkBlockLocation.getX() + subPosX),
                            chunkSnapshot.getHighestBlockYAt(subChunkX, subChunkZ) + 0.1,
                            (subChunkBlockLocation.getZ())));
                    chunkHighlightParticleLocations.add(new Location(parallelSubChunkBlockLocation.getWorld(),
                            (parallelSubChunkBlockLocation.getX() + subPosX),
                            chunkSnapshot.getHighestBlockYAt(subChunkX, 15) + 0.1,
                            (parallelSubChunkBlockLocation.getZ() + 1)));
                }
            }
            for (subChunkZ = 0; subChunkZ < 16; subChunkZ++) {
                Location subChunkBlockLocation = chunk.getBlock(15, subChunkY, subChunkZ).getLocation();
                Location parallelSubChunkBlockLocation = chunk.getBlock(0, subChunkY, subChunkZ).getLocation();
                for (double subPosZ = 0; subPosZ <= 1; subPosZ = subPosZ + 0.2D) {
                    chunkHighlightParticleLocations.add(new Location(subChunkBlockLocation.getWorld(),
                            (subChunkBlockLocation.getX() + 1),
                            chunkSnapshot.getHighestBlockYAt(15, subChunkZ) + 0.1,
                            (subChunkBlockLocation.getZ() + subPosZ)));
                    chunkHighlightParticleLocations.add(new Location(parallelSubChunkBlockLocation.getWorld(),
                            (parallelSubChunkBlockLocation.getX()),
                            chunkSnapshot.getHighestBlockYAt(0, subChunkZ) + 0.1,
                            (parallelSubChunkBlockLocation.getZ() + subPosZ)));
                }
            }

            ClaimedChunk chunkAt = HuskTowns.getClaimCache().getChunkAt(chunkX, chunkZ, world.getName());
            Color color = Color.GRAY;
            if (chunkAt != null) {
                color = Town.getTownColor(chunkAt.getTown());
            }

            org.bukkit.Color bukkitColor = org.bukkit.Color.fromBGR(color.getBlue(), color.getGreen(), color.getRed());
            if (highlightedChunks.containsKey(player.getUniqueId())) {
                if (highlightedChunks.get(player.getUniqueId()) == chunkAt) {
                    return;
                }
            }
            highlightedChunks.put(player.getUniqueId(), chunkAt);

            AtomicInteger repeats = new AtomicInteger();
            new BukkitRunnable() {
                @Override
                public void run() {
                    repeats.getAndIncrement();
                    if (highlightedChunks.get(player.getUniqueId()) != chunkAt) {
                        cancel();
                    }
                    for (Location location : chunkHighlightParticleLocations) {
                        player.spawnParticle(BORDER_PARTICLE, location,1, new Particle.DustOptions(bukkitColor, 1));
                    }
                    if (repeats.get() >= (duration)*2) {
                        highlightedChunks.remove(player.getUniqueId());
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, 10);
        });

    }

}
