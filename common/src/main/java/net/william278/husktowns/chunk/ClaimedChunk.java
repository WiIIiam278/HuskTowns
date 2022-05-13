package net.william278.husktowns.chunk;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

/**
 * An object representing a chunk that has been claimed by a town.
 */
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

    /**
     * Create a new ClaimedChunk object as a plot chunk with an owner and list of members
     * @param server The ID of the server on the proxy that the chunk is on
     * @param worldName The name of the world the chunk is on
     * @param chunkX The x position on the chunk grid the claim is on
     * @param chunkZ The z position on the chunk grid the claim is on
     * @param claimerUUID The {@link UUID} of the person claiming the chunk
     * @param chunkType The {@link ChunkType} of the chunk
     * @param plotChunkOwner The {@link UUID} of the plot chunk owner
     * @param plotChunkMembers {@link HashSet} of {@link UUID}s of plot chunk members; empty for no members
     * @param town The name of the town who owns the chunk
     * @param timestamp The time since epoch of when the chunk was claimed
     */
    public ClaimedChunk(String server, String worldName, int chunkX, int chunkZ, UUID claimerUUID, ChunkType chunkType, UUID plotChunkOwner, HashSet<UUID> plotChunkMembers, String town, long timestamp) {
        super(server, worldName, chunkX, chunkZ);
        this.chunkType = chunkType;
        this.claimer = claimerUUID;
        this.plotChunkOwner = plotChunkOwner;
        this.plotChunkMembers.addAll(plotChunkMembers);
        this.town = town;
        this.claimTimestamp = timestamp;
    }

    /**
     * Create a new ClaimedChunk object
     * @param server The ID of the server on the proxy that the chunk is on
     * @param worldName The name of the world the chunk is on
     * @param chunkX The x position on the chunk grid the claim is on
     * @param chunkZ The z position on the chunk grid the claim is on
     * @param claimerUUID The {@link UUID} of the person claiming the chunk
     * @param chunkType The {@link ChunkType} of the chunk
     * @param town The name of the town who owns the chunk
     * @param timestamp The time since epoch of when the chunk was claimed
     */
    public ClaimedChunk(String server, String worldName, int chunkX, int chunkZ, UUID claimerUUID, ChunkType chunkType, String town, long timestamp) {
        super(server, worldName, chunkX, chunkZ);
        this.chunkType = chunkType;
        this.claimer = claimerUUID;
        this.plotChunkOwner = null;
        this.town = town;
        this.claimTimestamp = timestamp;
    }

    /**
     * Have a {@link Player} create a new claimed chunk object at the specified location
     * @param player The {@link Player} doing the claiming
     * @param server The ID of the server on the proxy where the claim will be made
     * @param location The {@link Location} to claim
     * @param town The name of the {@link Player}'s town
     */
    public ClaimedChunk(Player player, String server, Location location, String town) {
        super(server, location.getWorld().getName(), location.getChunk().getX(), location.getChunk().getZ());
        this.chunkType = ChunkType.REGULAR;
        this.claimer = player.getUniqueId();
        this.plotChunkOwner = null;
        this.town = town;
        this.claimTimestamp = Instant.now().getEpochSecond();
    }

    /**
     * Set the name of the town who owns this chunk
     * @param newName The town who now owns this chunk
     */
    public void updateTownName(String newName) {
        town = newName;
    }

    /**
     * Add a plot member to this chunk
     * @param plotMember {@link UUID} to add
     */
    public void addPlotMember(UUID plotMember) {
        plotChunkMembers.add(plotMember);
    }

    /**
     * Removes a plot member from this chunk
     * @param plotMember {@link UUID} to remove
     */
    public void removePlotMember(UUID plotMember) {
        plotChunkMembers.remove(plotMember);
    }

    /**
     * Returns the time from epoch of when this chunk was claimed
     * @return When this chunk was claimed
     */
    public long getClaimTimestamp() {
        return claimTimestamp;
    }

    /**
     * Returns the formatted timestamp string of when this chunk was claimed
     * @return When this chunk was claimed
     */
    public String getFormattedClaimTime() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochSecond(getClaimTimestamp()));
    }

    /**
     * Returns the {@link UUID} of the claimer of this chunk
     * @return Claimer's {@link UUID}
     */
    public UUID getClaimerUUID() {
        return claimer;
    }

    /**
     * Returns the {@link UUID} of the plot chunk owner (Returns {@code null} if it is unassigned or not a plot)
     * @return Plot owner's {@link UUID}
     */
    public UUID getPlotChunkOwner() {
        return plotChunkOwner;
    }

    /**
     * Returns a list of plot chunk member {@link UUID}s
     * @return The plot chunk members
     */
    public HashSet<UUID> getPlotChunkMembers() {
        return plotChunkMembers;
    }

    /**
     * Returns the {@link ChunkType} of this chunk
     * @return the {@link ChunkType}
     */
    public ChunkType getChunkType() {
        return chunkType;
    }

    /**
     * Returns the name of the town who own this cv
     * @return the town name
     */
    public String getTown() {
        return town;
    }

    /**
     * Designates the types of {@link ClaimedChunk}
     */
    public enum ChunkType {
        /**
         * This is a regular chunk
         */
        REGULAR,
        /**
         * This is a town farm chunk
         */
        FARM,
        /**
         * This is a plot chunk; it can have a plot owner and members
         */
        PLOT
    }

    /**
     * Designates the status of a player's ability to perform actions within a {@link ClaimedChunk}
     */
    public enum PlayerAccess {
        /**
         * The player can perform the action because they are the plot owner of this chunk
         */
        CAN_PERFORM_ACTION_PLOT_OWNER,
        /**
         * The player can perform the action because they are a member of this plot
         */
        CAN_PERFORM_ACTION_PLOT_MEMBER,
        /**
         * The player can perform the action because this is a farm chunk
         */
        CAN_PERFORM_ACTION_TOWN_FARM,
        /**
         * The player can perform the action because they are ignoring claims
         */
        CAN_PERFORM_ACTION_IGNORING_CLAIMS,
        /**
         * The player can perform the action because they have access to admin claims
         */
        CAN_PERFORM_ACTION_ADMIN_CLAIM_ACCESS,
        /**
         * The player can perform the action because they are a trusted citizen or mayor
         */
        CAN_PERFORM_ACTION_TRUSTED_ACCESS,
        /**
         * The player can perform the action because the town has a public build flag set here
         */
        CAN_PERFORM_ACTION_PUBLIC_BUILD_ACCESS_FLAG,

        /**
         * The player cannot perform the action because they are only a resident
         */
        CANNOT_PERFORM_ACTION_NO_TRUSTED_ACCESS,
        /**
         * The player cannot perform the action because they do not have permission to build in admin claims
         */
        CANNOT_PERFORM_ACTION_ADMIN_CLAIM,
        /**
         * The player cannot perform the action because they are not in a town
         */
        CANNOT_PERFORM_ACTION_NOT_IN_TOWN,
        /**
         * The player cannot perform the action because they are not in the same town
         */
        CANNOT_PERFORM_ACTION_DIFFERENT_TOWN
    }
}