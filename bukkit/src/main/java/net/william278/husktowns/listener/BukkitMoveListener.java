package net.william278.husktowns.listener;

import net.william278.husktowns.user.BukkitUser;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public interface BukkitMoveListener extends BukkitListener {

    @EventHandler(ignoreCancelled = true)
    default void onPlayerMove(@NotNull PlayerMoveEvent e) {
        final Location fromLocation = e.getFrom();
        final Location toLocation = e.getTo();
        if (toLocation == null) {
            return;
        }

        if (fromLocation.getChunk().equals(toLocation.getChunk())) {
            return;
        }
        if (getHandler().cancelChunkChange(BukkitUser.adapt(e.getPlayer()),
                getPosition(fromLocation), getPosition(toLocation))) {
            e.setCancelled(true);
        }
    }

}
