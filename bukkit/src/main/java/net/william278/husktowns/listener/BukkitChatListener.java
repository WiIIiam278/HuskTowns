package net.william278.husktowns.listener;

import net.william278.husktowns.user.BukkitUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public interface BukkitChatListener extends BukkitListener {

    @EventHandler(ignoreCancelled = true)
    default void onPlayerChat(@NotNull AsyncPlayerChatEvent e) {
        if (getHandler().handlePlayerChat(BukkitUser.adapt(e.getPlayer()), e.getMessage())) {
            e.setCancelled(true);
        }
    }

}