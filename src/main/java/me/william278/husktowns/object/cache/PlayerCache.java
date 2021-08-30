package me.william278.husktowns.object.cache;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.town.Town;
import me.william278.husktowns.util.UpgradeUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 * This class manages a cache of all players and the town they are in and their role in that town.
 * without pulling data from SQL every time a player mines a block.
 * <p>
 * It is pulled when the player joins the server and updated when they join a town or change roles
 * It is removed when the player leaves the server
 */
public class PlayerCache extends Cache {

    private final HashMap<UUID, String> playerTowns;
    private final HashMap<UUID, Town.TownRole> playerRoles;
    private final HashMap<UUID, String> playerNames;

    public PlayerCache() {
        super("Player Data");
        playerTowns = new HashMap<>();
        playerRoles = new HashMap<>();
        playerNames = new HashMap<>();
    }

    public void reload() {
        if (UpgradeUtil.getIsUpgrading()) {
            return;
        }
        playerRoles.clear();
        playerTowns.clear();
        playerNames.clear();
        clearItemsLoaded();
        DataManager.updatePlayerCachedData();
    }

    public boolean isPlayerInTown(UUID uuid) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        return playerTowns.containsKey(uuid);
    }

    public void setPlayerRole(UUID uuid, Town.TownRole townRole) {
        playerRoles.put(uuid, townRole);
        incrementItemsLoaded();
    }

    public void setPlayerTown(UUID uuid, String townName) {
        playerTowns.put(uuid, townName);
        incrementItemsLoaded();
    }

    public void setPlayerName(UUID uuid, String username) {
        playerNames.put(uuid, username);
        incrementItemsLoaded();
    }

    public void clearPlayerTown(UUID uuid) {
        playerTowns.remove(uuid);
        decrementItemsLoaded();
    }

    public void clearPlayerRole(UUID uuid) {
        playerRoles.remove(uuid);
        decrementItemsLoaded();
    }

    // Number of players in a town
    public int getResidentCount() {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        return playerTowns.keySet().size();
    }

    public String getPlayerTown(UUID uuid) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        String town = playerTowns.get(uuid);
        if (HuskTowns.getSettings().isFallbackOnDatabaseIfCacheFailed() && town == null) {
            try {
                Town fetchedTown = DataManager.getPlayerTown(uuid, HuskTowns.getConnection());
                if (fetchedTown != null) {
                    return fetchedTown.getName();
                }
                return null;
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return town;
    }

    public Town.TownRole getPlayerRole(UUID uuid) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        Town.TownRole role = playerRoles.get(uuid);
        if (HuskTowns.getSettings().isFallbackOnDatabaseIfCacheFailed() && role == null) {
            try {
                return DataManager.getTownRole(uuid, HuskTowns.getConnection());
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return role;
    }

    public String getPlayerUsername(UUID uuid) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        String username = playerNames.get(uuid);
        if (HuskTowns.getSettings().isFallbackOnDatabaseIfCacheFailed() && username == null) {
            try {
                return DataManager.getPlayerName(uuid, HuskTowns.getConnection());
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return username;
    }

    public void renameReload(String oldName, String newName) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        HashSet<UUID> uuidsToUpdate = new HashSet<>();
        final HashMap<UUID, String> towns = playerTowns;
        for (UUID uuid : towns.keySet()) {
            if (towns.get(uuid).equals(oldName)) {
                uuidsToUpdate.add(uuid);
            }
        }
        for (UUID uuid : uuidsToUpdate) {
            playerTowns.remove(uuid);
            playerTowns.put(uuid, newName);
        }
    }

    public void disbandReload(String disbandingTown) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        HashSet<UUID> uuidsToUpdate = new HashSet<>();
        final HashMap<UUID, String> towns = playerTowns;
        for (UUID uuid : towns.keySet()) {
            String town = towns.get(uuid);
            if (town != null) {
                if (town.equals(disbandingTown)) {
                    uuidsToUpdate.add(uuid);
                }
            }
        }
        for (UUID uuid : uuidsToUpdate) {
            playerTowns.remove(uuid);
            decrementItemsLoaded();
        }
    }

    public HashSet<String> getPlayersInTown(String townName) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        final HashMap<UUID, String> towns = playerTowns;
        HashSet<String> playerUsernames = new HashSet<>();
        for (UUID uuid : towns.keySet()) {
            if (towns.get(uuid).equals(townName)) {
                playerUsernames.add(getPlayerUsername(uuid));
            }
        }
        if (HuskTowns.getSettings().isFallbackOnDatabaseIfCacheFailed() && playerUsernames.isEmpty()) {
            try {
                for (UUID uuid : DataManager.getTownFromName(townName, HuskTowns.getConnection()).getMembers().keySet()) {
                    playerUsernames.add(getPlayerUsername(uuid));
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return playerUsernames;
    }

    public HashSet<String> getTowns() throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        HashSet<String> towns = new HashSet<>(playerTowns.values());
        if (HuskTowns.getSettings().isFallbackOnDatabaseIfCacheFailed() && towns.isEmpty()) {
            try {
                final ArrayList<Town> townList = DataManager.getTowns(HuskTowns.getConnection(), "name", true);
                for (Town town : townList) {
                    towns.add(town.getName());
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return towns;
    }

    public UUID getUUID(String name) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        final HashMap<UUID, String> towns = playerNames;
        for (UUID uuid : towns.keySet()) {
            if (towns.get(uuid).equalsIgnoreCase(name)) {
                return uuid;
            }
        }
        if (HuskTowns.getSettings().isFallbackOnDatabaseIfCacheFailed()) {
            try {
                return DataManager.getPlayerUUID(name, HuskTowns.getConnection());
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
}
