package net.william278.husktowns.gui.census;

import net.william278.husktowns.gui.PagedItemsGuiAbstract;
import net.william278.husktowns.town.Member;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;
import xyz.xenondevs.invui.window.Window;

import java.util.List;

public class CensusGui extends PagedItemsGuiAbstract {

    public CensusGui(List<Member> memberList) {
        super(9, 4, true, 3);
        applyStructure(new Structure(
                "xxxxxxxxx",
                "xxxxxxxxx",
                "xxxxxxxxx",
                "<##fff##>")
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL) // where paged items should be put
                .addIngredient('<', getPageButton(false))
                .addIngredient('>', getPageButton(true))
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

    public AbstractItem getPageButton(boolean forward) {
        return new PageItem(forward) {
            @Override
            public ItemProvider getItemProvider(PagedGui<?> gui) {
                return new ItemBuilder(Material.ARROW);
            }
        };
    }


}
