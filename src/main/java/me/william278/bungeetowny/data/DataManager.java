package me.william278.bungeetowny.data;

import me.william278.bungeetowny.HuskTowns;
import me.william278.bungeetowny.MessageManager;
import me.william278.bungeetowny.object.chunk.ClaimedChunk;
import me.william278.bungeetowny.object.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.logging.Level;

public class DataManager {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    /**
     * Update a player's name in SQL; create a new player if they don't exist
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
                PreparedStatement alreadyInTownCheck = connection.prepareStatement(
                        "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE uuid=?;");
                alreadyInTownCheck.setString(1, player.getUniqueId().toString());
                ResultSet alreadyInTownResult = alreadyInTownCheck.executeQuery();
                alreadyInTownCheck.close();
                if (alreadyInTownResult != null) {
                    alreadyInTownResult.next();
                    if (alreadyInTownResult.getInt("town_id") == 0) {
                        PreparedStatement existStatement = connection.prepareStatement(
                                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE name=?;");
                        existStatement.setString(1, townName);
                        ResultSet existResult = existStatement.executeQuery();
                        existStatement.close();
                        if (existResult != null) {
                            if (!existResult.next()) {
                                Town town = new Town(player, townName);
                                // Create player if they don't exist
                                PreparedStatement townCreationStatement = connection.prepareStatement(
                                        "INSERT INTO " + HuskTowns.getSettings().getTownsTable() + " (name,money,founded,greeting_message,farewell_message,spawn_location_id) VALUES(?,0,?,?,?,?);");
                                townCreationStatement.setString(1, town.getName());
                                townCreationStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                                townCreationStatement.setString(3, MessageManager.getRawMessage("default_greeting_message", town.getName()));
                                townCreationStatement.setString(4, MessageManager.getRawMessage("default_farewell_message", town.getName()));
                                townCreationStatement.setInt(5, 0); //todo Have this insert a new TeleportationPoint
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
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }
}
