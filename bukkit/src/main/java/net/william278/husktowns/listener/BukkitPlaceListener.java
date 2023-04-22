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

import net.william278.husktowns.user.BukkitUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.jetbrains.annotations.NotNull;

public interface BukkitPlaceListener extends BukkitListener {

    @EventHandler(ignoreCancelled = true)
    default void onPlayerPlaceBlock(@NotNull BlockPlaceEvent e) {
        if (getListener().handler().cancelOperation(Operation.of(
                BukkitUser.adapt(e.getPlayer()),
                getPlugin().getSpecialTypes().isFarmBlock(e.getBlock().getType().getKey().toString())
                        ? Operation.Type.FARM_BLOCK_PLACE : Operation.Type.BLOCK_PLACE,
                getPosition(e.getBlock().getLocation()))
        )) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    default void onPlayerEmptyBucket(@NotNull PlayerBucketEmptyEvent e) {
        if (getListener().handler().cancelOperation(Operation.of(
                BukkitUser.adapt(e.getPlayer()),
                Operation.Type.EMPTY_BUCKET,
                getPosition(e.getBlockClicked().getLocation()))
        )) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    default void onPlayerPlaceHangingEntity(@NotNull HangingPlaceEvent e) {
        if (e.getPlayer() == null) {
            return;
        }
        if (getListener().handler().cancelOperation(Operation.of(
                BukkitUser.adapt(e.getPlayer()),
                Operation.Type.PLACE_HANGING_ENTITY,
                getPosition(e.getEntity().getLocation()))
        )) {
            e.setCancelled(true);
        }
    }

}
