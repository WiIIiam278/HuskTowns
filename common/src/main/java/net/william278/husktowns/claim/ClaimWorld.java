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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClaimWorld {

    private int id;
    @Expose
    private ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Claim>> claims;

    @Expose
    @SerializedName("admin_claims")
    private ConcurrentLinkedQueue<Claim> adminClaims;

    private ClaimWorld(int id, @NotNull Map<Integer, List<Claim>> claims, @NotNull List<Claim> adminClaims) {
        this.id = id;
        this.adminClaims = new ConcurrentLinkedQueue<>(adminClaims);
        this.claims = new ConcurrentHashMap<>();
        claims.forEach((key, value) -> this.claims.put(key, new ConcurrentLinkedQueue<>(value)));
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
     * Returns the number of claims in this world, including admin claims
     *
     * @return the number of claims in this world
     */
    public int getClaimCount() {
        return claims.values().stream().mapToInt(ConcurrentLinkedQueue::size).sum() + getAdminClaimCount();
    }

    /**
     * Returns the number of admin claims in this world
     *
     * @return the number of admin claims in this world
     */
    public int getAdminClaimCount() {
        return adminClaims.size();
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
            claims.put(townClaim.town().getId(), new ConcurrentLinkedQueue<>());
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

    @NotNull
    public List<TownClaim> getClaimsNear(@NotNull Chunk chunk, int radius, @NotNull HuskTowns plugin) {
        if (radius <= 0) {
            return getClaimAt(chunk, plugin).map(List::of).orElse(List.of());
        }
        final List<TownClaim> townClaims = new ArrayList<>();
        for (int x = chunk.getX() - radius; x <= chunk.getX() + radius; x++) {
            for (int z = chunk.getZ() - radius; z <= chunk.getZ() + radius; z++) {
                getClaimAt(Chunk.at(x, z), plugin).ifPresent(townClaims::add);
            }
        }
        townClaims.sort((chunk1, chunk2) -> chunk1.claim().getChunk().distanceBetween(chunk2.claim().getChunk()));
        return townClaims;
    }

    @NotNull
    public List<TownClaim> getAdjacentClaims(@NotNull Chunk chunk, @NotNull HuskTowns plugin) {
        return getClaimsNear(chunk, 1, plugin);
    }

    public void removeAdminClaim(@NotNull Chunk chunk) {
        adminClaims.removeIf(claim -> claim.getChunk().equals(chunk));
    }

    @NotNull
    public ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Claim>> getClaims() {
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
