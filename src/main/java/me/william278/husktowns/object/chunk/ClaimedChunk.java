package me.william278.husktowns.object.chunk;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.object.cache.PlayerCache;
import me.william278.husktowns.object.town.Town;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

public class ClaimedChunk extends ChunkLocation {

    // Type of chunk
    private final ChunkType chunkType;

    // UUID of player who claimed the chunk
    private final UUID claimer;

    // Timestamp which the chunk was claimed on
    private final long claimTimestamp;

    // Name of the town the chunk is claimed by
    private String town;

    // UUID of the chunk owner if this is a plot chunk; null if unclaimed
    private final UUID plotChunkOwner;
    private final HashSet<UUID> plotChunkMembers = new HashSet<>();

    public ClaimedChunk(String server, String worldName, int chunkX, int chunkZ, UUID claimerUUID, ChunkType chunkType, UUID plotChunkOwner, HashSet<UUID> plotChunkMembers, String town, long timestamp) {
        super(server, worldName, chunkX, chunkZ);
        this.chunkType = chunkType;
        this.claimer = claimerUUID;
        this.plotChunkOwner = plotChunkOwner;
        this.plotChunkMembers.addAll(plotChunkMembers);
        this.town = town;
        this.claimTimestamp = timestamp;
    }

    public ClaimedChunk(String server, String worldName, int chunkX, int chunkZ, UUID claimerUUID, ChunkType chunkType, String town, long timestamp) {
        super(server, worldName, chunkX, chunkZ);
        this.chunkType = chunkType;
        this.claimer = claimerUUID;
        this.plotChunkOwner = null;
        this.town = town;
        this.claimTimestamp = timestamp;
    }

    public ClaimedChunk(Player player, String town) {
        super(HuskTowns.getSettings().getServerID(), player.getWorld().getName(), player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());
        this.chunkType = ChunkType.REGULAR;
        this.claimer = player.getUniqueId();
        this.plotChunkOwner = null;
        this.town = town;
        this.claimTimestamp = Instant.now().getEpochSecond();
    }

    // Returns whether or not the given player can build in the town
    public boolean canPlayerBuildIn(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (HuskTowns.ignoreClaimPlayers.contains(playerUUID)) {
            return true;
        }
        if (town.equals(HuskTowns.getSettings().getAdminTownName())) {
            return !player.hasPermission("husktowns.administrator.admin_claim_access");
        }
        final PlayerCache cache = HuskTowns.getPlayerCache();
        if (chunkType == ChunkType.PLOT) {
            if (plotChunkOwner.equals(playerUUID)) {
                return true;
            }
            if (plotChunkMembers.contains(playerUUID)) {
                return true;
            }
        }
        if (cache.isPlayerInTown(playerUUID)) {
            // The player is in this claim's town
            if (cache.getTown(playerUUID).equalsIgnoreCase(town)) {
                if (chunkType == ChunkType.FARM) {
                    return true;
                }
                return cache.getRole(playerUUID) != Town.TownRole.RESIDENT;
            }
        }
        return false;
    }

    public void updateTownName(String newName) {
        town = newName;
    }

    public void addPlotMember(UUID plotMember) { plotChunkMembers.add(plotMember); }

    public void removePlotMember(UUID plotMember) { plotChunkMembers.remove(plotMember); }

    public long getClaimTimestamp() {
        return claimTimestamp;
    }

    public String getFormattedTime() {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochSecond(getClaimTimestamp()));
    }

    public UUID getClaimerUUID() { return claimer; }

    public UUID getPlotChunkOwner() { return  plotChunkOwner; }

    public HashSet<UUID> getPlotChunkMembers() { return plotChunkMembers; }

    public ChunkType getChunkType() {
        return chunkType;
    }

    public String getTown() {
        return town;
    }

    public enum ChunkType {
        REGULAR,
        FARM,
        PLOT
    }
}