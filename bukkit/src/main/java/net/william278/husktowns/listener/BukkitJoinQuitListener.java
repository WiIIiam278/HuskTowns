package net.william278.husktowns.listener;

import net.william278.husktowns.user.BukkitUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public interface BukkitJoinQuitListener extends BukkitListener {

    @EventHandler(ignoreCancelled = true)
    default void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        getHandler().onPlayerJoin(BukkitUser.adapt(e.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true)
    default void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        getHandler().onPlayerQuit(BukkitUser.adapt(e.getPlayer()));
    }

}
