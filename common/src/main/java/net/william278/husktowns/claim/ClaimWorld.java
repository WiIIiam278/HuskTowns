package net.william278.husktowns.claim;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClaimWorld {

    private int id;
    @Expose
    private Map<Integer, List<Claim>> claims;

    @Expose
    @SerializedName("admin_claims")
    private List<Claim> adminClaims;

    private ClaimWorld(int id, @NotNull Map<Integer, List<Claim>> claims, @NotNull List<Claim> adminClaims) {
        this.id = id;
        this.claims = claims;
        this.adminClaims = adminClaims;
    }

    @NotNull
    public static ClaimWorld of(int id, @NotNull Map<Integer, List<Claim>> claims, @NotNull List<Claim> adminClaims) {
        return new ClaimWorld(id, claims, adminClaims);
    }

    @SuppressWarnings("unused")
    private ClaimWorld() {
    }

    public Optional<TownClaim> getClaimAt(@NotNull Chunk chunk, @NotNull HuskTowns plugin) {
        return claims.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(claim -> claim.getChunk().equals(chunk)))
                .findFirst()
                .flatMap(entry -> entry.getValue().stream()
                        .filter(claim -> claim.getChunk().equals(chunk))
                        .findFirst()
                        .flatMap(claim -> plugin.findTown(entry.getKey())
                                .map(town1 -> new TownClaim(town1, claim))))
                .or(() -> adminClaims.stream()
                        .filter(claim -> claim.getChunk().equals(chunk))
                        .findFirst()
                        .map(claim -> new TownClaim(plugin.getAdminTown(), claim)));
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
        return claims.values().stream().mapToInt(List::size).sum() + adminClaims.size();
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

    public void addClaim(@NotNull TownClaim townClaim) {
        if (!claims.containsKey(townClaim.town().getId())) {
            claims.put(townClaim.town().getId(), new ArrayList<>());
        }
        claims.get(townClaim.town().getId()).add(townClaim.claim());
    }

    public void addAdminClaim(@NotNull Claim claim) {
        adminClaims.add(claim);
    }

    public void removeClaim(@NotNull Town town, @NotNull Chunk chunk) {
        if (claims.containsKey(town.getId())) {
            claims.get(town.getId()).removeIf(claim -> claim.getChunk().equals(chunk));
        }
    }

    public void removeAdminClaim(@NotNull Chunk chunk) {
        adminClaims.removeIf(claim -> claim.getChunk().equals(chunk));
    }

    @NotNull
    public Map<Integer, List<Claim>> getClaims() {
        return claims;
    }

    @NotNull
    public List<TownClaim> getClaims(@NotNull HuskTowns plugin) {
        List<TownClaim> townClaims = new ArrayList<>();
        claims.forEach((townId, claimList) -> {
            Optional<Town> town = plugin.findTown(townId);
            town.ifPresent(value -> claimList.forEach(claim -> townClaims.add(new TownClaim(value, claim))));
        });
        adminClaims.forEach(claim -> townClaims.add(new TownClaim(plugin.getAdminTown(), claim)));
        return townClaims;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final ClaimWorld claimWorld = (ClaimWorld) obj;
        return id == claimWorld.id;
    }

}
