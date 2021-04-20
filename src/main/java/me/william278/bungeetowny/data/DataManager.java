package me.william278.bungeetowny.data;

import me.william278.bungeetowny.HuskTowns;
import me.william278.bungeetowny.MessageManager;
import me.william278.bungeetowny.object.chunk.ClaimedChunk;
import me.william278.bungeetowny.object.town.Town;
import me.william278.bungeetowny.object.town.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.logging.Level;

public class DataManager {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    /**
     * Update a player's name in SQL; create a new player if they don't exist
     *
     * @param player the Player to update
     */
    public static void updatePlayerData(Player player) {
        final String playerUUID = player.getUniqueId().toString();
        final String playerName = player.getName();

        Connection connection = HuskTowns.getConnection();

        // Check if the player exists
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement existStatement = connection.prepareStatement(
                        "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE uuid=?;");
                existStatement.setString(1, playerUUID);
                ResultSet resultSet = existStatement.executeQuery();
                existStatement.close();
                if (resultSet != null) {
                    if (!resultSet.next()) {
                        // Create player if they don't exist
                        PreparedStatement playerCreationStatement = connection.prepareStatement(
                                "INSERT INTO " + HuskTowns.getSettings().getPlayerTable() + " (username,uuid,is_teleporting) VALUES(?,?,0);");
                        playerCreationStatement.setString(1, playerName);
                        playerCreationStatement.setString(2, playerUUID);
                        playerCreationStatement.executeUpdate();
                        playerCreationStatement.close();
                    } else {
                        // Update player username
                        PreparedStatement usernameUpdateStatement = connection.prepareStatement(
                                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `username`=? WHERE `uuid`=?;");
                        usernameUpdateStatement.setString(1, playerName);
                        usernameUpdateStatement.executeUpdate();
                        usernameUpdateStatement.close();
                    }
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void createTown(Player player, String townName) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                    if (!inTown(player, connection)) {
                        PreparedStatement existStatement = connection.prepareStatement(
                                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE name=?;");
                        existStatement.setString(1, townName);
                        ResultSet existResult = existStatement.executeQuery();
                        existStatement.close();
                        if (existResult != null) {
                            if (!existResult.next()) {
                                Town town = new Town(player, townName);
                                PreparedStatement townCreationStatement = connection.prepareStatement(
                                        "INSERT INTO " + HuskTowns.getSettings().getTownsTable() + " (name,money,founded,greeting_message,farewell_message) VALUES(?,0,?,?,?);");
                                townCreationStatement.setString(1, town.getName());
                                townCreationStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                                townCreationStatement.setString(3, MessageManager.getRawMessage("default_greeting_message", town.getName()));
                                townCreationStatement.setString(4, MessageManager.getRawMessage("default_farewell_message", town.getName()));
                                townCreationStatement.executeUpdate();
                                townCreationStatement.close();
                                MessageManager.sendMessage(player, "town_creation_success", town.getName());
                            } else {
                                MessageManager.sendMessage(player, "error_town_already_exists");
                            }
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_already_in_town");
                    }

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
                        return TownRole.NONE;
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
                if (role == TownRole.NONE || role == TownRole.RESIDENT) {
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
}
