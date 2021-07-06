package me.william278.husktowns.object.cache;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.town.TownRole;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

/**
 * This class manages a cache of all players and the town they are in and their role in that town.
 * without pulling data from SQL every time a player mines a block.
 *
 * It is pulled when the player joins the server and updated when they join a town or change roles
 * It is removed when the player leaves the server
 */
public class PlayerCache extends Cache {

    private final HashMap<UUID, String> playerTowns;
    private final HashMap<UUID, TownRole> playerRoles;
    private final HashMap<UUID, String> playerNames;

    public PlayerCache() {
        super("Player Data");
        playerTowns = new HashMap<>();
        playerRoles = new HashMap<>();
        playerNames = new HashMap<>();
        reload();
    }

    public void reload() {
        playerRoles.clear();
        playerTowns.clear();
        playerNames.clear();
        DataManager.updatePlayerCachedData();
    }

    public boolean isPlayerInTown(UUID uuid) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        return playerTowns.containsKey(uuid);
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

    public String getTown(UUID uuid) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        String town = playerTowns.get(uuid);;
        if (HuskTowns.getSettings().isFallbackOnDatabaseIfCacheFailed()) {
            if (town == null) {
                try {
                    town = DataManager.getPlayerTown(uuid, HuskTowns.getConnection()).getName();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            }
        }
        return town;
    }

    public TownRole getRole(UUID uuid) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        return playerRoles.get(uuid);
    }

    public String getUsername(UUID uuid) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        return playerNames.get(uuid);
    }

    public void renameReload(String oldName, String newName) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        HashSet<UUID> uuidsToUpdate = new HashSet<>();
        final HashMap<UUID,String> towns = playerTowns;
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
        final HashMap<UUID,String> towns = playerTowns;
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
        }
    }

    public HashSet<String> getPlayersInTown(String townName) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        final HashMap<UUID,String> towns = playerTowns;
        HashSet<String> playerUsernames = new HashSet<>();
        for (UUID uuid : towns.keySet()) {
            if (towns.get(uuid).equals(townName)) {
                playerUsernames.add(towns.get(uuid));
            }
        }
        return playerUsernames;
    }

    public HashSet<String> getTowns() throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        return new HashSet<>(playerTowns.values());
    }

    public UUID getUUID(String name) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        final HashMap<UUID,String> towns = playerTowns;
        for (UUID uuid : towns.keySet()) {
            if (towns.get(uuid).equalsIgnoreCase(name)) {
                return uuid;
            }
        }
        if (HuskTowns.getSettings().isFallbackOnDatabaseIfCacheFailed()) {
            return getPlayerUUIDFromName(name);
        }
        return null;
    }

    private UUID getPlayerUUIDFromName(String username) throws CacheNotLoadedException {
        if (getStatus() != CacheStatus.LOADED) {
            throw new CacheNotLoadedException(getIllegalAccessMessage());
        }
        try (PreparedStatement getUUIDStatement = HuskTowns.getConnection().prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE username=?;")) {
            getUUIDStatement.setString(1, username);
            ResultSet resultSet = getUUIDStatement.executeQuery();
            if (resultSet != null) {
                if (resultSet.next()) {
                    final String userUUID = resultSet.getString("uuid");
                    getUUIDStatement.close();
                    if (userUUID == null) {
                        return null;
                    } else {
                        return UUID.fromString(userUUID);
                    }
                }
            }
        } catch (SQLException e) {
            HuskTowns.getInstance().getLogger().log(Level.SEVERE, "An SQL exception occurred retrieving a player's UUID from the database", e);
        }
        return null;
    }
}
