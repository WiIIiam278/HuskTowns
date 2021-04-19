package me.william278.bungeetowny.listeners;

import me.william278.bungeetowny.HuskTowns;
import me.william278.bungeetowny.data.DataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // Synchronise mySQL player data
        DataManager.updatePlayerData(e.getPlayer());
    }
}
