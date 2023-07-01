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
