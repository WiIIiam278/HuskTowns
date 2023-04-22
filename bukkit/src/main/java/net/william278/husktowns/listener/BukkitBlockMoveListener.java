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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.jetbrains.annotations.NotNull;

public interface BukkitBlockMoveListener extends BukkitListener {

    // Stop fluids from entering claims
    @EventHandler(ignoreCancelled = true)
    default void onBlockFromTo(@NotNull BlockFromToEvent e) {
        final Material material = e.getBlock().getType();
        if (material == Material.LAVA || material == Material.WATER) {
            final Position blockPosition = getPosition(e.getBlock().getLocation());
            if (getListener().handler().cancelNature(blockPosition.getChunk(),
                    getPosition(e.getToBlock().getLocation()).getChunk(),
                    blockPosition.getWorld())) {
                e.setCancelled(true);
            }
        }
    }

    // Stop people from pushing blocks into claims
    @EventHandler(ignoreCancelled = true)
    default void onPistonPush(@NotNull BlockPistonExtendEvent e) {
        final Position pistonLocation = getPosition(e.getBlock().getLocation());
        for (final Block pushedBlock : e.getBlocks()) {
            if (getListener().handler().cancelNature(pistonLocation.getChunk(),
                    getPosition(pushedBlock.getLocation()).getChunk(),
                    pistonLocation.getWorld())) {
                e.setCancelled(true);
                return;
            }
        }
    }


    // Stop people from pulling blocks from claims
    @EventHandler(ignoreCancelled = true)
    default void onPistonPull(@NotNull BlockPistonRetractEvent e) {
        final Position pistonLocation = getPosition(e.getBlock().getLocation());
        for (final Block pushedBlock : e.getBlocks()) {
            if (getListener().handler().cancelNature(pistonLocation.getChunk(),
                    getPosition(pushedBlock.getLocation()).getChunk(),
                    pistonLocation.getWorld())) {
                e.setCancelled(true);
                return;
            }
        }
    }

}
