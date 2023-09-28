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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface WarSystem {

    @NotNull
    List<War> getActiveWars();

    @NotNull
    List<Declaration> getPendingDeclarations();

    default Optional<Declaration> getPendingDeclaration(@NotNull Town town) {
        return getPendingDeclarations().stream()
                .filter(declaration -> declaration.defendingTown() == town.getId())
                .findFirst();
    }

    default void showWarStatus(@NotNull OnlineUser user, @Nullable String townName) {
        final Optional<Town> optionalTown = townName == null ? getPlugin().getUserTown(user).map(Member::town) :
                getPlugin().findTown(townName);
        if (optionalTown.isEmpty()) {
            if (townName == null) {
                getPlugin().getLocales().getLocale("error_not_in_town")
                        .ifPresent(user::sendMessage);
            } else {
                getPlugin().getLocales().getLocale("error_town_not_found", townName)
                        .ifPresent(user::sendMessage);
            }
            return;
        }

        final Town town = optionalTown.get();
        final Optional<War> optionalWar = town.getCurrentWar();
        if (optionalWar.isEmpty()) {
            getPlugin().getLocales().getLocale("error_town_not_at_war", town.getName())
                    .ifPresent(user::sendMessage);
            return;
        }

        // Build and show a war status menu
        WarStatus.of(optionalWar.get(), user, getPlugin()).show();
    }

    default void sendWarDeclaration(@NotNull OnlineUser sender, @NotNull String townName, @NotNull BigDecimal wager) {
        getPlugin().getManager().memberEditTown(sender, Privilege.DECLARE_WAR, (member -> {
            final Optional<Town> optionalTown = getPlugin().findTown(townName);
            if (optionalTown.isEmpty()) {
                getPlugin().getLocales().getLocale("error_town_not_found", townName)
                        .ifPresent(sender::sendMessage);
                return false;
            }

            final Town defendingTown = optionalTown.get();
            if (getPendingDeclaration(defendingTown).isPresent()) {
                getPlugin().getLocales().getLocale("error_pending_war_declaration_exists", defendingTown.getName())
                        .ifPresent(sender::sendMessage);
                return false;
            }

            // Check towns aren't already at war
            if (validateProhibitedFromWar(sender, wager, member.town(), defendingTown)) {
                return false;
            }

            // Require defending town to have a spawn
            if (defendingTown.getSpawn().isEmpty()) {
                getPlugin().getLocales().getLocale("error_town_spawn_not_set")
                        .ifPresent(sender::sendMessage);
                return false;
            }

            // Create declaration and dispatch
            final Declaration declaration = Declaration.create(member, defendingTown, wager);
            getPendingDeclarations().add(declaration);
            getPlugin().getMessageBroker().ifPresent(broker -> Message.builder()
                    .type(Message.Type.TOWN_WAR_DECLARATION_SENT)
                    .payload(Payload.declaration(declaration))
                    .target(Message.TARGET_ALL, Message.TargetType.SERVER).build()
                    .send(broker, sender));

            //todo locales
            final String formattedWager = getPlugin().getEconomyHook().map(hook -> hook.formatMoney(wager))
                    .orElse(wager.toString());
            getPlugin().getLocales().getLocale("war_declaration_sent", member.town().getName(),
                            defendingTown.getName(), formattedWager)
                    .ifPresent(t -> getPlugin().getManager().sendTownMessage(member.town(), t.toComponent()));
            getPlugin().getLocales().getLocale("war_declaration_received", member.town().getName(),
                            defendingTown.getName(), formattedWager)
                    .ifPresent(t -> getPlugin().getManager().sendTownMessage(defendingTown, t.toComponent()));
            member.town().getLog().log(Action.of(Action.Type.DECLARED_WAR, defendingTown.getName()));
            return true;
        }));

    }

    default void acceptWarDeclaration(@NotNull OnlineUser acceptor) {
        getPlugin().getManager().memberEditTown(acceptor, Privilege.DECLARE_WAR, (member -> {
            final Town defendingTown = member.town();
            final Optional<Declaration> optionalDeclaration = getPendingDeclaration(member.town());
            if (optionalDeclaration.isEmpty()) {
                getPlugin().getLocales().getLocale("error_no_pending_war_declaration", defendingTown.getName())
                        .ifPresent(acceptor::sendMessage);
                return false;
            }

            // Check if the declaration has expired
            final Declaration declaration = optionalDeclaration.get();
            if (declaration.hasExpired()) {
                getPlugin().getLocales().getLocale("error_war_declaration_expired")
                        .ifPresent(acceptor::sendMessage);
                getPendingDeclarations().remove(declaration);
                return false;
            }

            final Optional<Town> optionalAttackingTown = declaration.getAttackingTown(getPlugin());
            if (optionalAttackingTown.isEmpty()) {
                getPlugin().getLocales().getLocale("error_town_no_longer_exists")
                        .ifPresent(acceptor::sendMessage);
                return false;
            }

            // Check towns aren't already at war
            if (validateProhibitedFromWar(acceptor, declaration.wager(), member.town(), defendingTown)) {
                return false;
            }

            final Town attackingTown = optionalAttackingTown.get();
            final Optional<String> warServer = declaration.getWarServerName(getPlugin());
            if (warServer.isEmpty()) {
                getPlugin().getLocales().getLocale("error_town_spawn_not_set")
                        .ifPresent(acceptor::sendMessage);
                return false;
            }

            if (!getPlugin().getSettings().doCrossServer()
                    || warServer.get().equalsIgnoreCase(getPlugin().getServerName())) {
                startWar(
                        acceptor, attackingTown, defendingTown, declaration.wager(),
                        (startedWar) -> {
                            //todo teleport everyone and stuff
                        }
                );
            }

            getPlugin().getMessageBroker().ifPresent(broker -> Message.builder()
                    .type(Message.Type.TOWN_WAR_DECLARATION_ACCEPTED)
                    .payload(Payload.declaration(declaration))
                    .target(Message.TARGET_ALL, Message.TargetType.SERVER).build()
                    .send(broker, acceptor));
            getPlugin().getLocales().getLocale("war_declaration_accepted", //todo locales
                            attackingTown.getName(), defendingTown.getName())
                    .ifPresent(l -> getPlugin().getManager().sendTownMessage(attackingTown, l.toComponent()));
            getPlugin().getLocales().getLocale("war_declaration_accepted",
                            attackingTown.getName(), defendingTown.getName())
                    .ifPresent(l -> getPlugin().getManager().sendTownMessage(defendingTown, l.toComponent()));
            getPendingDeclarations().remove(declaration);
            return true;
        }));
    }

    // Validate that the towns are not already at war, that they are not on cooldown and that they can afford the wager
    private boolean validateProhibitedFromWar(@NotNull CommandUser user, @NotNull BigDecimal wager,
                                              @NotNull Town... towns) {
        for (Town town : towns) {
            if (town.getCurrentWar().isPresent()) {
                getPlugin().getLocales().getLocale("error_town_already_at_war", town.getName())
                        .ifPresent(user::sendMessage);
                return true;
            }
            if (OffsetDateTime.now().isBefore(town.getLog().getLastWarTime()
                    .plusHours(getPlugin().getSettings().getWarCooldown()))) {
                getPlugin().getLocales().getLocale("error_town_war_cooldown", town.getName(),
                                Long.toString(getPlugin().getSettings().getWarCooldown()))
                        .ifPresent(user::sendMessage);
                return true;
            }
            if (wager.compareTo(BigDecimal.ZERO) < 0 || town.getMoney().compareTo(wager) < 0) {
                getPlugin().getLocales().getLocale("error_economy_town_insufficient_funds", getPlugin()
                                .getEconomyHook().map(hook -> hook.formatMoney(wager)).orElse(wager.toString()))
                        .ifPresent(user::sendMessage);
                return true;
            }
        }
        return false;
    }

    default void startWar(@NotNull OnlineUser acceptor, @NotNull Town attacker,
                          @NotNull Town defender, @NotNull BigDecimal wager,
                          @NotNull ThrowingConsumer<War> callback) {
        if (attacker.getCurrentWar().isPresent() || defender.getCurrentWar().isPresent()) {
            throw new IllegalStateException("One of the towns is already in a war");
        }

        // Create the war, edit towns
        final long warZoneRadius = Math.max(getPlugin().getSettings().getWarZoneRadius(), 16);
        final War war = War.create(getPlugin(), attacker, defender, wager, warZoneRadius);
        getPlugin().getManager().editTown(
                acceptor, attacker,
                (town -> {
                    town.setCurrentWar(war);
                    town.getLog().log(Action.of(Action.Type.START_WAR, defender.getName()));
                }),
                (attacking -> getPlugin().getManager().editTown(
                        acceptor, defender,
                        (town -> {
                            town.setCurrentWar(war);
                            town.getLog().log(Action.of(Action.Type.START_WAR, attacker.getName()));
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

    default void deactivateWar(@NotNull War war) {
        getActiveWars().remove(war);
    }

    default void handlePlayerQuit(@NotNull OnlineUser user) {
        getActiveWars().forEach(war -> {
            if (war.isPlayerActive(user.getUuid())) {
                war.checkVictoryCondition(getPlugin());
            }
        });
    }

    @NotNull
    @ApiStatus.Internal
    HuskTowns getPlugin();

}
