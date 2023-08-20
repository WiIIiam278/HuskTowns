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

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Position;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockGrowEvent;
import org.jetbrains.annotations.NotNull;

public interface BukkitBlockGrowListener extends BukkitListener {

    // Boosted crop growth in farms
    @EventHandler(ignoreCancelled = true)
    default void onBlockGrow(@NotNull BlockGrowEvent e) {
        if (!(e.getNewState().getBlockData() instanceof Ageable ageable) || ageable.getAge() >= ageable.getMaximumAge()) {
            return;
        }

        final Block block = e.getBlock();
        final Position position = getPosition(block.getLocation());
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

}
