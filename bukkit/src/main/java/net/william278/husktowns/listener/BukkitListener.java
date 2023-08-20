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

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;

public interface BukkitListener extends Listener {
    @NotNull
    HuskTowns getPlugin();

    @NotNull
    EventListener getListener();

    @NotNull
    default Position getPosition(@NotNull Location location) {
        final World world = World.of(Objects.requireNonNull(location.getWorld()).getUID(),
                location.getWorld().getName(),
                location.getWorld().getEnvironment().name().toLowerCase());
        return Position.at(location.getX(), location.getY(), location.getZ(), world);
    }

    default Optional<Player> getPlayerSource(@Nullable Entity e) {
        if (e == null) {
            return Optional.empty();
        }
        if (e instanceof Player player) {
            return Optional.of(player);
        }
        if (e instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return Optional.of(player);
        }
        return e.getPassengers().stream()
                .filter(p -> p instanceof Player)
                .map(p -> (Player) p)
                .findFirst();
    }

    default boolean doBoostRate(double chance) {
        return new Random().nextDouble() <= Math.max(chance, 0);
    }

    default void spawnBoostParticles(@NotNull Location location) {
        if (!getPlugin().getSettings().doSpawnBoostParticles()) {
            return;
        }

        final String particleId = getPlugin().getSettings().getBoostParticle();
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

}
