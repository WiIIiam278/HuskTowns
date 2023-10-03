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

import net.william278.husktowns.claim.Position;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface BukkitEntityDamageEvent extends BukkitListener {

    @EventHandler(ignoreCancelled = true)
    default void onEntityDamageEntity(@NotNull EntityDamageByEntityEvent e) {
        final Optional<Player> damaged = getPlayerSource(e.getEntity());
        final Optional<Player> damaging = getPlayerSource(e.getDamager());
        if (damaging.isPresent()) {
            if (damaged.isPresent()) {
                final BukkitUser damagingUser = BukkitUser.adapt(damaging.get());

                // Cancel PvP based on town relations
                final Optional<Town> optionalDamaged = getPlugin().getUserTown(BukkitUser.adapt(damaged.get())).map(Member::town);
                final Optional<Town> optionalDamager = getPlugin().getUserTown(damagingUser).map(Member::town);
                if (optionalDamaged.isPresent() && optionalDamager.isPresent()) {
                    final Town damagedTown = optionalDamaged.get();
                    final Town damagerTown = optionalDamager.get();

                    // Prevent friendly fire between members and allied towns
                    if (!getPlugin().getSettings().doAllowFriendlyFire()) {
                        if (damagerTown.equals(damagedTown) || damagerTown.areRelationsBilateral(
                                damagedTown, Town.Relation.ALLY)) {
                            e.setCancelled(true);
                            return;
                        }
                    }

                    // Allow PvP if the two towns are at war
                    if (getPlugin().getSettings().doTownWars() && damagedTown.isAtWarWith(damagerTown)) {
                        return;
                    }
                }

                // Cancel PvP based on claims
                if (getListener().handler().cancelOperation(Operation.of(
                        damagingUser,
                        Operation.Type.PLAYER_DAMAGE_PLAYER,
                        getPosition(damaged.get().getLocation())
                ))) {
                    e.setCancelled(true);
                }
                return;
            }

            // Determine the Operation type based on the entity being damaged
            if (getListener().handler().cancelOperation(Operation.of(
                    BukkitUser.adapt(damaging.get()),
                    getPlayerDamageType(e),
                    getPosition(e.getEntity().getLocation())
            ))) {
                e.setCancelled(true);
            }
            return;
        }

        if (e.getDamager() instanceof Projectile projectile) {
            // Prevent projectiles dispensed outside of claims from harming stuff in claims
            if (projectile.getShooter() instanceof BlockProjectileSource shooter) {
                final Position blockLocation = getPosition(shooter.getBlock().getLocation());
                if (getListener().handler().cancelNature(
                        blockLocation.getChunk(),
                        getPosition(e.getEntity().getLocation()).getChunk(),
                        blockLocation.getWorld())
                ) {
                    e.setCancelled(true);
                }
                return;
            }

            // Prevent projectiles shot by mobs from harming passive mobs, hanging entities & armor stands
            if (!(e.getEntity() instanceof Player || e.getEntity() instanceof Monster)
                    && projectile.getShooter() instanceof Monster) {
                if (getListener().handler().cancelOperation(Operation.of(
                        Operation.Type.MONSTER_DAMAGE_TERRAIN,
                        getPosition(e.getEntity().getLocation())
                ))) {
                    e.setCancelled(true);
                }
            }
            return;
        }

        // Protect against mobs being hurt by explosions
        final EntityDamageEvent.DamageCause cause = e.getCause();
        if (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                && !(e.getEntity() instanceof Monster)) {
            if (getListener().handler().cancelOperation(Operation.of(
                    Operation.Type.EXPLOSION_DAMAGE_ENTITY,
                    getPosition(e.getEntity().getLocation())
            ))) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    default void onPlayerDeath(@NotNull PlayerDeathEvent e) {
        getPlugin().getManager().wars().ifPresent(wars -> wars.handlePlayerDeath(BukkitUser.adapt(e.getEntity())));
    }

    @NotNull
    private static Operation.Type getPlayerDamageType(@NotNull EntityDamageByEntityEvent e) {
        Operation.Type type = Operation.Type.PLAYER_DAMAGE_ENTITY;
        if (e.getEntity() instanceof Monster) {
            type = Operation.Type.PLAYER_DAMAGE_MONSTER;
        } else if (e.getEntity() instanceof LivingEntity living && !living.getRemoveWhenFarAway()
                || e.getEntity().getCustomName() != null) {
            type = Operation.Type.PLAYER_DAMAGE_PERSISTENT_ENTITY;
        }
        return type;
    }

}
