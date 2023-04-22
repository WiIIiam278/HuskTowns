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

package net.william278.husktowns.claim;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link World} mapped to a specific server, by ID
 *
 * @param server The ID of the server the world is on
 * @param world  The world
 */
public record ServerWorld(@NotNull String server, @NotNull World world) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof final ServerWorld serverWorld)) {
            return false;
        }
        return serverWorld.server().equals(server) && serverWorld.world().equals(world);
    }

    @Override
    public String toString() {
        return server + "/" + world.getName();
    }
}
