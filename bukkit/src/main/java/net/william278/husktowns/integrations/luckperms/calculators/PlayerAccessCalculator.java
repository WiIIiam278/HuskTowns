package net.william278.husktowns.integrations.luckperms.calculators;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.cache.ClaimCache;
import net.william278.husktowns.cache.PlayerCache;
import net.william278.husktowns.chunk.ClaimedChunk;
import net.william278.husktowns.integrations.luckperms.LuckPermsIntegration;
import net.william278.husktowns.listener.ActionType;
import net.william278.husktowns.util.AccessManager;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PlayerAccessCalculator implements ContextCalculator<Player> {

    private static final String CAN_PLAYER_BUILD = "husktowns:can-build";
    private static final String CAN_PLAYER_OPEN_CONTAINERS = "husktowns:can-open-containers";
    private static final String CAN_PLAYER_INTERACT = "husktowns:can-interact";
    private static final String STANDING_IN_OWN_TOWN = "husktowns:standing-in-own-town";

    @Override
    public void calculate(@NonNull Player target, @NonNull ContextConsumer consumer) {
        ClaimCache claimCache = HuskTowns.getClaimCache();
        PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (claimCache.hasLoaded() && playerCache.hasLoaded()) {
            Location location = target.getLocation();
            ClaimedChunk chunk = claimCache.getChunkAt(LuckPermsIntegration.toChunkCoordinate(location.getX()),
                    LuckPermsIntegration.toChunkCoordinate(location.getZ()), target.getWorld().getName());
            if (chunk != null) {
                ClaimedChunk.PlayerAccess buildAccess = AccessManager.getPlayerAccess(target.getUniqueId(), ActionType.PLACE_BLOCK, chunk, false);
                ClaimedChunk.PlayerAccess containerAccess = AccessManager.getPlayerAccess(target.getUniqueId(), ActionType.OPEN_CONTAINER, chunk, false);
                ClaimedChunk.PlayerAccess interactAccess = AccessManager.getPlayerAccess(target.getUniqueId(), ActionType.INTERACT_BLOCKS, chunk, false);
                switch (buildAccess) {
                    case CANNOT_PERFORM_ACTION_ADMIN_CLAIM, CANNOT_PERFORM_ACTION_DIFFERENT_TOWN, CANNOT_PERFORM_ACTION_NOT_IN_TOWN, CANNOT_PERFORM_ACTION_NO_TRUSTED_ACCESS -> consumer.accept(CAN_PLAYER_BUILD, "false");
                    default -> consumer.accept(CAN_PLAYER_BUILD, "true");
                }
                switch (containerAccess) {
                    case CANNOT_PERFORM_ACTION_ADMIN_CLAIM, CANNOT_PERFORM_ACTION_DIFFERENT_TOWN, CANNOT_PERFORM_ACTION_NOT_IN_TOWN, CANNOT_PERFORM_ACTION_NO_TRUSTED_ACCESS -> consumer.accept(CAN_PLAYER_OPEN_CONTAINERS, "false");
                    default -> consumer.accept(CAN_PLAYER_OPEN_CONTAINERS, "true");
                }
                switch (interactAccess) {
                    case CANNOT_PERFORM_ACTION_ADMIN_CLAIM, CANNOT_PERFORM_ACTION_DIFFERENT_TOWN, CANNOT_PERFORM_ACTION_NOT_IN_TOWN, CANNOT_PERFORM_ACTION_NO_TRUSTED_ACCESS -> consumer.accept(CAN_PLAYER_INTERACT, "false");
                    default -> consumer.accept(CAN_PLAYER_INTERACT, "true");
                }
                if (playerCache.isPlayerInTown(target.getUniqueId())) {
                    if (playerCache.getPlayerTown(target.getUniqueId()).equals(chunk.getTown())) {
                        consumer.accept(STANDING_IN_OWN_TOWN, "true");
                    } else {
                        consumer.accept(STANDING_IN_OWN_TOWN, "false");
                    }
                } else {
                    consumer.accept(STANDING_IN_OWN_TOWN, "false");
                }
            } else {
                consumer.accept(CAN_PLAYER_BUILD, "true");
                consumer.accept(CAN_PLAYER_OPEN_CONTAINERS, "true");
                consumer.accept(CAN_PLAYER_INTERACT, "true");
                consumer.accept(STANDING_IN_OWN_TOWN, "false");
            }
        } else {
            consumer.accept(CAN_PLAYER_BUILD, "false");
            consumer.accept(CAN_PLAYER_OPEN_CONTAINERS, "false");
            consumer.accept(CAN_PLAYER_INTERACT, "false");
        }
    }

    @Override
    public ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
        ClaimCache claimCache = HuskTowns.getClaimCache();
        PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (claimCache.hasLoaded() && playerCache.hasLoaded()) {
            builder.add(CAN_PLAYER_BUILD, "true");
            builder.add(CAN_PLAYER_OPEN_CONTAINERS, "true");
            builder.add(CAN_PLAYER_INTERACT, "true");
        }
        builder.add(CAN_PLAYER_BUILD, "false");
        builder.add(CAN_PLAYER_OPEN_CONTAINERS, "false");
        builder.add(CAN_PLAYER_INTERACT, "false");
        return builder.build();
    }
}
