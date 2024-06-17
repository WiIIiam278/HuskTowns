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

package net.william278.husktowns.manager;

import net.william278.desertwell.util.ThrowingConsumer;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.menu.WarStatus;
import net.william278.husktowns.network.Message;
import net.william278.husktowns.network.Payload;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.war.Declaration;
import net.william278.husktowns.war.War;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.william278.husktowns.config.Settings.TownSettings.RelationsSettings.WarSettings;

public class WarManager {

    private final HuskTowns plugin;
    private final List<War> activeWars;
    private final List<Declaration> pendingDeclarations;

    public WarManager(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
        this.activeWars = new ArrayList<>();
        this.pendingDeclarations = new ArrayList<>();
    }

    @NotNull
    public List<War> getActiveWars() {
        return activeWars;
    }

    @NotNull
    public List<Declaration> getPendingDeclarations() {
        return pendingDeclarations;
    }

    public Optional<Declaration> getPendingDeclaration(@NotNull Town town) {
        return getPendingDeclarations().stream()
            .filter(declaration -> declaration.defendingTown() == town.getId())
            .findFirst();
    }

    public void showWarStatus(@NotNull OnlineUser user, @Nullable String townName) {
        final Optional<Town> optionalTown = townName == null
            ? plugin.getUserTown(user).map(Member::town) : plugin.findTown(townName);
        if (optionalTown.isEmpty()) {
            if (townName == null) {
                plugin.getLocales().getLocale("error_not_in_town")
                    .ifPresent(user::sendMessage);
            } else {
                plugin.getLocales().getLocale("error_town_not_found", townName)
                    .ifPresent(user::sendMessage);
            }
            return;
        }

        final Town town = optionalTown.get();
        final Optional<War> optionalWar = town.getCurrentWar();
        if (optionalWar.isEmpty()) {
            plugin.getLocales().getLocale("error_town_not_at_war", town.getName())
                .ifPresent(user::sendMessage);
            return;
        }

        // Build and show a war status menu
        WarStatus.of(optionalWar.get(), user, plugin).show();
    }

    public void sendWarDeclaration(@NotNull OnlineUser sender, @NotNull String townName, @NotNull BigDecimal wager) {
        plugin.getManager().memberEditTown(sender, Privilege.DECLARE_WAR, (member -> {
            final Optional<Town> optionalTown = plugin.findTown(townName);
            if (optionalTown.isEmpty() || member.town().equals(optionalTown.get())) {
                plugin.getLocales().getLocale("error_town_not_found", townName)
                    .ifPresent(sender::sendMessage);
                return false;
            }
            final Town defendingTown = optionalTown.get();
            if (getPendingDeclaration(defendingTown).isPresent()) {
                plugin.getLocales().getLocale("error_pending_war_declaration_exists", defendingTown.getName())
                    .ifPresent(sender::sendMessage);
                return false;
            }

            // Check towns aren't already at war
            if (validateProhibitedFromWar(sender, wager, member.town(), defendingTown)) {
                return false;
            }

            // Require defending town to have a spawn
            if (defendingTown.getSpawn().isEmpty()) {
                plugin.getLocales().getLocale("error_town_spawn_not_set")
                    .ifPresent(sender::sendMessage);
                return false;
            }

            // Create declaration and dispatch
            final Declaration declaration = Declaration.create(member, defendingTown, wager, plugin);
            getPendingDeclarations().add(declaration);
            plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                .type(Message.Type.TOWN_WAR_DECLARATION_SENT)
                .payload(Payload.declaration(declaration))
                .target(Message.TARGET_ALL, Message.TargetType.SERVER).build()
                .send(broker, sender));

            // Send notification
            final WarSettings settings = plugin.getSettings().getTowns().getRelations().getWars();
            plugin.getLocales().getLocale("war_declaration_notification",
                    member.town().getName(), defendingTown.getName(), plugin.formatMoney(declaration.wager()),
                    Long.toString(settings.getDeclarationExpiry()))
                .ifPresent(t -> {
                    plugin.getManager().sendTownMessage(member.town(), t.toComponent());
                    plugin.getManager().sendTownMessage(defendingTown, t.toComponent());
                });

            // Send options to the defending town.
            plugin.getLocales().getLocale("war_declaration_options", member.town().getName())
                .ifPresent(l -> plugin.getManager().sendTownMessage(defendingTown, l.toComponent()));

            // Log that a declaration has been sent
            member.town().getLog().log(Action.of(sender, Action.Type.DECLARED_WAR,
                String.format("%s (%s)", defendingTown.getName(), plugin.formatMoney(declaration.wager()))));
            return true;
        }));

    }

    public void acceptWarDeclaration(@NotNull OnlineUser acceptor) {
        plugin.getManager().memberEditTown(acceptor, Privilege.DECLARE_WAR, (member -> {
            final Town defendingTown = member.town();
            final Optional<Declaration> optionalDeclaration = getPendingDeclaration(member.town());
            if (optionalDeclaration.isEmpty()) {
                plugin.getLocales().getLocale("error_no_pending_war_declaration", defendingTown.getName())
                    .ifPresent(acceptor::sendMessage);
                return false;
            }

            // Check if the declaration has expired
            final Declaration declaration = optionalDeclaration.get();
            if (declaration.hasExpired()) {
                plugin.getLocales().getLocale("error_war_declaration_expired")
                    .ifPresent(acceptor::sendMessage);
                getPendingDeclarations().remove(declaration);
                return false;
            }

            final Optional<Town> optionalAttackingTown = declaration.getAttackingTown(plugin);
            if (optionalAttackingTown.isEmpty()) {
                plugin.getLocales().getLocale("error_town_no_longer_exists")
                    .ifPresent(acceptor::sendMessage);
                return false;
            }

            // Check towns aren't already at war
            final Town attackingTown = optionalAttackingTown.get();
            if (validateProhibitedFromWar(acceptor, declaration.wager(), attackingTown, defendingTown)) {
                return false;
            }

            final Optional<String> warServer = declaration.getWarServerName(plugin);
            if (warServer.isEmpty()) {
                plugin.getLocales().getLocale("error_town_spawn_not_set")
                    .ifPresent(acceptor::sendMessage);
                return false;
            }

            if (!plugin.getSettings().getCrossServer().isEnabled()
                || warServer.get().equalsIgnoreCase(plugin.getServerName())) {
                startWar(
                    acceptor, attackingTown, defendingTown, declaration.wager(),
                    (startedWar) -> startedWar.teleportUsers(plugin)
                );
            }

            final WarSettings settings = plugin.getSettings().getTowns().getRelations().getWars();
            plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                .type(Message.Type.TOWN_WAR_DECLARATION_ACCEPTED)
                .payload(Payload.declaration(declaration))
                .target(Message.TARGET_ALL, Message.TargetType.SERVER).build()
                .send(broker, acceptor));
            plugin.getLocales().getLocale("war_declaration_accepted",
                    defendingTown.getName(), attackingTown.getName(),
                    Long.toString(settings.getWarZoneRadius()))
                .ifPresent(l -> {
                    plugin.getManager().sendTownMessage(attackingTown, l.toComponent());
                    plugin.getManager().sendTownMessage(defendingTown, l.toComponent());
                });
            getPendingDeclarations().remove(declaration);
            return true;
        }));
    }

    // Validate that the towns are not already at war, that they are not on cooldown and that they can afford the wager
    private boolean validateProhibitedFromWar(@NotNull CommandUser user, @NotNull BigDecimal wager,
                                              @NotNull Town... towns) {
        Town previousTown = null;
        final WarSettings settings = plugin.getSettings().getTowns().getRelations().getWars();
        for (Town town : towns) {
            if (town.getCurrentWar().isPresent()) {
                plugin.getLocales().getLocale("error_town_already_at_war", town.getName())
                    .ifPresent(user::sendMessage);
                return true;
            }
            if (previousTown != null && previousTown.equals(town)) {
                plugin.getLocales().getLocale("error_cannot_start_civil_war")
                    .ifPresent(user::sendMessage);
                return true;
            }
            if (previousTown != null && !previousTown.areRelationsBilateral(town, Town.Relation.ENEMY)) {
                plugin.getLocales().getLocale("error_war_towns_not_mutual_enemies",
                    previousTown.getName(), town.getName()).ifPresent(user::sendMessage);
                return true;
            }
            if (OffsetDateTime.now().isBefore(town.getLog().getLastWarTime().orElse(OffsetDateTime.MIN)
                .plusHours(settings.getCooldown()))) {
                plugin.getLocales().getLocale("error_town_war_cooldown", town.getName(),
                        Long.toString(settings.getCooldown()))
                    .ifPresent(user::sendMessage);
                return true;
            }
            if (wager.compareTo(BigDecimal.ZERO) > 0 && town.getMoney().compareTo(wager) < 0) {
                plugin.getLocales().getLocale("error_economy_town_insufficient_funds", plugin
                        .getEconomyHook().map(hook -> hook.formatMoney(wager)).orElse(wager.toString()))
                    .ifPresent(user::sendMessage);
                return true;
            }
            previousTown = town;
        }
        return false;
    }

    public void startWar(@NotNull OnlineUser acceptor, @NotNull Town attacker,
                         @NotNull Town defender, @NotNull BigDecimal wager,
                         @NotNull ThrowingConsumer<War> callback) {
        if (attacker.getCurrentWar().isPresent() || defender.getCurrentWar().isPresent()) {
            throw new IllegalStateException("One of the towns is already in a war");
        }

        // Create the war, edit towns
        final WarSettings settings = plugin.getSettings().getTowns().getRelations().getWars();
        final long warZoneRadius = Math.max(settings.getWarZoneRadius(), 16);
        final War war = War.create(plugin, attacker, defender, wager, warZoneRadius);
        plugin.getManager().editTown(
            acceptor, attacker,
            (town -> {
                town.setCurrentWar(war);
                town.setMoney(town.getMoney().subtract(wager));
                town.getLog().log(Action.of(acceptor, Action.Type.START_WAR, defender.getName()));
            }),
            (attacking -> plugin.getManager().editTown(
                acceptor, defender,
                (town -> {
                    town.setCurrentWar(war);
                    town.setMoney(town.getMoney().subtract(wager));
                    town.getLog().log(Action.of(acceptor, Action.Type.START_WAR, attacker.getName()));
                }),
                (defending -> {
                    // Add the war to the local map
                    getActiveWars().removeIf(
                        w -> w.getAttacking() == attacker.getId() || w.getDefending() == defender.getId()
                    );
                    getActiveWars().add(war);

                    // Accept callback
                    callback.accept(war);
                })
            ))
        );
    }

    public void surrenderWar(@NotNull OnlineUser user) {
        plugin.getManager().ifMember(user, Privilege.DECLARE_WAR, (member -> {
            if (member.town().getCurrentWar().isEmpty()) {
                plugin.getLocales().getLocale("error_town_not_at_war", member.town().getName())
                    .ifPresent(user::sendMessage);
                return;
            }

            final War war = member.town().getCurrentWar().get();
            boolean areAttackers = war.getAttacking() == member.town().getId();
            war.end(plugin, areAttackers ? War.EndState.DEFENDER_WIN : War.EndState.ATTACKER_WIN);
            plugin.getLocales().getLocale("war_surrendered", member.user().getUsername())
                .ifPresent(l -> plugin.getManager().sendTownMessage(member.town(), l.toComponent()));
        }));
    }

    public void removeActiveWar(@NotNull War war) {
        getActiveWars().remove(war);
    }

    public void handlePlayerQuit(@NotNull OnlineUser user) {
        for (int i = 0; i < getActiveWars().size(); i++) {
            final War war = getActiveWars().get(i);
            if (war.isPlayerActive(user.getUuid())) {
                plugin.runSyncDelayed(
                    () -> war.handlePlayerDieOrFlee(plugin, user, true), user, 10L
                );
                return;
            }
        }
    }

    public void handlePlayerDeath(@NotNull OnlineUser user) {
        for (int i = 0; i < getActiveWars().size(); i++) {
            getActiveWars().get(i).handlePlayerDieOrFlee(plugin, user, false);
        }
    }

    public void handlePlayerFlee(@NotNull OnlineUser user) {
        for (int i = 0; i < getActiveWars().size(); i++) {
            final War war = getActiveWars().get(i);
            if (war.isPlayerActive(user.getUuid()) &&
                user.getPosition().distanceBetween(war.getDefenderSpawn()) > war.getWarZoneRadius()) {
                war.handlePlayerDieOrFlee(plugin, user, true);
            }
        }
    }

}
