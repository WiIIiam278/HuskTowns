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

import com.google.common.collect.Maps;
import com.google.gson.annotations.Expose;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Represents the audit log of actions taken in a {@link net.william278.husktowns.town.Town}
 */
public class Log {

    @Expose
    private Map<String, Action> actions;

    private Log(@NotNull Map<OffsetDateTime, Action> actions) {
        this.actions = new TreeMap<>();
        actions.forEach((key, value) -> this.actions.put(key.toString(), value));
    }

    @SuppressWarnings("unused")
    private Log() {
    }

    /**
     * Create a new Log instance for a newly created town
     *
     * @param creator the creator of the town
     * @return the new {@code Log} instance.
     * <p>
     * This will contain a log with a single {@link Action.Type#CREATE_TOWN} mapped to the creator
     */
    @NotNull
    public static Log newTownLog(@NotNull User creator) {
        final Log log = new Log(new TreeMap<>());
        log.log(Action.of(creator, Action.Type.CREATE_TOWN));
        return log;
    }

    /**
     * Create a new, empty Log instance
     *
     * @return the new {@code Log} instance
     */
    @NotNull
    public static Log empty() {
        return new Log(new TreeMap<>());
    }

    /**
     * Create a new Log instance from a migrated town
     *
     * @param foundedTime the time the town was founded
     * @return the new {@code Log} instance
     * <p>
     * This will contain a log with two action entries: One, when the migrated town was originally created
     * ({@link Action.Type#CREATE_TOWN}), and another ({@link Action.Type#TOWN_DATA_MIGRATED}), when the migration occurred.
     */
    @NotNull
    public static Log migratedLog(@NotNull OffsetDateTime foundedTime) {
        final Log log = new Log(Maps.newTreeMap());
        log.actions.put(foundedTime.toString(), Action.of(Action.Type.CREATE_TOWN));
        log.log(Action.of(Action.Type.TOWN_DATA_MIGRATED));
        return log;
    }

    /**
     * Log an {@link Action}
     *
     * @param action the action to add to the log.
     * @apiNote The action will be logged as having occurred just now
     */
    public void log(@NotNull Action action) {
        this.actions.putIfAbsent(OffsetDateTime.now().toString(), action);
    }

    /**
     * Get the map of {@link OffsetDateTime}s to their respective {@link Action}s
     *
     * @return the map of actions to when they occurred
     */
    @NotNull
    public Map<OffsetDateTime, Action> getActions() {
        return actions.entrySet().stream().collect(
                TreeMap::new,
                (m, e) -> m.put(OffsetDateTime.parse(e.getKey()), e.getValue()),
                Map::putAll
        );
    }

    /**
     * Returns when the town was founded
     *
     * @return the {@link OffsetDateTime} of the first found {@link Action.Type#CREATE_TOWN} action
     */
    @NotNull
    public OffsetDateTime getFoundedTime() {
        return getActions().entrySet().stream()
                .filter(entry -> entry.getValue().getType() == Action.Type.CREATE_TOWN)
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(OffsetDateTime.now());
    }

    /**
     * Returns the last time a war was started
     *
     * @return the {@link OffsetDateTime} of the last found {@link Action.Type#START_WAR} action
     */
    public Optional<OffsetDateTime> getLastWarTime() {
        return getActions().entrySet().stream()
                .filter(entry -> entry.getValue().getType() == Action.Type.START_WAR)
                .findFirst()
                .map(Map.Entry::getKey);
    }

}
