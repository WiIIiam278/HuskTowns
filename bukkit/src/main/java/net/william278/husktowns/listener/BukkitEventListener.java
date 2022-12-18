package net.william278.husktowns.listener;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class BukkitEventListener extends EventListener implements Listener {

    public BukkitEventListener(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e) {
        super.onPlayerJoin(BukkitUser.adapt(e.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent e) {
        super.onPlayerQuit(BukkitUser.adapt(e.getPlayer()));
    }

}
