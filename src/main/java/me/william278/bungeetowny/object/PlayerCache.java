package me.william278.bungeetowny.object;

import me.william278.bungeetowny.data.DataManager;
import me.william278.bungeetowny.object.town.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class manages a cache of all online players and the town they are in and their role in that town.
 * without pulling data from SQL every time a player mines a block.
 *
 * It is pulled when the player joins the server and updated when they join a town or change roles
 * It is removed when the player leaves the server

 */
public class PlayerCache {

    private final HashMap<UUID,String> playerTowns;
    private final HashMap<UUID, TownRole> playerRoles;

    public PlayerCache() {
        playerTowns = new HashMap<>();
        playerRoles = new HashMap<>();
    }

    public void reload() {
        playerRoles.clear();
        playerTowns.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            DataManager.updatePlayerCachedData(player);
        }
    }

    public void addPlayer(UUID uuid, String townName, TownRole townRole) {
        playerTowns.put(uuid, townName);
        playerRoles.put(uuid, townRole);
    }

    public void removePlayer(UUID uuid) {
        playerRoles.remove(uuid);
        playerTowns.remove(uuid);
    }

    public void setPlayerRole(UUID uuid, TownRole townRole) {
        playerRoles.put(uuid, townRole);
    }

    public void setPlayerTown(UUID uuid, String townName) {
        playerTowns.put(uuid, townName);
    }

    public String getTown(UUID uuid) {
        return playerTowns.get(uuid);
    }

    public TownRole getRole(UUID uuid) {
        return playerRoles.get(uuid);
    }
}
