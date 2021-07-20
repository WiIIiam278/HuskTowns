package me.william278.husktowns.data;

import de.themoep.minedown.MineDown;
import de.themoep.minedown.MineDownParser;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.TeleportationHandler;
import me.william278.husktowns.commands.InviteCommand;
import me.william278.husktowns.commands.TownListCommand;
import me.william278.husktowns.data.pluginmessage.PluginMessage;
import me.william278.husktowns.data.pluginmessage.PluginMessageType;
import me.william278.husktowns.integrations.Vault;
import me.william278.husktowns.object.cache.Cache;
import me.william278.husktowns.object.cache.ClaimCache;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.object.teleport.TeleportationPoint;
import me.william278.husktowns.object.town.Town;
import me.william278.husktowns.object.town.TownBonus;
import me.william278.husktowns.object.town.TownInvite;
import me.william278.husktowns.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class DataManager {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    // Add a new player
    private static void createPlayer(String playerName, String playerUUID, Connection connection) throws SQLException {
        PreparedStatement playerCreationStatement = connection.prepareStatement(
                "INSERT INTO " + HuskTowns.getSettings().getPlayerTable() + " (username,uuid) VALUES(?,?);");
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

    public static String getPlayerName(UUID uuid, Connection connection) throws SQLException {
        PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?;");
        existStatement.setString(1, uuid.toString());
        ResultSet resultSet = existStatement.executeQuery();
        if (resultSet != null) {
            if (resultSet.next()) {
                final String username = resultSet.getString("username");
                existStatement.close();
                return username;
            }
        }
        existStatement.close();
        return null;
    }

    // Returns true if a player exists
    private static boolean playerExists(String playerUUID, Connection connection) throws SQLException {
        PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?;");
        existStatement.setString(1, playerUUID);
        ResultSet resultSet = existStatement.executeQuery();
        if (resultSet != null) {
            final boolean playerExists = resultSet.next();
            existStatement.close();
            return playerExists;
        }
        existStatement.close();
        return false;
    }

    // Returns true if a player exists
    private static boolean playerNameExists(String playerName, Connection connection) throws SQLException {
        PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `username`=?;");
        existStatement.setString(1, playerName);
        ResultSet resultSet = existStatement.executeQuery();
        if (resultSet != null) {
            final boolean playerNameExists = resultSet.next();
            existStatement.close();
            return playerNameExists;
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

                    // Handle teleporting players
                    if (HuskTowns.getSettings().doBungee()) {
                        DataManager.handleTeleportingPlayers(player);
                    }
                }
                HuskTowns.getPlayerCache().setPlayerName(UUID.fromString(playerUUID), playerName);
                if (HuskTowns.getSettings().doBungee()) {
                    new PluginMessage(PluginMessageType.ADD_PLAYER_TO_CACHE, playerUUID, playerName).sendToAll(player);
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    // Update the type of a chunk
    private static void setChunkType(ClaimedChunk claimedChunk, ClaimedChunk.ChunkType type, Connection connection) throws SQLException {
        int chunkTypeID = getIDFromChunkType(type);
        PreparedStatement chunkUpdateStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getClaimsTable() + " SET `chunk_type`=?, `plot_owner_id`=NULL WHERE `town_id`=" +
                        "(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) " +
                        "AND `server`=? AND `world`=? AND `chunk_x`=? AND `chunk_z`=?;");
        chunkUpdateStatement.setInt(1, chunkTypeID);
        chunkUpdateStatement.setString(2, claimedChunk.getTown());
        chunkUpdateStatement.setString(3, claimedChunk.getServer());
        chunkUpdateStatement.setString(4, claimedChunk.getWorld());
        chunkUpdateStatement.setInt(5, claimedChunk.getChunkX());
        chunkUpdateStatement.setInt(6, claimedChunk.getChunkZ());
        chunkUpdateStatement.executeUpdate();
        chunkUpdateStatement.close();

        HuskTowns.getClaimCache().remove(claimedChunk.getChunkX(), claimedChunk.getChunkZ(), claimedChunk.getWorld());
        HuskTowns.getClaimCache().add(new ClaimedChunk(claimedChunk.getServer(), claimedChunk.getWorld(), claimedChunk.getChunkX(),
                claimedChunk.getChunkZ(), claimedChunk.getClaimerUUID(), type, claimedChunk.getPlotChunkOwner(), claimedChunk.getPlotChunkMembers(), claimedChunk.getTown(), claimedChunk.getClaimTimestamp()));
    }

    // Update money in town coffers
    private static void depositIntoCoffers(UUID playerUUID, double moneyToDeposit, Connection connection) throws SQLException {
        PreparedStatement coffersUpdateStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getTownsTable() + " SET `money`=`money`+? WHERE `id`=(SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);");
        coffersUpdateStatement.setDouble(1, moneyToDeposit);
        coffersUpdateStatement.setString(2, playerUUID.toString());
        coffersUpdateStatement.executeUpdate();
        coffersUpdateStatement.close();
    }

    // Set the plot owner of a claim
    private static void setPlotOwner(ClaimedChunk claimedChunk, UUID plotOwner, Connection connection) throws SQLException {
        PreparedStatement chunkUpdateStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getClaimsTable() + " SET `plot_owner_id`=(SELECT `id` FROM "
                        + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?) WHERE `town_id`=" +
                        "(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) " +
                        "AND `server`=? AND `world`=? AND `chunk_x`=? AND `chunk_z`=?;");
        chunkUpdateStatement.setString(1, plotOwner.toString());
        chunkUpdateStatement.setString(2, claimedChunk.getTown());
        chunkUpdateStatement.setString(3, claimedChunk.getServer());
        chunkUpdateStatement.setString(4, claimedChunk.getWorld());
        chunkUpdateStatement.setInt(5, claimedChunk.getChunkX());
        chunkUpdateStatement.setInt(6, claimedChunk.getChunkZ());
        chunkUpdateStatement.executeUpdate();
        chunkUpdateStatement.close();

        HuskTowns.getClaimCache().remove(claimedChunk.getChunkX(), claimedChunk.getChunkZ(), claimedChunk.getWorld());
        HuskTowns.getClaimCache().add(new ClaimedChunk(claimedChunk.getServer(), claimedChunk.getWorld(), claimedChunk.getChunkX(),
                claimedChunk.getChunkZ(), claimedChunk.getClaimerUUID(), claimedChunk.getChunkType(), plotOwner, claimedChunk.getPlotChunkMembers(), claimedChunk.getTown(), claimedChunk.getClaimTimestamp()));
    }

    private static void clearPlotOwner(ClaimedChunk claimedChunk, Connection connection) throws SQLException {
        PreparedStatement chunkUpdateStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getClaimsTable() + " SET `plot_owner_id`=NULL WHERE `town_id`=" +
                        "(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) " +
                        "AND `server`=? AND `world`=? AND `chunk_x`=? AND `chunk_z`=?;");
        chunkUpdateStatement.setString(1, claimedChunk.getTown());
        chunkUpdateStatement.setString(2, claimedChunk.getServer());
        chunkUpdateStatement.setString(3, claimedChunk.getWorld());
        chunkUpdateStatement.setInt(4, claimedChunk.getChunkX());
        chunkUpdateStatement.setInt(5, claimedChunk.getChunkZ());
        chunkUpdateStatement.executeUpdate();
        chunkUpdateStatement.close();

        HuskTowns.getClaimCache().remove(claimedChunk.getChunkX(), claimedChunk.getChunkZ(), claimedChunk.getWorld());
        HuskTowns.getClaimCache().add(new ClaimedChunk(claimedChunk.getServer(), claimedChunk.getWorld(), claimedChunk.getChunkX(),
                claimedChunk.getChunkZ(), claimedChunk.getClaimerUUID(), claimedChunk.getChunkType(), null,  new HashSet<>(), claimedChunk.getTown(), claimedChunk.getClaimTimestamp()));
    }

    private static ArrayList<Town> getTownsByLevel(Connection connection) throws SQLException {
        PreparedStatement getTown = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() + " ORDER BY `money` DESC;");
        ResultSet towns = getTown.executeQuery();
        ArrayList<Town> townList = new ArrayList<>();
        if (towns != null) {
            while (towns.next()) {
                final String name = towns.getString("name");
                final double money = towns.getDouble("money");
                final Timestamp timestamp = towns.getTimestamp("founded");
                final String greetingMessage = towns.getString("greeting_message");
                final String farewellMessage = towns.getString("farewell_message");
                final TeleportationPoint spawnTeleportationPoint = getTeleportationPoint(towns.getInt("spawn_location_id"), connection);
                final HashSet<ClaimedChunk> claimedChunks = getClaimedChunks(name, connection);
                final HashMap<UUID, Town.TownRole> members = getTownMembers(name, connection);
                townList.add(new Town(name, claimedChunks, members, spawnTeleportationPoint, money, greetingMessage, farewellMessage, timestamp.toInstant().getEpochSecond()));
            }
        }
        getTown.close();
        return townList;
    }

    public static ArrayList<Town> getTownsByName(Connection connection) throws SQLException {
        PreparedStatement getTown = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() + " ORDER BY `name` ASC;");
        ResultSet towns = getTown.executeQuery();
        ArrayList<Town> townList = new ArrayList<>();
        if (towns != null) {
            while (towns.next()) {
                final String name = towns.getString("name");
                final double money = towns.getDouble("money");
                final Timestamp timestamp = towns.getTimestamp("founded");
                final String greetingMessage = towns.getString("greeting_message");
                final String farewellMessage = towns.getString("farewell_message");
                final TeleportationPoint spawnTeleportationPoint = getTeleportationPoint(towns.getInt("spawn_location_id"), connection);
                final HashSet<ClaimedChunk> claimedChunks = getClaimedChunks(name, connection);
                final HashMap<UUID, Town.TownRole> members = getTownMembers(name, connection);
                townList.add(new Town(name, claimedChunks, members, spawnTeleportationPoint, money, greetingMessage, farewellMessage, timestamp.toInstant().getEpochSecond()));
            }
        }
        getTown.close();
        return townList;
    }

    private static ArrayList<Town> getTownsByNewest(Connection connection) throws SQLException {
        PreparedStatement getTown = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() + " ORDER BY `founded` DESC;");
        ResultSet towns = getTown.executeQuery();
        ArrayList<Town> townList = new ArrayList<>();
        if (towns != null) {
            while (towns.next()) {
                String name = towns.getString("name");
                double money = towns.getDouble("money");
                Timestamp timestamp = towns.getTimestamp("founded");
                String greetingMessage = towns.getString("greeting_message");
                String farewellMessage = towns.getString("farewell_message");
                TeleportationPoint spawnTeleportationPoint = getTeleportationPoint(towns.getInt("spawn_location_id"), connection);
                HashSet<ClaimedChunk> claimedChunks = getClaimedChunks(name, connection);
                HashMap<UUID, Town.TownRole> members = getTownMembers(name, connection);
                townList.add(new Town(name, claimedChunks, members, spawnTeleportationPoint, money, greetingMessage, farewellMessage, timestamp.toInstant().getEpochSecond()));
            }
        }
        getTown.close();
        return townList;
    }

    private static ArrayList<Town> getTownsByOldest(Connection connection) throws SQLException {
        PreparedStatement getTown = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() + " ORDER BY `founded` ASC;");
        ResultSet towns = getTown.executeQuery();
        ArrayList<Town> townList = new ArrayList<>();
        if (towns != null) {
            while (towns.next()) {
                String name = towns.getString("name");
                double money = towns.getDouble("money");
                Timestamp timestamp = towns.getTimestamp("founded");
                String greetingMessage = towns.getString("greeting_message");
                String farewellMessage = towns.getString("farewell_message");
                TeleportationPoint spawnTeleportationPoint = getTeleportationPoint(towns.getInt("spawn_location_id"), connection);
                HashSet<ClaimedChunk> claimedChunks = getClaimedChunks(name, connection);
                HashMap<UUID, Town.TownRole> members = getTownMembers(name, connection);
                townList.add(new Town(name, claimedChunks, members, spawnTeleportationPoint, money, greetingMessage, farewellMessage, timestamp.toInstant().getEpochSecond()));
            }
        }
        getTown.close();
        return townList;
    }

    public static Town getTownFromName(String townName, Connection connection) throws SQLException {
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
                HashMap<UUID, Town.TownRole> members = getTownMembers(townName, connection);
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
            boolean townExists = existResult.next();
            existStatement.close();
            return townExists;
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
        townCreationStatement.setString(3, town.getGreetingMessage());
        townCreationStatement.setString(4, town.getFarewellMessage());
        townCreationStatement.executeUpdate();
        townCreationStatement.close();
    }

    private static Integer getIDFromTownRole(Town.TownRole townRole) {
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

    private static Town.TownRole getTownRoleFromID(Integer id) {
        switch (id) {
            case 0:
                return null;
            case 1:
                return Town.TownRole.RESIDENT;
            case 2:
                return Town.TownRole.TRUSTED;
            case 3:
                return Town.TownRole.MAYOR;
        }
        return null;
    }

    private static void updateTownMayor(UUID newMayor, UUID oldMayor, Connection connection) throws SQLException {
        setPlayerRoleData(oldMayor, Town.TownRole.TRUSTED, connection);
        setPlayerRoleData(newMayor, Town.TownRole.MAYOR, connection);
        HuskTowns.getPlayerCache().setPlayerRole(oldMayor, Town.TownRole.TRUSTED);
        HuskTowns.getPlayerCache().setPlayerRole(newMayor, Town.TownRole.MAYOR);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                new PluginMessage(PluginMessageType.SET_PLAYER_ROLE, oldMayor.toString(), Town.TownRole.TRUSTED.toString()).sendToAll(updateNotificationDispatcher);
                new PluginMessage(PluginMessageType.SET_PLAYER_ROLE, newMayor.toString(), Town.TownRole.MAYOR.toString()).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void updateTownFarewellData(UUID updaterUUID, String newFarewell, Connection connection) throws SQLException {
        PreparedStatement changeTownFarewellStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getTownsTable() + " SET `farewell_message`=? WHERE `id`=(SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);");
        changeTownFarewellStatement.setString(1, newFarewell);
        changeTownFarewellStatement.setString(2, updaterUUID.toString());
        changeTownFarewellStatement.executeUpdate();
        changeTownFarewellStatement.close();

        String townName = getPlayerTown(updaterUUID, connection).getName();
        HuskTowns.getTownMessageCache().setFarewellMessage(townName, newFarewell);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                new PluginMessage(PluginMessageType.UPDATE_CACHED_FAREWELL_MESSAGE, townName, newFarewell).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void updateTownGreetingData(UUID updaterUUID, String newGreeting, Connection connection) throws SQLException {
        PreparedStatement changeTownGreetingStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getTownsTable() + " SET `greeting_message`=? WHERE `id`=(SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);");
        changeTownGreetingStatement.setString(1, newGreeting);
        changeTownGreetingStatement.setString(2, updaterUUID.toString());
        changeTownGreetingStatement.executeUpdate();
        changeTownGreetingStatement.close();

        String townName = getPlayerTown(updaterUUID, connection).getName();
        HuskTowns.getTownMessageCache().setGreetingMessage(townName, newGreeting);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                new PluginMessage(PluginMessageType.UPDATE_CACHED_GREETING_MESSAGE, townName, newGreeting).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void updateTownName(UUID mayorUUID, String oldName, String newName, Connection connection) throws SQLException {
        PreparedStatement changeTownNameStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getTownsTable() + " SET `name`=? WHERE `id`=(SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);");
        changeTownNameStatement.setString(1, newName);
        changeTownNameStatement.setString(2, mayorUUID.toString());
        changeTownNameStatement.executeUpdate();
        changeTownNameStatement.close();

        if (HuskTowns.getPlayerCache().hasLoaded()) {
            HuskTowns.getPlayerCache().renameReload(oldName, newName);
        }
        if (HuskTowns.getClaimCache().hasLoaded()) {
            HuskTowns.getClaimCache().renameReload(oldName, newName);
        }
        if (HuskTowns.getTownMessageCache().hasLoaded()) {
            HuskTowns.getTownMessageCache().renameReload(oldName, newName);
        }
        if (HuskTowns.getTownBonusesCache().hasLoaded()) {
            HuskTowns.getTownBonusesCache().renameTown(oldName, newName);
        }
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                new PluginMessage(PluginMessageType.TOWN_RENAME, oldName, newName).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void setPlayerRoleData(UUID uuid, Town.TownRole townRole, Connection connection) throws SQLException {
        PreparedStatement changeTownRoleStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `town_role`=? WHERE `uuid`=?;");
        changeTownRoleStatement.setInt(1, getIDFromTownRole(townRole));
        changeTownRoleStatement.setString(2, uuid.toString());
        changeTownRoleStatement.executeUpdate();
        changeTownRoleStatement.close();
        HuskTowns.getPlayerCache().setPlayerRole(uuid, townRole);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                new PluginMessage(PluginMessageType.SET_PLAYER_ROLE, uuid.toString(), townRole.toString()).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void clearPlayerRoleData(UUID uuid, Connection connection) throws SQLException {
        PreparedStatement clearTownRoleStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `town_role`=NULL WHERE `uuid`=?;");
        clearTownRoleStatement.setString(1, uuid.toString());
        clearTownRoleStatement.executeUpdate();
        clearTownRoleStatement.close();
        HuskTowns.getPlayerCache().clearPlayerRole(uuid);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                new PluginMessage(PluginMessageType.CLEAR_PLAYER_ROLE, uuid.toString()).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void setPlayerTownData(UUID uuid, String townName, Connection connection) throws SQLException {
        PreparedStatement joinTownStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `town_id`=(SELECT `id` from " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) WHERE `uuid`=?;");
        joinTownStatement.setString(1, townName);
        joinTownStatement.setString(2, uuid.toString());
        joinTownStatement.executeUpdate();
        joinTownStatement.close();
        Bukkit.getLogger().info("updated " + Bukkit.getPlayer(uuid).getName() + "'s town to " + townName);
        HuskTowns.getPlayerCache().setPlayerTown(uuid, townName);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                new PluginMessage(PluginMessageType.SET_PLAYER_TOWN, uuid.toString(), townName).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void clearPlayerTownData(UUID uuid, Connection connection) throws SQLException {
        PreparedStatement leaveTownStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `town_id`=NULL WHERE `uuid`=?;");
        leaveTownStatement.setString(1, uuid.toString());
        leaveTownStatement.executeUpdate();
        leaveTownStatement.close();
        HuskTowns.getPlayerCache().clearPlayerTown(uuid);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                new PluginMessage(PluginMessageType.CLEAR_PLAYER_TOWN, uuid.toString()).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    public static void evictPlayerFromTown(Player evicter, String playerToEvict) {
        if (!HuskTowns.getPlayerCache().hasLoaded()) {
            MessageManager.sendMessage(evicter, "error_cache_updating", "Player Data");
            return;
        }
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(evicter.getUniqueId(), connection)) {
                    MessageManager.sendMessage(evicter, "error_not_in_town");
                    return;
                }
                UUID uuidToEvict = getPlayerUUID(playerToEvict, connection);
                if (uuidToEvict == null) {
                    MessageManager.sendMessage(evicter, "error_invalid_player");
                    return;
                }
                Town.TownRole evicterRole = getTownRole(evicter.getUniqueId(), connection);
                if (evicterRole == Town.TownRole.RESIDENT) {
                    MessageManager.sendMessage(evicter, "error_insufficient_evict_privileges");
                    return;
                }
                Town town = getPlayerTown(uuidToEvict, connection);
                if (town == null) {
                    MessageManager.sendMessage(evicter, "error_not_both_members");
                    return;
                }
                if (!town.getName().equals(getPlayerTown(evicter.getUniqueId(), connection).getName())) {
                    MessageManager.sendMessage(evicter, "error_not_both_members");
                    return;
                }
                if (evicter.getUniqueId() == uuidToEvict) {
                    MessageManager.sendMessage(evicter, "error_cant_evict_self");
                    return;
                }
                Town.TownRole roleOfPlayerToEvict = getTownRole(uuidToEvict, connection);
                if (roleOfPlayerToEvict == Town.TownRole.MAYOR) {
                    MessageManager.sendMessage(evicter, "error_cant_evict_mayor");
                    return;
                }
                if (evicterRole == Town.TownRole.TRUSTED && roleOfPlayerToEvict == Town.TownRole.TRUSTED) {
                    MessageManager.sendMessage(evicter, "error_cant_evict_other_trusted_member");
                    return;
                }

                clearPlayerTownData(uuidToEvict, connection);
                clearPlayerRoleData(uuidToEvict, connection);
                MessageManager.sendMessage(evicter, "you_evict_success", playerToEvict, town.getName());

                // Send a notification to all town members
                for (UUID uuid : town.getMembers().keySet()) {
                    if (uuid != evicter.getUniqueId()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            if (uuid == uuidToEvict) {
                                MessageManager.sendMessage(p, "have_been_evicted", town.getName(), evicter.getName());
                            } else {
                                MessageManager.sendMessage(p, "player_evicted", playerToEvict, evicter.getName());
                            }

                        } else {
                            if (HuskTowns.getSettings().doBungee()) {
                                if (uuid == uuidToEvict) {
                                    new PluginMessage(getPlayerName(uuid, connection), PluginMessageType.EVICTED_NOTIFICATION_YOURSELF,
                                            town.getName(), evicter.getName()).send(evicter);
                                } else {
                                    new PluginMessage(getPlayerName(uuid, connection), PluginMessageType.EVICTED_NOTIFICATION,
                                            playerToEvict, evicter.getName()).send(evicter);
                                }

                            }
                        }
                    }
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void joinTown(Player player, String townName) {
        if (!HuskTowns.getPlayerCache().hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
            return;
        }
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!townExists(townName, connection)) {
                    MessageManager.sendMessage(player, "error_town_no_longer_exists");
                    return;
                }
                if (inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_already_in_town");
                    return;
                }
                Town town = getTownFromName(townName, connection);
                if (town.getMembers().size() + 1 > town.getMaxMembers()) {
                    MessageManager.sendMessage(player, "error_town_full", town.getName());
                    return;
                }
                setPlayerTownData(player.getUniqueId(), townName, connection);
                setPlayerRoleData(player.getUniqueId(), Town.TownRole.RESIDENT, connection);
                HuskTowns.getPlayerCache().setPlayerName(player.getUniqueId(), player.getName());
                MessageManager.sendMessage(player, "join_town_success", townName);

                // Send a notification to all town members
                for (UUID uuid : town.getMembers().keySet()) {
                    if (!uuid.toString().equals(player.getUniqueId().toString())) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            MessageManager.sendMessage(p, "player_joined", player.getName());
                        } else {
                            if (HuskTowns.getSettings().doBungee()) {
                                new PluginMessage(getPlayerName(uuid, connection), PluginMessageType.PLAYER_HAS_JOINED_NOTIFICATION,
                                        player.getName()).send(player);
                            }
                        }
                    }
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void leaveTown(Player player) {
        if (!HuskTowns.getPlayerCache().hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
            return;
        }
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                String townName = getPlayerTown(player.getUniqueId(), connection).getName();
                if (getTownRole(player.getUniqueId(), connection) == Town.TownRole.MAYOR) {
                    MessageManager.sendMessage(player, "error_mayor_leave");
                    return;
                }
                clearPlayerTownData(player.getUniqueId(), connection);
                clearPlayerRoleData(player.getUniqueId(), connection);
                MessageManager.sendMessage(player, "leave_town_success", townName);
                AutoClaimUtil.removeAutoClaimer(player);
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
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                if (getTownRole(player.getUniqueId(), connection) != Town.TownRole.MAYOR) {
                    MessageManager.sendMessage(player, "error_insufficient_disband_privileges");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                String townName = town.getName();

                deleteTownData(townName, connection);
                AutoClaimUtil.removeAutoClaimer(player);
                MessageManager.sendMessage(player, "disband_town_success");

                // Update caches
                if (HuskTowns.getPlayerCache().hasLoaded()) {
                    HuskTowns.getPlayerCache().disbandReload(townName);
                }
                if (HuskTowns.getClaimCache().hasLoaded()) {
                    HuskTowns.getClaimCache().disbandReload(townName);
                }
                if (HuskTowns.getTownMessageCache().hasLoaded()) {
                    HuskTowns.getTownMessageCache().disbandReload(townName);
                }
                if (HuskTowns.getTownBonusesCache().hasLoaded()) {
                    HuskTowns.getTownBonusesCache().clearTownBonuses(townName);
                }

                // Send a notification to all town members
                for (UUID uuid : town.getMembers().keySet()) {
                    if (!uuid.toString().equals(player.getUniqueId().toString())) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            if (p.getUniqueId() != player.getUniqueId()) {
                                MessageManager.sendMessage(p, "town_disbanded", player.getName(), town.getName());
                            }

                        } else {
                            if (HuskTowns.getSettings().doBungee()) {
                                new PluginMessage(getPlayerName(uuid, connection), PluginMessageType.DISBAND_NOTIFICATION,
                                        player.getName(), town.getName()).send(player);

                            }
                        }
                    }
                }
                if (HuskTowns.getSettings().doBungee()) {
                    new PluginMessage(PluginMessageType.TOWN_DISBAND, townName).sendToAll(player);
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void depositMoney(Player player, double amountToDeposit) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!HuskTowns.getSettings().doEconomy()) {
                    MessageManager.sendMessage(player, "error_economy_disabled");
                    return;
                }
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                if (amountToDeposit <= 0) {
                    MessageManager.sendMessage(player, "error_invalid_amount");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                boolean sendDepositNotification = false;
                boolean sendLevelUpNotification = false;
                int currentTownLevel = town.getLevel();
                int afterTownLevel = TownLimitsUtil.getLevel(town.getMoneyDeposited() + amountToDeposit);
                if (amountToDeposit > (town.getMoneyDeposited() * HuskTowns.getSettings().getDepositNotificationThreshold())) {
                    sendDepositNotification = true;
                }
                if (afterTownLevel > currentTownLevel) {
                    sendLevelUpNotification = true;
                }
                if (Vault.takeMoney(player, amountToDeposit)) {
                    DataManager.depositIntoCoffers(player.getUniqueId(), amountToDeposit, connection);
                    MessageManager.sendMessage(player, "money_deposited_success", Vault.format(amountToDeposit), Vault.format(town.getMoneyDeposited() + amountToDeposit));
                } else {
                    MessageManager.sendMessage(player, "error_insufficient_funds");
                    return;
                }

                // Send a notification to all town members
                if (sendDepositNotification) {
                    for (UUID uuid : town.getMembers().keySet()) {
                        if (!uuid.toString().equals(player.getUniqueId().toString())) {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) {
                                MessageManager.sendMessage(p, "town_deposit_notification", player.getName(), Vault.format(amountToDeposit));
                            } else {
                                if (HuskTowns.getSettings().doBungee()) {
                                    new PluginMessage(getPlayerName(uuid, connection), PluginMessageType.DEPOSIT_NOTIFICATION,
                                            player.getName(), Vault.format(amountToDeposit)).send(player);
                                }
                            }
                        }
                    }
                }
                if (sendLevelUpNotification) {
                    for (UUID uuid : town.getMembers().keySet()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            MessageManager.sendMessage(p, "town_level_up_notification", town.getName(), Integer.toString(currentTownLevel), Integer.toString(afterTownLevel));
                        } else {
                            if (HuskTowns.getSettings().doBungee()) {
                                new PluginMessage(getPlayerName(uuid, connection), PluginMessageType.LEVEL_UP_NOTIFICATION,
                                        town.getName(), Integer.toString(currentTownLevel), Integer.toString(afterTownLevel)).send(player);

                            }
                        }

                    }
                }

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void demotePlayer(Player player, String playerToDemote) {
        if (!HuskTowns.getPlayerCache().hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
            return;
        }
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                if (getTownRole(player.getUniqueId(), connection) != Town.TownRole.MAYOR) {
                    MessageManager.sendMessage(player, "error_insufficient_role_privileges");
                    return;
                }
                UUID uuidToDemote = getPlayerUUID(playerToDemote, connection);
                if (uuidToDemote == null) {
                    MessageManager.sendMessage(player, "error_invalid_player");
                    return;
                }
                Town town = getPlayerTown(uuidToDemote, connection);
                if (town == null) {
                    MessageManager.sendMessage(player, "error_not_both_members");
                    return;
                }
                if (!town.getName().equals(getPlayerTown(player.getUniqueId(), connection).getName())) {
                    MessageManager.sendMessage(player, "error_not_both_members");
                    return;
                }
                if (getTownRole(uuidToDemote, connection) == Town.TownRole.MAYOR) {
                    MessageManager.sendMessage(player, "error_cant_demote_self");
                    return;
                }
                if (getTownRole(uuidToDemote, connection) == Town.TownRole.RESIDENT) {
                    MessageManager.sendMessage(player, "error_cant_demote_resident");
                    return;
                }
                DataManager.setPlayerRoleData(uuidToDemote, Town.TownRole.RESIDENT, connection);
                MessageManager.sendMessage(player, "player_demoted_success", playerToDemote, town.getName());

                // Send a notification to all town members
                for (UUID uuid : town.getMembers().keySet()) {
                    if (!uuid.toString().equals(player.getUniqueId().toString())) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            if (uuid == uuidToDemote) {
                                MessageManager.sendMessage(p, "have_been_demoted", player.getName(), town.getName());
                            } else {
                                MessageManager.sendMessage(p, "player_demoted", playerToDemote, player.getName(), town.getName());
                            }

                        } else {
                            if (HuskTowns.getSettings().doBungee()) {
                                if (uuid == uuidToDemote) {
                                    new PluginMessage(getPlayerName(uuid, connection), PluginMessageType.DEMOTED_NOTIFICATION_YOURSELF,
                                            player.getName(), town.getName()).send(player);
                                } else {
                                    new PluginMessage(getPlayerName(uuid, connection), PluginMessageType.DEMOTED_NOTIFICATION,
                                            playerToDemote, player.getName(), town.getName()).send(player);
                                }

                            }
                        }
                    }
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void sendInvite(Player invitingPlayer, String inviteeName) {
        if (!HuskTowns.getPlayerCache().hasLoaded()) {
            MessageManager.sendMessage(invitingPlayer, "error_cache_updating", "Player Data");
            return;
        }
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(invitingPlayer.getUniqueId(), connection)) {
                    MessageManager.sendMessage(invitingPlayer, "error_not_in_town");
                    return;
                }
                if (getTownRole(invitingPlayer.getUniqueId(), connection) == Town.TownRole.RESIDENT) {
                    MessageManager.sendMessage(invitingPlayer, "error_insufficient_invite_privileges");
                    return;
                }
                if (!playerNameExists(inviteeName, connection)) {
                    MessageManager.sendMessage(invitingPlayer, "error_invalid_player");
                    return;
                }
                if (inTown(getPlayerUUID(inviteeName, connection), connection)) {
                    MessageManager.sendMessage(invitingPlayer, "error_other_already_in_town", inviteeName);
                    return;
                }
                Town town = getPlayerTown(invitingPlayer.getUniqueId(), connection);
                if (town.getMembers().size() + 1 > town.getMaxMembers()) {
                    MessageManager.sendMessage(invitingPlayer, "error_town_full", town.getName());
                    return;
                }

                Player inviteePlayer = Bukkit.getPlayer(inviteeName);
                if (inviteePlayer != null) {
                    // Handle on server
                    InviteCommand.sendInvite(inviteePlayer, new TownInvite(invitingPlayer.getName(),
                            town.getName()));
                    MessageManager.sendMessage(invitingPlayer, "invite_sent_success", inviteeName, town.getName());
                } else {
                    if (HuskTowns.getSettings().doBungee()) {
                        // Handle with Plugin Messages
                        TownInvite invite = new TownInvite(invitingPlayer.getName(), getPlayerTown(invitingPlayer.getUniqueId(), connection).getName());
                        new PluginMessage(inviteeName, PluginMessageType.INVITED_TO_JOIN,
                                invite.getTownName() + "$" + invite.getInviter() + "$" + invite.getExpiry()).send(invitingPlayer);
                        MessageManager.sendMessage(invitingPlayer, "invite_sent_success", inviteeName, town.getName());
                    } else {
                        MessageManager.sendMessage(invitingPlayer, "error_invalid_player");
                        return;
                    }
                }

                // Send a notification to all town members
                for (UUID uuid : town.getMembers().keySet()) {
                    if (!uuid.toString().equals(invitingPlayer.getUniqueId().toString())) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            if (!p.getName().equalsIgnoreCase(inviteeName)) {
                                MessageManager.sendMessage(p, "player_invited", inviteeName, invitingPlayer.getName());
                            }
                        } else {
                            if (HuskTowns.getSettings().doBungee()) {
                                new PluginMessage(getPlayerName(uuid, connection), PluginMessageType.INVITED_NOTIFICATION,
                                        inviteeName, invitingPlayer.getName()).send(invitingPlayer);
                            }
                        }
                    }
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void transferTownOwnership(Player player, String newMayor) {
        if (!HuskTowns.getPlayerCache().hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
            return;
        }
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                if (getTownRole(player.getUniqueId(), connection) != Town.TownRole.MAYOR) {
                    MessageManager.sendMessage(player, "error_insufficient_transfer_privileges");
                    return;
                }
                UUID newMayorUUID = getPlayerUUID(newMayor, connection);
                if (newMayorUUID == null) {
                    MessageManager.sendMessage(player, "error_invalid_player");
                    return;
                }
                Town town = getPlayerTown(newMayorUUID, connection);
                if (town == null) {
                    MessageManager.sendMessage(player, "error_not_both_members");
                    return;
                }
                if (!town.getName().equals(getPlayerTown(player.getUniqueId(), connection).getName())) {
                    MessageManager.sendMessage(player, "error_not_both_members");
                    return;
                }
                if (getTownRole(newMayorUUID, connection) == Town.TownRole.MAYOR) {
                    MessageManager.sendMessage(player, "error_cant_transfer_self");
                    return;
                }
                DataManager.updateTownMayor(newMayorUUID, player.getUniqueId(), connection);

                AutoClaimUtil.removeAutoClaimer(player);
                MessageManager.sendMessage(player, "town_transfer_success", town.getName(), newMayor);

                // Send a notification to all town members
                for (UUID uuid : town.getMembers().keySet()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (!uuid.toString().equals(player.getUniqueId().toString())) {
                        if (p != null) {
                            if (uuid == newMayorUUID) {
                                MessageManager.sendMessage(p, "town_transferred_to_you", player.getName(), town.getName());
                            } else {
                                MessageManager.sendMessage(p, "town_transferred", player.getName(), town.getName(), newMayor);
                            }

                        } else {
                            if (HuskTowns.getSettings().doBungee()) {
                                if (uuid == newMayorUUID) {
                                    new PluginMessage(getPlayerName(uuid, connection), PluginMessageType.TRANSFER_YOU_NOTIFICATION,
                                            player.getName(), town.getName()).send(player);
                                } else {
                                    new PluginMessage(getPlayerName(uuid, connection), PluginMessageType.TRANSFER_NOTIFICATION,
                                            player.getName(), town.getName(), newMayor).send(player);
                                }
                            }
                        }
                    }
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void promotePlayer(Player player, String playerToPromote) {
        if (!HuskTowns.getPlayerCache().hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
            return;
        }
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                if (getTownRole(player.getUniqueId(), connection) != Town.TownRole.MAYOR) {
                    MessageManager.sendMessage(player, "error_insufficient_role_privileges");
                    return;
                }
                UUID uuidToPromote = getPlayerUUID(playerToPromote, connection);
                if (uuidToPromote == null) {
                    MessageManager.sendMessage(player, "error_invalid_player");
                    return;
                }
                Town town = getPlayerTown(uuidToPromote, connection);
                if (town == null) {
                    MessageManager.sendMessage(player, "error_not_both_members");
                    return;
                }
                if (!town.getName().equals(getPlayerTown(player.getUniqueId(), connection).getName())) {
                    MessageManager.sendMessage(player, "error_not_both_members");
                    return;
                }
                if (getTownRole(uuidToPromote, connection) == Town.TownRole.MAYOR) {
                    MessageManager.sendMessage(player, "error_cant_promote_self");
                    return;
                }
                if (getTownRole(uuidToPromote, connection) == Town.TownRole.TRUSTED) {
                    MessageManager.sendMessage(player, "error_cant_promote_trusted");
                    return;
                }
                DataManager.setPlayerRoleData(uuidToPromote, Town.TownRole.TRUSTED, connection);
                MessageManager.sendMessage(player, "player_promoted_success", playerToPromote, town.getName());

                // Send a notification to all town members
                for (UUID uuid : town.getMembers().keySet()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (!uuid.toString().equals(player.getUniqueId().toString())) {
                        if (p != null) {
                            if (p.getUniqueId() == uuidToPromote) {
                                MessageManager.sendMessage(p, "have_been_promoted", player.getName(), town.getName());
                            } else {
                                MessageManager.sendMessage(p, "player_promoted", playerToPromote, player.getName(), town.getName());
                            }

                        } else {
                            if (HuskTowns.getSettings().doBungee()) {
                                if (uuid == uuidToPromote) {
                                    new PluginMessage(getPlayerName(uuid, connection), PluginMessageType.PROMOTED_NOTIFICATION_YOURSELF,
                                            player.getName(), town.getName()).send(player);
                                } else {
                                    new PluginMessage(getPlayerName(uuid, connection), PluginMessageType.PROMOTED_NOTIFICATION,
                                            playerToPromote, player.getName(), town.getName()).send(player);
                                }
                            }
                        }
                    }
                }

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void sendTownMenu(Player player, Town town, Connection connection) throws SQLException {
        if (town.getName().equals(HuskTowns.getSettings().getAdminTownName())) {
            MessageManager.sendMessage(player, "admin_town_information");
            return;
        }
        StringBuilder mayorName = new StringBuilder().append("[Mayor:](#00fb9a show_text=&#00fb9a&The head of the town\n&7Can manage residents & claims) ");
        StringBuilder trustedMembers = new StringBuilder().append("[Trustees:](#00fb9a show_text=&#00fb9a&Trusted citizens of the town\n&7Can build anywhere in town\nCan invite new residents\nCan claim new land) ");
        StringBuilder residentMembers = new StringBuilder().append("[Residents:](#00fb9a show_text=&#00fb9a&Standard residents of the town\n&7Default rank for new citizens\nCan build in plots within town) ");

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
                    mayorName.append(playerName).append("show_text=&7UUID: ").append(uuid).append(")");
                    break;
                case RESIDENT:
                    residentMembers.append(playerName).append("show_text=&77UUID: ").append(uuid).append("), ");
                    break;
                case TRUSTED:
                    trustedMembers.append(playerName).append("show_text=&77UUID: ").append(uuid).append("), ");
                    break;
            }
        }

        player.spigot().sendMessage(new MineDown("\n[Town Overview for](#00fb9a) [" + town.getName() + "](#00fb9a bold)").toComponent());
        player.spigot().sendMessage(new MineDown("[Level:](#00fb9a show_text=&#00fb9a&Level of the town\n&7Calculated based on value of coffers) &f" + town.getLevel()).toComponent());
        if (HuskTowns.getSettings().doEconomy()) {
            player.spigot().sendMessage(new MineDown("[Coffers:](#00fb9a show_text=&#00fb9a&Amount of money deposited into town\n&7Money paid in with /town deposit) &f" + Vault.format(town.getMoneyDeposited())).toComponent());
        }
        player.spigot().sendMessage(new MineDown("[Founded:](#00fb9a show_text=&#00fb9a&Date the town was founded.) &f" + town.getFormattedFoundedTime()).toComponent());

        if (HuskTowns.getTownBonusesCache().contains(town.getName())) {
            int bonusClaims = HuskTowns.getTownBonusesCache().getBonusClaims(town.getName());
            if (bonusClaims > 0) {
                player.spigot().sendMessage(new MineDown("[Claims:](#00fb9a show_text=&7Total number of chunks claimed out of maximum possible, based on current town level.) [█](" + town.getTownColorHex() + ") ["
                        + town.getClaimedChunksNumber() + "/" + town.getMaximumClaimedChunks() + "; " + bonusClaims + " bonus](white show_text=&#00fb9a&Click to view a list of claims run_command=/town claims " + town.getName() + ")\n").toComponent());
            } else {
                player.spigot().sendMessage(new MineDown("[Claims:](#00fb9a show_text=&7Total number of chunks claimed out of maximum possible, based on current town level.) [█](" + town.getTownColorHex() + ") ["
                        + town.getClaimedChunksNumber() + "/" + town.getMaximumClaimedChunks() + "](white show_text=&#00fb9a&Click to view a list of claims run_command=/town claims " + town.getName() + ")\n").toComponent());
            }
        } else {
            player.spigot().sendMessage(new MineDown("[Claims:](#00fb9a show_text=&7Total number of chunks claimed out of maximum possible, based on current town level.) [█](" + town.getTownColorHex() + ") ["
                    + town.getClaimedChunksNumber() + "/" + town.getMaximumClaimedChunks() + "](white show_text=&#00fb9a&Click to view a list of claims run_command=/town claims " + town.getName() + ")\n").toComponent());
        }

        if (HuskTowns.getTownBonusesCache().contains(town.getName())) {
            int bonusMembers = HuskTowns.getTownBonusesCache().getBonusMembers(town.getName());
            if (bonusMembers > 0) {
                player.spigot().sendMessage(new MineDown("[Citizens](#00fb9a bold) &#00fb9a&(Population: &f" + town.getMembers().size() + "/" + town.getMaxMembers() + "; " + bonusMembers + " bonus&#00fb9a&)").toComponent());
            } else {
                player.spigot().sendMessage(new MineDown("[Citizens](#00fb9a bold) &#00fb9a&(Population: &f" + town.getMembers().size() + "/" + town.getMaxMembers() + "&#00fb9a&)").toComponent());
            }
        } else {
            player.spigot().sendMessage(new MineDown("[Citizens](#00fb9a bold) &#00fb9a&(Population: &f" + town.getMembers().size() + "/" + town.getMaxMembers() + "&#00fb9a&)").toComponent());
        }

        player.spigot().sendMessage(new MineDown(mayorName.toString()).toComponent());
        player.spigot().sendMessage(new MineDown(trustedMembers.toString().replaceAll(", $", "")).toComponent());
        player.spigot().sendMessage(new MineDown(residentMembers.toString().replaceAll(", $", "")).toComponent());
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
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "town_menu_no_town");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);

                sendTownMenu(player, town, connection);

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void sendTownBonusesList(CommandSender sender, String targetName, int pageNumber) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String townName = targetName;
                if (!townExists(targetName, connection)) {
                    if (playerNameExists(targetName, connection)) {
                        UUID playerUUID = getPlayerUUID(townName, connection);
                        if (playerUUID == null) {
                            MessageManager.sendMessage(sender, "error_town_bonus_invalid_target");
                            return;
                        }
                        if (inTown(playerUUID, connection)) {
                            townName = getPlayerTown(playerUUID, connection).getName();
                        } else {
                            MessageManager.sendMessage(sender, "error_town_bonus_invalid_target");
                            return;
                        }
                    } else {
                        MessageManager.sendMessage(sender, "error_town_bonus_invalid_target");
                        return;
                    }
                }
                ArrayList<TownBonus> bonuses = getTownBonuses(townName, connection);
                if (bonuses == null) {
                    MessageManager.sendMessage(sender, "error_no_town_bonuses", townName);
                    return;
                }
                if (bonuses.isEmpty()) {
                    MessageManager.sendMessage(sender, "error_no_town_bonuses", townName);
                    return;
                }
                ArrayList<String> bonusesListStrings = new ArrayList<>();
                for (TownBonus bonus : bonuses) {
                    StringBuilder bonusesList = new StringBuilder();
                    bonusesList.append("[").append(bonus.getFormattedAppliedTime())
                            .append("](white show_text=&7When the bonus was applied)  [•](gray)  [")
                            .append("+").append(bonus.getBonusClaims()).append(" claims](white)  [•](gray)  [")
                            .append("+").append(bonus.getBonusMembers()).append(" members](white)");

                    if (bonus.getApplierUUID() != null) {
                        bonusesList.append("  [•](gray)  [").append(getPlayerName(bonus.getApplierUUID(), connection)).append("](white show_text=&7The person who applied the bonus.)");
                    } else {
                        bonusesList.append("  [•](gray)  [CONSOLE](white italic show_text=&7This bonus was applied by a console operator.)");
                    }
                    bonusesListStrings.add(bonusesList.toString());
                }

                MessageManager.sendMessage(sender, "town_bonus_list_header", townName, Integer.toString(bonuses.size()));

                PageChatList list = new PageChatList(bonusesListStrings, 10, "/townbonus view " + townName);
                if (list.doesNotContainPage(pageNumber)) {
                    MessageManager.sendMessage(sender, "error_invalid_page_number");
                    return;
                }
                sender.spigot().sendMessage(list.getPage(pageNumber));
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });


    }

    private static void sendClaimList(Player player, Town town, int pageNumber) {
        HashSet<ClaimedChunk> claimedChunks = town.getClaimedChunks();
        if (claimedChunks.isEmpty()) {
            MessageManager.sendMessage(player, "error_no_claims_list", town.getName());
            return;
        }
        ArrayList<String> claimListStrings = new ArrayList<>();
        for (ClaimedChunk chunk : claimedChunks) {
            StringBuilder claimList = new StringBuilder();
            claimList.append("[█](")
                    .append(town.getTownColorHex())
                    .append(") [Claim at ")
                    .append(chunk.getChunkX() * 16)
                    .append(", ")
                    .append(chunk.getChunkZ() * 16)
                    .append(" on world: ")
                    .append(chunk.getWorld())
                    .append(", server: ")
                    .append(chunk.getServer())
                    .append("](gray show_text=")
                    .append("&")
                    .append(town.getTownColorHex())
                    .append("&").append(town.getName()).append("&r\n");

            if (town.getName().equals(HuskTowns.getSettings().getAdminTownName())) {
                claimList.append("&r&")
                        .append(town.getTownColorHex())
                        .append("&Ⓐ &r&#b0b0b0&Admin Claim")
                        .append("&r\n");
            } else {
                switch (chunk.getChunkType()) {
                    case FARM:
                        claimList.append("&r&")
                                .append(town.getTownColorHex())
                                .append("&Ⓕ &r&#b0b0b0&Farming Chunk")
                                .append("&r\n");
                        break;
                    case PLOT:
                        if (chunk.getPlotChunkOwner() != null) {
                            claimList.append("&r&").append(town.getTownColorHex()).append("&Ⓟ&r &#b0b0b0&")
                                    .append(HuskTowns.getPlayerCache().getUsername(chunk.getPlotChunkOwner()))
                                    .append("'s Plot")
                                    .append("&r\n");
                            if (!chunk.getPlotChunkMembers().isEmpty()) {
                                claimList.append("&#b0b0b0&Plot Members: &").append(town.getTownColorHex()).append("&")
                                        .append(chunk.getPlotChunkMembers().size())
                                        .append("&r\n");
                            }
                        } else {
                            claimList.append("&r&")
                                    .append(town.getTownColorHex())
                                    .append("&Ⓟ &r&#b0b0b0&Unclaimed Plot")
                                    .append("&r\n");
                        }
                        break;
                }
            }
            claimList.append("&r&#b0b0b0&Chunk: &").append(town.getTownColorHex()).append("&")
                    .append((chunk.getChunkX() * 16))
                    .append(", ")
                    .append((chunk.getChunkZ() * 16))
                    .append("&r\n")
                    .append("&#b0b0b0&Claimed: &").append(town.getTownColorHex()).append("&")
                    .append(chunk.getFormattedTime());

            if (chunk.getClaimerUUID() != null) {
                String claimedBy = HuskTowns.getPlayerCache().getUsername(chunk.getClaimerUUID());
                claimList.append("&r\n")
                        .append("&#b0b0b0&By: &").append(town.getTownColorHex()).append("&")
                        .append(claimedBy);
            }
            if (chunk.getServer().equals(HuskTowns.getSettings().getServerID())) {
                claimList.append(" run_command=/claim info ")
                        .append(chunk.getChunkX()).append(" ").append(chunk.getChunkZ()).append(" ").append(chunk.getWorld());
            }
            claimList.append(")");
            claimListStrings.add(claimList.toString());
        }

        if (town.getName().equals(HuskTowns.getSettings().getAdminTownName())) {
            MessageManager.sendMessage(player, "admin_claim_list_header",
                    Integer.toString(town.getClaimedChunksNumber()), "∞");
        } else {
            MessageManager.sendMessage(player, "claim_list_header", town.getName(),
                    Integer.toString(town.getClaimedChunksNumber()), Integer.toString(town.getMaximumClaimedChunks()));
        }

        PageChatList list = new PageChatList(claimListStrings, 10, "/claimlist " + town.getName());
        if (list.doesNotContainPage(pageNumber)) {
            MessageManager.sendMessage(player, "error_invalid_page_number");
            return;
        }
        player.spigot().sendMessage(list.getPage(pageNumber));
    }

    public static void showClaimList(Player player, int pageNumber) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);

                sendClaimList(player, town, pageNumber);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void showClaimList(Player player, String townName, int pageNumber) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!townExists(townName, connection)) {
                    MessageManager.sendMessage(player, "error_invalid_town");
                    return;
                }
                Town town = getTownFromName(townName, connection);

                sendClaimList(player, town, pageNumber);
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
                final String townName = townRoleResults.getString("name");
                final double money = townRoleResults.getDouble("money");
                final Timestamp timestamp = townRoleResults.getTimestamp("founded");
                final String greetingMessage = townRoleResults.getString("greeting_message");
                final String farewellMessage = townRoleResults.getString("farewell_message");
                final TeleportationPoint spawnTeleportationPoint = getTeleportationPoint(townRoleResults.getInt("spawn_location_id"), connection);
                final HashSet<ClaimedChunk> claimedChunks = getClaimedChunks(townName, connection);
                final HashMap<UUID, Town.TownRole> members = getTownMembers(townName, connection);
                getTownRole.close();
                return new Town(townName, claimedChunks, members, spawnTeleportationPoint, money, greetingMessage, farewellMessage, timestamp.toInstant().getEpochSecond());
            }
        }
        getTownRole.close();
        return null;
    }

    public static Town getPlayerTown(UUID uuid, Connection connection) throws SQLException {
        PreparedStatement getTown = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() +
                        " WHERE `id`=(SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);");
        getTown.setString(1, uuid.toString());
        ResultSet townResults = getTown.executeQuery();
        if (townResults != null) {
            if (townResults.next()) {
                final String townName = townResults.getString("name");
                final double money = townResults.getDouble("money");
                final Timestamp timestamp = townResults.getTimestamp("founded");
                final String greetingMessage = townResults.getString("greeting_message");
                final String farewellMessage = townResults.getString("farewell_message");
                final TeleportationPoint spawnTeleportationPoint = getTeleportationPoint(townResults.getInt("spawn_location_id"), connection);
                final HashSet<ClaimedChunk> claimedChunks = getClaimedChunks(townName, connection);
                final HashMap<UUID, Town.TownRole> members = getTownMembers(townName, connection);
                getTown.close();
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
                final String server = teleportationPointResults.getString("server");
                final String world = teleportationPointResults.getString("world");
                final double x = teleportationPointResults.getDouble("x");
                final double y = teleportationPointResults.getDouble("y");
                final double z = teleportationPointResults.getDouble("z");
                final float yaw = teleportationPointResults.getFloat("yaw");
                final float pitch = teleportationPointResults.getFloat("pitch");
                getTeleportationPoint.close();
                return new TeleportationPoint(world, x, y, z, yaw, pitch, server);
            }
        }
        getTeleportationPoint.close();
        return null;
    }

    public static Boolean getIsTeleporting(Player player, Connection connection) throws SQLException {
        PreparedStatement getPlayerTeleporting = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?;");
        getPlayerTeleporting.setString(1, player.getUniqueId().toString());
        ResultSet isTeleportingResults = getPlayerTeleporting.executeQuery();
        if (isTeleportingResults.next()) {
            boolean isTeleporting = isTeleportingResults.getBoolean("is_teleporting");
            getPlayerTeleporting.close();
            return isTeleporting;
        } else {
            getPlayerTeleporting.close();
            return null;
        }
    }

    public static TeleportationPoint getPlayerDestination(Player player, Connection connection) throws SQLException {
        PreparedStatement getPlayerDestination = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getLocationsTable() + " WHERE `id`=(SELECT `teleport_destination_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);");
        getPlayerDestination.setString(1, player.getUniqueId().toString());
        ResultSet teleportationPointResults = getPlayerDestination.executeQuery();

        if (teleportationPointResults != null) {
            if (teleportationPointResults.next()) {
                final String server = teleportationPointResults.getString("server");
                final String world = teleportationPointResults.getString("world");
                final double x = teleportationPointResults.getDouble("x");
                final double y = teleportationPointResults.getDouble("y");
                final double z = teleportationPointResults.getDouble("z");
                final float yaw = teleportationPointResults.getFloat("yaw");
                final float pitch = teleportationPointResults.getFloat("pitch");
                getPlayerDestination.close();
                return new TeleportationPoint(world, x, y, z, yaw, pitch, server);
            }
        }
        getPlayerDestination.close();
        return null;
    }

    public static void setPlayerDestinationToSpawn(Player player, Connection connection) throws SQLException {
        Town town = getPlayerTown(player.getUniqueId(), connection);
        PreparedStatement setPlayerDestinationStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `teleport_destination_id`=(SELECT `spawn_location_id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) WHERE `uuid`=?;");
        setPlayerDestinationStatement.setString(1, town.getName());
        setPlayerDestinationStatement.setString(2, player.getUniqueId().toString());
        setPlayerDestinationStatement.executeUpdate();
        setPlayerDestinationStatement.close();
    }

    public static void setPlayerTeleporting(Player player, boolean isTeleporting, Connection connection) throws SQLException {
        PreparedStatement setPlayerTeleportingStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `is_teleporting`=? WHERE `uuid`=?;");
        setPlayerTeleportingStatement.setBoolean(1, isTeleporting);
        setPlayerTeleportingStatement.setString(2, player.getUniqueId().toString());
        setPlayerTeleportingStatement.executeUpdate();
        setPlayerTeleportingStatement.close();
    }

    public static void clearPlayerDestination(Player player, Connection connection) throws SQLException {
        PreparedStatement clearDestinationStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `teleport_destination_id`=NULL) WHERE `uuid`=?;");
        clearDestinationStatement.setString(1, player.getUniqueId().toString());
        clearDestinationStatement.executeUpdate();
        clearDestinationStatement.close();
    }

    public static void deleteTownSpawnData(Player player, Connection connection) throws SQLException {
        PreparedStatement townSpawnData = connection.prepareStatement(
                "DELETE FROM " + HuskTowns.getSettings().getLocationsTable() + " WHERE `id`=(SELECT `spawn_location_id` FROM "
                        + HuskTowns.getSettings().getTownsTable() + " WHERE `id`=(SELECT `town_id` FROM "
                        + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?));");
        townSpawnData.setString(1, player.getUniqueId().toString());
        townSpawnData.executeUpdate();
        townSpawnData.close();
    }


    private static void setTownSpawnData(Player player, TeleportationPoint point, Connection connection) throws SQLException {
        PreparedStatement dataInsertionStatement = connection.prepareStatement("INSERT INTO " + HuskTowns.getSettings().getLocationsTable() + " (server,world,x,y,z,yaw,pitch) VALUES(?,?,?,?,?,?,?);");
        dataInsertionStatement.setString(1, point.getServer());
        dataInsertionStatement.setString(2, point.getWorldName());
        dataInsertionStatement.setDouble(3, point.getX());
        dataInsertionStatement.setDouble(4, point.getY());
        dataInsertionStatement.setDouble(5, point.getZ());
        dataInsertionStatement.setFloat(6, point.getYaw());
        dataInsertionStatement.setFloat(7, point.getPitch());
        dataInsertionStatement.executeUpdate();
        dataInsertionStatement.close();

        String lastInsertString;
        if (HuskTowns.getSettings().getDatabaseType().equalsIgnoreCase("mysql")) {
            lastInsertString = "LAST_INSERT_ID()";
        } else {
            lastInsertString = "last_insert_rowid()";
        }
        PreparedStatement townSpawnData = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getTownsTable() + " SET `spawn_location_id`=(SELECT "
                        + lastInsertString + ") WHERE `id`=(SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable()
                        + " WHERE `uuid`=?);");
        townSpawnData.setString(1, player.getUniqueId().toString());
        townSpawnData.executeUpdate();
        townSpawnData.close();
    }

    private static void createAdminTown(Connection connection) throws SQLException {
        Town adminTown = new Town();
        addTownData(adminTown, connection);
        HuskTowns.getTownMessageCache().setGreetingMessage(HuskTowns.getSettings().getAdminTownName(),
                MessageManager.getRawMessage("admin_claim_greeting_message"));
        HuskTowns.getTownMessageCache().setFarewellMessage(HuskTowns.getSettings().getAdminTownName(),
                MessageManager.getRawMessage("admin_claim_farewell_message"));
    }

    public static void createAdminClaim(Player player, Location location) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!townExists(HuskTowns.getSettings().getAdminTownName(), connection)) {
                    createAdminTown(connection);
                }

                ClaimedChunk chunk = new ClaimedChunk(player, location, HuskTowns.getSettings().getAdminTownName());
                if (isClaimed(chunk.getServer(), chunk.getWorld(), chunk.getChunkX(), chunk.getChunkZ(), connection)) {
                    MessageManager.sendMessage(player, "error_already_claimed");
                    return;
                }

                for (String worldName : HuskTowns.getSettings().getUnClaimableWorlds()) {
                    if (player.getWorld().getName().equals(worldName)) {
                        MessageManager.sendMessage(player, "error_unclaimable_world");
                        return;
                    }
                }

                addAdminClaim(chunk, connection);
                MessageManager.sendMessage(player, "admin_claim_success", Integer.toString(chunk.getChunkX() * 16), Integer.toString(chunk.getChunkZ() * 16));

                Bukkit.getScheduler().runTask(plugin, () -> ClaimViewerUtil.showParticles(player, chunk, 5));

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });

    }

    public static void createTown(Player player, String townName) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Check that the player is not already in a town
                if (inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_already_in_town");
                    return;
                }
                // Check that the town does not exist
                if (townExists(townName, connection)) {
                    MessageManager.sendMessage(player, "error_town_already_exists");
                    return;
                }
                // Check that the town name is of a valid length
                if (townName.length() > 16 || townName.length() < 3) {
                    MessageManager.sendMessage(player, "error_town_name_invalid_length");
                    return;
                }
                // Check that the town name doesn't contain invalid characters
                if (!RegexUtil.TOWN_NAME_PATTERN.matcher(townName).matches()) {
                    MessageManager.sendMessage(player, "error_town_name_invalid_characters");
                    return;
                }
                for (String name : HuskTowns.getSettings().getProhibitedTownNames()) {
                    if (townName.toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT)) || townName.equalsIgnoreCase(HuskTowns.getSettings().getAdminTownName())) {
                        MessageManager.sendMessage(player, "error_town_name_prohibited");
                        return;
                    }
                }
                // Check economy stuff
                if (HuskTowns.getSettings().doEconomy()) {
                    double creationCost = HuskTowns.getSettings().getTownCreationCost();
                    if (creationCost > 0) {
                        if (!Vault.takeMoney(player, creationCost)) {
                            MessageManager.sendMessage(player, "error_insufficient_funds_need", Vault.format(creationCost));
                            return;
                        }
                        MessageManager.sendMessage(player, "money_spent_notice", Vault.format(creationCost), "found a new town");
                    }
                }

                // Insert the town into the database
                Town town = new Town(player, townName);
                addTownData(town, connection);
                setPlayerTownData(player.getUniqueId(), townName, connection);
                setPlayerRoleData(player.getUniqueId(), Town.TownRole.MAYOR, connection);
                HuskTowns.getPlayerCache().setPlayerName(player.getUniqueId(), player.getName());

                HuskTowns.getTownMessageCache().setGreetingMessage(townName,
                        MessageManager.getRawMessage("default_greeting_message", town.getName()));
                HuskTowns.getTownMessageCache().setFarewellMessage(townName,
                        MessageManager.getRawMessage("default_farewell_message", town.getName()));
                MessageManager.sendMessage(player, "town_creation_success", town.getName());

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void renameTown(Player player, String newTownName) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Check that the player is in a town
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                // Check that the player is mayor
                Town.TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role != Town.TownRole.MAYOR) {
                    MessageManager.sendMessage(player, "error_insufficient_rename_privileges");
                    return;
                }
                // Check that the town name is of a valid length
                if (newTownName.length() > 16 || newTownName.length() < 3) {
                    MessageManager.sendMessage(player, "error_town_name_invalid_length");
                    return;
                }
                // Check that the town name doesn't contain invalid characters
                if (!RegexUtil.TOWN_NAME_PATTERN.matcher(newTownName).matches()) {
                    MessageManager.sendMessage(player, "error_town_name_invalid_characters");
                    return;
                }
                for (String name : HuskTowns.getSettings().getProhibitedTownNames()) {
                    if (newTownName.toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT)) || newTownName.equalsIgnoreCase(HuskTowns.getSettings().getAdminTownName())) {
                        MessageManager.sendMessage(player, "error_town_name_prohibited");
                        return;
                    }
                }
                if (townExists(newTownName, connection)) {
                    MessageManager.sendMessage(player, "error_town_already_exists");
                    return;
                }
                // Check economy stuff
                if (HuskTowns.getSettings().doEconomy()) {
                    double renameCost = HuskTowns.getSettings().getRenameCost();
                    if (renameCost > 0) {
                        if (!Vault.takeMoney(player, renameCost)) {
                            MessageManager.sendMessage(player, "error_insufficient_funds_need", Vault.format(renameCost));
                            return;
                        }
                        MessageManager.sendMessage(player, "money_spent_notice", Vault.format(renameCost), "change the town name");
                    }
                }

                // Update the town name on the database & cache
                updateTownName(player.getUniqueId(), town.getName(), newTownName, connection);
                MessageManager.sendMessage(player, "town_rename_success", newTownName);

                // Send a notification to all town members
                for (UUID uuid : town.getMembers().keySet()) {
                    if (!uuid.toString().equals(player.getUniqueId().toString())) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            MessageManager.sendMessage(p, "town_renamed", player.getName(), newTownName);
                        } else {
                            if (HuskTowns.getSettings().doBungee()) {
                                new PluginMessage(getPlayerName(uuid, connection), PluginMessageType.RENAME_NOTIFICATION,
                                        player.getName(), newTownName).send(player);
                            }
                        }
                    }
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void updateTownSpawn(Player player) {
        Connection connection = HuskTowns.getConnection();
        final Location playerLocation = player.getLocation();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Check that the player is in a town
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                // Check that the player is a trusted resident or mayor
                Town.TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == Town.TownRole.RESIDENT) {
                    MessageManager.sendMessage(player, "error_insufficient_set_spawn_privileges");
                    return;
                }
                // Check that the town message is of a valid length
                ClaimedChunk chunk = getClaimedChunk(HuskTowns.getSettings().getServerID(),
                        playerLocation.getWorld().getName(),
                        playerLocation.getChunk().getX(),
                        playerLocation.getChunk().getZ(),
                        connection);
                if (chunk == null) {
                    MessageManager.sendMessage(player, "error_cant_set_spawn_outside_claim");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                if (!chunk.getTown().equals(town.getName())) {
                    MessageManager.sendMessage(player, "error_claim_not_member_of_town", chunk.getTown());
                    return;
                }

                // Check economy stuff
                if (HuskTowns.getSettings().doEconomy()) {
                    double farewellCost = HuskTowns.getSettings().getSetSpawnCost();
                    if (farewellCost > 0) {
                        if (!Vault.takeMoney(player, farewellCost)) {
                            MessageManager.sendMessage(player, "error_insufficient_funds_need", Vault.format(farewellCost));
                            return;
                        }
                        MessageManager.sendMessage(player, "money_spent_notice", Vault.format(farewellCost), "set the town spawn point");
                    }
                }

                // Update the town name on the database & cache
                Location playerLoc = player.getLocation();
                deleteTownSpawnData(player, connection);
                setTownSpawnData(player, new TeleportationPoint(playerLoc, HuskTowns.getSettings().getServerID()), connection);
                MessageManager.sendMessage(player, "town_update_spawn_success");

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void teleportPlayerToSpawn(Player player) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Check that the player is in a town
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                TeleportationPoint spawn = town.getTownSpawn();
                if (spawn == null) {
                    MessageManager.sendMessage(player, "error_town_spawn_not_set");
                    return;
                }

                // Update the town name on the database & cache
                TeleportationHandler.teleportPlayer(player, spawn);
                MessageManager.sendMessage(player, "teleporting_you_to_town_spawn");

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }


    public static void executeTeleportToSpawn(Player player, TeleportationPoint point) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                DataManager.setPlayerDestinationToSpawn(player, connection);
                DataManager.setPlayerTeleporting(player, true, connection);
                PluginMessage.sendPlayer(player, point.getServer());
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void handleTeleportingPlayers(Player player) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Boolean isTeleporting = getIsTeleporting(player, connection);
                if (isTeleporting == null) {
                    return;
                }
                if (isTeleporting) {
                    setPlayerTeleporting(player, false, connection);
                    TeleportationPoint targetPoint = getPlayerDestination(player, connection);
                    if (targetPoint == null) {
                        return;
                    }
                    TeleportationHandler.executeTeleport(player, targetPoint);
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void updateTownFarewell(Player player, String newFarewellMessage) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Check that the player is in a town
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                // Check that the player is a trusted resident or mayor
                Town.TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == Town.TownRole.RESIDENT) {
                    MessageManager.sendMessage(player, "error_insufficient_message_privileges");
                    return;
                }
                // Check that the town message is of a valid length
                if (newFarewellMessage.length() > 255 || newFarewellMessage.length() < 3) {
                    MessageManager.sendMessage(player, "error_town_message_invalid_length");
                    return;
                }
                // Check that the town message doesn't contain invalid characters
                if (!RegexUtil.TOWN_MESSAGE_PATTERN.matcher(newFarewellMessage).matches()) {
                    MessageManager.sendMessage(player, "error_town_message_invalid_characters");
                    return;
                }
                // Check economy stuff
                if (HuskTowns.getSettings().doEconomy()) {
                    double farewellCost = HuskTowns.getSettings().getFarewellCost();
                    if (farewellCost > 0) {
                        if (!Vault.takeMoney(player, farewellCost)) {
                            MessageManager.sendMessage(player, "error_insufficient_funds_need", Vault.format(farewellCost));
                            return;
                        }
                        MessageManager.sendMessage(player, "money_spent_notice", Vault.format(farewellCost), "update the town farewell message");
                    }
                }

                // Update the town name on the database & cache
                updateTownFarewellData(player.getUniqueId(), newFarewellMessage, connection);
                MessageManager.sendMessage(player, "town_update_farewell_success");
                player.spigot().sendMessage(new MineDown("&7\"" + newFarewellMessage + "&7\"").disable(MineDownParser.Option.ADVANCED_FORMATTING).toComponent());

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void updateTownGreeting(Player player, String newGreetingMessage) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Check that the player is in a town
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                // Check that the player is a trusted resident or mayor
                Town.TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == Town.TownRole.RESIDENT) {
                    MessageManager.sendMessage(player, "error_insufficient_message_privileges");
                    return;
                }
                // Check that the town message is of a valid length
                if (newGreetingMessage.length() > 255 || newGreetingMessage.length() < 3) {
                    MessageManager.sendMessage(player, "error_town_message_invalid_length");
                    return;
                }
                // Check that the town message doesn't contain invalid characters
                if (!RegexUtil.TOWN_MESSAGE_PATTERN.matcher(newGreetingMessage).matches()) {
                    MessageManager.sendMessage(player, "error_town_message_invalid_characters");
                    return;
                }
                // Check economy stuff
                if (HuskTowns.getSettings().doEconomy()) {
                    double greetingCost = HuskTowns.getSettings().getGreetingCost();
                    if (greetingCost > 0) {
                        if (!Vault.takeMoney(player, greetingCost)) {
                            MessageManager.sendMessage(player, "error_insufficient_funds_need", Vault.format(greetingCost));
                            return;
                        }
                        MessageManager.sendMessage(player, "money_spent_notice", Vault.format(greetingCost), "update the town greeting message");
                    }
                }

                // Update the town name on the database & cache
                updateTownGreetingData(player.getUniqueId(), newGreetingMessage, connection);
                MessageManager.sendMessage(player, "town_update_greeting_success");
                player.spigot().sendMessage(new MineDown("&7\"" + newGreetingMessage + "&7\"").disable(MineDownParser.Option.ADVANCED_FORMATTING).toComponent());

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static Town.TownRole getTownRole(UUID uuid, Connection connection) throws SQLException {
        PreparedStatement getTownRole = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE uuid=?;");
        getTownRole.setString(1, uuid.toString());
        ResultSet townRoleResults = getTownRole.executeQuery();
        if (townRoleResults != null) {
            if (townRoleResults.next()) {
                final Town.TownRole role = getTownRoleFromID(townRoleResults.getInt("town_role"));
                getTownRole.close();
                return role;
            } else {
                getTownRole.close();
                return null;
            }
        }
        getTownRole.close();
        return null;
    }

    // Returns if a player is in a town
    private static boolean inTown(UUID uuid, Connection connection) throws SQLException {
        PreparedStatement alreadyInTownCheck = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=? AND `town_id` IS NOT NULL;");
        alreadyInTownCheck.setString(1, uuid.toString());
        ResultSet alreadyInTownResult = alreadyInTownCheck.executeQuery();
        if (alreadyInTownResult != null) {
            final boolean inTown = alreadyInTownResult.next();
            alreadyInTownCheck.close();
            return inTown;
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
            final boolean isClaimed = checkClaimedResult.next();
            checkClaimed.close();
            return isClaimed;
        }
        checkClaimed.close();
        return false;
    }

    private static void addAdminClaim(ClaimedChunk chunk, Connection connection) throws SQLException {
        PreparedStatement claimCreationStatement = connection.prepareStatement(
                "INSERT INTO " + HuskTowns.getSettings().getClaimsTable() + " (town_id,claim_time,claimer_id,server,world,chunk_x,chunk_z,chunk_type) " +
                        "VALUES((SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE name=?),?," +
                        "(SELECT `id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?),?,?,?,?,0);");
        claimCreationStatement.setString(1, HuskTowns.getSettings().getAdminTownName());
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

    private static void addClaimData(ClaimedChunk chunk, Connection connection) throws SQLException {
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
        try {
            claimCreationStatement.executeUpdate();
            HuskTowns.getClaimCache().add(chunk);
            claimCreationStatement.close();
            return;
        } catch (SQLIntegrityConstraintViolationException ignored) {
        }
        claimCreationStatement.close();
    }

    private static void addBonusData(TownBonus bonus, String townName, Connection connection) throws SQLException {
        PreparedStatement bonusCreationStatement;
        if (bonus.getApplierUUID() != null) {
            bonusCreationStatement = connection.prepareStatement(
                    "INSERT INTO " + HuskTowns.getSettings().getBonusesTable() + " (town_id,applier_id,applied_time,bonus_claims,bonus_members) " +
                            "VALUES((SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE name=?)," +
                            "(SELECT `id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?),?,?,?);");
            bonusCreationStatement.setString(1, townName);
            bonusCreationStatement.setString(2, bonus.getApplierUUID().toString());
            bonusCreationStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            bonusCreationStatement.setInt(4, bonus.getBonusClaims());
            bonusCreationStatement.setInt(5, bonus.getBonusMembers());
        } else {
            bonusCreationStatement = connection.prepareStatement(
                    "INSERT INTO " + HuskTowns.getSettings().getBonusesTable() + " (town_id,applied_time,bonus_claims,bonus_members) " +
                            "VALUES((SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE name=?),?,?,?);");
            bonusCreationStatement.setString(1, townName);
            bonusCreationStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            bonusCreationStatement.setInt(3, bonus.getBonusClaims());
            bonusCreationStatement.setInt(4, bonus.getBonusMembers());
        }
        bonusCreationStatement.executeUpdate();
        bonusCreationStatement.close();

        HuskTowns.getTownBonusesCache().add(townName, bonus);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                final UUID applierUUID = bonus.getApplierUUID();
                if (applierUUID != null) {
                    new PluginMessage(PluginMessageType.ADD_TOWN_BONUS, townName, Integer.toString(bonus.getBonusClaims()), Integer.toString(bonus.getBonusMembers()), Long.toString(bonus.getAppliedTimestamp()), applierUUID.toString()).sendToAll(updateNotificationDispatcher);
                } else {
                    new PluginMessage(PluginMessageType.ADD_TOWN_BONUS, townName, Integer.toString(bonus.getBonusClaims()), Integer.toString(bonus.getBonusMembers()), Long.toString(bonus.getAppliedTimestamp())).sendToAll(updateNotificationDispatcher);
                }
                return;
            }
        }
    }

    public static void claimChunk(Player player, Location claimLocation) {
        final ClaimCache claimCache = HuskTowns.getClaimCache();
        if (!claimCache.hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", claimCache.getName());
            return;
        }

        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }

                ClaimedChunk chunk = new ClaimedChunk(player, claimLocation, getPlayerTown(player.getUniqueId(), connection).getName());
                if (isClaimed(chunk.getServer(), chunk.getWorld(), chunk.getChunkX(), chunk.getChunkZ(), connection)) {
                    MessageManager.sendMessage(player, "error_already_claimed");
                    return;
                }

                Town.TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == Town.TownRole.RESIDENT) {
                    MessageManager.sendMessage(player, "error_insufficient_claim_privileges");
                    return;
                }

                for (String worldName : HuskTowns.getSettings().getUnClaimableWorlds()) {
                    if (player.getWorld().getName().equals(worldName)) {
                        MessageManager.sendMessage(player, "error_unclaimable_world");
                        return;
                    }
                }

                Town town = getPlayerTown(player.getUniqueId(), connection);
                if (town.getClaimedChunks().size() >= town.getMaximumClaimedChunks()) {
                    MessageManager.sendMessage(player, "error_maximum_claims_made", Integer.toString(town.getMaximumClaimedChunks()));
                    return;
                }

                // Charge for setting the town spawn if needed
                if (HuskTowns.getSettings().doEconomy() && town.getClaimedChunks().size() == 0 && HuskTowns.getSettings().setTownSpawnInFirstClaim()) {
                    double spawnCost = HuskTowns.getSettings().getSetSpawnCost();
                    if (spawnCost > 0) {
                        if (!Vault.takeMoney(player, spawnCost)) {
                            MessageManager.sendMessage(player, "error_insufficient_funds_need", Vault.format(spawnCost));
                            return;
                        }
                        MessageManager.sendMessage(player, "money_spent_notice", Vault.format(spawnCost), "create a claim and set the town spawn point");
                    }
                }

                addClaimData(chunk, connection);
                MessageManager.sendMessage(player, "claim_success", Integer.toString(chunk.getChunkX() * 16), Integer.toString(chunk.getChunkZ() * 16));

                if (town.getClaimedChunks().size() == 0 && HuskTowns.getSettings().setTownSpawnInFirstClaim()) {
                    setTownSpawnData(player, new TeleportationPoint(player.getLocation(), HuskTowns.getSettings().getServerID()), connection);
                }
                Bukkit.getScheduler().runTask(plugin, () -> {
                    ClaimViewerUtil.showParticles(player, chunk, 5);
                });

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static UUID getPlayerUUID(String username, Connection connection) throws SQLException {
        PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE username=?;");
        existStatement.setString(1, username);
        ResultSet resultSet = existStatement.executeQuery();
        if (resultSet != null) {
            if (resultSet.next()) {
                final String userUUID = resultSet.getString("uuid");
                existStatement.close();
                if (userUUID == null) {
                    return null;
                } else {
                    return UUID.fromString(userUUID);
                }
            }
        }
        existStatement.close();
        return null;
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
                final String userUUID = resultSet.getString("uuid");
                existStatement.close();
                if (userUUID == null) {
                    return null;
                } else {
                    return UUID.fromString(userUUID);
                }
            }
        }
        existStatement.close();
        return null;
    }

    private static ClaimedChunk.ChunkType getChunkType(int chunkTypeID) {
        switch (chunkTypeID) {
            case 0:
                return ClaimedChunk.ChunkType.REGULAR;
            case 1:
                return ClaimedChunk.ChunkType.FARM;
            case 2:
                return ClaimedChunk.ChunkType.PLOT;
        }
        return null;
    }

    private static Integer getIDFromChunkType(ClaimedChunk.ChunkType type) {
        switch (type) {
            case REGULAR:
                return 0;
            case FARM:
                return 1;
            case PLOT:
                return 2;
        }
        return null;
    }

    private static void addPlotMemberData(ClaimedChunk chunk, UUID plotMember, Connection connection) throws SQLException {
        try(PreparedStatement addPlotMemberStatement = connection.prepareStatement("INSERT INTO " + HuskTowns.getSettings().getPlotMembersTable() + "(`claim_id`,`member_id`) VALUES ((SELECT `id` FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE chunk_x=? AND chunk_z=? AND world=? AND server=?),(SELECT `id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?));")) {
            addPlotMemberStatement.setInt(1, chunk.getChunkX());
            addPlotMemberStatement.setInt(2, chunk.getChunkZ());
            addPlotMemberStatement.setString(3, chunk.getWorld());
            addPlotMemberStatement.setString(4, chunk.getServer());
            addPlotMemberStatement.setString(5, plotMember.toString());
        }
        // Update in the cache (this does NOT need to be updated cross-server since the cache only holds claims on this server)
        HuskTowns.getClaimCache().getChunkAt(chunk.getChunkX(), chunk.getChunkZ(), chunk.getWorld()).addPlotMember(plotMember);
    }

    public static void addPlotMember(Player adder, ClaimedChunk claimedChunk, String newPlotMember) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
           try {
               if (!inTown(adder.getUniqueId(), connection)) {
                   MessageManager.sendMessage(adder, "error_not_in_town");
                   return;
               }
               if (claimedChunk == null) {
                   MessageManager.sendMessage(adder, "error_not_standing_on_claim");
                   return;
               }
               Town town = getPlayerTown(adder.getUniqueId(), connection);
               if (!town.getName().equals(claimedChunk.getTown())) {
                   MessageManager.sendMessage(adder, "error_claim_not_member_of_town", claimedChunk.getTown());
                   return;
               }
               if (claimedChunk.getChunkType() != ClaimedChunk.ChunkType.PLOT) {
                   MessageManager.sendMessage(adder, "error_not_a_plot");
                   return;
               }
               if (claimedChunk.getPlotChunkOwner() == null) {
                   MessageManager.sendMessage(adder, "error_plot_not_claimed");
                   return;
               }
               Town.TownRole role = getTownRole(adder.getUniqueId(), connection);
               if (role == Town.TownRole.RESIDENT) {
                   if (claimedChunk.getPlotChunkOwner() != adder.getUniqueId()) {
                       MessageManager.sendMessage(adder, "error_not_your_plot");
                       return;
                   }
               }
               if (!playerNameExists(newPlotMember, connection)) {
                   MessageManager.sendMessage(adder, "error_invalid_player");
                   return;
               }
               UUID targetPlayerUUID = getPlayerUUID(newPlotMember, connection);
               addPlotMemberData(claimedChunk, targetPlayerUUID, connection);
           } catch (SQLException exception) {
               plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
           }
        });
    }

    private static void removePlotMemberData(ClaimedChunk chunk, UUID plotMember, Connection connection) throws SQLException {
        try(PreparedStatement addPlotMemberStatement = connection.prepareStatement("DELETE FROM " + HuskTowns.getSettings().getPlotMembersTable() + " WHERE `claim_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE chunk_x=? AND chunk_z=? AND world=? AND server=?) AND `member_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);")) {
            addPlotMemberStatement.setInt(1, chunk.getChunkX());
            addPlotMemberStatement.setInt(2, chunk.getChunkZ());
            addPlotMemberStatement.setString(3, chunk.getWorld());
            addPlotMemberStatement.setString(4, chunk.getServer());
            addPlotMemberStatement.setString(5, plotMember.toString());
        }
        // Update in the cache (this does NOT need to be updated cross-server since the cache only holds claims on this server)
        HuskTowns.getClaimCache().getChunkAt(chunk.getChunkX(), chunk.getChunkZ(), chunk.getWorld()).removePlotMember(plotMember);
    }

    public static void removePlotMember(Player adder, ClaimedChunk claimedChunk, String plotMemberToRemove) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(adder.getUniqueId(), connection)) {
                    MessageManager.sendMessage(adder, "error_not_in_town");
                    return;
                }
                if (claimedChunk == null) {
                    MessageManager.sendMessage(adder, "error_not_standing_on_claim");
                    return;
                }
                Town town = getPlayerTown(adder.getUniqueId(), connection);
                if (!town.getName().equals(claimedChunk.getTown())) {
                    MessageManager.sendMessage(adder, "error_claim_not_member_of_town", claimedChunk.getTown());
                    return;
                }
                if (claimedChunk.getChunkType() != ClaimedChunk.ChunkType.PLOT) {
                    MessageManager.sendMessage(adder, "error_not_a_plot");
                    return;
                }
                if (claimedChunk.getPlotChunkOwner() == null) {
                    MessageManager.sendMessage(adder, "error_plot_not_claimed");
                    return;
                }
                Town.TownRole role = getTownRole(adder.getUniqueId(), connection);
                if (role == Town.TownRole.RESIDENT) {
                    if (claimedChunk.getPlotChunkOwner() != adder.getUniqueId()) {
                        MessageManager.sendMessage(adder, "error_not_your_plot");
                        return;
                    }
                }
                if (!playerNameExists(plotMemberToRemove, connection)) {
                    MessageManager.sendMessage(adder, "error_invalid_player");
                    return;
                }
                UUID targetPlayerUUID = getPlayerUUID(plotMemberToRemove, connection);
                if (!getPlotMembers(claimedChunk, connection).contains(targetPlayerUUID)) {
                    removePlotMemberData(claimedChunk, targetPlayerUUID, connection);
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    private static void clearPlotMembers(ClaimedChunk chunk, Connection connection) throws SQLException {
        try(PreparedStatement deletePlotMembersStatement = connection.prepareStatement("DELETE FROM " + HuskTowns.getSettings().getPlotMembersTable() + " WHERE `claim_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE chunk_x=? AND chunk_z=? AND world=? AND server=?);")) {
            deletePlotMembersStatement.setInt(1, chunk.getChunkX());
            deletePlotMembersStatement.setInt(2, chunk.getChunkZ());
            deletePlotMembersStatement.setString(3, chunk.getWorld());
            deletePlotMembersStatement.setString(4, chunk.getServer());
            deletePlotMembersStatement.executeUpdate();
        }
    }

    private static HashSet<UUID> getPlotMembers(ClaimedChunk chunk, Connection connection) throws SQLException {
        try(PreparedStatement getPlotMembersStatement = connection.prepareStatement("SELECT * FROM " + HuskTowns.getSettings().getPlotMembersTable() + " WHERE `claim_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE chunk_x=? AND chunk_z=? AND world=? AND server=?);")) {
            getPlotMembersStatement.setInt(1, chunk.getChunkX());
            getPlotMembersStatement.setInt(2, chunk.getChunkZ());
            getPlotMembersStatement.setString(3, chunk.getWorld());
            getPlotMembersStatement.setString(4, chunk.getServer());
            ResultSet resultSet = getPlotMembersStatement.executeQuery();
            HashSet<UUID> plotMembers = new HashSet<>();
            if (resultSet != null) {
                while (resultSet.next()) {
                    plotMembers.add(getPlayerUUID(resultSet.getInt("member_id"), connection));
                }
                return plotMembers;
            }
        }
        return null;
    }

    private static HashSet<UUID> getPlotMembers(int plotClaimID, Connection connection) throws SQLException {
        try(PreparedStatement getPlotMembersStatement = connection.prepareStatement("SELECT * FROM " + HuskTowns.getSettings().getPlotMembersTable() + " WHERE `claim_id`=?;")) {
            getPlotMembersStatement.setInt(1, plotClaimID);
            ResultSet resultSet = getPlotMembersStatement.executeQuery();
            HashSet<UUID> plotMembers = new HashSet<>();
            if (resultSet != null) {
                while (resultSet.next()) {
                    plotMembers.add(getPlayerUUID(resultSet.getInt("member_id"), connection));
                }
                return plotMembers;
            }
        }
        return null;
    }

    public static ClaimedChunk getClaimedChunk(String server, String worldName, int chunkX, int chunkZ, Connection connection) throws SQLException {
        PreparedStatement checkClaimed = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE chunk_x=? AND chunk_z=? AND world=? AND server=?;");
        checkClaimed.setInt(1, chunkX);
        checkClaimed.setInt(2, chunkZ);
        checkClaimed.setString(3, worldName);
        checkClaimed.setString(4, server);
        ResultSet checkClaimedResult = checkClaimed.executeQuery();

        if (checkClaimedResult != null) {
            if (checkClaimedResult.next()) {
                final ClaimedChunk.ChunkType chunkType = getChunkType(checkClaimedResult.getInt("chunk_type"));
                final Timestamp timestamp = checkClaimedResult.getTimestamp("claim_time");
                final String townName = getTownFromID(checkClaimedResult.getInt("town_id"), connection).getName();
                final String world = checkClaimedResult.getString("world");
                final UUID claimerUUID = getPlayerUUID(checkClaimedResult.getInt("claimer_id"), connection);
                if (chunkType == ClaimedChunk.ChunkType.PLOT) {
                    final UUID plotOwnerUUID = getPlayerUUID(checkClaimedResult.getInt("plot_owner_id"), connection);
                    final HashSet<UUID> plotMembers = getPlotMembers(checkClaimedResult.getInt("id"), connection);
                    checkClaimed.close();
                    return new ClaimedChunk(server, world, chunkX, chunkZ, claimerUUID, chunkType, plotOwnerUUID, plotMembers, townName, timestamp.toInstant().getEpochSecond());
                } else {
                    checkClaimed.close();
                    return new ClaimedChunk(server, world, chunkX, chunkZ, claimerUUID, chunkType, townName, timestamp.toInstant().getEpochSecond());
                }
            }
        }
        checkClaimed.close();
        return null;
    }

    public static HashMap<UUID, Town.TownRole> getTownMembers(String townName, Connection connection) throws SQLException {
        PreparedStatement getMembers = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `town_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?);");
        getMembers.setString(1, townName);
        ResultSet memberResult = getMembers.executeQuery();

        final HashMap<UUID, Town.TownRole> members = new HashMap<>();
        if (memberResult != null) {
            while (memberResult.next()) {
                members.put(UUID.fromString(memberResult.getString("uuid")),
                        getTownRoleFromID(memberResult.getInt("town_role")));
            }
        }
        getMembers.close();
        return members;
    }

    // Returns a list of a town's chunks on ALL servers
    public static HashSet<ClaimedChunk> getClaimedChunks(String townName, Connection connection) throws SQLException {
        PreparedStatement getChunks = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE `town_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) ORDER BY `claim_time` ASC;");
        getChunks.setString(1, townName);
        ResultSet chunkResults = getChunks.executeQuery();
        HashSet<ClaimedChunk> chunks = new HashSet<>();
        if (chunkResults != null) {
            while (chunkResults.next()) {
                final ClaimedChunk.ChunkType chunkType = getChunkType(chunkResults.getInt("chunk_type"));
                final Timestamp timestamp = chunkResults.getTimestamp("claim_time");
                final String server = chunkResults.getString("server");
                final String world = chunkResults.getString("world");
                final int chunkX = chunkResults.getInt("chunk_x");
                final int chunkZ = chunkResults.getInt("chunk_z");
                final UUID claimerUUID = getPlayerUUID(chunkResults.getInt("claimer_id"), connection);
                if (chunkType == ClaimedChunk.ChunkType.PLOT) {
                    final UUID plotOwnerUUID = getPlayerUUID(chunkResults.getInt("plot_owner_id"), connection);
                    final HashSet<UUID> plotMembers = getPlotMembers(chunkResults.getInt("id"), connection);
                    chunks.add(new ClaimedChunk(server, world, chunkX, chunkZ, claimerUUID, chunkType, plotOwnerUUID, plotMembers, townName, timestamp.toInstant().getEpochSecond()));
                } else {
                    chunks.add(new ClaimedChunk(server, world, chunkX, chunkZ, claimerUUID, chunkType, townName, timestamp.toInstant().getEpochSecond()));
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
    public static void updateTownMessageCache() {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (HuskTowns.getTownMessageCache().getStatus() == Cache.CacheStatus.UPDATING) {
                    return;
                }
                HuskTowns.getTownMessageCache().setStatus(Cache.CacheStatus.UPDATING);
                PreparedStatement towns = connection.prepareStatement(
                        "SELECT * FROM " + HuskTowns.getSettings().getTownsTable());
                ResultSet townResults = towns.executeQuery();

                if (townResults != null) {
                    while (townResults.next()) {
                        final String townName = townResults.getString("name");
                        final String welcomeMessage = townResults.getString("greeting_message");
                        final String farewellMessage = townResults.getString("farewell_message");
                        HuskTowns.getTownMessageCache().setGreetingMessage(townName, welcomeMessage);
                        HuskTowns.getTownMessageCache().setFarewellMessage(townName, farewellMessage);
                    }
                }
                towns.close();
                HuskTowns.getTownMessageCache().setStatus(Cache.CacheStatus.LOADED);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
                HuskTowns.getTownMessageCache().setStatus(Cache.CacheStatus.ERROR);
            }
        });
    }

    // Returns ALL claimed chunks on the server
    public static void updateClaimedChunkCache() {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (HuskTowns.getClaimCache().getStatus() == Cache.CacheStatus.UPDATING) {
                    return;
                }

                HuskTowns.getClaimCache().setStatus(Cache.CacheStatus.UPDATING);
                plugin.getLogger().info("Loading claim data into cache...");

                PreparedStatement getChunks = connection.prepareStatement(
                        "SELECT * FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE `server`=?");
                getChunks.setString(1, HuskTowns.getSettings().getServerID());
                ResultSet chunkResults = getChunks.executeQuery();
                HashSet<ClaimedChunk> chunksToAdd = new HashSet<>();

                if (chunkResults != null) {
                    while (chunkResults.next()) {
                        final ClaimedChunk.ChunkType chunkType = getChunkType(chunkResults.getInt("chunk_type"));
                        final String server = chunkResults.getString("server");
                        final String world = chunkResults.getString("world");
                        final int chunkX = chunkResults.getInt("chunk_x");
                        final int chunkZ = chunkResults.getInt("chunk_z");
                        final Timestamp timestamp = chunkResults.getTimestamp("claim_time");
                        final UUID claimerUUID = getPlayerUUID(chunkResults.getInt("claimer_id"), connection);
                        final String townName = getTownFromID(chunkResults.getInt("town_id"), connection).getName();
                        if (chunkType == ClaimedChunk.ChunkType.PLOT) {
                            final UUID plotOwnerUUID = getPlayerUUID(chunkResults.getInt("plot_owner_id"), connection);
                            final HashSet<UUID> plotMembers = getPlotMembers(chunkResults.getInt("id"), connection);
                            chunksToAdd.add(new ClaimedChunk(server, world, chunkX, chunkZ, claimerUUID, chunkType, plotOwnerUUID, plotMembers, townName, timestamp.toInstant().getEpochSecond()));
                        } else {
                            chunksToAdd.add(new ClaimedChunk(server, world, chunkX, chunkZ, claimerUUID, chunkType, townName, timestamp.toInstant().getEpochSecond()));
                        }
                    }
                }
                getChunks.close();
                for (ClaimedChunk chunk : chunksToAdd) {
                    HuskTowns.getClaimCache().add(chunk);
                }
                HuskTowns.getClaimCache().setStatus(Cache.CacheStatus.LOADED);
                plugin.getLogger().info("Claim data caching complete (took " + HuskTowns.getClaimCache().getTimeSinceInitialization() + " secs)");
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
                HuskTowns.getClaimCache().setStatus(Cache.CacheStatus.ERROR);
            }
        });
    }

    private static HashSet<UUID> getPlayers(Connection connection) throws SQLException {
        PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + ";");
        ResultSet resultSet = existStatement.executeQuery();
        final HashSet<UUID> players = new HashSet<>();
        if (resultSet != null) {
            while (resultSet.next()) {
                players.add(UUID.fromString(resultSet.getString("uuid")));
            }
        }
        existStatement.close();
        return players;
    }

    // Remove the claim data and cache information
    private static void deleteClaimData(ClaimedChunk claimedChunk, Connection connection) throws SQLException {
        PreparedStatement claimRemovalStatement = connection.prepareStatement(
                "DELETE FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE `town_id`=" +
                        "(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) " +
                        "AND `server`=? AND `world`=? AND `chunk_x`=? AND `chunk_z`=?;");
        claimRemovalStatement.setString(1, claimedChunk.getTown());
        claimRemovalStatement.setString(2, claimedChunk.getServer());
        claimRemovalStatement.setString(3, claimedChunk.getWorld());
        claimRemovalStatement.setInt(4, claimedChunk.getChunkX());
        claimRemovalStatement.setInt(5, claimedChunk.getChunkZ());

        claimRemovalStatement.executeUpdate();
        claimRemovalStatement.close();
        HuskTowns.getClaimCache().remove(claimedChunk.getChunkX(), claimedChunk.getChunkZ(), claimedChunk.getWorld());
    }

    public static void changeToFarm(Player player, ClaimedChunk claimedChunk) {
        ClaimCache cache = HuskTowns.getClaimCache();
        if (!cache.hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", cache.getName());
            return;
        }

        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                if (claimedChunk == null) {
                    MessageManager.sendMessage(player, "error_not_standing_on_claim");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                if (!town.getName().equals(claimedChunk.getTown())) {
                    MessageManager.sendMessage(player, "error_claim_not_member_of_town", claimedChunk.getTown());
                    return;
                }
                Town.TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == Town.TownRole.RESIDENT) {
                    MessageManager.sendMessage(player, "error_insufficient_claim_privileges");
                    return;
                }
                if (claimedChunk.getChunkType() == ClaimedChunk.ChunkType.PLOT) {
                    MessageManager.sendMessage(player, "error_already_plot_chunk");
                    return;
                }
                if (claimedChunk.getChunkType() == ClaimedChunk.ChunkType.FARM) {
                    setChunkType(claimedChunk, ClaimedChunk.ChunkType.REGULAR, connection);
                    MessageManager.sendMessage(player, "make_regular_success", Integer.toString(claimedChunk.getChunkX()), Integer.toString(claimedChunk.getChunkZ()));
                } else {
                    setChunkType(claimedChunk, ClaimedChunk.ChunkType.FARM, connection);
                    MessageManager.sendMessage(player, "make_farm_success", Integer.toString(claimedChunk.getChunkX()), Integer.toString(claimedChunk.getChunkZ()));
                }
                Bukkit.getScheduler().runTask(plugin, () -> ClaimViewerUtil.showParticles(player, claimedChunk, 5));
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void assignPlotPlayer(Player assignee, String playerNameToAssign, ClaimedChunk claimedChunk) {
        if (!HuskTowns.getPlayerCache().hasLoaded()) {
            MessageManager.sendMessage(assignee, "error_cache_updating", "Player Data");
            return;
        }
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(assignee.getUniqueId(), connection)) {
                    MessageManager.sendMessage(assignee, "error_not_in_town");
                    return;
                }
                if (claimedChunk == null) {
                    MessageManager.sendMessage(assignee, "error_not_standing_on_claim");
                    return;
                }
                Town assigneeTown = getPlayerTown(assignee.getUniqueId(), connection);
                if (!assigneeTown.getName().equals(claimedChunk.getTown())) {
                    MessageManager.sendMessage(assignee, "error_claim_not_member_of_town", claimedChunk.getTown());
                    return;
                }
                UUID playerToBeAssigned = getPlayerUUID(playerNameToAssign, connection);
                if (playerToBeAssigned == null) {
                    MessageManager.sendMessage(assignee, "error_invalid_player");
                    return;
                }
                Town playerToBeAssignedTown = getPlayerTown(playerToBeAssigned, connection);
                if (!playerToBeAssignedTown.getName().equals(claimedChunk.getTown())) {
                    MessageManager.sendMessage(assignee, "error_claim_other_not_member_of_town",
                            playerNameToAssign, claimedChunk.getTown());
                    return;
                }
                if (getTownRole(assignee.getUniqueId(), connection) == Town.TownRole.RESIDENT) {
                    MessageManager.sendMessage(assignee, "error_insufficient_assign_privileges");
                    return;
                }
                if (claimedChunk.getChunkType() != ClaimedChunk.ChunkType.PLOT) {
                    MessageManager.sendMessage(assignee, "error_not_a_plot");
                    return;
                }
                if (claimedChunk.getPlotChunkOwner() != null) {
                    MessageManager.sendMessage(assignee, "error_assign_plot_already_claimed", HuskTowns.getPlayerCache().getUsername(claimedChunk.getPlotChunkOwner()));
                    return;
                }
                setPlotOwner(claimedChunk, playerToBeAssigned, connection);
                if (playerNameToAssign.equals(assignee.getName())) {
                    MessageManager.sendMessage(assignee, "assigned_plot_success", "yourself", Integer.toString(claimedChunk.getChunkX()),
                            Integer.toString(claimedChunk.getChunkZ()), claimedChunk.getWorld());
                } else {
                    MessageManager.sendMessage(assignee, "assigned_plot_success", playerNameToAssign, Integer.toString(claimedChunk.getChunkX()),
                            Integer.toString(claimedChunk.getChunkZ()), claimedChunk.getWorld());
                }
                Bukkit.getScheduler().runTask(plugin, () -> ClaimViewerUtil.showParticles(assignee, claimedChunk, 5));
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void sendTownList(Player player, TownListCommand.TownListOrderType orderBy, int pageNumber) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                ArrayList<Town> townList;
                switch (orderBy) {
                    case BY_NAME:
                        townList = getTownsByName(connection);
                        break;
                    case BY_LEVEL:
                        townList = getTownsByLevel(connection);
                        break;
                    case BY_NEWEST:
                        townList = getTownsByNewest(connection);
                        break;
                    case BY_OLDEST:
                        townList = getTownsByOldest(connection);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected town list order type: " + orderBy);
                }
                ArrayList<String> pages = new ArrayList<>();
                int adminTownAdjustmentSize = 0;
                for (Town town : townList) {
                    if (town.getName().equals(HuskTowns.getSettings().getAdminTownName())) {
                        adminTownAdjustmentSize = 1;
                        continue;
                    }
                    pages.add("[" + town.getName() + "](" + town.getTownColorHex() + " show_text=&" + town.getTownColorHex() + "&" + town.getName() + "\nFounded: &f" + town.getFormattedFoundedTime() + " run_command=/town info " + town.getName() + ")  [•](dark_gray)  [☻" + town.getMembers().size() + "/" + town.getMaxMembers() + "](gray show_text=&7Number of members out of max members run_command=/town info " + town.getName() + ")  [•](dark_gray)  [" + town.getClaimedChunksNumber() + "/" + town.getMaximumClaimedChunks() + "](gray show_text=&7Number of claims made out of max claims, including bonuses run_command=/claimslist " + town.getName() + ")  [•](dark_gray)  [Lv." + town.getLevel() + "](gray show_text=&7The town's level based on money deposited.)  [•](dark_gray)  [" + town.getFormattedFoundedTime() + "](gray show_text=&7When the town was founded)");
                }
                MessageManager.sendMessage(player, "town_list_header", orderBy.toString().toLowerCase(Locale.ENGLISH).replace("_", " "), Integer.toString(townList.size() - adminTownAdjustmentSize));
                player.spigot().sendMessage(new PageChatList(pages, 10, "/townlist " + orderBy.toString().toLowerCase(Locale.ENGLISH)).getPage(pageNumber));
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void claimPlot(Player player, ClaimedChunk claimedChunk) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                if (claimedChunk == null) {
                    MessageManager.sendMessage(player, "error_not_standing_on_claim");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                if (!town.getName().equals(claimedChunk.getTown())) {
                    MessageManager.sendMessage(player, "error_claim_not_member_of_town", claimedChunk.getTown());
                    return;
                }
                if (claimedChunk.getChunkType() != ClaimedChunk.ChunkType.PLOT) {
                    MessageManager.sendMessage(player, "error_not_a_plot");
                    return;
                }
                if (claimedChunk.getPlotChunkOwner() != null) {
                    MessageManager.sendMessage(player, "error_plot_already_claimed", HuskTowns.getPlayerCache().getUsername(claimedChunk.getPlotChunkOwner()));
                    return;
                }
                setPlotOwner(claimedChunk, player.getUniqueId(), connection);
                MessageManager.sendMessage(player, "claimed_plot_success", Integer.toString(claimedChunk.getChunkX()),
                        Integer.toString(claimedChunk.getChunkZ()), claimedChunk.getWorld());
                Bukkit.getScheduler().runTask(plugin, () -> ClaimViewerUtil.showParticles(player, claimedChunk, 5));
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void unClaimPlot(Player player, ClaimedChunk claimedChunk) {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                if (claimedChunk == null) {
                    MessageManager.sendMessage(player, "error_not_standing_on_claim");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                if (!town.getName().equals(claimedChunk.getTown())) {
                    MessageManager.sendMessage(player, "error_claim_not_member_of_town", claimedChunk.getTown());
                    return;
                }
                if (claimedChunk.getChunkType() != ClaimedChunk.ChunkType.PLOT) {
                    MessageManager.sendMessage(player, "error_not_a_plot");
                    return;
                }
                if (claimedChunk.getPlotChunkOwner() != player.getUniqueId()) {
                    Town.TownRole unClaimerRole = getTownRole(player.getUniqueId(), connection);
                    if (unClaimerRole == Town.TownRole.RESIDENT) {
                        MessageManager.sendMessage(player, "error_not_your_plot");
                        return;
                    }
                }
                clearPlotOwner(claimedChunk, connection);
                clearPlotMembers(claimedChunk, connection);
                MessageManager.sendMessage(player, "unclaimed_plot_success", claimedChunk.getTown());
                Bukkit.getScheduler().runTask(plugin, () -> ClaimViewerUtil.showParticles(player, claimedChunk, 5));
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void changeToPlot(Player player, ClaimedChunk claimedChunk) {
        ClaimCache cache = HuskTowns.getClaimCache();
        if (!cache.hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", cache.getName());
            return;
        }

        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                if (claimedChunk == null) {
                    MessageManager.sendMessage(player, "error_not_standing_on_claim");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                if (!town.getName().equals(claimedChunk.getTown())) {
                    MessageManager.sendMessage(player, "error_claim_not_member_of_town", claimedChunk.getTown());
                    return;
                }
                Town.TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == Town.TownRole.RESIDENT) {
                    MessageManager.sendMessage(player, "error_insufficient_claim_privileges");
                    return;
                }
                if (claimedChunk.getChunkType() == ClaimedChunk.ChunkType.FARM) {
                    MessageManager.sendMessage(player, "error_already_farm_chunk");
                    return;
                }
                if (claimedChunk.getChunkType() == ClaimedChunk.ChunkType.PLOT) {
                    setChunkType(claimedChunk, ClaimedChunk.ChunkType.REGULAR, connection);
                    clearPlotOwner(claimedChunk, connection);
                    clearPlotMembers(claimedChunk, connection);
                    MessageManager.sendMessage(player, "make_regular_success", Integer.toString(claimedChunk.getChunkX()), Integer.toString(claimedChunk.getChunkZ()));
                } else {
                    setChunkType(claimedChunk, ClaimedChunk.ChunkType.PLOT, connection);
                    MessageManager.sendMessage(player, "make_plot_success", Integer.toString(claimedChunk.getChunkX()), Integer.toString(claimedChunk.getChunkZ()));
                }
                Bukkit.getScheduler().runTask(plugin, () -> ClaimViewerUtil.showParticles(player, claimedChunk, 5));
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void removeClaim(Player player, ClaimedChunk claimedChunk) {
        ClaimCache cache = HuskTowns.getClaimCache();
        if (!cache.hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", cache.getName());
            return;
        }

        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (claimedChunk == null) {
                    MessageManager.sendMessage(player, "error_not_standing_on_claim");
                    return;
                }
                if (claimedChunk.getTown().equals(HuskTowns.getSettings().getAdminTownName())) {
                    if (player.hasPermission("husktowns.administrator.claim") || player.hasPermission("husktowns.administrator.unclaim_any")) {
                        deleteClaimData(claimedChunk, connection);
                        MessageManager.sendMessage(player, "remove_admin_claim_success", Integer.toString(claimedChunk.getChunkX()), Integer.toString(claimedChunk.getChunkZ()));
                        return;
                    }
                }
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                if (!town.getName().equals(claimedChunk.getTown())) {
                    if (player.hasPermission("husktowns.administrator.unclaim_any")) {
                        deleteClaimData(claimedChunk, connection);
                        MessageManager.sendMessage(player, "remove_claim_success_override", town.getName(), Integer.toString(claimedChunk.getChunkX()), Integer.toString(claimedChunk.getChunkZ()));
                        return;
                    }
                    MessageManager.sendMessage(player, "error_claim_not_member_of_town", claimedChunk.getTown());
                    return;
                }
                Town.TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == Town.TownRole.RESIDENT) {
                    if (player.hasPermission("husktowns.administrator.unclaim_any")) {
                        deleteClaimData(claimedChunk, connection);
                        MessageManager.sendMessage(player, "remove_claim_success_override", town.getName(), Integer.toString(claimedChunk.getChunkX()), Integer.toString(claimedChunk.getChunkZ()));
                        return;
                    }
                    MessageManager.sendMessage(player, "error_insufficient_claim_privileges");
                    return;
                }
                deleteClaimData(claimedChunk, connection);
                MessageManager.sendMessage(player, "remove_claim_success", Integer.toString(claimedChunk.getChunkX()), Integer.toString(claimedChunk.getChunkZ()));

                // Check if the town spawn was set within the claimed chunk and if so remove.
                Chunk chunk = player.getLocation().getWorld().getChunkAt(claimedChunk.getChunkX(), claimedChunk.getChunkZ());
                if (town.getTownSpawn().getLocation().getChunk() == chunk) {
                    deleteTownSpawnData(player, connection);
                    MessageManager.sendMessage(player, "spawn_removed_in_claim");
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    private static void updateCachedBonuses(Connection connection) throws SQLException {
        PreparedStatement bonusesStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getBonusesTable() + ";");
        ResultSet resultSet = bonusesStatement.executeQuery();
        if (resultSet != null) {
            while (resultSet.next()) {
                Town town = getTownFromID(resultSet.getInt("town_id"), connection);
                if (town != null) {
                    HuskTowns.getTownBonusesCache().add(town.getName(),
                            new TownBonus(getPlayerUUID(resultSet.getInt("applier_id"), connection),
                            resultSet.getInt("bonus_claims"),
                            resultSet.getInt("bonus_members"),
                            resultSet.getTimestamp("applied_time").toInstant().getEpochSecond()));
                }
            }
        }
        bonusesStatement.close();
    }

    public static void addTownBonus(CommandSender sender, String targetName, TownBonus bonus) {
        if (!HuskTowns.getPlayerCache().hasLoaded()) {
            MessageManager.sendMessage(sender, "error_cache_updating", "Player Data");
            return;
        }
        if (!HuskTowns.getTownBonusesCache().hasLoaded()) {
            MessageManager.sendMessage(sender, "error_cache_updating", "Town Bonuses");
            return;
        }
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String townName = targetName;
                if (!townExists(targetName, connection)) {
                    if (playerNameExists(targetName, connection)) {
                        UUID playerUUID = getPlayerUUID(townName, connection);
                        if (playerUUID == null) {
                            MessageManager.sendMessage(sender, "error_town_bonus_invalid_target");
                            return;
                        }
                        if (inTown(playerUUID, connection)) {
                            townName = getPlayerTown(playerUUID, connection).getName();
                        } else {
                            MessageManager.sendMessage(sender, "error_town_bonus_invalid_target");
                            return;
                        }
                    } else {
                        MessageManager.sendMessage(sender, "error_town_bonus_invalid_target");
                        return;
                    }
                }
                addBonusData(bonus, townName, connection);
                MessageManager.sendMessage(sender, "bonus_application_successful",
                        Integer.toString(bonus.getBonusClaims()), Integer.toString(bonus.getBonusMembers()),
                        townName);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void updateTownBonusCache() {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (HuskTowns.getTownBonusesCache().getStatus() == Cache.CacheStatus.UPDATING) {
                    return;
                }
                HuskTowns.getTownBonusesCache().setStatus(Cache.CacheStatus.UPDATING);
                updateCachedBonuses(connection);
                HuskTowns.getTownBonusesCache().setStatus(Cache.CacheStatus.LOADED);

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
                HuskTowns.getTownBonusesCache().setStatus(Cache.CacheStatus.ERROR);
            }
        });
    }

    private static void deleteTownBonuses(String townName, Connection connection) throws SQLException {
        PreparedStatement deleteBonusesStatement = connection.prepareStatement(
                "DELETE FROM " + HuskTowns.getSettings().getBonusesTable() + " WHERE `town_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?);");
        deleteBonusesStatement.setString(1, townName);
        deleteBonusesStatement.executeUpdate();
        deleteBonusesStatement.close();
    }

    public static void clearTownBonuses(CommandSender sender, String targetName) {
        if (!HuskTowns.getPlayerCache().hasLoaded()) {
            MessageManager.sendMessage(sender, "error_cache_updating", "Player Data");
            return;
        }
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String townName = targetName;
                if (!townExists(targetName, connection)) {
                    if (playerNameExists(targetName, connection)) {
                        UUID playerUUID = getPlayerUUID(townName, connection);
                        if (playerUUID == null) {
                            MessageManager.sendMessage(sender, "error_town_bonus_invalid_target");
                            return;
                        }
                        if (inTown(playerUUID, connection)) {
                            townName = getPlayerTown(playerUUID, connection).getName();
                        } else {
                            MessageManager.sendMessage(sender, "error_town_bonus_invalid_target");
                            return;
                        }
                    } else {
                        MessageManager.sendMessage(sender, "error_town_bonus_invalid_target");
                        return;
                    }
                }
                deleteTownBonuses(townName, connection);
                HuskTowns.getTownBonusesCache().reload();
                MessageManager.sendMessage(sender, "bonus_deletion_successful", townName);
                if (HuskTowns.getSettings().doBungee()) {
                    for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                        new PluginMessage(PluginMessageType.CLEAR_TOWN_BONUSES, townName).sendToAll(updateNotificationDispatcher);
                        break;
                    }
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }


    public static ArrayList<TownBonus> getTownBonuses(String townName, Connection connection) throws SQLException {
        PreparedStatement bonusesStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getBonusesTable() + " WHERE `town_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) ORDER BY `applied_time` DESC;");
        bonusesStatement.setString(1, townName);
        ResultSet resultSet = bonusesStatement.executeQuery();
        if (resultSet != null) {
            final ArrayList<TownBonus> bonuses = new ArrayList<>();
            while (resultSet.next()) {
                bonuses.add(new TownBonus(getPlayerUUID(resultSet.getInt("applier_id"), connection),
                        resultSet.getInt("bonus_claims"),
                        resultSet.getInt("bonus_members"),
                        resultSet.getTimestamp("applied_time").toInstant().getEpochSecond()));
            }
            bonusesStatement.close();
            return bonuses;
        }
        bonusesStatement.close();
        return null;
    }

    public static void updatePlayerCachedData() {
        Connection connection = HuskTowns.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (HuskTowns.getPlayerCache().getStatus() == Cache.CacheStatus.UPDATING) {
                return;
            }
            HuskTowns.getPlayerCache().setStatus(Cache.CacheStatus.UPDATING);

            try {
                for (UUID uuid : getPlayers(connection)) {
                    HuskTowns.getPlayerCache().setPlayerName(uuid, getPlayerName(uuid, connection));
                    if (inTown(uuid, connection)) {
                        HuskTowns.getPlayerCache().setPlayerTown(uuid, getPlayerTown(uuid, connection).getName());
                        HuskTowns.getPlayerCache().setPlayerRole(uuid, getTownRole(uuid, connection));
                    }
                }
                HuskTowns.getPlayerCache().setStatus(Cache.CacheStatus.LOADED);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
                HuskTowns.getPlayerCache().setStatus(Cache.CacheStatus.ERROR);
            }
        });
    }
}