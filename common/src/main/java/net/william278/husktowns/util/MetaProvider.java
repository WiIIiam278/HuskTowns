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

package net.william278.husktowns.util;

import net.william278.desertwell.util.UpdateChecker;
import net.william278.desertwell.util.Version;
import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public interface MetaProvider {

    int SPIGOT_RESOURCE_ID = 92672;
    int BSTATS_PLUGIN_ID = 11265;

    /**
     * Get the plugin {@link Version}
     *
     * @return The plugin {@link Version}
     * @since 3.1
     */
    @NotNull
    Version getPluginVersion();

    /**
     * Get the plugin {@link UpdateChecker}
     *
     * @return the plugin {@link UpdateChecker}
     * @since 3.1
     */
    @NotNull
    default UpdateChecker getUpdateChecker() {
        return UpdateChecker.builder()
                .currentVersion(getPluginVersion())
                .endpoint(UpdateChecker.Endpoint.SPIGOT)
                .resource(Integer.toString(SPIGOT_RESOURCE_ID))
                .build();
    }

    /**
     * Check for updates and log a warning if an update is available
     *
     * @since 3.1
     */
    default void checkForUpdates() {
        if (!getPlugin().getSettings().isCheckForUpdates()) {
            return;
        }
        getUpdateChecker().check().thenAccept(checked -> {
            if (checked.isUpToDate()) {
                return;
            }
            getPlugin().log(Level.WARNING, String.format(
                    "A new version of HuskTowns is available: v%s (running v%s)",
                    checked.getLatestVersion(), getPluginVersion())
            );
        });
    }

    /**
     * Get the server type
     *
     * @return the server type
     * @since 3.1
     */
    @NotNull
    String getServerType();

    /**
     * Get the Minecraft version
     *
     * @return the Minecraft version
     * @since 3.1
     */
    @NotNull
    Version getMinecraftVersion();

    @NotNull
    HuskTowns getPlugin();

}
