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
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.town.Spawn;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a war between two {@link Town.Relation#ENEMY enemy} {@link Town Towns}; an attacker and defender
 */
public class War {

    // Maximum length of a war before it times out
    private static final long WAR_TIMEOUT_HOURS = 3;

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

    public void checkVictoryCondition(@NotNull HuskTowns plugin) {
        determineEndState(plugin).ifPresent(end -> {
            switch (end) {
                // todo locales x3
                case ATTACKER_WIN -> {
                    plugin.getLocales().getLocale("war_attacker_win", getAttacking(plugin).getName())
                            .ifPresent(message -> this.sendWarAnnouncement(plugin, message.toComponent()));
                    // todo something cool
                }
                case DEFENDER_WIN -> {
                    plugin.getLocales().getLocale("war_defender_win", getDefending(plugin).getName())
                            .ifPresent(message -> this.sendWarAnnouncement(plugin, message.toComponent()));
                    // todo something cool
                }
                case TIME_OUT -> plugin.getLocales().getLocale("war_time_out")
                        .ifPresent(message -> this.sendWarAnnouncement(plugin, message.toComponent()));
            }
            this.end(plugin, end);
        });
    }

    private Optional<EndState> determineEndState(@NotNull HuskTowns plugin) {
        // Calculate end-state flags
        boolean defendersDead = getAttackingPlayers(plugin).isEmpty();
        boolean attackersDead = getDefendingPlayers(plugin).isEmpty();
        boolean hasTimedOut = startTime.plus(Duration.of(
                WAR_TIMEOUT_HOURS, ChronoUnit.HOURS)
        ).isBefore(OffsetDateTime.now());

        // Determine state conditions
        if ((defendersDead && attackersDead) || hasTimedOut) {
            return Optional.of(EndState.TIME_OUT);
        } else if (attackersDead) {
            return Optional.of(EndState.DEFENDER_WIN);
        } else if (defendersDead) {
            return Optional.of(EndState.ATTACKER_WIN);
        }

        return Optional.empty();
    }

    public void end(@NotNull HuskTowns plugin, @NotNull EndState state) {
        plugin.getOnlineUsers().stream().findAny().ifPresentOrElse(
                delegate -> {
                    plugin.getManager().editTown(delegate, getAttacking(plugin),
                            (attacking -> endForTown(attacking, false, state)));
                    plugin.getManager().editTown(delegate, getDefending(plugin),
                            (defending -> endForTown(defending, true, state)));
                },
                () -> {
                    plugin.getDatabase().getTown(getAttacking())
                            .ifPresent(attacking -> {
                                endForTown(attacking, false, state);
                                plugin.updateTown(attacking);
                                plugin.getDatabase().updateTown(attacking);
                            });
                    plugin.getDatabase().getTown(getDefending())
                            .ifPresent(defending -> {
                                endForTown(defending, true, state);
                                plugin.updateTown(defending);
                                plugin.getDatabase().updateTown(defending);
                            });
                }
        );
        plugin.getManager().wars()
                .orElseThrow(() -> new IllegalStateException("War manager not present to allow war deactivation!"))
                .deactivateWar(this);
    }

    private void endForTown(@NotNull Town town, boolean isDefending, @NotNull EndState state) {
        final BigDecimal halfWager = wager.divide(BigDecimal.valueOf(2), RoundingMode.FLOOR);
        final EndState idealState = isDefending ? EndState.DEFENDER_WIN : EndState.ATTACKER_WIN;
        town.getLog().log(Action.of(
                state == idealState ? Action.Type.WON_WAR : Action.Type.LOST_WAR
        ));
        town.setMoney(town.getMoney().add(
                state == idealState ? wager.multiply(BigDecimal.valueOf(2))
                        : (state == EndState.TIME_OUT ? halfWager : wager.negate()))
        );
        town.clearCurrentWar();
    }

    public void declarePlayerDead(@NotNull HuskTowns plugin, @NotNull OnlineUser player) {
        if (this.aliveAttackers.remove(player.getUuid())) {
            plugin.getLocales().getLocale("war_attacker_died",
                            player.getUsername(), getDefending(plugin).getName())
                    .ifPresent(message -> this.sendWarAnnouncement(plugin, message.toComponent()));
        } else if (this.aliveDefenders.remove(player.getUuid())) {
            plugin.getLocales().getLocale("war_defender_died",
                            player.getUsername(), getDefending(plugin).getName())
                    .ifPresent(message -> this.sendWarAnnouncement(plugin, message.toComponent()));
        }
        this.checkVictoryCondition(plugin);
    }

    public boolean isPlayerActive(@NotNull UUID player) {
        return this.aliveAttackers.contains(player) || this.aliveDefenders.contains(player);
    }

    public void sendWarAnnouncement(@NotNull HuskTowns plugin, @NotNull Component component) {
        this.getWarAudience(plugin).sendMessage(component);
    }

    @NotNull
    private Audience getWarAudience(@NotNull HuskTowns plugin) {
        return Audience.audience(getWarPlayers(plugin).stream()
                .map(OnlineUser::getAudience).toArray(Audience[]::new));
    }

    @NotNull
    private List<OnlineUser> getWarPlayers(@NotNull HuskTowns plugin) {
        return new ArrayList<>(
                Stream.of(getAttackingPlayers(plugin), getDefendingPlayers(plugin)).flatMap(List::stream).toList()
        );
    }

    @NotNull
    private List<OnlineUser> getAttackingPlayers(@NotNull HuskTowns plugin) {
        return plugin.getOnlineUsers().stream()
                .filter(user -> this.aliveAttackers.contains(user.getUuid()))
                .collect(Collectors.toUnmodifiableList());
    }

    @NotNull
    private List<OnlineUser> getDefendingPlayers(@NotNull HuskTowns plugin) {
        return plugin.getOnlineUsers().stream()
                .filter(user -> this.aliveDefenders.contains(user.getUuid()))
                .collect(Collectors.toUnmodifiableList());
    }

    @NotNull
    public Town getAttacking(@NotNull HuskTowns plugin) {
        return plugin.findTown(getAttacking()).orElseThrow(
                () -> new IllegalStateException("Attacking town does not exist")
        );
    }

    @NotNull
    public Town getDefending(@NotNull HuskTowns plugin) {
        return plugin.findTown(getDefending()).orElseThrow(
                () -> new IllegalStateException("Defending town does not exist")
        );
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

    public int getAttacking() {
        return attackingTown;
    }

    public int getDefending() {
        return defendingTown;
    }

    @NotNull
    public BigDecimal getWager() {
        return wager;
    }

    @NotNull
    public String getHostServer() {
        return serverName;
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

    public enum EndState {
        ATTACKER_WIN,
        DEFENDER_WIN,
        TIME_OUT
    }

}
