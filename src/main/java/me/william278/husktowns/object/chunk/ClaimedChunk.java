package me.william278.husktowns.object.chunk;

import me.william278.husktowns.HuskTowns;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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

    public ClaimedChunk(String server, String worldName, int chunkX, int chunkZ, UUID claimerUUID, ChunkType chunkType, UUID plotChunkOwner, String town, long timestamp) {
        super(server, worldName, chunkX, chunkZ);
        this.chunkType = chunkType;
        this.claimer = claimerUUID;
        this.plotChunkOwner = plotChunkOwner;
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

    public void updateTownName(String newName) {
        town = newName;
    }

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