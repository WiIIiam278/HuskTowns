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

package net.william278.husktowns.advancement;

import net.william278.husktowns.audit.Action;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

public class Variable<T> {
    private static final Map<String, Variable<?>> registered = new HashMap<>();
    public static final Variable<Integer> TOWN_LEVEL = create(
        "town_level",
        (town, onlineUser, level) -> (town.getLevel() - level),
        Integer.class
    );
    public static final Variable<BigDecimal> TOWN_MONEY = create(
        "town_money",
        (town, onlineUser, balance) -> (town.getMoney().compareTo(balance)),
        BigDecimal.class
    );
    public static final Variable<Integer> TOWN_UNIQUE_DEPOSITORS = create(
        "unique_depositors",
        (town, onlineUser, uniqueDepositors) -> Math.toIntExact((town.getLog().getActions().values().stream()
            .filter(action -> action.getType() == Action.Type.DEPOSIT_MONEY)
            .map(Action::getUser).distinct().count() - uniqueDepositors)),
        Integer.class
    );
    public static final Variable<Integer> TOWN_MEMBERS = create(
        "town_members",
        (town, onlineUser, members) -> (town.getMembers().size() - members),
        Integer.class
    );
    public static final Variable<Integer> MEMBERS_LEFT = create(
        "members_left",
        (town, onlineUser, membersLeft) -> Math.toIntExact((town.getLog().getActions().values().stream()
            .filter(action -> action.getType() == Action.Type.MEMBER_LEAVE).map(Action::getUser).distinct().count() - membersLeft)),
        Integer.class
    );
    public static final Variable<Integer> TOWN_CLAIMS = create(
        "town_claims",
        (town, onlineUser, claims) -> (town.getClaimCount() - claims),
        Integer.class
    );
    public static final Variable<Integer> TOWN_CHANGED_COLOR = create(
        "changed_color",
        (town, onlineUser, changedColor) -> Math.toIntExact((town.getLog().getActions().values().stream()
            .filter(action -> action.getType() == Action.Type.UPDATE_COLOR).count() - changedColor)),
        Integer.class
    );
    public static final Variable<Boolean> TOWN_HAS_BIO = create(
        "town_has_bio",
        (town, onlineUser, hasBio) -> town.getBio().isPresent() == hasBio ? 0 : -1,
        Boolean.class
    );
    public static final Variable<Integer> TOWN_CHANGED_SPAWN = create(
        "changed_spawn",
        (town, onlineUser, changedSpawn) -> Math.toIntExact((town.getLog().getActions().values().stream()
            .filter(action -> action.getType() == Action.Type.UPDATE_SPAWN).count() - changedSpawn)),
        Integer.class
    );
    public static final Variable<Boolean> TOWN_HAS_GREETING = create(
        "town_has_greeting",
        (town, onlineUser, hasGreeting) -> town.getGreeting().isPresent() == hasGreeting ? 0 : -1,
        Boolean.class
    );
    public static final Variable<Boolean> TOWN_HAS_FAREWELL = create(
        "town_has_farewell",
        (town, onlineUser, hasFarewell) -> town.getFarewell().isPresent() == hasFarewell ? 0 : -1,
        Boolean.class
    );
    public static final Variable<Integer> TOWN_MAYOR_CHANGED = create(
        "town_mayor_changed",
        (town, onlineUser, mayorChanged) -> Math.toIntExact((town.getLog().getActions().values().stream()
            .filter(action -> action.getType() == Action.Type.TRANSFER_OWNERSHIP).map(Action::getUser).distinct().count() - mayorChanged)),
        Integer.class
    );
    public static final Variable<OffsetDateTime> TOWN_FOUNDED = create(
        "town_founded",
        (town, onlineUser, founded) -> (town.getFoundedTime().compareTo(founded)),
        OffsetDateTime.class
    );

    public static final Variable<Boolean> USER_IN_TOWN = create(
        "user_in_town",
        (town, onlineUser, inTown) -> town.getMembers().containsKey(onlineUser.getUuid()) == inTown ? 0 : -1,
        Boolean.class
    );

    public static final Variable<Boolean> TOWN_HAS_SPAWN = create(
        "town_has_spawn",
        (town, onlineUser, hasSpawn) -> town.getSpawn().isPresent() == hasSpawn ? 0 : -1,
        Boolean.class
    );

    private final String identifier;
    private final TriFunction<Town, OnlineUser, T, Integer> resolver;
    private final Class<T> type;

    private Variable(@NotNull String identifier, @NotNull TriFunction<Town, OnlineUser, T, Integer> resolver, @NotNull Class<T> type) {
        this.identifier = identifier;
        this.resolver = resolver;
        this.type = type;
    }

    public static <T> Variable<T> create(@NotNull String identifier, @NotNull TriFunction<Town, OnlineUser, T, Integer> resolver, @NotNull Class<T> type) {
        final Variable<T> variable = new Variable<>(identifier, resolver, type);
        registered.put(variable.identifier, variable);
        return variable;
    }

    @NotNull
    public String getIdentifier() {
        return identifier;
    }


    @SuppressWarnings("unchecked")
    public int resolve(@NotNull Town town, @NotNull OnlineUser user, @NotNull String value) {
        if (type == Integer.class || type == int.class) {
            return resolver.apply(town, user, (T) Integer.valueOf(value));
        } else if (type == BigDecimal.class || type == double.class) {
            return resolver.apply(town, user, (T) BigDecimal.valueOf(Double.parseDouble(value)));
        } else if (type == OffsetDateTime.class) {
            return resolver.apply(town, user, (T) OffsetDateTime.parse(value));
        } else if (type == Boolean.class || type == boolean.class) {
            return resolver.apply(town, user, (T) Boolean.valueOf(value));
        } else {
            return resolver.apply(town, user, (T) value);
        }
    }

    @NotNull
    public static <T> String valueToString(@NotNull T value) {
        final Class<?> type = value.getClass();
        if (type == Integer.class || Objects.equals(type, int.class)) {
            return String.valueOf(value);
        } else if (type == BigDecimal.class || Objects.equals(type, double.class)) {
            return String.valueOf(value);
        } else if (type == OffsetDateTime.class) {
            return String.valueOf(value);
        } else if (type == Boolean.class || Objects.equals(type, boolean.class)) {
            return String.valueOf(value);
        } else {
            return value.toString();
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> Optional<Variable<T>> parseVariable(@NotNull String identifier) {
        return Optional.ofNullable((Variable<T>) registered.get(identifier.toLowerCase(Locale.ENGLISH)));
    }
}
