package net.william278.husktowns.claim;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record TownClaim(@NotNull Town town, @NotNull Claim claim) {

    public static Optional<TownClaim> from(@NotNull Map.Entry<UUID, Claim> entry, @NotNull HuskTowns plugin) {
        return plugin.findTown(entry.getKey()).map(town -> new TownClaim(town, entry.getValue()));
    }
}
