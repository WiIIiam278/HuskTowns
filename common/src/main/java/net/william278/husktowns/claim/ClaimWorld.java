package net.william278.husktowns.claim;

import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ClaimWorld {
    private Map<UUID, Claim> claims;

    private ClaimWorld(@NotNull Map<UUID, Claim> claims) {
        this.claims = claims;
    }

    @NotNull
    public static ClaimWorld of(@NotNull Map<UUID, Claim> claims) {
        return new ClaimWorld(claims);
    }

    @SuppressWarnings("unused")
    private ClaimWorld() {
    }

    private Optional<TownClaim> getClaimAt(@NotNull Position position, @NotNull HuskTowns plugin) {
        return claims.entrySet().stream()
                .filter(entry -> entry.getValue().getPosition().equals(position))
                .findFirst()
                .flatMap(entry -> TownClaim.from(entry, plugin));
    }


}
