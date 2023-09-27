package dev.unnm3d.kalyaclaims.gui.deeds;

import net.william278.husktowns.user.OnlineUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.inventoryaccess.component.ComponentWrapper;
import xyz.xenondevs.invui.window.AbstractMergedWindow;

import java.util.Objects;

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
