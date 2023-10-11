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
