package me.william278.bungeetowny.data;

import me.william278.bungeetowny.HuskTowns;
import me.william278.bungeetowny.MessageManager;
import me.william278.bungeetowny.object.chunk.ChunkType;
import me.william278.bungeetowny.object.chunk.ClaimedChunk;
import me.william278.bungeetowny.object.town.Town;
import me.william278.bungeetowny.object.town.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

public class DataManager {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    // Add a new player
    private static void createPlayer(String playerName, String playerUUID, Connection connection) throws SQLException {
        PreparedStatement playerCreationStatement = connection.prepareStatement(
                "INSERT INTO " + HuskTowns.getSettings().getPlayerTable() + " (username,uuid,is_teleporting) VALUES(?,?,0);");
        playerCreationStatement.setString(1, playerName);
        playerCreationStatement.setString(2, playerUUID);
        playerCreationStatement.executeUpdate();
        playerCreationStatement.close();
    }

    // Update a player username
    private static void updatePlayerName(String playerUUID, String playerName, Connection connection) throws SQLException {
        PreparedStatement usernameUpdateStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `username`=? WHERE `uuid`=?;");
        usernameUpdateStatement.setString(1, playerName);
        usernameUpdateStatement.executeUpdate();
        usernameUpdateStatement.close();
    }

    // Returns true if a player exists
    private static boolean playerExists(String playerUUID, Connection connection) throws SQLException {
        PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE uuid=?;");
        existStatement.setString(1, playerUUID);
        ResultSet resultSet = existStatement.executeQuery();
        existStatement.close();
        if (resultSet != null) {
            return resultSet.next();
        }
        return false;
    }

    /**
     * Update a player's name in SQL; create a new player if they don't exist
     *
     * @param player the Player to update
     */
    public static void updatePlayerData(Player player) {
        final String playerUUID = player.getUniqueId().toString();
        final String playerName = player.getName();

        Connection connection = HuskTowns.getConnection();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Add a player to the Database if they don't exist; otherwise update their username
                if (!playerExists(playerUUID, connection)) {
                    createPlayer(playerName, playerUUID, connection);
                } else {
                    updatePlayerName(playerUUID, playerName, connection);
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    // Returns if a town with that name already exists
    private static boolean townExists(String townName, Connection connection) throws SQLException {
        PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE name=?;");
        existStatement.setString(1, townName);
        ResultSet existResult = existStatement.executeQuery();
        existStatement.close();
        if (existResult != null) {
            return existResult.next();
        }
        return false;
    }

    // Add town data to SQL
    private static void addTownData(Town town, Connection connection) throws SQLException {
        PreparedStatement townCreationStatement = connection.prepareStatement(
                "INSERT INTO " + HuskTowns.getSettings().getTownsTable() + " (name,money,founded,greeting_message,farewell_message) VALUES(?,0,?,?,?);");
        townCreationStatement.setString(1, town.getName());
        townCreationStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        townCreationStatement.setString(3, MessageManager.getRawMessage("default_greeting_message", town.getName()));
        townCreationStatement.setString(4, MessageManager.getRawMessage("default_farewell_message", town.getName()));
        townCreationStatement.executeUpdate();
        townCreationStatement.close();
    }

    public static void createTown(Player player, String townName) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Check that the player is not already in a town
                if (inTown(player, connection)) {
                    MessageManager.sendMessage(player, "error_already_in_town");
                    return;
                }
                // Check that the town does not exist
                if (townExists(townName, connection)) {
                    MessageManager.sendMessage(player, "error_town_already_exists");
                    return;
                }
                // Insert the town into the database
                Town town = new Town(player, townName);
                addTownData(town, connection);
                MessageManager.sendMessage(player, "town_creation_success", town.getName());

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    private static TownRole getTownRole(Player player, Connection connection) throws SQLException {
        PreparedStatement getTownRole = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE uuid=?;");
        getTownRole.setString(1, player.getUniqueId().toString());
        ResultSet townRoleResults = getTownRole.executeQuery();
        getTownRole.close();
        if (townRoleResults != null) {
            if (townRoleResults.next()) {
                switch (townRoleResults.getInt("town_role")) {
                    case 0:
                        return null;
                    case 1:
                        return TownRole.RESIDENT;
                    case 2:
                        return TownRole.TRUSTED;
                    case 3:
                        return TownRole.MAYOR;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    // Returns if a player is in a town
    private static boolean inTown(Player player, Connection connection) throws SQLException {
        PreparedStatement alreadyInTownCheck = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE uuid=?;");
        alreadyInTownCheck.setString(1, player.getUniqueId().toString());
        ResultSet alreadyInTownResult = alreadyInTownCheck.executeQuery();
        alreadyInTownCheck.close();
        if (alreadyInTownResult != null) {
            alreadyInTownResult.next();
            return alreadyInTownResult.getInt("town_id") == 0;
        }
        return false;
    }

    // Returns if a chunk is claimed
    private static boolean isClaimed(String server, String worldName, int chunkX, int chunkZ, Connection connection) throws SQLException {
        PreparedStatement checkClaimed = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE chunk_x=? AND chunk_z=? AND world=? AND server=?;");
        checkClaimed.setInt(1, chunkX);
        checkClaimed.setInt(2, chunkZ);
        checkClaimed.setString(3, worldName);
        checkClaimed.setString(4, server);
        ResultSet checkClaimedResult = checkClaimed.executeQuery();
        checkClaimed.close();

        if (checkClaimedResult != null) {
            return checkClaimedResult.next();
        }
        return false;
    }

    private static void addClaim(ClaimedChunk chunk, Connection connection) throws SQLException {
        PreparedStatement claimCreationStatement = connection.prepareStatement(
                "INSERT INTO " + HuskTowns.getSettings().getClaimsTable() + " (town_id,claim_time,claimer_id,server,world,chunk_x,chunk_z,chunk_type) VALUES((SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE uuid=?),?,?,?,?,?,?,0);");
        claimCreationStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        claimCreationStatement.setString(2, chunk.getClaimerUUID().toString());
        claimCreationStatement.setString(3, chunk.getServer());
        claimCreationStatement.setString(4, chunk.getWorldName());
        claimCreationStatement.setInt(5, chunk.getChunkX());
        claimCreationStatement.setInt(6, chunk.getChunkZ());
        claimCreationStatement.executeUpdate();
        claimCreationStatement.close();
    }

    public static void claimChunk(Player player) {
        Connection connection = HuskTowns.getConnection();
        ClaimedChunk chunk = new ClaimedChunk(player);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (inTown(player, connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }

                if (isClaimed(chunk.getServer(), chunk.getWorldName(), chunk.getChunkX(), chunk.getChunkZ(), connection)) {
                    MessageManager.sendMessage(player, "error_already_claimed");
                    return;
                }

                TownRole role = getTownRole(player, connection);
                if (role == TownRole.RESIDENT) {
                    MessageManager.sendMessage(player, "error_insufficient_claim_privileges");
                    return;
                }

                addClaim(chunk, connection);
                MessageManager.sendMessage(player, "claim_success", Integer.toString(chunk.getChunkX()), Integer.toString(chunk.getChunkZ()));

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    private static UUID getPlayerUUID(int playerID, Connection connection) throws SQLException {
        if (playerID == 0) {
            return null;
        }
        PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE id=?;");
        existStatement.setInt(1, playerID);
        ResultSet resultSet = existStatement.executeQuery();
        existStatement.close();
        if (resultSet != null) {
            String userUUID = resultSet.getString("uuid");
            if (userUUID == null) {
                return null;
            } else {
                return UUID.fromString(userUUID);
            }
        }
        return null;
    }

    private static ChunkType getChunkType(int chunkTypeID) {
        switch (chunkTypeID) {
            case 0:
                return ChunkType.REGULAR;
            case 1:
                return ChunkType.FARM;
            case 2:
                return ChunkType.PLOT;
        }
        return null;
    }

    private static ClaimedChunk getClaimedChunk(String server, String worldName, int chunkX, int chunkZ, Connection connection) throws SQLException {
        PreparedStatement checkClaimed = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE chunk_x=? AND chunk_z=? AND world=? AND server=?;");
        checkClaimed.setInt(1, chunkX);
        checkClaimed.setInt(2, chunkZ);
        checkClaimed.setString(3, worldName);
        checkClaimed.setString(4, server);
        ResultSet checkClaimedResult = checkClaimed.executeQuery();
        checkClaimed.close();

        if (checkClaimedResult != null) {
            if (checkClaimedResult.next()) {
                ChunkType chunkType = getChunkType(checkClaimedResult.getInt("chunk_type"));
                if (chunkType == ChunkType.PLOT) {
                    return new ClaimedChunk(checkClaimedResult.getString("server"),
                            checkClaimedResult.getString("world"),
                            checkClaimedResult.getInt("chunk_x"),
                            checkClaimedResult.getInt("chunk_z"),
                            getPlayerUUID(checkClaimedResult.getInt("claimer_id"), connection),
                            chunkType,
                            getPlayerUUID(checkClaimedResult.getInt("plot_owner_id"), connection));
                } else {
                    return new ClaimedChunk(checkClaimedResult.getString("server"),
                            checkClaimedResult.getString("world"),
                            checkClaimedResult.getInt("chunk_x"),
                            checkClaimedResult.getInt("chunk_z"),
                            getPlayerUUID(checkClaimedResult.getInt("claimer_id"), connection),
                            chunkType);
                }
            }
        }
        return null;
    }

    // Returns a list of a town's chunks on ALL servers
    public static HashSet<ClaimedChunk> getClaimedChunks(String townName, Connection connection) throws SQLException {
        PreparedStatement getChunks = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE town_id=(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE name=?));");
        getChunks.setString(1, townName);
        ResultSet chunkResults = getChunks.executeQuery();
        HashSet<ClaimedChunk> chunks = new HashSet<>();
        while (chunkResults.next()) {
            ChunkType chunkType = getChunkType(chunkResults.getInt("chunk_type"));
            if (chunkType == ChunkType.PLOT) {
                chunks.add(new ClaimedChunk(chunkResults.getString("server"),
                        chunkResults.getString("world"),
                        chunkResults.getInt("chunk_x"),
                        chunkResults.getInt("chunk_z"),
                        getPlayerUUID(chunkResults.getInt("claimer_id"), connection),
                        chunkType,
                        getPlayerUUID(chunkResults.getInt("plot_owner_id"), connection)));
            } else {
                chunks.add(new ClaimedChunk(chunkResults.getString("server"),
                        chunkResults.getString("world"),
                        chunkResults.getInt("chunk_x"),
                        chunkResults.getInt("chunk_z"),
                        getPlayerUUID(chunkResults.getInt("claimer_id"), connection),
                        chunkType));
            }
        }
        getChunks.close();
        return chunks;
    }

    // Returns a list of a town's chunk on THIS server
    public static HashSet<ClaimedChunk> getServerChunks(String townName, Connection connection) throws SQLException {
        HashSet<ClaimedChunk> filteredChunks = new HashSet<>();
        HashSet<ClaimedChunk> chunks = getClaimedChunks(townName, connection);
        for (ClaimedChunk chunk : chunks) {
            if (chunk.getServer().equalsIgnoreCase(HuskTowns.getSettings().getServerID())) {
                filteredChunks.add(chunk);
            }
        }
        return filteredChunks;
    }
}