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
