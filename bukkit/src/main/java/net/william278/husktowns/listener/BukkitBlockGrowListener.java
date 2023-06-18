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

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Position;
import org.bukkit.Bukkit;
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
