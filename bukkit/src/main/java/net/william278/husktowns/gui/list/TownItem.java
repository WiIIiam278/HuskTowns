package net.william278.husktowns.gui.list;

import net.william278.husktowns.town.Town;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.List;

public class TownItem extends AbstractItem {
    private final Town town;

    public TownItem(Town town) {
        this.town = town;
    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(Material.BELL)
                .setDisplayName(town.getName())
                .setLegacyLore(List.of(
                        "§7" + town.getMembers().size() + " members",
                        "§7" + town.getClaimCount() + " plots",
                        "§7" + town.getMoney() + " coins",
                        "§7" + town.getSpawn().map(spawn -> "Spawn privacy: " + (spawn.isPublic() ? "public" : "private"))
                                .orElse("No spawn set")
                ));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

    }

}
