/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
 */

package net.william278.husktowns.claim;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClaimWorld {

    private int id;
    @Expose
    private ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Claim>> claims;
    private final transient Map<Chunk, ChunkClaim> claimsByChunk = new ConcurrentHashMap<>();

    @Expose
    @SerializedName("admin_claims")
    private ConcurrentLinkedQueue<Claim> adminClaims;
    private final transient Map<Chunk, ChunkClaim> adminClaimsByChunk = new ConcurrentHashMap<>();

    private boolean initialized = false;

    private ClaimWorld(int id, @NotNull Map<Integer, List<Claim>> claims, @NotNull List<Claim> adminClaims, @NotNull HuskTowns plugin) {
        this.id = id;
        this.adminClaims = new ConcurrentLinkedQueue<>(adminClaims);
        this.claims = new ConcurrentHashMap<>();
        claims.forEach((key, value) -> this.claims.put(key, new ConcurrentLinkedQueue<>(value)));
    }

    @NotNull
    public static ClaimWorld of(int id, @NotNull Map<Integer, List<Claim>> claims, @NotNull List<Claim> adminClaims, @NotNull HuskTowns plugin) {
        return new ClaimWorld(id, claims, adminClaims, plugin);
    }

    @SuppressWarnings("unused")
    private ClaimWorld() {
    }

    public Optional<TownClaim> getClaimAt(@NotNull Chunk chunk, @NotNull HuskTowns plugin) {
        this.checkInitialization(plugin);
        final ChunkClaim townClaim = this.claimsByChunk.get(chunk);
        final ChunkClaim adminClaim = this.adminClaimsByChunk.get(chunk);
        return Optional.ofNullable(townClaim)
                .flatMap(c -> plugin.findTown(c.getTownId()))
                .map(town -> new TownClaim(town, townClaim.getChunk()))
                .or(() -> Optional.ofNullable(adminClaim)
                        .map(claim -> new TownClaim(plugin.getAdminTown(), claim.getChunk())));
//        return claims.entrySet().stream()
//                .filter(entry -> entry.getValue().stream().anyMatch(claim -> claim.getChunk().equals(chunk)))
//                .findFirst()
//                .flatMap(entry -> entry.getValue().stream()
//                        .filter(claim -> claim.getChunk().equals(chunk))
//                        .findFirst()
//                        .flatMap(claim -> plugin.findTown(entry.getKey())
//                                .map(town1 -> new TownClaim(town1, claim))))
//                .or(() -> adminClaims.stream()
//                        .filter(claim -> claim.getChunk().equals(chunk))
//                        .findFirst()
//                        .map(claim -> new TownClaim(plugin.getAdminTown(), claim)));
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
            final Collection<Claim> removed = claims.remove(townId);
            if (removed != null) {
                removed.forEach(claim -> claimsByChunk.remove(claim.getChunk()));
            }
            return claimCount;
        }
        return 0;
    }

    public void addClaim(@NotNull TownClaim townClaim) {
        if (!claims.containsKey(townClaim.town().getId())) {
            claims.put(townClaim.town().getId(), new ConcurrentLinkedQueue<>());
        }
        claims.get(townClaim.town().getId()).add(townClaim.claim());
        this.claimsByChunk.put(townClaim.claim().getChunk(), new ChunkClaim(townClaim.town().getId(), townClaim.claim()));
    }

    public void addAdminClaim(@NotNull Claim claim, @NotNull HuskTowns plugin) {
        this.checkInitialization(plugin);
        adminClaims.add(claim);
        this.adminClaimsByChunk.put(claim.getChunk(), new ChunkClaim(plugin.getAdminTown().getId(), claim));
    }

    public void removeClaim(@NotNull Town town, @NotNull Chunk chunk) {
        if (claims.containsKey(town.getId())) {
            claims.get(town.getId()).removeIf(claim -> claim.getChunk().equals(chunk));
            this.claimsByChunk.remove(chunk);
        }
    }

    @NotNull
    public List<TownClaim> getClaimsNear(@NotNull Chunk chunk, int radius, @NotNull HuskTowns plugin) {
        this.checkInitialization(plugin);
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
        this.checkInitialization(plugin);
        return getClaimsNear(chunk, 1, plugin);
    }

    public void removeAdminClaim(@NotNull Chunk chunk) {
        adminClaims.removeIf(claim -> claim.getChunk().equals(chunk));
        this.adminClaimsByChunk.remove(chunk);
    }

    @NotNull
    public ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Claim>> getClaims() {
        return claims;
    }

    @NotNull
    public List<TownClaim> getClaims(@NotNull HuskTowns plugin) {
        this.checkInitialization(plugin);
        List<TownClaim> townClaims = new ArrayList<>();
        claims.forEach((townId, claimList) -> {
            Optional<Town> town = plugin.findTown(townId);
            town.ifPresent(value -> claimList.forEach(claim -> townClaims.add(new TownClaim(value, claim))));
        });
        adminClaims.forEach(claim -> townClaims.add(new TownClaim(plugin.getAdminTown(), claim)));
        return townClaims;
    }

    private void checkInitialization(@NotNull HuskTowns plugin) {
        if (!this.initialized) {
            this.claims.forEach((townId, value) -> value.forEach(claim -> this.claimsByChunk.put(claim.getChunk(), new ChunkClaim(townId, claim))));
            this.adminClaims.forEach(claim -> this.adminClaimsByChunk.put(claim.getChunk(), new ChunkClaim(plugin.getAdminTown().getId(), claim)));
            this.initialized = true;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final ClaimWorld claimWorld = (ClaimWorld) obj;
        return id == claimWorld.id;
    }

    private static class ChunkClaim {

        private final int townId;
        private final Claim claim;

        public ChunkClaim(int townId, Claim chunk) {
            this.townId = townId;
            this.claim = chunk;
        }

        public int getTownId() {
            return townId;
        }

        public Claim getChunk() {
            return claim;
        }

    }

}
