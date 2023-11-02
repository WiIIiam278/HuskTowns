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

package net.william278.husktowns.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.AbstractPagedGui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public abstract class PagedItemsGuiAbstract extends AbstractPagedGui<Item> {

    private List<Item> items;

    public PagedItemsGuiAbstract(int width, int height, boolean infinitePages, int contentLines) {
        super(width, height, infinitePages, generateContentListSlots(contentLines));
    }

    private static int[] generateContentListSlots(int contentLines) {
        int[] slots = new int[contentLines * 9];
        for (int i = 0; i < contentLines * 9; i++) {
            slots[i] = i;
        }
        return slots;
    }

    @Override
    public int getPageAmount() {
        return (int) Math.ceil((double) items.size() / (double) getContentListSlots().length);
    }

    @Override
    public void setContent(@Nullable List<@NotNull Item> items) {
        this.items = items != null ? items : new ArrayList<>();
        update();
    }
    @Override
    protected List<SlotElement> getPageElements(int page) {
        int length = getContentListSlots().length;
        int from = page * length;
        int to = Math.min(from + length, items.size());

        return items.subList(from, to).stream().map(SlotElement.ItemSlotElement::new).collect(Collectors.toList());
    }



}
