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
