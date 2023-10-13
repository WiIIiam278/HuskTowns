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

package net.william278.husktowns.gui.deeds;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.claim.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.AnvilWindow;

public class TrustButton extends AbstractItem {

    private final DeedsGui deedsGui;
    private final BukkitHuskTowns plugin;

    public TrustButton(DeedsGui deedsGui, BukkitHuskTowns plugin) {
        this.deedsGui = deedsGui;
        this.plugin = plugin;
    }

    @Override
    public ItemProvider getItemProvider() {
        if (deedsGui.selectedDeed.townClaim != null && deedsGui.selectedDeed.townClaim.claim().getType() == Claim.Type.PLOT) {
            return new ItemBuilder(Material.PAPER).setDisplayName("§bTrust player");
        } else
            return new ItemBuilder(Material.AIR);
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        AnvilWindow.single()
                .setGui(Gui.normal()
                        .setStructure("x x x")
                        .addIngredient('x', new ConfirmTrustButton(deedsGui, plugin)))
                .setTitle("InvUI")
                .open(player);
    }
}
