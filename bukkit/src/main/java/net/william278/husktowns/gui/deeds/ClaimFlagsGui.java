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

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Flag;
import net.william278.husktowns.gui.BukkitGuiManager;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class ClaimFlagsGui {
    private Gui gui;

    public ClaimFlagsGui(OnlineUser player, @Nullable Town town, BukkitGuiManager guiManager) {
        if (town == null)
            return;
        String[] structure = guiManager.getGuiSettings().getClaimFlagsGuiSettings().structure();
        Gui.Builder.Normal guiBuilder = Gui.normal();
        if (town.getLevel() == 1) {
            guiBuilder.setStructure(structure[0]);
        } else {
            guiBuilder.setStructure(structure[0],
                    structure[1],
                    structure[2]);
        }
        town.getRules().forEach((claimType, flagMap) ->
                flagMap.getFlagMap(guiManager.getPlugin().getFlags())
                        .forEach((flag, value) -> {
                            ClaimFlagsGui.PlotFlagItem pfi = new ClaimFlagsGui.PlotFlagItem(player, flag, claimType, value, guiManager);
                            guiBuilder.addIngredient(pfi.getGuiChar(), pfi);
                        }));
        gui = guiBuilder.build();
    }

    public Gui getGui() {
        return gui;
    }


    private static class PlotFlagItem extends AbstractItem {
        private final OnlineUser user;
        private final Flag flag;
        private final Claim.Type type;
        private boolean value;
        private final BukkitGuiManager guiManager;

        public PlotFlagItem(OnlineUser user, Flag flag, Claim.Type type, boolean value, BukkitGuiManager guiManager) {
            this.user = user;
            this.flag = flag;
            this.type = type;
            this.value = value;
            this.guiManager = guiManager;
        }

        @Override
        public ItemProvider getItemProvider() {
            return guiManager.getGuiSettings().getClaimFlagsGuiSettings()
                    .getItem(flag.getName())
                    .toItemProvider("%status%", this.value ? "§aEnabled" : "§cDisabled");
        }

        public char getGuiChar() {
            int i = switch (type) {
                case PLOT -> 97;
                case CLAIM -> 9 + 97;
                case FARM -> 2 * 9 + 97;
            };
            switch (flag.getName()) {
                case "pvp" -> {
                }
                case "explosion_damage" -> i = i + 1;
                case "public_container_access" -> i = i + 2;
                case "public_build_access" -> i = i + 3;
                case "public_farm_access" -> i = i + 4;
                case "public_interact_access" -> i = i + 5;
                case "mob_griefing" -> i = i + 6;
                case "monster_spawning" -> i = i + 7;
                case "fire_damage" -> i = i + 8;
            }
            return (char) i;
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            this.value = !this.value;
            guiManager.getPlugin().getManager().towns().setFlagRule(user, flag, type, this.value, false);
            notifyWindows();
        }
    }
}
