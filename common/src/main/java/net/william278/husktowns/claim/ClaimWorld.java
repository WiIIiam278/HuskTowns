/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.claim;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@NoArgsConstructor
public class ClaimWorld {

    @Getter
    private int id;
    @Expose
    @SerializedName("claims")
    private ConcurrentMap<Integer, ConcurrentLinkedQueue<Claim>> claims = Maps.newConcurrentMap();
    @Expose
    @SerializedName("admin_claims")
    private ConcurrentLinkedQueue<Claim> adminClaims = Queues.newConcurrentLinkedQueue();

    @Expose(deserialize = false, serialize = false)
    private transient Map<Long, CachedClaim> cachedClaims = Maps.newConcurrentMap();

    private ClaimWorld(int id, @NotNull ConcurrentMap<Integer, ConcurrentLinkedQueue<Claim>> claims,
                       @NotNull ConcurrentLinkedQueue<Claim> adminClaims) {
        this.id = id;
        this.claims = claims;
        this.adminClaims = adminClaims;
        this.cacheClaims();
    }

    @NotNull
    public static ClaimWorld of(int id, @NotNull ConcurrentMap<Integer, ConcurrentLinkedQueue<Claim>> claims,
                                @NotNull ConcurrentLinkedQueue<Claim> adminClaims) {
        return new ClaimWorld(id, claims, adminClaims);
    }

    public void cacheClaims() {
        cachedClaims.clear();
        claims.forEach((key, value) -> value.forEach(claim -> this.cachedClaims.put(
                claim.getChunk().asLong(), new CachedClaim(key, claim)
        )));
        adminClaims.forEach(claim -> this.cachedClaims.put(
                claim.getChunk().asLong(), new CachedClaim(-1, claim)
        ));
    }

    private Optional<TownClaim> getClaimAt(long chunkLong, @NotNull HuskTowns plugin) {
        return Optional.ofNullable(cachedClaims.get(chunkLong)).map(cached -> cached.getTownClaim(plugin));
    }

    public Optional<TownClaim> getClaimAt(@NotNull Chunk chunk, @NotNull HuskTowns plugin) {
        return getClaimAt(chunk.asLong(), plugin);
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
        return cachedClaims.size();
    }

    /**
     * Returns the number of admin claims in this world
     *
     * @return the number of admin claims in this world
     */
    public int getAdminClaimCount() {
        return adminClaims.size();
    }

    @NotNull
    public List<TownClaim> getTownClaims(int townId, @NotNull HuskTowns plugin) {
        return cachedClaims.values().stream()
                .filter(cachedClaim -> cachedClaim.townId == townId)
                .map(cachedClaim -> cachedClaim.getTownClaim(plugin))
                .collect(Collectors.toList());
    }

    @NotNull
    @Unmodifiable
    public Map<Integer, List<Claim>> getClaims() {
        return claims.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ArrayList<>(entry.getValue())));
    }

    @NotNull
    public List<TownClaim> getClaims(@NotNull HuskTowns plugin) {
        return cachedClaims.values().stream()
                .map(cachedClaim -> cachedClaim.getTownClaim(plugin))
                .collect(Collectors.toList());
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
            cachedClaims.values().removeIf(cachedClaim -> cachedClaim.townId == townId);
            return claimCount;
        }
        return 0;
    }

    public void addClaim(@NotNull TownClaim townClaim) {
        if (!claims.containsKey(townClaim.town().getId())) {
            claims.put(townClaim.town().getId(), new ConcurrentLinkedQueue<>());
        }
        claims.get(townClaim.town().getId()).add(townClaim.claim());
        cachedClaims.put(townClaim.claim().getChunk().asLong(), new CachedClaim(townClaim.town().getId(), townClaim.claim()));
    }

    public void replaceClaim(@NotNull TownClaim townClaim, @NotNull HuskTowns plugin) {
        final Claim claim = townClaim.claim();
        if (townClaim.isAdminClaim(plugin)) {
            adminClaims.removeIf(c -> c.getChunk().equals(claim.getChunk()));
            adminClaims.add(claim);
            cachedClaims.put(claim.getChunk().asLong(), new CachedClaim(-1, claim));
        } else if (claims.containsKey(townClaim.town().getId())) {
            claims.get(townClaim.town().getId()).removeIf(c -> c.getChunk().equals(claim.getChunk()));
            claims.get(townClaim.town().getId()).add(claim);
            cachedClaims.put(claim.getChunk().asLong(), new CachedClaim(townClaim.town().getId(), claim));
        }
    }

    public void addAdminClaim(@NotNull Claim claim) {
        cachedClaims.put(claim.getChunk().asLong(), new CachedClaim(-1, claim));
        adminClaims.add(claim);
    }

    public void removeClaim(@NotNull Town town, @NotNull Chunk chunk) {
        if (claims.containsKey(town.getId())) {
            claims.get(town.getId()).removeIf(claim -> claim.getChunk().equals(chunk));
            cachedClaims.remove(chunk.asLong());
        }
    }

    public void removeAdminClaim(@NotNull Chunk chunk) {
        cachedClaims.remove(chunk.asLong());
        adminClaims.removeIf(claim -> claim.getChunk().equals(chunk));
    }

    @NotNull
    public List<TownClaim> getClaimsNear(@NotNull Chunk chunk, int radius, @NotNull HuskTowns plugin) {
        if (radius <= 0) {
            return getClaimAt(chunk, plugin).map(List::of).orElse(List.of());
        }
        final List<TownClaim> townClaims = new ArrayList<>();
        for (int x = chunk.getX() - radius; x <= chunk.getX() + radius; x++) {
            for (int z = chunk.getZ() - radius; z <= chunk.getZ() + radius; z++) {
                getClaimAt(Chunk.asLong(x, z), plugin).ifPresent(townClaims::add);
            }
        }
        townClaims.sort((chunk1, chunk2) -> chunk1.claim().getChunk().distanceBetween(chunk2.claim().getChunk()));
        return townClaims;
    }

    @NotNull
    public List<TownClaim> getAdjacentClaims(@NotNull Chunk chunk, @NotNull HuskTowns plugin) {
        return getClaimsNear(chunk, 1, plugin);
    }


    public boolean pruneOrphanClaims(@NotNull HuskTowns plugin) {
        return new HashMap<>(claims).keySet().stream()
                .filter(town -> plugin.findTown(town).isEmpty())
                .map(this::removeTownClaims)
                .anyMatch(count -> count > 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final ClaimWorld claimWorld = (ClaimWorld) obj;
        return id == claimWorld.id;
    }

    private record CachedClaim(int townId, @NotNull Claim claim) {
        @NotNull
        TownClaim getTownClaim(@NotNull HuskTowns plugin) {
            if (townId == -1) {
                return new TownClaim(plugin.getAdminTown(), claim);
            }
            return plugin.findTown(townId)
                    .map(town -> new TownClaim(town, claim))
                    .orElseThrow(() -> new IllegalStateException("Claim has invalid town ID: " + townId));
        }
    }

}
