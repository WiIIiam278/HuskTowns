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
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.network.Message;
import net.william278.husktowns.network.Payload;
import net.william278.husktowns.town.Spawn;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
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

    @Expose(deserialize = false, serialize = false)
    private BossBar attackersBossBar;
    @Expose(deserialize = false, serialize = false)
    private BossBar defendersBossBar;

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
        this.defenderSpawn = defender.getSpawn().map(Spawn::getPosition).orElseThrow(
            () -> new IllegalStateException("Defending town does not have a spawn set")
        );
        this.attackerSpawn = this.findSafeAttackerSpawn(plugin);
        this.aliveAttackers = this.getOnlineMembersOf(plugin, attacker);
        this.aliveDefenders = this.getOnlineMembersOf(plugin, defender);
        this.updateBossBars(plugin);
    }

    @NotNull
    public static War create(@NotNull HuskTowns plugin, @NotNull Town attacker, @NotNull Town defender,
                             @NotNull BigDecimal wager, long warZoneRadius) {
        return new War(plugin, attacker, defender, wager, warZoneRadius);
    }

    public void teleportUsers(@NotNull HuskTowns plugin) {
        final Town attacking = getAttacking(plugin);
        final Town defending = getDefending(plugin);
        plugin.runAsync(() -> plugin.getOnlineUsers().forEach(user -> {
            if (attacking.getMembers().containsKey(user.getUuid())) {
                plugin.teleportUser(
                    user,
                    getAttackerSpawn(),
                    getHostServer(),
                    true
                );
            } else if (defending.getMembers().containsKey(user.getUuid())) {
                plugin.teleportUser(
                    user,
                    getDefenderSpawn(),
                    getHostServer(),
                    true
                );
            }
        }));
    }

    public void checkVictoryCondition(@NotNull HuskTowns plugin) {
        determineEndState(plugin).ifPresent(end -> {
            final Town attackers = getAttacking(plugin);
            final Town defenders = getDefending(plugin);
            switch (end) {
                case ATTACKER_WIN -> plugin.getLocales().getLocale("war_over_winner",
                        attackers.getName(), defenders.getName())
                    .ifPresent(message -> this.sendWarAnnouncement(plugin, message.toComponent(), false));
                case DEFENDER_WIN -> plugin.getLocales().getLocale("war_over_winner",
                        defenders.getName(), attackers.getName())
                    .ifPresent(message -> this.sendWarAnnouncement(plugin, message.toComponent(), false));
                case TIME_OUT -> plugin.getLocales().getLocale("war_over_stalemate",
                        attackers.getName(), defenders.getName())
                    .ifPresent(message -> this.sendWarAnnouncement(plugin, message.toComponent(), false));
            }
            this.end(plugin, end);
        });
    }

    private Optional<EndState> determineEndState(@NotNull HuskTowns plugin) {
        // Calculate end-state flags
        boolean defendersDead = getOnlineDefenders(plugin, true).isEmpty();
        boolean attackersDead = getOnlineAttackers(plugin, true).isEmpty();
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
                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                    .type(Message.Type.TOWN_WAR_END)
                    .target(getHostServer(), Message.TargetType.SERVER)
                    .payload(Payload.integer(
                        state == EndState.DEFENDER_WIN ? getDefending() : getAttacking()
                    )).build()
                    .send(broker, delegate));
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
        if (!plugin.getSettings().getCrossServer().isEnabled() || getHostServer().equals(plugin.getServerName())) {
            plugin.getManager().wars().ifPresent(wars -> wars.removeActiveWar(this));
        }
        getAttackersAudience(plugin, false).hideBossBar(attackersBossBar);
        getDefendersAudience(plugin, false).hideBossBar(defendersBossBar);
    }

    private void endForTown(@NotNull Town town, boolean isDefending, @NotNull EndState state) {
        final EndState idealState = isDefending ? EndState.DEFENDER_WIN : EndState.ATTACKER_WIN;
        town.getLog().log(Action.of(
            state == idealState ? Action.Type.WON_WAR : Action.Type.LOST_WAR
        ));
        town.setMoney(town.getMoney().add(
            state == idealState
                ? wager.multiply(BigDecimal.valueOf(2))
                : (state == EndState.TIME_OUT ? wager : BigDecimal.ZERO)
        ));
        town.clearCurrentWar();
    }

    public void handlePlayerDieOrFlee(@NotNull HuskTowns plugin, @NotNull OnlineUser player, boolean fled) {
        if (this.aliveAttackers.remove(player.getUuid())) {
            final Town attacking = getAttacking(plugin);
            plugin.getLocales().getLocale(!fled ? "war_user_died" : "war_user_fled",
                    player.getUsername(), attacking.getName(), attacking.getColorRgb())
                .ifPresent(message -> this.sendWarAnnouncement(plugin, message.toComponent(), false));
        } else if (this.aliveDefenders.remove(player.getUuid())) {
            final Town defending = getDefending(plugin);
            plugin.getLocales().getLocale(!fled ? "war_user_died" : "war_user_fled",
                    player.getUsername(), defending.getName(), defending.getColorRgb())
                .ifPresent(message -> this.sendWarAnnouncement(plugin, message.toComponent(), false));
        }
        this.updateBossBars(plugin);
        this.checkVictoryCondition(plugin);
    }

    public boolean isPlayerActive(@NotNull UUID player) {
        return this.aliveAttackers.contains(player) || this.aliveDefenders.contains(player);
    }

    public void sendWarAnnouncement(@NotNull HuskTowns plugin, @NotNull Component component, boolean onlyAlive) {
        this.getWarAudience(plugin, onlyAlive).sendMessage(component);
    }

    private void updateBossBars(@NotNull HuskTowns plugin) {
        if (attackersBossBar == null) {
            attackersBossBar = generateBossBar(plugin, getDefending(plugin), aliveDefenders.size());
            getAttackersAudience(plugin, false).showBossBar(attackersBossBar);
        } else {
            attackersBossBar.name(getBossBarName(plugin, getDefending(plugin), aliveDefenders.size()));
        }
        if (defendersBossBar == null) {
            defendersBossBar = generateBossBar(plugin, getAttacking(plugin), aliveAttackers.size());
            getDefendersAudience(plugin, false).showBossBar(defendersBossBar);
        } else {
            defendersBossBar.name(getBossBarName(plugin, getAttacking(plugin), aliveAttackers.size()));
        }
    }

    @NotNull
    private BossBar generateBossBar(@NotNull HuskTowns plugin, @NotNull Town opponents, int aliveOpponents) {
        return BossBar.bossBar(
            getBossBarName(plugin, opponents, aliveOpponents),
            1.0f,
            plugin.getSettings().getTowns().getRelations().getWars().getBossBarColor(),
            BossBar.Overlay.PROGRESS
        );
    }

    @NotNull
    private Component getBossBarName(@NotNull HuskTowns plugin, @NotNull Town opponents, int aliveOpponents) {
        return plugin.getLocales().getLocale("war_boss_bar_title",
            opponents.getName(), Integer.toString(aliveOpponents),
            Integer.toString(opponents.getMembers().keySet().size())
        ).map(MineDown::toComponent).orElse(Component.empty());
    }

    @NotNull
    private Audience getWarAudience(@NotNull HuskTowns plugin, boolean onlyAlive) {
        return Audience.audience(getAttackersAudience(plugin, onlyAlive), getDefendersAudience(plugin, onlyAlive));
    }

    @NotNull
    private List<OnlineUser> getOnlineUsers(@NotNull HuskTowns plugin, boolean onlyAlive) {
        return new ArrayList<>(
            Stream.of(getOnlineAttackers(plugin, onlyAlive), getOnlineDefenders(plugin, onlyAlive))
                .flatMap(List::stream).toList()
        );
    }

    @NotNull
    private Audience getAttackersAudience(@NotNull HuskTowns plugin, boolean onlyAlive) {
        return Audience.audience(getOnlineAttackers(plugin, onlyAlive).stream()
            .map(OnlineUser::getAudience).toArray(Audience[]::new));
    }

    @NotNull
    private List<OnlineUser> getOnlineAttackers(@NotNull HuskTowns plugin, boolean onlyAlive) {
        return plugin.getOnlineUsers().stream()
            .filter(user -> (onlyAlive ? this.aliveAttackers : getAttacking(plugin).getMembers().keySet())
                .contains(user.getUuid()))
            .collect(Collectors.toUnmodifiableList());
    }

    @NotNull
    private Audience getDefendersAudience(@NotNull HuskTowns plugin, boolean onlyAlive) {
        return Audience.audience(getOnlineDefenders(plugin, onlyAlive).stream()
            .map(OnlineUser::getAudience).toArray(Audience[]::new));
    }

    @NotNull
    private List<OnlineUser> getOnlineDefenders(@NotNull HuskTowns plugin, boolean onlyAlive) {
        return plugin.getOnlineUsers().stream()
            .filter(user -> (onlyAlive ? this.aliveDefenders : getDefending(plugin).getMembers().keySet())
                .contains(user.getUuid()))
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

    // Calculate an x and z position that is at a random angle, distanceFromSpawn, away from defenderSpawn
    @NotNull
    private Position findSafeAttackerSpawn(@NotNull HuskTowns plugin) {
        int distanceFromSpawn = (int) (warZoneRadius / 2);
        double angle = Math.random() * 2 * Math.PI;
        double x = (defenderSpawn.getX() + distanceFromSpawn * Math.cos(angle)) + 0.5d;
        double z = (defenderSpawn.getZ() + distanceFromSpawn * Math.sin(angle)) + 0.5d;
        return Position.at(
            x,
            plugin.getHighestYAt(x, z, defenderSpawn.getWorld()) + 1.5d,
            z,
            defenderSpawn.getWorld()
        ).facing(defenderSpawn);
    }

    @NotNull
    private List<UUID> getOnlineMembersOf(@NotNull HuskTowns plugin, @NotNull Town town) {
        final Set<UUID> onlineUsers = plugin.getUserList().stream().map(User::getUuid).collect(Collectors.toSet());
        return new ArrayList<>(town.getMembers().keySet().stream().filter(onlineUsers::contains).toList());
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof War other) {
            return other.getAttacking() == this.getAttacking()
                && other.getDefending() == this.getDefending();
        }
        return false;
    }

    /**
     * Represents different ways a war can end
     */
    public enum EndState {
        /**
         * A war ended by the attackers winning
         */
        ATTACKER_WIN,
        /**
         * A war ended by the defenders winning
         */
        DEFENDER_WIN,
        /**
         * A war ended by it timing out
         **/
        TIME_OUT
    }

}
