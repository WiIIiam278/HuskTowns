package net.william278.husktowns.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.jetbrains.annotations.NotNull;

public interface BukkitFireListener extends BukkitListener {

    @EventHandler(ignoreCancelled = true)
    default void onBlockSpread(@NotNull BlockSpreadEvent e) {
        if (e.getSource().getType() == Material.FIRE) {
            if (getHandler().cancelOperation(Operation.of(
                    Operation.Type.FIRE_SPREAD,
                    getPosition(e.getBlock().getLocation())))) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    default void onBlockBurn(@NotNull BlockBurnEvent e) {
        if (getHandler().cancelOperation(Operation.of(
                Operation.Type.FIRE_BURN,
                getPosition(e.getBlock().getLocation())))) {
            e.setCancelled(true);
        }
    }


}
