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

package net.william278.husktowns.listener;

import net.william278.husktowns.claim.Position;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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
                final Optional<Town> damagedTown = getPlugin().getUserTown(BukkitUser.adapt(damaged.get())).map(Member::town);
                if (!getPlugin().getSettings().doAllowFriendlyFire() && damagedTown.isPresent()) {
                    final boolean townsMatch = getPlugin().getUserTown(damagingUser).map(Member::town).equals(damagedTown);
                    if (townsMatch) {
                        e.setCancelled(true);
                        return;
                    }
                }

                if (getListener().handler().cancelOperation(Operation.of(
                        damagingUser,
                        Operation.Type.PLAYER_DAMAGE_PLAYER,
                        getPosition(damaged.get().getLocation())
                ))) {
                    e.setCancelled(true);
                }
                return;
            }
            if (getListener().handler().cancelOperation(Operation.of(
                    BukkitUser.adapt(damaging.get()),
                    (e.getEntity().isPersistent() || e.getEntity().getCustomName() != null)
                            ? Operation.Type.PLAYER_DAMAGE_PERSISTENT_ENTITY
                            : (e.getEntity() instanceof Monster ? Operation.Type.PLAYER_DAMAGE_MONSTER
                            : Operation.Type.PLAYER_DAMAGE_ENTITY),
                    getPosition(e.getEntity().getLocation())
            ))) {
                e.setCancelled(true);
            }
            return;
        }

        if (e.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof BlockProjectileSource shooter) {
            final Position blockLocation = getPosition(shooter.getBlock().getLocation());
            if (getListener().handler().cancelNature(blockLocation.getChunk(), getPosition(e.getEntity().getLocation()).getChunk(),
                    blockLocation.getWorld())) {
                e.setCancelled(true);
            }
            return;
        }

        final EntityDamageEvent.DamageCause cause = e.getCause();
        if (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            if (!(e.getEntity() instanceof Monster)) {
                if (getListener().handler().cancelOperation(Operation.of(
                        Operation.Type.EXPLOSION_DAMAGE_ENTITY,
                        getPosition(e.getEntity().getLocation())
                ))) {
                    e.setCancelled(true);
                }
            }
        }
    }

}
