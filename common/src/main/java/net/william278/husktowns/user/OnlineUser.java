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

package net.william278.husktowns.user;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.UUID;

public abstract class OnlineUser extends User implements CommandUser {

    protected OnlineUser(@NotNull UUID uuid, @NotNull String username) {
        super(uuid, username);
    }

    @NotNull
    public abstract Chunk getChunk();

    @NotNull
    public abstract Position getPosition();

    @NotNull
    public abstract World getWorld();

    public abstract void sendPluginMessage(@NotNull String channel, byte[] message);

    public final void sendActionBar(@NotNull Component component) {
        getAudience().sendActionBar(component);
    }

    public final void sendActionBar(@NotNull MineDown mineDown) {
        this.sendActionBar(mineDown.toComponent());
    }

    public abstract void spawnMarkerParticle(@NotNull Position position, @NotNull Color color, int count);

    @NotNull
    public abstract Audience getAudience();

    public abstract void teleportTo(@NotNull Position position);

    public abstract void giveExperiencePoints(int quantity);

    public abstract void giveExperienceLevels(int quantity);

    public abstract void giveItem(@NotNull Key material, int quantity);

}
