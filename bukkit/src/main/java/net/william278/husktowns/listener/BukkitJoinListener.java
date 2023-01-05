package net.william278.husktowns.listener;

import net.william278.husktowns.user.BukkitUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public interface BukkitJoinListener extends BukkitListener {

    @EventHandler(ignoreCancelled = true)
    default void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        getListener().onPlayerJoin(BukkitUser.adapt(e.getPlayer()));
    }

}
