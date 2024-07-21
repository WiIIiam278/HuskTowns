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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the audit log of actions taken in a {@link net.william278.husktowns.town.Town}
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Log {

    // Format used for storing map timestamps
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Expose
    private Map<String, Action> actions = Maps.newLinkedHashMap();

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
        final Log log = new Log();
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
        return new Log();
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
        final Log log = new Log();
        log.actions.put(foundedTime.format(FORMAT), Action.of(Action.Type.CREATE_TOWN));
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
        this.actions.putIfAbsent(OffsetDateTime.now().format(FORMAT), action);
    }

    /**
     * Get the map of {@link OffsetDateTime}s to their respective {@link Action}s
     *
     * @return the map of actions to when they occurred
     */
    @NotNull
    @Unmodifiable
    public Map<OffsetDateTime, Action> getActions() {
        final Map<OffsetDateTime, Action> parsed = Maps.newLinkedHashMap();
        actions.forEach((key, value) -> parsed.put(OffsetDateTime.parse(key, FORMAT), value));
        return parsed;
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
            .map(Map.Entry::getKey)
            .findFirst().orElse(OffsetDateTime.now());
    }

    /**
     * Returns the last time a war was started
     *
     * @return the {@link OffsetDateTime} of the last found {@link Action.Type#START_WAR} action
     */
    public Optional<OffsetDateTime> getLastWarTime() {
        return getActions().entrySet().stream()
            .filter(entry -> entry.getValue().getType() == Action.Type.START_WAR)
            .map(Map.Entry::getKey)
            .max(OffsetDateTime::compareTo);
    }

}
