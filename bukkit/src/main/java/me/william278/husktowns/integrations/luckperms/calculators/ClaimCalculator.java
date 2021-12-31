package me.william278.husktowns.integrations.luckperms.calculators;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.cache.ClaimCache;
import me.william278.husktowns.chunk.ClaimedChunk;
import me.william278.husktowns.integrations.luckperms.LuckPermsIntegration;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;

public class ClaimCalculator implements ContextCalculator<Player> {

    private static final String CLAIM_TOWN_KEY = "husktowns:claim-town";
    private static final String IN_CLAIM_KEY = "husktowns:in-claim";

    @Override
    public void calculate(@NonNull Player target, @NonNull ContextConsumer consumer) {
        ClaimCache claimCache = HuskTowns.getClaimCache();
        if (claimCache.hasLoaded()) {
            final Location location = target.getLocation();
            ClaimedChunk chunk = claimCache.getChunkAt(LuckPermsIntegration.toChunkCoordinate(location.getX()),
                    LuckPermsIntegration.toChunkCoordinate(location.getZ()), target.getWorld().getName());
            if (chunk != null) {
                consumer.accept(IN_CLAIM_KEY, "true");
                consumer.accept(CLAIM_TOWN_KEY, chunk.getTown());
            } else {
                consumer.accept(IN_CLAIM_KEY, "false");
            }
        } else {
            consumer.accept(IN_CLAIM_KEY, "false");
        }
    }

    @Override
    public ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
        ClaimCache claimCache = HuskTowns.getClaimCache();
        if (claimCache.hasLoaded()) {
            HashSet<String> possibleTowns = new HashSet<>();
            for (ClaimedChunk chunk : claimCache.getAllChunks()) {
                possibleTowns.add(chunk.getTown());
            }
            for (String town : possibleTowns) {
                builder.add(CLAIM_TOWN_KEY, town);
            }
            builder.add(IN_CLAIM_KEY, "true");
        }
        builder.add(IN_CLAIM_KEY, "false");
        return builder.build();
    }
}
