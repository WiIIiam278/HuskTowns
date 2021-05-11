package me.william278.husktowns.object.cache;

import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.town.TownRole;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 * This class manages a cache of all players and the town they are in and their role in that town.
 * without pulling data from SQL every time a player mines a block.
 *
 * It is pulled when the player joins the server and updated when they join a town or change roles
 * It is removed when the player leaves the server
 */
public class PlayerCache {

    private final HashMap<UUID,String> playerTowns;
    private final HashMap<UUID, TownRole> playerRoles;
    private final HashMap<UUID, String> playerNames;

    public PlayerCache() {
        playerTowns = new HashMap<>();
        playerRoles = new HashMap<>();
        playerNames = new HashMap<>();
    }

    public void reload() {
        playerRoles.clear();
        playerTowns.clear();
        playerNames.clear();
        DataManager.updatePlayerCachedData();
    }

    public boolean containsPlayer(UUID uuid) {
        return playerTowns.containsKey(uuid) && playerRoles.containsKey(uuid) && playerNames.containsKey(uuid);
    }

    public void addPlayer(UUID uuid, String username, String townName, TownRole townRole) {
        playerTowns.put(uuid, townName);
        playerRoles.put(uuid, townRole);
        playerNames.put(uuid, username);
    }

    public void removePlayer(UUID uuid) {
        playerRoles.remove(uuid);
        playerTowns.remove(uuid);
        playerNames.remove(uuid);
    }

    public void setPlayerRole(UUID uuid, TownRole townRole) {
        playerRoles.put(uuid, townRole);
    }

    public void setPlayerTown(UUID uuid, String townName) {
        playerTowns.put(uuid, townName);
    }

    public void setPlayerName(UUID uuid, String username) {
        playerNames.put(uuid, username);
    }

    public String getTown(UUID uuid) {
        return playerTowns.get(uuid);
    }

    public TownRole getRole(UUID uuid) {
        return playerRoles.get(uuid);
    }

    public String getUsername(UUID uuid) {
        return playerNames.get(uuid);
    }

    public HashSet<String> getPlayersInTown(String townName) {
        HashSet<String> playerUsernames = new HashSet<>();
        for (UUID uuid : playerTowns.keySet()) {
            if (playerTowns.get(uuid).equals(townName)) {
                playerUsernames.add(playerNames.get(uuid));
            }
        }
        return playerUsernames;
    }

    public HashSet<String> getTowns() {
        return new HashSet<>(playerTowns.values());
    }

    public UUID getUUID(String name) {
        for (UUID uuid : playerNames.keySet()) {
            if (playerNames.get(uuid).equalsIgnoreCase(name)) {
                return uuid;
            }
        }
        return null;
    }
}
