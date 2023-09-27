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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.inventoryaccess.component.ComponentWrapper;
import xyz.xenondevs.invui.window.AbstractMergedWindow;

public class DeedsWindow extends AbstractMergedWindow {

    private final DeedsGui deedsGui;

    public DeedsWindow(
            @NotNull Player player,
            @Nullable ComponentWrapper title,
            @NotNull DeedsGui gui
    ) {
        super(player, title, gui.getGui(), Bukkit.createInventory(null, 9 * 6), true);
        this.deedsGui = gui;
    }

    public void selectDeed(DeedsGui.DeedItem newDeedItem) {
        DeedsGui.DeedItem previousDeedItem = this.deedsGui.selectedDeed;
        this.deedsGui.selectedDeed = newDeedItem;
        if (previousDeedItem != null) {
            previousDeedItem.notifyWindows();
        }
        newDeedItem.notifyWindows();
    }
}
