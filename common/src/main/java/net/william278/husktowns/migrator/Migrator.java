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

package net.william278.husktowns.migrator;

import de.themoep.minedown.adventure.MineDown;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Level;

public abstract class Migrator {

    protected final HuskTowns plugin;
    private final String name;
    private final TreeMap<String, String> parameters;
    private OffsetDateTime startTime = OffsetDateTime.now();

    protected Migrator(@NotNull HuskTowns plugin, @NotNull String name) {
        this.plugin = plugin;
        this.name = name;
        this.parameters = new TreeMap<>();
    }

    public void start(@NotNull CommandUser executor) {
        startTime = OffsetDateTime.now();
        plugin.runAsync(() -> {
            plugin.setLoaded(false);
            executor.sendMessage(new MineDown("[[%1% Migrator] Data migration has started](#00fb9a)"
                .replaceAll("%1%", getName())));
            try {
                onStart();
                executor.sendMessage(new MineDown("[[%1% Migrator] Finished data migration in %2%. A server restart is recommended.](#00fb9a)"
                    .replaceAll("%1%", getName())
                    .replaceAll("%2%", getDuration())));
            } catch (Exception e) {
                executor.sendMessage(new MineDown("[[%1% Migrator] Migration failed after %2% due to an exception (%3%)](#ff3300)"
                    .replaceAll("%1%", getName())
                    .replaceAll("%2%", getDuration())
                    .replaceAll("%3%", e.getMessage())));
                plugin.log(Level.SEVERE, "Exception during " + getName() + " migration; aborted: " + e.getCause(), e);
            } finally {
                plugin.reload();
            }
        });
    }

    @NotNull
    private String getDuration() {
        return OffsetDateTime.now().minusSeconds(startTime.toEpochSecond()).toEpochSecond() + "s";
    }

    @NotNull
    public TreeMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameter(@NotNull String key, @NotNull String value) {
        parameters.put(key.toUpperCase(), value);
    }

    public Optional<String> getParameter(@NotNull String key) {
        return Optional.ofNullable(getParameters().get(key));
    }

    @NotNull
    public String getName() {
        return name;
    }

    protected abstract void onStart();

}
