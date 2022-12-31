package net.william278.husktowns.listener;

import net.william278.husktowns.user.BukkitUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.jetbrains.annotations.NotNull;

public interface BukkitPlaceListener extends BukkitListener {

    @EventHandler(ignoreCancelled = true)
    default void onPlayerPlaceBlock(@NotNull BlockPlaceEvent e) {
        if (getHandler().cancelOperation(Operation.of(
                BukkitUser.adapt(e.getPlayer()),
                getPlugin().getSpecialTypes().isCropBlock(e.getBlock().getType().getKey().toString())
                        ? Operation.Type.FARM_BLOCK_PLACE : Operation.Type.BLOCK_PLACE,
                getPosition(e.getBlock().getLocation()))
        )) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    default void onPlayerEmptyBucket(@NotNull PlayerBucketEmptyEvent e) {
        if (getHandler().cancelOperation(Operation.of(
                BukkitUser.adapt(e.getPlayer()),
                Operation.Type.EMPTY_BUCKET,
                getPosition(e.getBlockClicked().getLocation()))
        )) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    default void onPlayerPlaceHangingEntity(@NotNull HangingPlaceEvent e) {
        if (e.getPlayer() == null) {
            return;
        }
        if (getHandler().cancelOperation(Operation.of(
                BukkitUser.adapt(e.getPlayer()),
                Operation.Type.PLACE_HANGING_ENTITY,
                getPosition(e.getEntity().getLocation()))
        )) {
            e.setCancelled(true);
        }
    }

}
