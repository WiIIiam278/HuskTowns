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
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.Chunk;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.PortalCreateEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface BukkitPortalListener extends BukkitListener {

    @EventHandler(ignoreCancelled = true)
    default void onPlayerCreatePortal(@NotNull PortalCreateEvent e) {
        if (e.getReason() != PortalCreateEvent.CreateReason.NETHER_PAIR) {
            return;
        }

        // Get the list of locations in distinct chunks
        final List<Position> locations = new ArrayList<>();
        final List<Chunk> chunks = new ArrayList<>();
        for (BlockState state : e.getBlocks()) {
            if (chunks.contains(state.getChunk())) {
                continue;
            }
            chunks.add(state.getChunk());
            locations.add(getPosition(state.getLocation()));
        }

        // Cancel the portal creation operation, specifying the player if applicable
        final BukkitUser player = e.getEntity() instanceof Player ? BukkitUser.adapt((Player) e.getEntity()) : null;
        locations.forEach(position -> {
            if (getListener().handler().cancelOperation(Operation.of(
                    player,
                    Operation.Type.BLOCK_PLACE,
                    position)
            )) {
                e.setCancelled(true);
            }
        });
    }

}
