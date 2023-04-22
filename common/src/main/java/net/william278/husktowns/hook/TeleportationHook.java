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
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

/**
 * A hook for handling player teleportation
 */
public abstract class TeleportationHook extends Hook {
    protected TeleportationHook(@NotNull HuskTowns plugin, @NotNull String name) {
        super(plugin, name);
    }

    /**
     * Teleport a player to a given position
     *
     * @param user     the user to teleport
     * @param position the position to teleport to
     */
    public abstract void teleport(@NotNull OnlineUser user, @NotNull Position position, @NotNull String server);

}
