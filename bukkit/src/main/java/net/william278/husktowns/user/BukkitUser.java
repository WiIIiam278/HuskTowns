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

import io.papermc.lib.PaperLib;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.william278.husktowns.BukkitHuskTowns;
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

import java.awt.*;

public final class BukkitUser extends OnlineUser {

    private static final BukkitHuskTowns plugin = BukkitHuskTowns.getInstance();
    private final Player player;

    private BukkitUser(@NotNull Player player) {
        super(player.getUniqueId(), player.getName());
        this.player = player;
    }

    @NotNull
    public static BukkitUser adapt(@NotNull Player player) {
        return new BukkitUser(player);
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
        Bukkit.getScheduler().runTaskLater(plugin,
                () -> player.sendPluginMessage(BukkitHuskTowns.getInstance(), channel, message), 5L);
    }

    @Override
    public void spawnMarkerParticle(@NotNull Position position, @NotNull Color color, int count) {
        player.spawnParticle(Particle.REDSTONE, new Location(player.getWorld(), position.getX(), position.getY() + 1.1d, position.getZ()),
                1, new Particle.DustOptions(org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue()), 1));
    }

    @Override
    @NotNull
    public Audience getAudience() {
        return plugin.getAudiences().player(player);
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

    @Override
    public void setFlying(boolean flying) {
        player.setAllowFlight(flying);
        player.setFlying(flying);
    }

}
