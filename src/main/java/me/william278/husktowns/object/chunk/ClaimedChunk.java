package me.william278.husktowns.object.chunk;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.listener.EventListener;
import me.william278.husktowns.object.cache.PlayerCache;
import me.william278.husktowns.object.flag.Flag;
import me.william278.husktowns.object.town.Town;
import org.bukkit.Bukkit;
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

    public PlayerAccess getPlayerAccess(UUID uuid, EventListener.ActionType actionType) {

        // If the town has a public build access flag set in this type of claim then let them build
        boolean allowedByFlags = false;
        for (Flag flag : HuskTowns.getTownDataCache().getFlags(town, chunkType)) {
            if (flag.actionMatches(actionType)) {
                allowedByFlags = flag.isActionAllowed(actionType);
            }
        }
        if (allowedByFlags) {
            return PlayerAccess.CAN_BUILD_PUBLIC_BUILD_ACCESS_FLAG;
        }

        // If the player is ignoring claim rights, then let them build
        if (HuskTowns.ignoreClaimPlayers.contains(uuid)) {
            return PlayerAccess.CAN_BUILD_IGNORING_CLAIMS;
        }

        // If public access flags are set, permit the action.
        if (town.equals(HuskTowns.getSettings().getAdminTownName())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if (player.hasPermission("husktowns.administrator.admin_claim_access")) {
                    return PlayerAccess.CAN_BUILD_ADMIN_CLAIM_ACCESS;
                }
            }

            return PlayerAccess.CANNOT_BUILD_ADMIN_CLAIM;
        }

        // If this is a claimed plot chunk and the player is a member, let them build in it.
        if (chunkType == ChunkType.PLOT) {
            if (plotChunkOwner != null) {
                if (plotChunkMembers.contains(uuid)) {
                    return PlayerAccess.CAN_BUILD_PLOT_MEMBER;
                }
            }
        }

        final PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (playerCache.isPlayerInTown(uuid)) {
            if (playerCache.getPlayerTown(uuid).equalsIgnoreCase(town)) {
                switch (chunkType) {
                    case FARM:
                        return PlayerAccess.CAN_BUILD_TOWN_FARM;
                    case PLOT:
                        if (plotChunkOwner != null) {
                            if (plotChunkOwner.equals(uuid)) {
                                return PlayerAccess.CAN_BUILD_PLOT_OWNER;
                            }
                        }
                }
                if (playerCache.getPlayerRole(uuid) == Town.TownRole.RESIDENT) {
                    return PlayerAccess.CANNOT_BUILD_RESIDENT;
                }
                return PlayerAccess.CAN_BUILD_TRUSTED;
            } else {
                return PlayerAccess.CANNOT_BUILD_DIFFERENT_TOWN;
            }
        } else {
            return PlayerAccess.CANNOT_BUILD_NOT_IN_TOWN;
        }
    }

    /**
     * Returns the {@link PlayerAccess} a player has within this claimed chunk
     *
     * @param player The {@link Player} to check
     * @return The {@link PlayerAccess} the player has in this chunk
     */
    public PlayerAccess getPlayerAccess(Player player, EventListener.ActionType actionType) {
        return getPlayerAccess(player.getUniqueId(), actionType);
    }

    public void updateTownName(String newName) {
        town = newName;
    }

    public void addPlotMember(UUID plotMember) {
        plotChunkMembers.add(plotMember);
    }

    public void removePlotMember(UUID plotMember) {
        plotChunkMembers.remove(plotMember);
    }

    public long getClaimTimestamp() {
        return claimTimestamp;
    }

    public String getFormattedClaimTime() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochSecond(getClaimTimestamp()));
    }

    public UUID getClaimerUUID() {
        return claimer;
    }

    public UUID getPlotChunkOwner() {
        return plotChunkOwner;
    }

    public HashSet<UUID> getPlotChunkMembers() {
        return plotChunkMembers;
    }

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
     * Enum for the status of a player's ability to do stuff within a {@link ClaimedChunk}
     */
    public enum PlayerAccess {
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
         * The player can build because the town has a public build flag set here
         */
        CAN_BUILD_PUBLIC_BUILD_ACCESS_FLAG,

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
        CANNOT_BUILD_DIFFERENT_TOWN
    }
}