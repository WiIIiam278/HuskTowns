package me.william278.husktowns.object.chunk;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.listeners.EventListener;
import me.william278.husktowns.object.cache.PlayerCache;
import me.william278.husktowns.object.town.Town;
import org.bukkit.Location;
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

    public ClaimedChunk(Player player, Location location, String town) {
        super(HuskTowns.getSettings().getServerID(), location.getWorld().getName(), location.getChunk().getX(), location.getChunk().getZ());
        this.chunkType = ChunkType.REGULAR;
        this.claimer = player.getUniqueId();
        this.plotChunkOwner = null;
        this.town = town;
        this.claimTimestamp = Instant.now().getEpochSecond();
    }

    /**
     * Returns the {@link BuildAccess} a player has within this claimed chunk
     * @param player The {@link Player} to check
     * @return The {@link BuildAccess} the player has in this chunk
     */
    public BuildAccess getPlayerAccess(Player player) {
        UUID playerUUID = player.getUniqueId();

        // If the player is ignoring claim rights, then let them build
        if (HuskTowns.ignoreClaimPlayers.contains(playerUUID)) {
            return BuildAccess.CAN_BUILD_IGNORING_CLAIMS;
        }

        // Determine their access level if this is an admin claim.
        if (town.equals(HuskTowns.getSettings().getAdminTownName())) {
            if (player.hasPermission("husktowns.administrator.admin_claim_access")) {
                return BuildAccess.CAN_BUILD_ADMIN_CLAIM_ACCESS;
            }
            return BuildAccess.CANNOT_BUILD_ADMIN_CLAIM;
        }

        switch (chunkType) {
            // If this is a claimed plot chunk and the player is a member, let them build in it.
            case PLOT:
                if (plotChunkOwner != null) {
                    if (plotChunkMembers.contains(playerUUID)) {
                        return BuildAccess.CAN_BUILD_PLOT_MEMBER;
                    }
                }
                break;
            // If this is a farm chunk and the "allow the public to use farm chunks" setting is on let them build
            case FARM:
                if (HuskTowns.getSettings().allowPublicAccessToFarmChunks()) {
                    return BuildAccess.CAN_BUILD_TOWN_FARM;
                }
                break;
            default:
                break;
        }

        final PlayerCache cache = HuskTowns.getPlayerCache();
        if (cache.isPlayerInTown(playerUUID)) {
            if (cache.getTown(playerUUID).equalsIgnoreCase(town)) {
                switch (chunkType) {
                    case FARM:
                        return BuildAccess.CAN_BUILD_TOWN_FARM;
                    case PLOT:
                        if (plotChunkOwner != null) {
                            if (plotChunkOwner.equals(playerUUID)) {
                                return BuildAccess.CAN_BUILD_PLOT_OWNER;
                            }
                        }
                }
                if (cache.getRole(playerUUID) == Town.TownRole.RESIDENT) {
                    return BuildAccess.CANNOT_BUILD_RESIDENT;
                }
                return BuildAccess.CAN_BUILD_TRUSTED;
            } else {
                return BuildAccess.CANNOT_BUILD_DIFFERENT_TOWN;
            }
        } else {
            return BuildAccess.CANNOT_BUILD_NOT_IN_TOWN;
        }
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

    /**
     * Enum determining the type of a {@link ClaimedChunk}
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
     * Enum for the status of a player's ability to build within a {@link ClaimedChunk}
     */
    public enum BuildAccess {
        /**
         * The player can build because they are the plot owner of this chunk
         */
        CAN_BUILD_PLOT_OWNER,
        /**
         * The player can build because they are a member of this plot
         */
        CAN_BUILD_PLOT_MEMBER,
        /**
         * The player can build because this is a farm chunk
         */
        CAN_BUILD_TOWN_FARM,
        /**
         * The player can build because they are ignoring claims
         */
        CAN_BUILD_IGNORING_CLAIMS,
        /**
         * The player can build because they have access to admin claims
         */
        CAN_BUILD_ADMIN_CLAIM_ACCESS,
        /**
         * The player can build because they are a trusted citizen or mayor
         */
        CAN_BUILD_TRUSTED,


        /**
         * The player cannot build because they are only a resident
         */
        CANNOT_BUILD_RESIDENT,
        /**
         * The player cannot build because they do not have permission to build in admin claims
         */
        CANNOT_BUILD_ADMIN_CLAIM,
        /**
         * The player cannot build because they are not in a town
         */
        CANNOT_BUILD_NOT_IN_TOWN,
        /**
         * The player cannot build because they are not in the same town
         */
        CANNOT_BUILD_DIFFERENT_TOWN,
    }
}