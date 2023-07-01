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

package net.william278.husktowns.hook;

import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

public abstract class Hook {

    protected final HuskTowns plugin;
    private final String name;
    private boolean enabled = false;

    protected Hook(@NotNull HuskTowns plugin, @NotNull String name) {
        this.plugin = plugin;
        this.name = name;
    }

    /**
     * Fired when a hook is enabled
     */
    protected abstract void onEnable();

    /**
     * Enable the hook
     */
    public final void enable() {
        this.onEnable();
        this.enabled = true;
    }

    /**
     * Get if the hook is disabled
     *
     * @return {@code true} if the hook is disabled, {@code false} otherwise
     */
    public boolean isDisabled() {
        return !enabled;
    }

    /**
     * Get the name of the hook
     *
     * @return the name of the hook
     */
    @NotNull
    public String getName() {
        return name;
    }

}
