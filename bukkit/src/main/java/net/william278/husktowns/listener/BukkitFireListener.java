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

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.jetbrains.annotations.NotNull;

public interface BukkitFireListener extends BukkitListener {

    @EventHandler(ignoreCancelled = true)
    default void onBlockSpread(@NotNull BlockSpreadEvent e) {
        if (e.getSource().getType() == Material.FIRE) {
            if (getListener().handler().cancelOperation(Operation.of(
                    Operation.Type.FIRE_SPREAD,
                    getPosition(e.getBlock().getLocation())))) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    default void onBlockBurn(@NotNull BlockBurnEvent e) {
        if (getListener().handler().cancelOperation(Operation.of(
                Operation.Type.FIRE_BURN,
                getPosition(e.getBlock().getLocation())))) {
            e.setCancelled(true);
        }
    }


}
