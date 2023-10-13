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
package net.william278.husktowns.gui.census;

import net.william278.husktowns.gui.GuiSettings;
import net.william278.husktowns.gui.PagedItemsGuiAbstract;
import net.william278.husktowns.town.Member;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;
import xyz.xenondevs.invui.window.Window;

import java.util.List;

public class CensusGui extends PagedItemsGuiAbstract {

    public CensusGui(List<Member> memberList) {
        super(9, 4, true, 3);
        GuiSettings.SingleGuiSettings guiSettings = GuiSettings.getInstance().getCensusGuiSettings();
        applyStructure(new Structure(
                guiSettings.structure())
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL) // where paged items should be put
                .addIngredient('<', getPageButton(guiSettings, false))
                .addIngredient('>', getPageButton(guiSettings, true))
        );
        setContent(memberList.stream()
                .map(member -> (Item) new MemberItem(member))
                .toList());
    }

    public void open(Player player) {
        Window.single()
                .setTitle("Census")
                .setGui(this)
                .open(player);
    }

    public AbstractItem getPageButton(GuiSettings.SingleGuiSettings guiSettings, boolean forward) {
        return new PageItem(forward) {
            @Override
            public ItemProvider getItemProvider(PagedGui<?> gui) {
                return forward ?
                        guiSettings.getItem("forwardItem").toItemProvider() :
                        guiSettings.getItem("backItem").toItemProvider();
            }
        };
    }

}
