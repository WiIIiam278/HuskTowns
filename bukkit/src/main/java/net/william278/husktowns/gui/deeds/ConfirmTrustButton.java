package net.william278.husktowns.gui.deeds;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.user.User;
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


public class ConfirmTrustButton extends AbstractItem {

    private final BukkitHuskTowns plugin;
    private final DeedsGui deedsGui;

    public ConfirmTrustButton(DeedsGui deedsGui, BukkitHuskTowns plugin) {
        this.deedsGui = deedsGui;
        this.plugin = plugin;
    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).setDisplayName("§aTrust player");
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        getWindows().stream().map(w -> (AnvilWindow) w).findFirst().ifPresent(w -> {
            if (w.getRenameText() != null) {
                plugin.getManager().claims()
                        .addPlotMember(deedsGui.onlineUser, deedsGui.onlineUser.getWorld(),
                                deedsGui.selectedDeed.townClaim.claim().getChunk(),
                                w.getRenameText(), false);
                player.closeInventory();
            }
        });
    }

}




