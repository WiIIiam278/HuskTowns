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

package net.william278.husktowns.gui.list;

import net.william278.husktowns.gui.GuiSettings;
import net.william278.husktowns.town.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class TownItem extends AbstractItem {
    private final Town town;

    public TownItem(Town town) {
        this.town = town;
    }

    @Override
    public ItemProvider getItemProvider() {
        return GuiSettings.getInstance().getTownListGuiSettings().getItem("townItem")
                .toItemProvider(
                        "%town_name%", town.getName(),
                        "%town_members%", String.valueOf(town.getMembers().size()),
                        "%town_claims%", String.valueOf(town.getClaimCount()),
                        "%town_money%", String.valueOf(town.getMoney()),
                        "%town_privacy%", town.getSpawn().map(spawn -> spawn.isPublic() ? "public" : "private").orElse("No spawn set"),
                        "%town_level%", String.valueOf(town.getLevel())
                );
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

    }

}
