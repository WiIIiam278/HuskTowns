package net.william278.husktowns.claim;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public record TownClaim(@NotNull Town town, @NotNull Claim claim) {

    private static final char CLAIM_CHAR = '█';

    public static Optional<TownClaim> from(@NotNull Map.Entry<Integer, Claim> entry, @NotNull HuskTowns plugin) {
        return plugin.findTown(entry.getKey()).map(town -> new TownClaim(town, entry.getValue()));
    }

    public Component getChunkIcon(@NotNull HuskTowns plugin) {
        return Component.text(CLAIM_CHAR).color(TextColor.fromHexString(town.getColorRgb()));
    }
}
