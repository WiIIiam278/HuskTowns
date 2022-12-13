package net.william278.husktowns.claim;

import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ClaimWorld {

    private int id;
    private Map<UUID, Claim> claims;

    private ClaimWorld(int id, @NotNull Map<UUID, Claim> claims) {
        this.id = id;
        this.claims = claims;
    }

    @NotNull
    public static ClaimWorld of(int id, @NotNull Map<UUID, Claim> claims) {
        return new ClaimWorld(id, claims);
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

    public int getId() {
        return id;
    }


}
