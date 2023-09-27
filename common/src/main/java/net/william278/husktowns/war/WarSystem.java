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
import net.william278.husktowns.network.Message;
import net.william278.husktowns.network.Payload;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
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
                getPlugin().getLocales().getLocale("error_pending_declaration_exists", defendingTown.getName())
                        .ifPresent(sender::sendMessage); // TODO LOCALE
                return false;
            }

            // Check not already at war
            if (defendingTown.getCurrentWar().isPresent()) {
                getPlugin().getLocales().getLocale("error_town_already_at_war", defendingTown.getName())
                        .ifPresent(sender::sendMessage); // TODO LOCALE
                return false;
            } else if (member.town().getCurrentWar().isPresent()) {
                getPlugin().getLocales().getLocale("error_town_already_at_war", member.town().getName())
                        .ifPresent(sender::sendMessage); // TODO LOCALE
                return false;
            }

            // Require defending town to have a spawn
            if (defendingTown.getSpawn().isEmpty()) {
                getPlugin().getLocales().getLocale("error_town_spawn_not_set")
                        .ifPresent(sender::sendMessage);
                return false;
            }

            //todo cooldown checks, minimum online user checks, can afford wager checks

            // Create declaration and dispatch
            final Declaration declaration = Declaration.create(member, defendingTown, wager);
            getPendingDeclarations().add(declaration);
            getPlugin().getMessageBroker().ifPresent(broker -> Message.builder()
                    .type(Message.Type.TOWN_WAR_DECLARATION_SENT)
                    .payload(Payload.declaration(declaration))
                    .target(Message.TARGET_ALL, Message.TargetType.SERVER).build()
                    .send(broker, sender));

            //todo locales
            getPlugin().getLocales().getLocale("war_declaration_sent", member.town().getName(),
                            defendingTown.getName(), wager.toString()) //todo format wager
                    .ifPresent(t -> getPlugin().getManager().sendTownMessage(member.town(), t.toComponent()));
            getPlugin().getLocales().getLocale("war_declaration_received", member.town().getName(),
                            defendingTown.getName(), wager.toString()) //todo format wager
                    .ifPresent(t -> getPlugin().getManager().sendTownMessage(defendingTown, t.toComponent()));
            member.town().getLog().log(Action.of(Action.Type.DECLARED_WAR, defendingTown.getName()));
            return true;
        }));

    }

    default void acceptWarDeclaration(@NotNull OnlineUser acceptor, @NotNull Town defendingTown) {
        getPlugin().getManager().memberEditTown(acceptor, Privilege.DECLARE_WAR, (member -> {
            final Optional<Declaration> optionalDeclaration = getPendingDeclaration(defendingTown);
            if (optionalDeclaration.isEmpty()) {
                getPlugin().getLocales().getLocale("error_no_pending_declaration", defendingTown.getName())
                        .ifPresent(acceptor::sendMessage); // TODO LOCALE
                return false;
            }

            // Check if the declaration has expired
            final Declaration declaration = optionalDeclaration.get();
            if (declaration.hasExpired()) {
                getPlugin().getLocales().getLocale("error_declaration_expired", defendingTown.getName())
                        .ifPresent(acceptor::sendMessage); // TODO LOCALE
                getPendingDeclarations().remove(declaration);
                return false;
            }

            final Optional<Town> optionalAttackingTown = declaration.getAttackingTown(getPlugin());
            if (optionalAttackingTown.isEmpty()) {
                getPlugin().getLocales().getLocale("error_town_no_longer_exists")
                        .ifPresent(acceptor::sendMessage);
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
            getPlugin().getLocales().getLocale("war_declaration_accepted",
                            attackingTown.getName(), defendingTown.getName())
                    .ifPresent(l -> getPlugin().getManager().sendTownMessage(attackingTown, l.toComponent()));
            getPlugin().getLocales().getLocale("war_declaration_accepted",
                            attackingTown.getName(), defendingTown.getName())
                    .ifPresent(l -> getPlugin().getManager().sendTownMessage(defendingTown, l.toComponent()));
            getPendingDeclarations().remove(declaration);
            return true;
        }));
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

    @NotNull
    @ApiStatus.Internal
    HuskTowns getPlugin();

}
