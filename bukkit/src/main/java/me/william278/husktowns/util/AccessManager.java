package me.william278.husktowns.util;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.cache.PlayerCache;
import me.william278.husktowns.chunk.ClaimedChunk;
import me.william278.husktowns.flags.Flag;
import me.william278.husktowns.listener.ActionType;
import me.william278.husktowns.town.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AccessManager {

    public static ClaimedChunk.PlayerAccess getPlayerAccess(UUID uuid, ActionType actionType, ClaimedChunk chunk) {
        // If the town has a public build access flag set in this type of claim then let them build
        boolean allowedByFlags = false;
        for (Flag flag : HuskTowns.getTownDataCache().getFlags(chunk.getTown(), chunk.getChunkType())) {
            if (flag.actionMatches(actionType)) {
                allowedByFlags = flag.isActionAllowed(actionType);
            }
        }
        if (allowedByFlags) {
            return ClaimedChunk.PlayerAccess.CAN_PERFORM_ACTION_PUBLIC_BUILD_ACCESS_FLAG;
        }

        // If the player is ignoring claim rights, then let them build
        if (HuskTowns.ignoreClaimPlayers.contains(uuid)) {
            return ClaimedChunk.PlayerAccess.CAN_PERFORM_ACTION_IGNORING_CLAIMS;
        }

        // If public access flags are set, permit the action.
        if (chunk.getTown().equals(HuskTowns.getSettings().getAdminTownName())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if (player.hasPermission("husktowns.administrator.admin_claim_access")) {
                    return ClaimedChunk.PlayerAccess.CAN_PERFORM_ACTION_ADMIN_CLAIM_ACCESS;
                }
            }

            return ClaimedChunk.PlayerAccess.CANNOT_PERFORM_ACTION_ADMIN_CLAIM;
        }

        // If this is a claimed plot chunk and the player is a member, let them build in it.
        if (chunk.getChunkType() == ClaimedChunk.ChunkType.PLOT) {
            if (chunk.getPlotChunkOwner() != null) {
                if (chunk.getPlotChunkMembers().contains(uuid)) {
                    return ClaimedChunk.PlayerAccess.CAN_PERFORM_ACTION_PLOT_MEMBER;
                }
            }
        }

        final PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (playerCache.isPlayerInTown(uuid)) {
            if (playerCache.getPlayerTown(uuid).equalsIgnoreCase(chunk.getTown())) {
                switch (chunk.getChunkType()) {
                    case FARM:
                        return ClaimedChunk.PlayerAccess.CAN_PERFORM_ACTION_TOWN_FARM;
                    case PLOT:
                        if (chunk.getPlotChunkOwner() != null) {
                            if (chunk.getPlotChunkOwner().equals(uuid)) {
                                return ClaimedChunk.PlayerAccess.CAN_PERFORM_ACTION_PLOT_OWNER;
                            }
                        }
                }
                if (playerCache.getPlayerRole(uuid) == TownRole.RESIDENT) {
                    return ClaimedChunk.PlayerAccess.CANNOT_PERFORM_ACTION_RESIDENT;
                }
                return ClaimedChunk.PlayerAccess.CAN_PERFORM_ACTION_TRUSTED;
            } else {
                return ClaimedChunk.PlayerAccess.CANNOT_PERFORM_ACTION_DIFFERENT_TOWN;
            }
        } else {
            return ClaimedChunk.PlayerAccess.CANNOT_PERFORM_ACTION_NOT_IN_TOWN;
        }
    }

    /**
     * Returns the {@link ClaimedChunk.PlayerAccess} a player has within this claimed chunk
     *
     * @param player The {@link Player} to check
     * @return The {@link ClaimedChunk.PlayerAccess} the player has in this chunk
     */
    public static ClaimedChunk.PlayerAccess getPlayerAccess(Player player, ActionType actionType, ClaimedChunk chunk) {
        return getPlayerAccess(player.getUniqueId(), actionType, chunk);
    }

}
