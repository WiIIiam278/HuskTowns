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

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public interface BukkitMoveListener extends BukkitListener {

    @EventHandler(ignoreCancelled = true)
    default void onPlayerMove(@NotNull PlayerMoveEvent e) {
        final Location fromLocation = e.getFrom();
        final Location toLocation = e.getTo();
        if (toLocation == null) {
            return;
        }
        BukkitHuskTowns.getInstance().getScheduler().regionSpecificScheduler(fromLocation).run(
                () -> {
                    if (fromLocation.getChunk().equals(toLocation.getChunk())) {
                        return;
                    }
                    BukkitHuskTowns.getInstance().getScheduler().regionSpecificScheduler(toLocation).run(() -> {
                        if (getListener().handler().cancelChunkChange(BukkitUser.adapt(e.getPlayer()),
                                getPosition(fromLocation), getPosition(toLocation))) {
                            e.setCancelled(true);
                        }
                    });
                }
        );
    }

}
