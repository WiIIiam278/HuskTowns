package net.william278.husktowns.claim;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public record TownClaim(@NotNull Town town, @NotNull Claim claim) {
    public static Optional<TownClaim> from(@NotNull Map.Entry<Integer, Claim> entry, @NotNull HuskTowns plugin) {
        return plugin.findTown(entry.getKey()).map(town -> new TownClaim(town, entry.getValue()));
    }

    public boolean isAdminClaim(@NotNull HuskTowns plugin) {
        return town.getName().equalsIgnoreCase(plugin.getSettings().getAdminTownName());
    }

    public boolean contains(@NotNull Position position) {
        return claim.contains(position);
    }
}
