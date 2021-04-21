package me.william278.bungeetowny.data;

import de.themoep.minedown.MineDown;
import me.william278.bungeetowny.HuskTowns;
import me.william278.bungeetowny.MessageManager;
import me.william278.bungeetowny.object.chunk.ChunkType;
import me.william278.bungeetowny.object.chunk.ClaimedChunk;
import me.william278.bungeetowny.object.teleport.TeleportationPoint;
import me.william278.bungeetowny.object.town.Town;
import me.william278.bungeetowny.object.town.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashMap;
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
        usernameUpdateStatement.setString(2, playerUUID);
        usernameUpdateStatement.executeUpdate();
        usernameUpdateStatement.close();
    }

    private static String getPlayerName(UUID uuid, Connection connection) throws SQLException {
        PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE uuid=?;");
        existStatement.setString(1, uuid.toString());
        ResultSet resultSet = existStatement.executeQuery();
        if (resultSet != null) {
            return resultSet.getString("username");
        }
        existStatement.close();
        return null;
    }

    // Returns true if a player exists
    private static boolean playerExists(String playerUUID, Connection connection) throws SQLException {
        PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE uuid=?;");
        existStatement.setString(1, playerUUID);
        ResultSet resultSet = existStatement.executeQuery();
        if (resultSet != null) {
            return resultSet.next();
        }
        existStatement.close();
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

    private static Town getTownFromName(String townName, Connection connection) throws SQLException {
        PreparedStatement getTown = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?;");
        getTown.setString(1, townName);
        ResultSet townResults = getTown.executeQuery();
        if (townResults != null) {
            if (townResults.next()) {
                double money = townResults.getDouble("money");
                Timestamp timestamp = townResults.getTimestamp("founded");
                String greetingMessage = townResults.getString("greeting_message");
                String farewellMessage = townResults.getString("farewell_message");
                TeleportationPoint spawnTeleportationPoint = getTeleportationPoint(townResults.getInt("spawn_location_id"), connection);
                HashSet<ClaimedChunk> claimedChunks = getClaimedChunks(townName, connection);
                HashMap<UUID, TownRole> members = getTownMembers(townName, connection);
                return new Town(townName, claimedChunks, members, spawnTeleportationPoint, money, greetingMessage, farewellMessage, timestamp.toInstant().getEpochSecond());
            }
        }
        getTown.close();
        return null;
    }

    // Returns if a town with that name already exists
    private static boolean townExists(String townName, Connection connection) throws SQLException {
        PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE name=?;");
        existStatement.setString(1, townName);
        ResultSet existResult = existStatement.executeQuery();
        if (existResult != null) {
            return existResult.next();
        }
        existStatement.close();
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

    private static Integer getIDFromTownRole(TownRole townRole) {
        switch (townRole) {
            case RESIDENT:
                return 1;
            case TRUSTED:
                return 2;
            case MAYOR:
                return 3;
        }
        return null;
    }

    private static TownRole getTownRoleFromID(Integer id) {
        switch (id) {
            case 0:
                return null;
            case 1:
                return TownRole.RESIDENT;
            case 2:
                return TownRole.TRUSTED;
            case 3:
                return TownRole.MAYOR;
        }
        return null;
    }

    private static void updatePlayerRole(UUID uuid, TownRole townRole, Connection connection) throws SQLException {
        PreparedStatement changeTownRoleStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `town_role`=? WHERE `uuid`=?;");
        changeTownRoleStatement.setInt(1, getIDFromTownRole(townRole));
        changeTownRoleStatement.setString(2, uuid.toString());
        changeTownRoleStatement.executeUpdate();
        changeTownRoleStatement.close();
        HuskTowns.getPlayerCache().setPlayerRole(uuid, townRole);
    }

    private static void clearPlayerRole(UUID uuid, Connection connection) throws SQLException {
        PreparedStatement clearTownRoleStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `town_role`=NULL WHERE `uuid`=?;");
        clearTownRoleStatement.setString(1, uuid.toString());
        clearTownRoleStatement.executeUpdate();
        clearTownRoleStatement.close();
        HuskTowns.getPlayerCache().removePlayer(uuid);
    }

    private static void updatePlayerTown(UUID uuid, String townName, Connection connection) throws SQLException {
        PreparedStatement joinTownStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `town_id`=(SELECT `id` from " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) WHERE `uuid`=?;");
        joinTownStatement.setString(1, townName);
        joinTownStatement.setString(2, uuid.toString());
        joinTownStatement.executeUpdate();
        joinTownStatement.close();
        HuskTowns.getPlayerCache().setPlayerTown(uuid, townName);
    }

    private static void leavePlayerTown(UUID uuid, Connection connection) throws SQLException {
        PreparedStatement leaveTownStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `town_id`=NULL WHERE `uuid`=?;");
        leaveTownStatement.setString(1, uuid.toString());
        leaveTownStatement.executeUpdate();
        leaveTownStatement.close();
        HuskTowns.getPlayerCache().removePlayer(uuid);
    }

    public static void joinTown(Player player, String townName) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (inTown(player, connection)) {
                    MessageManager.sendMessage(player, "error_already_in_town");
                    return;
                }
                updatePlayerTown(player.getUniqueId(), townName, connection);
                updatePlayerRole(player.getUniqueId(), TownRole.RESIDENT, connection);
                MessageManager.sendMessage(player, "join_town_success", townName);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void leaveTown(Player player) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(player, connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                if (getTownRole(player, connection) == TownRole.MAYOR) {
                    MessageManager.sendMessage(player, "error_mayor_leave");
                    return;
                }
                leavePlayerTown(player.getUniqueId(), connection);
                clearPlayerRole(player.getUniqueId(), connection);
                MessageManager.sendMessage(player, "leave_town_success");
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    // Delete the table from SQL. Cascading deletion means all claims will be cleared & player town ID will be set to null
    public static void deleteTownData(String townName, Connection connection) throws SQLException {
        // Clear the town roles of all members
        PreparedStatement clearPlayerRoles = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `town_role`=NULL WHERE `town_id`=(SELECT `id` FROM "
                        + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?);");
        clearPlayerRoles.setString(1, townName);
        clearPlayerRoles.executeUpdate();
        clearPlayerRoles.close();

        // Delete the town from database (triggers cascading nullification and deletion)
        PreparedStatement deleteTown = connection.prepareStatement(
                "DELETE FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?;");
        deleteTown.setString(1, townName);
        deleteTown.executeUpdate();
        deleteTown.close();
    }

    public static void disbandTown(Player player) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(player, connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                if (getTownRole(player, connection) != TownRole.MAYOR) {
                    MessageManager.sendMessage(player, "error_insufficient_disband_privileges");
                    return;
                }
                String townName = getPlayerTown(player, connection).getName();
                deleteTownData(townName, connection);
                MessageManager.sendMessage(player, "disband_town_success");
                HuskTowns.getClaimCache().reload();
                HuskTowns.getPlayerCache().reload();
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void sendTownMenu(Player player, Town town, Connection connection) throws SQLException {
        StringBuilder mayorName = new StringBuilder().append("[Mayor:](#4af7c9 show_text=&#4af7c9&The head of the town\n&7Can manage residents & claims) ");
        StringBuilder trustedMembers = new StringBuilder().append("[Trustees:](#4af7c9 show_text=&#4af7c9&Trusted citizens of the town\n&7Can build anywhere in town\nCan invite new residents\nCan claim new land) ");
        StringBuilder residentMembers = new StringBuilder().append("[Residents:](#4af7c9 show_text=&#4af7c9&Standard residents of the town\n&7Default rank for new citizens\nCan build in plots within town) ");

        for (UUID uuid : town.getMembers().keySet()) {
            String playerName = getPlayerName(uuid, connection);
            if (playerName == null) {
                continue;
            }
            playerName = "[" + playerName + "](white ";
            if (playerName.equals(player.getName())) {
                playerName = playerName + "bold ";
            }
            switch (town.getMembers().get(uuid)) {
                case MAYOR:
                    mayorName.append(playerName).append("show_text=&7").append(uuid).append(")");
                    break;
                case RESIDENT:
                    residentMembers.append(playerName).append("show_text=&7").append(uuid).append("), ");
                    break;
                case TRUSTED:
                    trustedMembers.append(playerName).append("show_text=&7").append(uuid).append("), ");
                    break;
            }
        }

        player.spigot().sendMessage(new MineDown("\n[Town Overview](#4af7c9 bold) [for](#4af7c9) [" + town.getName() + "](#4af7c9 bold)").toComponent());
        player.spigot().sendMessage(new MineDown("[Town Level:](#4af7c9 show_text=&#4af7c9&Level of the town\n&7Calculated based on value of coffers) &f" + town.getLevel()).toComponent());
        player.spigot().sendMessage(new MineDown("[Coffers:](#4af7c9 show_text=&#4af7c9&Amount of money deposited into town\n&7Money paid in with /town deposit) &f" + town.getMoneyDeposited() + "\n").toComponent());

        player.spigot().sendMessage(new MineDown("[Claims](#4af7c9 bold)").toComponent());
        player.spigot().sendMessage(new MineDown("[Chunks Claimed:](#4af7c9 show_text=&7Total number of chunks claimed\nout of maximum possible, based on\ncurrent town level.) &f"
                + town.getClaimedChunksNumber() + "/[" + town.getMaximumClaimedChunks() + "](white show_text=&7Max claims based on current Town Level)").toComponent());
        player.spigot().sendMessage(new MineDown("[⬛](" + town.getTownColor() + ") [View list](#4af7c9 underline show_text=&#4af7c9&Click to view a list of claims run_command=/town claims)\n").toComponent());

        player.spigot().sendMessage(new MineDown("[Citizen List](#4af7c9 bold) &#4af7c9&(Population: &f" + town.getMembers().size() + "&#4af7c9&)").toComponent());
        player.spigot().sendMessage(new MineDown(mayorName.toString()).toComponent());
        player.spigot().sendMessage(new MineDown(trustedMembers.toString()).toComponent());
        player.spigot().sendMessage(new MineDown(residentMembers.toString()).toComponent());
    }

    public static void showTownMenu(Player player, String townName) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!townExists(townName, connection)) {
                    MessageManager.sendMessage(player, "error_invalid_town");
                    return;
                }
                Town town = getTownFromName(townName, connection);
                sendTownMenu(player, town, connection);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void showTownMenu(Player player) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(player, connection)) {
                    MessageManager.sendMessage(player, "town_menu_no_town");
                    return;
                }
                Town town = getPlayerTown(player, connection);

                sendTownMenu(player, town, connection);

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    private static void sendClaimList(Player player, Town town) {
        StringBuilder claimList = new StringBuilder();
        for (ClaimedChunk chunk : town.getClaimedChunks()) {
            claimList.append("[⬛](")
                    .append(town.getTownColor())
                    .append(") [Claim at ")
                    .append(chunk.getChunkX())
                    .append(", ")
                    .append(chunk.getChunkZ())
                    .append(" on world: \"")
                    .append(chunk.getWorld())
                    .append("\", server: ")
                    .append(chunk.getServer())
                    .append("](#4af7c9 show_text=")
                    .append("&")
                    .append(town.getTownColor())
                    .append("&").append(town.getName()).append("&r\n")
                    .append("&r&#b0b0b0&Chunk: &").append(town.getTownColor()).append("&")
                    .append(chunk.getChunkX())
                    .append(", ")
                    .append(chunk.getChunkZ())
                    .append("&r\n")
                    .append("&#b0b0b0&Claimed: &").append(town.getTownColor()).append("&")
                    .append(chunk.getFormattedTime());
            if (chunk.getServer().equals(HuskTowns.getSettings().getServerID())) {
                claimList.append(" run_command=/map ")
                        .append(chunk.getChunkX()).append(" ").append(chunk.getChunkZ()).append(" ").append(chunk.getWorld());
            }
            claimList.append(")\n");
        }
        MessageManager.sendMessage(player, "claim_list_header", town.getName(),
                Integer.toString(town.getClaimedChunksNumber()), Integer.toString(town.getMaximumClaimedChunks()));
        player.spigot().sendMessage(new MineDown(claimList.toString()).toComponent());
    }

    public static void showClaimList(Player player) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(player, connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                Town town = getPlayerTown(player, connection);

                sendClaimList(player, town);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void showClaimList(Player player, String townName) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!townExists(townName, connection)) {
                    MessageManager.sendMessage(player, "error_invalid_town");
                    return;
                }
                Town town = getTownFromName(townName, connection);

                sendClaimList(player, town);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    private static Town getTownFromID(int townID, Connection connection) throws SQLException {
        PreparedStatement getTownRole = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `id`=?;");
        getTownRole.setInt(1, townID);
        ResultSet townRoleResults = getTownRole.executeQuery();
        if (townRoleResults != null) {
            if (townRoleResults.next()) {
                String townName = townRoleResults.getString("name");
                double money = townRoleResults.getDouble("money");
                Timestamp timestamp = townRoleResults.getTimestamp("founded");
                String greetingMessage = townRoleResults.getString("greeting_message");
                String farewellMessage = townRoleResults.getString("farewell_message");
                TeleportationPoint spawnTeleportationPoint = getTeleportationPoint(townRoleResults.getInt("spawn_location_id"), connection);
                HashSet<ClaimedChunk> claimedChunks = getClaimedChunks(townName, connection);
                HashMap<UUID, TownRole> members = getTownMembers(townName, connection);
                return new Town(townName, claimedChunks, members, spawnTeleportationPoint, money, greetingMessage, farewellMessage, timestamp.toInstant().getEpochSecond());
            }
        }
        getTownRole.close();
        return null;
    }

    private static Town getPlayerTown(Player player, Connection connection) throws SQLException {
        PreparedStatement getTown = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() +
                        " WHERE `id`=(SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);");
        getTown.setString(1, player.getUniqueId().toString());
        ResultSet townResults = getTown.executeQuery();
        if (townResults != null) {
            if (townResults.next()) {
                String townName = townResults.getString("name");
                double money = townResults.getDouble("money");
                Timestamp timestamp = townResults.getTimestamp("founded");
                String greetingMessage = townResults.getString("greeting_message");
                String farewellMessage = townResults.getString("farewell_message");
                TeleportationPoint spawnTeleportationPoint = getTeleportationPoint(townResults.getInt("spawn_location_id"), connection);
                HashSet<ClaimedChunk> claimedChunks = getClaimedChunks(townName, connection);
                HashMap<UUID, TownRole> members = getTownMembers(townName, connection);
                return new Town(townName, claimedChunks, members, spawnTeleportationPoint, money, greetingMessage, farewellMessage, timestamp.toInstant().getEpochSecond());
            }
        }
        getTown.close();
        return null;
    }

    private static TeleportationPoint getTeleportationPoint(int teleportationPointID, Connection connection) throws SQLException {
        PreparedStatement getTeleportationPoint = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getLocationsTable() + " WHERE `id`=?;");
        getTeleportationPoint.setInt(1, teleportationPointID);
        ResultSet teleportationPointResults = getTeleportationPoint.executeQuery();

        if (teleportationPointResults != null) {
            if (teleportationPointResults.next()) {
                String server = teleportationPointResults.getString("server");
                String world = teleportationPointResults.getString("world");
                double x = teleportationPointResults.getDouble("x");
                double y = teleportationPointResults.getDouble("y");
                double z = teleportationPointResults.getDouble("z");
                float yaw = teleportationPointResults.getFloat("yaw");
                float pitch = teleportationPointResults.getFloat("pitch");
                return new TeleportationPoint(world, x, y, z, yaw, pitch, server);
            }
        }
        return null;
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
                updatePlayerTown(player.getUniqueId(), townName, connection);
                updatePlayerRole(player.getUniqueId(), TownRole.MAYOR, connection);
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
        if (townRoleResults != null) {
            if (townRoleResults.next()) {
                return getTownRoleFromID(townRoleResults.getInt("town_role"));
            } else {
                return null;
            }
        }
        getTownRole.close();
        return null;
    }

    // Returns if a player is in a town
    private static boolean inTown(Player player, Connection connection) throws SQLException {
        PreparedStatement alreadyInTownCheck = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE uuid=?;");
        alreadyInTownCheck.setString(1, player.getUniqueId().toString());
        ResultSet alreadyInTownResult = alreadyInTownCheck.executeQuery();
        if (alreadyInTownResult != null) {
            alreadyInTownResult.next();
            return alreadyInTownResult.getInt("town_id") != 0;
        }
        alreadyInTownCheck.close();
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

        if (checkClaimedResult != null) {
            return checkClaimedResult.next();
        }
        checkClaimed.close();
        return false;
    }

    private static void addClaim(ClaimedChunk chunk, Connection connection) throws SQLException {
        PreparedStatement claimCreationStatement = connection.prepareStatement(
                "INSERT INTO " + HuskTowns.getSettings().getClaimsTable() + " (town_id,claim_time,claimer_id,server,world,chunk_x,chunk_z,chunk_type) " +
                        "VALUES((SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE uuid=?),?," +
                        "(SELECT `id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?),?,?,?,?,0);");
        claimCreationStatement.setString(1, chunk.getClaimerUUID().toString());
        claimCreationStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        claimCreationStatement.setString(3, chunk.getClaimerUUID().toString());
        claimCreationStatement.setString(4, chunk.getServer());
        claimCreationStatement.setString(5, chunk.getWorld());
        claimCreationStatement.setInt(6, chunk.getChunkX());
        claimCreationStatement.setInt(7, chunk.getChunkZ());
        claimCreationStatement.executeUpdate();
        claimCreationStatement.close();
        HuskTowns.getClaimCache().add(chunk);
    }

    public static void claimChunk(Player player) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(player, connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }

                ClaimedChunk chunk = new ClaimedChunk(player, getPlayerTown(player, connection).getName());
                if (isClaimed(chunk.getServer(), chunk.getWorld(), chunk.getChunkX(), chunk.getChunkZ(), connection)) {
                    MessageManager.sendMessage(player, "error_already_claimed");
                    return;
                }

                TownRole role = getTownRole(player, connection);
                if (role == TownRole.RESIDENT) {
                    MessageManager.sendMessage(player, "error_insufficient_claim_privileges");
                    return;
                }

                Town town = getPlayerTown(player, connection);
                if (town.getClaimedChunks().size() >= town.getMaximumClaimedChunks()) {
                    MessageManager.sendMessage(player, "error_maximum_claims_made");
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
        if (resultSet != null) {
            if (resultSet.next()) {
                String userUUID = resultSet.getString("uuid");
                if (userUUID == null) {
                    return null;
                } else {
                    return UUID.fromString(userUUID);
                }
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
                            getPlayerUUID(checkClaimedResult.getInt("plot_owner_id"), connection),
                            getTownFromID(checkClaimedResult.getInt("town_id"), connection).getName());
                } else {
                    return new ClaimedChunk(checkClaimedResult.getString("server"),
                            checkClaimedResult.getString("world"),
                            checkClaimedResult.getInt("chunk_x"),
                            checkClaimedResult.getInt("chunk_z"),
                            getPlayerUUID(checkClaimedResult.getInt("claimer_id"), connection),
                            chunkType,
                            getTownFromID(checkClaimedResult.getInt("town_id"), connection).getName());
                }
            }
        }
        checkClaimed.close();
        return null;
    }

    public static HashMap<UUID, TownRole> getTownMembers(String townName, Connection connection) throws SQLException {
        PreparedStatement getMembers = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `town_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?);");
        getMembers.setString(1, townName);
        ResultSet memberResult = getMembers.executeQuery();

        HashMap<UUID, TownRole> members = new HashMap<>();
        if (memberResult != null) {
            while (memberResult.next()) {
                members.put(UUID.fromString(memberResult.getString("uuid")),
                        getTownRoleFromID(memberResult.getInt("town_role")));
            }
        }
        return members;
    }

    // Returns a list of a town's chunks on ALL servers
    public static HashSet<ClaimedChunk> getClaimedChunks(String townName, Connection connection) throws SQLException {
        PreparedStatement getChunks = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE `town_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?);");
        getChunks.setString(1, townName);
        ResultSet chunkResults = getChunks.executeQuery();
        HashSet<ClaimedChunk> chunks = new HashSet<>();
        if (chunkResults != null) {
            while (chunkResults.next()) {
                ChunkType chunkType = getChunkType(chunkResults.getInt("chunk_type"));
                if (chunkType == ChunkType.PLOT) {
                    chunks.add(new ClaimedChunk(chunkResults.getString("server"),
                            chunkResults.getString("world"),
                            chunkResults.getInt("chunk_x"),
                            chunkResults.getInt("chunk_z"),
                            getPlayerUUID(chunkResults.getInt("claimer_id"), connection),
                            chunkType,
                            getPlayerUUID(chunkResults.getInt("plot_owner_id"), connection),
                            townName));
                } else {
                    chunks.add(new ClaimedChunk(chunkResults.getString("server"),
                            chunkResults.getString("world"),
                            chunkResults.getInt("chunk_x"),
                            chunkResults.getInt("chunk_z"),
                            getPlayerUUID(chunkResults.getInt("claimer_id"), connection),
                            chunkType,
                            townName));
                }
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

    // Returns ALL claimed chunks on the server
    public static void updateClaimedChunkCache() {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement getChunks = connection.prepareStatement(
                        "SELECT * FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE `server`=?");
                getChunks.setString(1, HuskTowns.getSettings().getServerID());
                ResultSet chunkResults = getChunks.executeQuery();

                if (chunkResults != null) {
                    while (chunkResults.next()) {
                        ChunkType chunkType = getChunkType(chunkResults.getInt("chunk_type"));
                        if (chunkType == ChunkType.PLOT) {
                            HuskTowns.getClaimCache().add(new ClaimedChunk(chunkResults.getString("server"),
                                    chunkResults.getString("world"),
                                    chunkResults.getInt("chunk_x"),
                                    chunkResults.getInt("chunk_z"),
                                    getPlayerUUID(chunkResults.getInt("claimer_id"), connection),
                                    chunkType,
                                    getPlayerUUID(chunkResults.getInt("plot_owner_id"), connection),
                                    getTownFromID(chunkResults.getInt("town_id"), connection).getName()));
                        } else {
                            HuskTowns.getClaimCache().add(new ClaimedChunk(chunkResults.getString("server"),
                                    chunkResults.getString("world"),
                                    chunkResults.getInt("chunk_x"),
                                    chunkResults.getInt("chunk_z"),
                                    getPlayerUUID(chunkResults.getInt("claimer_id"), connection),
                                    chunkType,
                                    getTownFromID(chunkResults.getInt("town_id"), connection).getName()));
                        }
                    }
                }
                getChunks.close();
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void updatePlayerCachedData(Player player) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (inTown(player, connection)) {
                    HuskTowns.getPlayerCache().addPlayer(player.getUniqueId(),
                            getPlayerTown(player, connection).getName(),
                            getTownRole(player, connection));
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }
}