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

package net.william278.husktowns.listener;

import net.william278.cloplib.listener.BukkitOperationListener;
import net.william278.cloplib.operation.OperationPosition;
import net.william278.cloplib.operation.OperationUser;
import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;

public class BukkitListener extends BukkitOperationListener implements ClaimsListener, UserListener {

    private final BukkitHuskTowns plugin;

    public BukkitListener(@NotNull BukkitHuskTowns plugin) {
        super(plugin, plugin);
        this.plugin = plugin;
    }

    @Override
    public void register() {
        ClaimsListener.super.register();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Boosted spawner rates in farms
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onMobSpawnBoosted(@NotNull CreatureSpawnEvent e) {
        final Entity entity = e.getEntity();
        final Location location = e.getLocation();
        final CreatureSpawnEvent.SpawnReason reason = e.getSpawnReason();
        if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            getPlugin().getClaimAt((Position) getPosition(location)).ifPresent(claim -> {
                if (claim.claim().getType() != Claim.Type.FARM) {
                    return;
                }

                if (doBoostRate(claim.town().getMobSpawnerRate(getPlugin()) - 1)) {
                    entity.getWorld().spawnEntity(location, e.getEntityType());
                    spawnBoostParticles(location);
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        getPlugin().handlePlayerJoin(BukkitUser.adapt(e.getPlayer(), plugin));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        getPlugin().handlePlayerQuit(BukkitUser.adapt(e.getPlayer(), plugin));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(@NotNull AsyncPlayerChatEvent e) {
        if (getPlugin().handlePlayerChat(BukkitUser.adapt(e.getPlayer(), plugin), e.getMessage())) {
            e.setCancelled(true);
        }
    }

    // Boosted crop growth in farms
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockGrowBoosted(@NotNull BlockGrowEvent e) {
        if (!(e.getNewState().getBlockData() instanceof Ageable ageable) || ageable.getAge() >= ageable.getMaximumAge()) {
            return;
        }

        final Block block = e.getBlock();
        final Position position = (Position) getPosition(block.getLocation());
        getPlugin().getClaimAt(position).ifPresent(claim -> {
            if (claim.claim().getType() != Claim.Type.FARM) {
                return;
            }

            // If a boost occurs, increase the age by a total of 3 (1 from the event, 2 from the boost) & cancel event
            if (doBoostRate(claim.town().getCropGrowthRate(getPlugin()) - 1)) {
                e.setCancelled(true);
                ageable.setAge(Math.min(ageable.getAge() + 2, ageable.getMaximumAge()));
                block.setBlockData(ageable);
                spawnBoostParticles(block.getLocation().add(0.5d, 0.5d, 0.5d));
            }
        });
    }

    private boolean doBoostRate(double chance) {
        return new Random().nextDouble() <= Math.max(chance, 0);
    }

    private void spawnBoostParticles(@NotNull Location location) {
        if (!getPlugin().getSettings().getTowns().isSpawnBoostParticles()) {
            return;
        }

        final String particleId = getPlugin().getSettings().getTowns().getBoostParticle();
        assert location.getWorld() != null : "World was null when spawning boost particle";
        try {
            location.getWorld().spawnParticle(
                Particle.valueOf(particleId.toUpperCase(Locale.ENGLISH)), location,
                new Random().nextInt(8) + 8, 0.4, 0.4, 0.4
            );
        } catch (IllegalArgumentException e) {
            getPlugin().log(Level.WARNING, "Invalid boost particle ID (" + particleId + ") set in config.yml");
        }
    }

    @NotNull
    public OperationPosition getPosition(@NotNull Location location) {
        final org.bukkit.World world = Objects.requireNonNull(location.getWorld());
        return Position.at(
            location.getX(), location.getY(), location.getZ(),
            World.of(world.getUID(), world.getName(), world.getEnvironment().name()),
            location.getYaw(), location.getPitch()
        );
    }

    @NotNull
    public OperationUser getUser(@NotNull Player player) {
        return BukkitUser.adapt(player, plugin);
    }

    @Override
    public int getInspectionDistance() {
        return plugin.getSettings().getGeneral().getMaxInspectionDistance();
    }

    @Override
    public void setInspectionDistance(int i) {
        throw new UnsupportedOperationException("Cannot change inspection distance");
    }

    @Override
    @NotNull
    public HuskTowns getPlugin() {
        return plugin;
    }

}
