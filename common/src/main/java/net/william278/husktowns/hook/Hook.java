/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
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
