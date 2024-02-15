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

package net.william278.husktowns.audit;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represents a {@link Log logged} action that was performed on a {@link net.william278.husktowns.town.Town}
 */
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Action {

    @Expose
    private Type action;
    @Expose
    @Nullable
    private User user;
    @Expose
    @Nullable
    private String details;

    /**
     * Return a new {@code Action} for a given {@link Type} and {@link User}
     *
     * @param user   the user who performed the action
     * @param action the type of action performed
     * @return the new {@code Action}
     */
    @NotNull
    public static Action of(@NotNull User user, @NotNull Type action) {
        return new Action(action, user, null);
    }

    /**
     * Return a new {@code Action} for a given {@link Type}
     *
     * @param action the type of action performed
     * @return the new {@code Action}
     */
    @NotNull
    public static Action of(@NotNull Type action) {
        return new Action(action, null, null);
    }

    /**
     * Return a new {@code Action} for a given {@link Type} and details
     *
     * @param action  the type of action performed
     * @param details the details of the action
     * @return the new {@code Action}
     */
    @NotNull
    public static Action of(@NotNull Type action, @NotNull String details) {
        return new Action(action, null, details);
    }

    /**
     * Return a new {@code Action} for a given {@link Type}, {@link User}, and details
     *
     * @param user    the user who performed the action
     * @param action  the type of action performed
     * @param details the details of the action
     * @return the new {@code Action}
     */
    @NotNull
    public static Action of(@NotNull User user, @NotNull Type action, @NotNull String details) {
        return new Action(action, user, details);
    }

    /**
     * Get the user involved in the action, if any
     *
     * @return the user involved in the action, wrapped in an {@link Optional}
     */
    public Optional<User> getUser() {
        return Optional.ofNullable(user);
    }

    /**
     * Get the details of the action, if any
     *
     * @return the details of the action, wrapped in an {@link Optional}
     */
    public Optional<String> getDetails() {
        return Optional.ofNullable(details);
    }

    /**
     * Get the type of action performed
     *
     * @return the type of action performed
     */
    @NotNull
    public Type getType() {
        return action;
    }

    /**
     * Get a string representation of the action
     *
     * @return a string representation of the action
     */
    @Override
    public String toString() {
        final StringJoiner joiner = new StringJoiner(" ");
        getUser().ifPresent(user -> joiner.add("[" + user.getUsername() + "]"));
        joiner.add(action.toString());
        getDetails().ifPresent(joiner::add);
        return joiner.toString();
    }

    /**
     * Different types of actions that can be {@link Log logged}
     */
    public enum Type {
        CREATE_TOWN,
        CREATE_CLAIM,
        DELETE_CLAIM,
        DELETE_ALL_CLAIMS,
        RENAME_TOWN,
        UPDATE_BIO,
        UPDATE_GREETING,
        UPDATE_FAREWELL,
        UPDATE_COLOR,
        UPDATE_SPAWN,
        UPDATE_SPAWN_IS_PUBLIC,
        CLEAR_SPAWN,
        MAKE_CLAIM_PLOT,
        MAKE_CLAIM_FARM,
        MAKE_CLAIM_REGULAR,
        ADD_PLOT_MEMBER,
        REMOVE_PLOT_MEMBER,
        CLAIM_VACANT_PLOT,
        DEPOSIT_MONEY,
        WITHDRAW_MONEY,
        SET_FLAG_RULE,
        LEVEL_UP,
        EVICT,
        MEMBER_LEAVE,
        MEMBER_JOIN,
        TRANSFER_OWNERSHIP,
        ADVANCEMENT_REWARD_MONEY,
        ADMIN_TAKE_OVER,
        ADMIN_SET_LEVEL,
        ADMIN_SET_BONUS,
        ADMIN_CLEAR_BONUS,
        ADMIN_SET_BALANCE,
        ADVANCEMENT_REWARD_LEVELS,
        ADVANCEMENT_REWARD_BONUS_CLAIMS,
        ADVANCEMENT_REWARD_BONUS_MEMBERS,
        SET_RELATION,
        TOWN_DATA_MIGRATED,
        DECLARED_WAR,
        START_WAR,
        WON_WAR,
        LOST_WAR
    }

}
