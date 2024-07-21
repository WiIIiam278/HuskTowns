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

package net.william278.husktowns.user;

import io.papermc.lib.PaperLib;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.TextColor;
import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class BukkitUser extends OnlineUser {

    private final Player player;
    private static final Particle PARTICLE = getCompatibleParticle();

    private BukkitUser(@NotNull Player player, @NotNull HuskTowns plugin) {
        super(player.getUniqueId(), player.getName(), plugin);
        this.player = player;
    }

    @NotNull
    public static BukkitUser adapt(@NotNull Player player, @NotNull HuskTowns plugin) {
        return new BukkitUser(player, plugin);
    }

    @Override
    @NotNull
    public Chunk getChunk() {
        return Chunk.at(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());
    }

    @Override
    @NotNull
    public Position getPosition() {
        return Position.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(),
            getWorld(), player.getLocation().getYaw(), player.getLocation().getPitch());
    }

    @Override
    @NotNull
    public World getWorld() {
        return World.of(player.getWorld().getUID(), player.getWorld().getName(),
            player.getWorld().getEnvironment().name().toLowerCase());
    }

    @Override
    public void sendPluginMessage(@NotNull String channel, byte[] message) {
        plugin.runSyncDelayed(
            () -> player.sendPluginMessage((BukkitHuskTowns) plugin, channel, message), this, 5L
        );
    }

    @Override
    public boolean isSneaking() {
        return player.isSneaking();
    }

    @Override
    public void spawnMarkerParticle(@NotNull Position position, @NotNull TextColor color, int count) {
        if (PARTICLE == null) {
            return;
        }
        player.spawnParticle(
            PARTICLE,
            new Location(player.getWorld(), position.getX(), position.getY() + 1.1d, position.getZ()),
            1,
            new Particle.DustOptions(org.bukkit.Color.fromRGB(color.red(), color.green(), color.blue()), 1)
        );
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private static Particle getCompatibleParticle() {
        try {
            return Particle.REDSTONE;
        } catch (NoSuchFieldError e) {
            try {
                return (Particle) Particle.class.getField("DUST").get(null);
            } catch (Throwable t) {
                return null;
            }
        }
    }

    @Override
    public void teleportTo(@NotNull Position position) {
        PaperLib.teleportAsync(player, new Location(Bukkit.getWorld(position.getWorld().getName()) == null
            ? Bukkit.getWorld(position.getWorld().getUuid())
            : Bukkit.getWorld(position.getWorld().getName()),
            position.getX(), position.getY(), position.getZ(), position.getYaw(), position.getPitch()));
    }

    @Override
    public void giveExperiencePoints(int quantity) {
        player.giveExp(quantity);
    }

    @Override
    public void giveExperienceLevels(int quantity) {
        player.giveExpLevels(quantity);
    }

    @Override
    public void giveItem(@NotNull Key material, int quantity) {
        final Material materialType = Material.matchMaterial(material.asString());
        if (materialType == null) {
            throw new IllegalArgumentException("Invalid material type: " + material.asString());
        }

        // Give the player the item(s); drop excess on the ground
        final ItemStack stack = new ItemStack(materialType, quantity);
        if (!player.getInventory().addItem(stack).isEmpty()) {
            player.getWorld().dropItem(player.getLocation(), stack);
        }
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return player.hasPermission(permission);
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

}
