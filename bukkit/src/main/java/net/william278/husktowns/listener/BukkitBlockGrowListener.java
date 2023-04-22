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
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockGrowEvent;
import org.jetbrains.annotations.NotNull;

public interface BukkitBlockGrowListener extends BukkitListener {

    // Boosted crop growth in farms
    @EventHandler(ignoreCancelled = true)
    default void onBlockGrow(@NotNull BlockGrowEvent e) {
        if (!(e.getBlock().getBlockData() instanceof Ageable ageable) || ageable.getAge() >= ageable.getMaximumAge()) {
            return;
        }

        final Position position = getPosition(e.getBlock().getLocation());
        getPlugin().getClaimAt(position).ifPresent(claim -> {
            if (claim.claim().getType() != Claim.Type.FARM) {
                return;
            }

            final double chance = claim.town().getCropGrowthRate(getPlugin());
            if (doBoostRate(chance)) {
                ageable.setAge(Math.min(ageable.getAge() + 2, ageable.getMaximumAge()));
                e.setCancelled(true);
            }
        });
    }

}
