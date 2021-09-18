package me.william278.husktowns.integrations.luckperms.calculators;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.cache.ClaimCache;
import me.william278.husktowns.cache.PlayerCache;
import me.william278.husktowns.chunk.ClaimedChunk;
import me.william278.husktowns.listener.ActionType;
import me.william278.husktowns.util.AccessManager;
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

    @Override
    public void calculate(@NonNull Player target, @NonNull ContextConsumer consumer) {
        ClaimCache claimCache = HuskTowns.getClaimCache();
        PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (claimCache.hasLoaded() && playerCache.hasLoaded()) {
            Location location = target.getLocation();
            ClaimedChunk chunk = claimCache.getChunkAt(location.getChunk().getX(),
                    location.getChunk().getZ(), target.getWorld().getName());
            if (chunk != null) {
                ClaimedChunk.PlayerAccess buildAccess = AccessManager.getPlayerAccess(target.getUniqueId(), ActionType.PLACE_BLOCK, chunk);
                ClaimedChunk.PlayerAccess containerAccess = AccessManager.getPlayerAccess(target.getUniqueId(), ActionType.OPEN_CONTAINER, chunk);
                ClaimedChunk.PlayerAccess interactAccess = AccessManager.getPlayerAccess(target.getUniqueId(), ActionType.INTERACT_BLOCKS, chunk);
                switch (buildAccess) {
                    case CANNOT_PERFORM_ACTION_ADMIN_CLAIM, CANNOT_PERFORM_ACTION_DIFFERENT_TOWN, CANNOT_PERFORM_ACTION_NOT_IN_TOWN, CANNOT_PERFORM_ACTION_RESIDENT -> consumer.accept(CAN_PLAYER_BUILD, "false");
                    default -> consumer.accept(CAN_PLAYER_BUILD, "true");
                }
                switch (containerAccess) {
                    case CANNOT_PERFORM_ACTION_ADMIN_CLAIM, CANNOT_PERFORM_ACTION_DIFFERENT_TOWN, CANNOT_PERFORM_ACTION_NOT_IN_TOWN, CANNOT_PERFORM_ACTION_RESIDENT -> consumer.accept(CAN_PLAYER_OPEN_CONTAINERS, "false");
                    default -> consumer.accept(CAN_PLAYER_OPEN_CONTAINERS, "true");
                }
                switch (interactAccess) {
                    case CANNOT_PERFORM_ACTION_ADMIN_CLAIM, CANNOT_PERFORM_ACTION_DIFFERENT_TOWN, CANNOT_PERFORM_ACTION_NOT_IN_TOWN, CANNOT_PERFORM_ACTION_RESIDENT -> consumer.accept(CAN_PLAYER_INTERACT, "false");
                    default -> consumer.accept(CAN_PLAYER_INTERACT, "true");
                }
            } else {
                consumer.accept(CAN_PLAYER_BUILD, "true");
                consumer.accept(CAN_PLAYER_OPEN_CONTAINERS, "true");
                consumer.accept(CAN_PLAYER_INTERACT, "true");
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
