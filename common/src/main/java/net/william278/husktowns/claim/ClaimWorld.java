package net.william278.husktowns.claim;

import com.google.gson.annotations.Expose;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClaimWorld {

    @Expose
    private int id;
    @Expose
    private Map<Integer, List<Claim>> claims;

    private ClaimWorld(int id, @NotNull Map<Integer, List<Claim>> claims) {
        this.id = id;
        this.claims = claims;
    }

    @NotNull
    public static ClaimWorld of(int id, @NotNull Map<Integer, List<Claim>> claims) {
        return new ClaimWorld(id, claims);
    }

    @SuppressWarnings("unused")
    private ClaimWorld() {
    }

    public Optional<TownClaim> getClaimAt(@NotNull Chunk chunk, @NotNull HuskTowns plugin) {
        return claims.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(claim -> claim.getPosition().equals(chunk)))
                .findFirst()
                .flatMap(entry -> entry.getValue().stream()
                        .filter(claim -> claim.getPosition().equals(chunk))
                        .findFirst()
                        .flatMap(claim -> plugin.findTown(entry.getKey())
                                .map(town1 -> new TownClaim(town1, claim))));
    }

    /**
     * Get the ID of the claim world
     *
     * @return the ID of the claim world
     */
    public int getId() {
        return id;
    }

    /**
     * Update the ID of this claim world
     *
     * @param id the new ID of this claim world
     */
    public void updateId(int id) {
        this.id = id;
    }

    /**
     * Returns the number of claims in this world
     *
     * @return the number of claims in this world
     */
    public int getClaimCount() {
        return claims.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Remove claims by a town on this world
     *
     * @param townId the ID of the town to remove claims for
     * @return the number of claims removed
     */
    public int removeTownClaims(int townId) {
        if (claims.containsKey(townId)) {
            int claimCount = claims.get(townId).size();
            claims.remove(townId);
            return claimCount;
        }
        return 0;
    }

    public void addClaim(@NotNull Town town, @NotNull Claim claim) {
        if (claims.containsKey(town.getId())) {
            claims.get(town.getId()).add(claim);
        } else {
            claims.put(town.getId(), List.of(claim));
        }
    }
}
