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

package net.william278.husktowns.war;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.town.Spawn;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class War {

    @Expose
    @SerializedName("server_name")
    private String serverName;
    @Expose
    @SerializedName("attacking_town")
    private int attackingTown;
    @Expose
    @SerializedName("defending_town")
    private int defendingTown;
    @Expose
    private BigDecimal wager;
    @Expose
    @SerializedName("start_time")
    private OffsetDateTime startTime;
    @Expose
    @SerializedName("defender_spawn")
    private Position defenderSpawn;
    @Expose
    @SerializedName("attacker_spawn")
    private Position attackerSpawn;
    @Expose
    @SerializedName("war_zone_radius")
    private long warZoneRadius;
    @Expose
    @SerializedName("alive_attackers")
    private List<UUID> aliveAttackers;
    @Expose
    @SerializedName("alive_defenders")
    private List<UUID> aliveDefenders;

    @SuppressWarnings("unused")
    private War() {
    }

    private War(@NotNull HuskTowns plugin, @NotNull Town attacker, @NotNull Town defender,
                @NotNull BigDecimal wager, long warZoneRadius) {
        this.serverName = plugin.getServerName();
        this.attackingTown = attacker.getId();
        this.defendingTown = defender.getId();
        this.wager = wager;
        this.startTime = OffsetDateTime.now();
        this.warZoneRadius = warZoneRadius;
        this.defenderSpawn = attacker.getSpawn().map(Spawn::getPosition).orElseThrow(
                () -> new IllegalStateException("Defending town does not have a spawn set")
        );
        this.attackerSpawn = this.findSafeAttackerSpawn(defenderSpawn);
        this.aliveAttackers = this.getOnlineMembersOf(plugin, attacker);
        this.aliveDefenders = this.getOnlineMembersOf(plugin, defender);
    }

    @NotNull
    public static War create(@NotNull HuskTowns plugin, @NotNull Town attacker, @NotNull Town defender,
                             @NotNull BigDecimal wager, long warZoneRadius) {
        return new War(plugin, attacker, defender, wager, warZoneRadius);
    }

    //todo - Implement
    @NotNull
    private Position findSafeAttackerSpawn(@NotNull Position defenderSpawn) {
        return Position.at(
                defenderSpawn.getX() + (warZoneRadius / 2d),
                defenderSpawn.getY(),
                defenderSpawn.getZ(),
                defenderSpawn.getWorld()
        );
    }

    //todo - Implement
    @NotNull
    private List<UUID> getOnlineMembersOf(@NotNull HuskTowns plugin, @NotNull Town town) {
        return List.of();
    }

    @NotNull
    public String getHostServer() {
        return serverName;
    }

    public void removePlayer(@NotNull UUID player) {
        this.aliveAttackers.remove(player);
        this.aliveDefenders.remove(player);
    }

    public boolean isPlayerActive(@NotNull UUID player) {
        return this.aliveAttackers.contains(player) || this.aliveDefenders.contains(player);
    }

    public int getAttacking() {
        return attackingTown;
    }

    @NotNull
    public Town getAttacking(@NotNull HuskTowns plugin) {
        return plugin.findTown(getAttacking()).orElseThrow(
                () -> new IllegalStateException("Attacking town does not exist")
        );
    }

    public int getDefending() {
        return defendingTown;
    }

    @NotNull
    public Town getDefending(@NotNull HuskTowns plugin) {
        return plugin.findTown(getDefending()).orElseThrow(
                () -> new IllegalStateException("Defending town does not exist")
        );
    }

    @NotNull
    public BigDecimal getWager() {
        return wager;
    }

    @NotNull
    public OffsetDateTime getStartTime() {
        return startTime;
    }

    @NotNull
    public Position getDefenderSpawn() {
        return defenderSpawn;
    }

    @NotNull
    public Position getAttackerSpawn() {
        return attackerSpawn;
    }

    public long getWarZoneRadius() {
        return warZoneRadius;
    }

    @NotNull
    public List<UUID> getAliveAttackers() {
        return aliveAttackers;
    }

    @NotNull
    public List<UUID> getAliveDefenders() {
        return aliveDefenders;
    }
}
