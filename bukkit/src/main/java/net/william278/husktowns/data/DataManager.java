package net.william278.husktowns.data;

import de.themoep.minedown.MineDown;
import de.themoep.minedown.MineDownParser;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.MessageManager;
import net.william278.husktowns.cache.CacheStatus;
import net.william278.husktowns.commands.*;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.data.message.CrossServerMessageHandler;
import net.william278.husktowns.data.message.Message;

import net.william278.husktowns.events.ClaimEvent;
import net.william278.husktowns.events.TownCreateEvent;
import net.william278.husktowns.events.TownDisbandEvent;
import net.william278.husktowns.events.UnClaimEvent;
import net.william278.husktowns.flags.*;
import net.william278.husktowns.teleport.TeleportationHandler;
import net.william278.husktowns.integrations.VaultIntegration;
import net.william278.husktowns.cache.ClaimCache;
import net.william278.husktowns.chunk.ClaimedChunk;
import net.william278.husktowns.teleport.TeleportationPoint;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.town.TownBonus;
import net.william278.husktowns.town.TownInvite;
import net.william278.husktowns.town.TownRole;
import net.william278.husktowns.util.*;
import net.william278.husktowns.util.ClaimViewerUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.william278.husktowns.commands.*;
import net.william278.husktowns.flags.*;
import net.william278.husktowns.util.*;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DataManager {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    // Add a new player
    private static void createPlayer(String playerName, UUID playerUUID, Connection connection) throws SQLException {
        try (PreparedStatement playerCreationStatement = connection.prepareStatement(
                "INSERT INTO " + HuskTowns.getSettings().getPlayerTable() + " (username,uuid) VALUES(?,?);")) {
            playerCreationStatement.setString(1, playerName);
            playerCreationStatement.setString(2, playerUUID.toString());
            playerCreationStatement.executeUpdate();
        }
    }

    // Update a player username
    private static void updatePlayerName(UUID playerUUID, String playerName, Connection connection) throws SQLException {
        try (PreparedStatement usernameUpdateStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `username`=? WHERE `uuid`=?;")) {
            usernameUpdateStatement.setString(1, playerName);
            usernameUpdateStatement.setString(2, playerUUID.toString());
            usernameUpdateStatement.executeUpdate();
        }
    }

    // Get a player's username
    public static String getPlayerName(UUID uuid, Connection connection) throws SQLException {
        try (PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?;")) {
            existStatement.setString(1, uuid.toString());
            ResultSet resultSet = existStatement.executeQuery();
            if (resultSet != null) {
                if (resultSet.next()) {
                    final String username = resultSet.getString("username");
                    existStatement.close();
                    return username;
                }
            }
        }
        return null;
    }

    // Returns true if a player exists
    private static boolean playerExists(UUID playerUUID, Connection connection) throws SQLException {
        try (PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?;")) {
            existStatement.setString(1, playerUUID.toString());
            ResultSet resultSet = existStatement.executeQuery();
            if (resultSet != null) {
                final boolean playerExists = resultSet.next();
                existStatement.close();
                return playerExists;
            }
        }
        return false;
    }

    // Returns true if a player exists
    private static boolean playerNameExists(String playerName, Connection connection) throws SQLException {
        try (PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `username`=?;")) {
            existStatement.setString(1, playerName);
            ResultSet resultSet = existStatement.executeQuery();
            if (resultSet != null) {
                final boolean playerNameExists = resultSet.next();
                existStatement.close();
                return playerNameExists;
            }
        }
        return false;
    }

    /**
     * Update a player's name in SQL; create a new player if they don't exist
     *
     * @param player the Player to update
     */
    public static void updatePlayerData(Player player) {
        final UUID playerUUID = player.getUniqueId();
        final String playerName = player.getName();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
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
                // Synchronise SQL data with the data in the cache
                HuskTowns.getPlayerCache().setPlayerName(playerUUID, playerName);
                if (HuskTowns.getSettings().doBungee()) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> CrossServerMessageHandler.getMessage(Message.MessageType.ADD_PLAYER_TO_CACHE, playerUUID.toString(), playerName).sendToAll(player), 5);
                }
                if (DataManager.inTown(playerUUID, connection)) {
                    final Town town = DataManager.getPlayerTown(playerUUID, connection);
                    final TownRole townRole = DataManager.getTownRole(playerUUID, connection);
                    assert town != null;
                    assert townRole != null;
                    HuskTowns.getPlayerCache().setPlayerTown(playerUUID, town.getName());
                    HuskTowns.getPlayerCache().setPlayerRole(playerUUID, townRole);
                    if (HuskTowns.getSettings().doBungee()) {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                            CrossServerMessageHandler.getMessage(Message.MessageType.SET_PLAYER_TOWN, playerUUID.toString(), town.getName()).sendToAll(player);
                            CrossServerMessageHandler.getMessage(Message.MessageType.SET_PLAYER_ROLE, playerUUID.toString(), townRole.toString()).sendToAll(player);
                        }, 5);
                    }
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    // Update the type of chunk
    private static void setChunkType(ClaimedChunk claimedChunk, ClaimedChunk.ChunkType type, Connection connection) throws SQLException {
        final int chunkTypeID = getIDFromChunkType(type);
        try (PreparedStatement chunkUpdateStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getClaimsTable() + " SET `chunk_type`=?, `plot_owner_id`=NULL WHERE `town_id`=" +
                        "(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) " +
                        "AND `server`=? AND `world`=? AND `chunk_x`=? AND `chunk_z`=?;")) {
            chunkUpdateStatement.setInt(1, chunkTypeID);
            chunkUpdateStatement.setString(2, claimedChunk.getTown());
            chunkUpdateStatement.setString(3, claimedChunk.getServer());
            chunkUpdateStatement.setString(4, claimedChunk.getWorld());
            chunkUpdateStatement.setInt(5, claimedChunk.getChunkX());
            chunkUpdateStatement.setInt(6, claimedChunk.getChunkZ());
            chunkUpdateStatement.executeUpdate();
        }
        HuskTowns.getClaimCache().remove(claimedChunk.getChunkX(), claimedChunk.getChunkZ(), claimedChunk.getWorld());
        HuskTowns.getClaimCache().add(new ClaimedChunk(claimedChunk.getServer(), claimedChunk.getWorld(), claimedChunk.getChunkX(),
                claimedChunk.getChunkZ(), claimedChunk.getClaimerUUID(), type, claimedChunk.getPlotChunkOwner(), claimedChunk.getPlotChunkMembers(), claimedChunk.getTown(), claimedChunk.getClaimTimestamp()));
    }

    // Update money in town coffers
    private static void depositIntoCoffers(UUID playerUUID, double moneyToDeposit, Connection connection) throws SQLException {
        try (PreparedStatement coffersUpdateStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getTownsTable() + " SET `money`=`money`+? WHERE `id`=(SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);")) {
            coffersUpdateStatement.setDouble(1, moneyToDeposit);
            coffersUpdateStatement.setString(2, playerUUID.toString());
            coffersUpdateStatement.executeUpdate();
        }
    }

    // Set the plot owner of a claim
    private static void setPlotOwner(ClaimedChunk claimedChunk, UUID plotOwner, Connection connection) throws SQLException {
        try (PreparedStatement chunkUpdateStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getClaimsTable() + " SET `plot_owner_id`=(SELECT `id` FROM "
                        + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?) WHERE `town_id`=" +
                        "(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) " +
                        "AND `server`=? AND `world`=? AND `chunk_x`=? AND `chunk_z`=?;")) {
            chunkUpdateStatement.setString(1, plotOwner.toString());
            chunkUpdateStatement.setString(2, claimedChunk.getTown());
            chunkUpdateStatement.setString(3, claimedChunk.getServer());
            chunkUpdateStatement.setString(4, claimedChunk.getWorld());
            chunkUpdateStatement.setInt(5, claimedChunk.getChunkX());
            chunkUpdateStatement.setInt(6, claimedChunk.getChunkZ());
            chunkUpdateStatement.executeUpdate();
        }
        HuskTowns.getClaimCache().remove(claimedChunk.getChunkX(), claimedChunk.getChunkZ(), claimedChunk.getWorld());
        HuskTowns.getClaimCache().add(new ClaimedChunk(claimedChunk.getServer(), claimedChunk.getWorld(), claimedChunk.getChunkX(),
                claimedChunk.getChunkZ(), claimedChunk.getClaimerUUID(), claimedChunk.getChunkType(), plotOwner, claimedChunk.getPlotChunkMembers(), claimedChunk.getTown(), claimedChunk.getClaimTimestamp()));
    }

    private static void clearPlotOwner(ClaimedChunk claimedChunk, Connection connection) throws SQLException {
        try (PreparedStatement chunkUpdateStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getClaimsTable() + " SET `plot_owner_id`=NULL WHERE `town_id`=" +
                        "(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) " +
                        "AND `server`=? AND `world`=? AND `chunk_x`=? AND `chunk_z`=?;")) {
            chunkUpdateStatement.setString(1, claimedChunk.getTown());
            chunkUpdateStatement.setString(2, claimedChunk.getServer());
            chunkUpdateStatement.setString(3, claimedChunk.getWorld());
            chunkUpdateStatement.setInt(4, claimedChunk.getChunkX());
            chunkUpdateStatement.setInt(5, claimedChunk.getChunkZ());
            chunkUpdateStatement.executeUpdate();
        }
        HuskTowns.getClaimCache().remove(claimedChunk.getChunkX(), claimedChunk.getChunkZ(), claimedChunk.getWorld());
        HuskTowns.getClaimCache().add(new ClaimedChunk(claimedChunk.getServer(), claimedChunk.getWorld(), claimedChunk.getChunkX(),
                claimedChunk.getChunkZ(), claimedChunk.getClaimerUUID(), claimedChunk.getChunkType(), null, new HashSet<>(), claimedChunk.getTown(), claimedChunk.getClaimTimestamp()));
    }

    // Return the flags a town has set
    private static HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> getTownFlags(String townName, Connection connection) throws SQLException {
        HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> townFlags = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getTownFlagsTable() + " WHERE `town_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?);")) {
            statement.setString(1, townName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                final HashSet<Flag> flags = new HashSet<>();
                flags.add(new ExplosionDamageFlag(resultSet.getBoolean(ExplosionDamageFlag.FLAG_IDENTIFIER)));
                flags.add(new FireDamageFlag(resultSet.getBoolean(FireDamageFlag.FLAG_IDENTIFIER)));
                flags.add(new MobGriefingFlag(resultSet.getBoolean(MobGriefingFlag.FLAG_IDENTIFIER)));
                flags.add(new MonsterSpawningFlag(resultSet.getBoolean(MonsterSpawningFlag.FLAG_IDENTIFIER)));
                flags.add(new PvpFlag(resultSet.getBoolean(PvpFlag.FLAG_IDENTIFIER)));
                flags.add(new PublicInteractAccessFlag(resultSet.getBoolean(PublicInteractAccessFlag.FLAG_IDENTIFIER)));
                flags.add(new PublicContainerAccessFlag(resultSet.getBoolean(PublicContainerAccessFlag.FLAG_IDENTIFIER)));
                flags.add(new PublicBuildAccessFlag(resultSet.getBoolean(PublicBuildAccessFlag.FLAG_IDENTIFIER)));
                flags.add(new PublicFarmAccessFlag(resultSet.getBoolean(PublicFarmAccessFlag.FLAG_IDENTIFIER)));
                townFlags.put(getChunkType(resultSet.getInt("chunk_type")), flags);
            }
        }
        return townFlags;
    }

    // Insert new town flag values to the database on town creation
    public static void addTownFlagData(String townName, HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> flags, boolean addToCache, Connection connection) throws SQLException {
        for (ClaimedChunk.ChunkType type : flags.keySet()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO " + HuskTowns.getSettings().getTownFlagsTable() + " (`town_id`,`chunk_type`,`" + ExplosionDamageFlag.FLAG_IDENTIFIER + "`,`" + FireDamageFlag.FLAG_IDENTIFIER + "`,`" + MobGriefingFlag.FLAG_IDENTIFIER + "`,`" + MonsterSpawningFlag.FLAG_IDENTIFIER + "`,`" + PvpFlag.FLAG_IDENTIFIER + "`,`" + PublicInteractAccessFlag.FLAG_IDENTIFIER + "`,`" + PublicContainerAccessFlag.FLAG_IDENTIFIER + "`,`" + PublicBuildAccessFlag.FLAG_IDENTIFIER + "`,`" + PublicFarmAccessFlag.FLAG_IDENTIFIER + "`) VALUES ((SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?),?,?,?,?,?,?,?,?,?,?);")) {
                statement.setString(1, townName);
                statement.setInt(2, getIDFromChunkType(type));
                for (Flag flag : flags.get(type)) {
                    final int flagIndex = switch (flag.getIdentifier()) {
                        case ExplosionDamageFlag.FLAG_IDENTIFIER -> 3;
                        case FireDamageFlag.FLAG_IDENTIFIER -> 4;
                        case MobGriefingFlag.FLAG_IDENTIFIER -> 5;
                        case MonsterSpawningFlag.FLAG_IDENTIFIER -> 6;
                        case PvpFlag.FLAG_IDENTIFIER -> 7;
                        case PublicInteractAccessFlag.FLAG_IDENTIFIER -> 8;
                        case PublicContainerAccessFlag.FLAG_IDENTIFIER -> 9;
                        case PublicBuildAccessFlag.FLAG_IDENTIFIER -> 10;
                        case PublicFarmAccessFlag.FLAG_IDENTIFIER -> 11;
                        default -> throw new IllegalStateException("Unexpected flag identifier value: " + flag.getIdentifier());
                    };
                    statement.setBoolean(flagIndex, flag.isFlagSet());
                }
                statement.executeUpdate();
            }
        }
        if (addToCache) {
            HuskTowns.getTownDataCache().setFlags(townName, flags);
            if (HuskTowns.getSettings().doBungee()) {
                for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                    CrossServerMessageHandler.getMessage(Message.MessageType.CREATE_TOWN_FLAGS, townName).sendToAll(updateNotificationDispatcher);
                    return;
                }
            }
        }
    }

    // Update a certain flag value in the database
    private static void updateTownFlagData(String townName, ClaimedChunk.ChunkType type, Flag flag, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getTownFlagsTable() + " SET `" + flag.getIdentifier() + "`=? WHERE `town_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) AND `chunk_type`=?")) {
            statement.setBoolean(1, flag.isFlagSet());
            statement.setString(2, townName);
            statement.setInt(3, getIDFromChunkType(type));
            statement.executeUpdate();
        }
        HuskTowns.getTownDataCache().setFlag(townName, type, flag.getIdentifier(), flag.isFlagSet());
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                CrossServerMessageHandler.getMessage(Message.MessageType.UPDATE_TOWN_FLAG, townName, type.toString(), flag.getIdentifier(), Boolean.toString(flag.isFlagSet())).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    public static ArrayList<Town> getTowns(Connection connection, String orderBy, boolean ascendingOrder) throws SQLException {
        final ArrayList<Town> townList = new ArrayList<>();
        String order;
        if (ascendingOrder) {
            order = "ASC";
        } else {
            order = "DESC";
        }
        try (PreparedStatement getTownsStatement = connection.prepareStatement("SELECT * FROM " + HuskTowns.getSettings().getTownsTable() + " ORDER BY `" + orderBy + "` " + order + ";")) {
            ResultSet resultSet = getTownsStatement.executeQuery();
            if (resultSet != null) {
                while (resultSet.next()) {
                    final String name = resultSet.getString("name");
                    final double money = resultSet.getDouble("money");
                    final Timestamp timestamp = resultSet.getTimestamp("founded");
                    final String greetingMessage = resultSet.getString("greeting_message");
                    final String farewellMessage = resultSet.getString("farewell_message");
                    final String bio = resultSet.getString("bio");
                    final TeleportationPoint spawnTeleportationPoint = getTeleportationPoint(resultSet.getInt("spawn_location_id"), connection);
                    final boolean townSpawnPrivacy = resultSet.getBoolean("is_spawn_public");
                    final HashSet<ClaimedChunk> claimedChunks = getClaimedChunks(name, connection);
                    final HashMap<UUID, TownRole> members = getTownMembers(name, connection);
                    final HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> flags = getTownFlags(name, connection);
                    townList.add(new Town(name, claimedChunks, members, spawnTeleportationPoint, townSpawnPrivacy, money, greetingMessage, farewellMessage, bio, timestamp.toInstant().getEpochSecond(), flags));
                }
            }
        }
        return townList;
    }

    public static Town getTownFromName(String townName, Connection connection) throws SQLException {
        try (PreparedStatement getTown = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?;")) {
            getTown.setString(1, townName);
            ResultSet townResults = getTown.executeQuery();
            if (townResults != null) {
                if (townResults.next()) {
                    double money = townResults.getDouble("money");
                    Timestamp timestamp = townResults.getTimestamp("founded");
                    String greetingMessage = townResults.getString("greeting_message");
                    String farewellMessage = townResults.getString("farewell_message");
                    String bio = townResults.getString("bio");
                    TeleportationPoint spawnTeleportationPoint = getTeleportationPoint(townResults.getInt("spawn_location_id"), connection);
                    boolean townSpawnPrivacy = townResults.getBoolean("is_spawn_public");
                    HashSet<ClaimedChunk> claimedChunks = getClaimedChunks(townName, connection);
                    HashMap<UUID, TownRole> members = getTownMembers(townName, connection);
                    HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> flags = getTownFlags(townName, connection);
                    return new Town(townName, claimedChunks, members, spawnTeleportationPoint, townSpawnPrivacy, money, greetingMessage, farewellMessage, bio, timestamp.toInstant().getEpochSecond(), flags);
                }
            }
        }
        return null;
    }

    // Returns if a town with that name already exists
    private static boolean townExists(String townName, Connection connection) throws SQLException {
        try (PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE name=?;")) {
            existStatement.setString(1, townName);
            ResultSet existResult = existStatement.executeQuery();
            if (existResult != null) {
                boolean townExists = existResult.next();
                existStatement.close();
                return townExists;
            }
        }
        return false;
    }

    // Add town data and flags to SQL
    private static void addTownData(Town town, Connection connection) throws SQLException {
        try (PreparedStatement townCreationStatement = connection.prepareStatement(
                "INSERT INTO " + HuskTowns.getSettings().getTownsTable() + " (name,money,founded,greeting_message,farewell_message,bio,is_spawn_public) VALUES(?,0,?,?,?,?,0);")) {
            townCreationStatement.setString(1, town.getName());
            townCreationStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            townCreationStatement.setString(3, town.getGreetingMessage());
            townCreationStatement.setString(4, town.getFarewellMessage());
            townCreationStatement.setString(5, town.getBio());
            townCreationStatement.executeUpdate();
        }
        addTownFlagData(town.getName(), town.getFlags(), true, connection);
    }

    private static Integer getIDFromTownRole(TownRole townRole) {
        return switch (townRole) {
            case RESIDENT -> 1;
            case TRUSTED -> 2;
            case MAYOR -> 3;
        };
    }

    private static TownRole getTownRoleFromID(Integer id) {
        return switch (id) {
            case 1 -> TownRole.RESIDENT;
            case 2 -> TownRole.TRUSTED;
            case 3 -> TownRole.MAYOR;
            default -> null;
        };
    }

    private static void updateTownMayor(UUID newMayor, UUID oldMayor, Connection connection) throws SQLException {
        setPlayerRoleData(oldMayor, TownRole.TRUSTED, connection);
        setPlayerRoleData(newMayor, TownRole.MAYOR, connection);
        HuskTowns.getPlayerCache().setPlayerRole(oldMayor, TownRole.TRUSTED);
        HuskTowns.getPlayerCache().setPlayerRole(newMayor, TownRole.MAYOR);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                CrossServerMessageHandler.getMessage(Message.MessageType.SET_PLAYER_ROLE, oldMayor.toString(), TownRole.TRUSTED.toString()).sendToAll(updateNotificationDispatcher);
                CrossServerMessageHandler.getMessage(Message.MessageType.SET_PLAYER_ROLE, newMayor.toString(), TownRole.MAYOR.toString()).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void updateTownSpawnPrivacyData(String townName, boolean isPublic, Connection connection) throws SQLException {
        try (PreparedStatement changeTownGreetingStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getTownsTable() + " SET `is_spawn_public`=? WHERE `name`=?;")) {
            changeTownGreetingStatement.setBoolean(1, isPublic);
            changeTownGreetingStatement.setString(2, townName);
            changeTownGreetingStatement.executeUpdate();
        }
        if (isPublic) {
            HuskTowns.getTownDataCache().addTownWithPublicSpawn(townName);
        } else {
            HuskTowns.getTownDataCache().removeTownWithPublicSpawn(townName);
        }
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                CrossServerMessageHandler.getMessage(Message.MessageType.SET_TOWN_SPAWN_PRIVACY, townName, Boolean.toString(isPublic)).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void updateTownBioData(UUID updaterUUID, String newBio, Connection connection) throws SQLException {
        try (PreparedStatement changeTownGreetingStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getTownsTable() + " SET `bio`=? WHERE `id`=(SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);")) {
            changeTownGreetingStatement.setString(1, newBio);
            changeTownGreetingStatement.setString(2, updaterUUID.toString());
            changeTownGreetingStatement.executeUpdate();
        }
        final Town town = getPlayerTown(updaterUUID, connection);
        assert town != null;
        HuskTowns.getTownDataCache().setTownBio(town.getName(), newBio);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                CrossServerMessageHandler.getMessage(Message.MessageType.UPDATE_CACHED_BIO_MESSAGE, town.getName(), newBio).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void updateTownFarewellData(UUID updaterUUID, String newFarewell, Connection connection) throws SQLException {
        try (PreparedStatement changeTownFarewellStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getTownsTable() + " SET `farewell_message`=? WHERE `id`=(SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);")) {
            changeTownFarewellStatement.setString(1, newFarewell);
            changeTownFarewellStatement.setString(2, updaterUUID.toString());
            changeTownFarewellStatement.executeUpdate();
        }
        final Town town = getPlayerTown(updaterUUID, connection);
        assert town != null;
        HuskTowns.getTownDataCache().setFarewellMessage(town.getName(), newFarewell);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                CrossServerMessageHandler.getMessage(Message.MessageType.UPDATE_CACHED_FAREWELL_MESSAGE, town.getName(), newFarewell).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void updateTownGreetingData(UUID updaterUUID, String newGreeting, Connection connection) throws SQLException {
        try (PreparedStatement changeTownGreetingStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getTownsTable() + " SET `greeting_message`=? WHERE `id`=(SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);")) {
            changeTownGreetingStatement.setString(1, newGreeting);
            changeTownGreetingStatement.setString(2, updaterUUID.toString());
            changeTownGreetingStatement.executeUpdate();
        }
        final Town town = getPlayerTown(updaterUUID, connection);
        assert town != null;
        HuskTowns.getTownDataCache().setGreetingMessage(town.getName(), newGreeting);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                CrossServerMessageHandler.getMessage(Message.MessageType.UPDATE_CACHED_GREETING_MESSAGE, town.getName(), newGreeting).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void updateTownName(UUID mayorUUID, String oldName, String newName, Connection connection) throws SQLException {
        try (PreparedStatement changeTownNameStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getTownsTable() + " SET `name`=? WHERE `id`=(SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);")) {
            changeTownNameStatement.setString(1, newName);
            changeTownNameStatement.setString(2, mayorUUID.toString());
            changeTownNameStatement.executeUpdate();
        }
        HuskTowns.getPlayerCache().renameReload(oldName, newName);
        HuskTowns.getClaimCache().renameReload(oldName, newName);
        HuskTowns.getTownDataCache().renameReload(oldName, newName);
        HuskTowns.getTownBonusesCache().renameTown(oldName, newName);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                CrossServerMessageHandler.getMessage(Message.MessageType.TOWN_RENAME, oldName, newName).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void setPlayerRoleData(UUID uuid, TownRole townRole, Connection connection) throws SQLException {
        try (PreparedStatement changeTownRoleStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `town_role`=? WHERE `uuid`=?;")) {
            changeTownRoleStatement.setInt(1, getIDFromTownRole(townRole));
            changeTownRoleStatement.setString(2, uuid.toString());
            changeTownRoleStatement.executeUpdate();
        }
        HuskTowns.getPlayerCache().setPlayerRole(uuid, townRole);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                CrossServerMessageHandler.getMessage(Message.MessageType.SET_PLAYER_ROLE, uuid.toString(), townRole.toString()).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void clearPlayerRoleData(UUID uuid, Connection connection) throws SQLException {
        try (PreparedStatement clearTownRoleStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `town_role`=NULL WHERE `uuid`=?;")) {
            clearTownRoleStatement.setString(1, uuid.toString());
            clearTownRoleStatement.executeUpdate();
        }
        HuskTowns.getPlayerCache().clearPlayerRole(uuid);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                CrossServerMessageHandler.getMessage(Message.MessageType.CLEAR_PLAYER_ROLE, uuid.toString()).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void setPlayerTownData(UUID uuid, String townName, Connection connection) throws SQLException {
        try (PreparedStatement joinTownStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `town_id`=(SELECT `id` from " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) WHERE `uuid`=?;")) {
            joinTownStatement.setString(1, townName);
            joinTownStatement.setString(2, uuid.toString());
            joinTownStatement.executeUpdate();
        }
        HuskTowns.getPlayerCache().setPlayerTown(uuid, townName);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                CrossServerMessageHandler.getMessage(Message.MessageType.SET_PLAYER_TOWN, uuid.toString(), townName).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    private static void clearPlayerTownData(UUID uuid, Connection connection) throws SQLException {
        try (PreparedStatement leaveTownStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `town_id`=NULL WHERE `uuid`=?;")) {
            leaveTownStatement.setString(1, uuid.toString());
            leaveTownStatement.executeUpdate();
        }
        HuskTowns.getPlayerCache().clearPlayerTown(uuid);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                CrossServerMessageHandler.getMessage(Message.MessageType.CLEAR_PLAYER_TOWN, uuid.toString()).sendToAll(updateNotificationDispatcher);
                return;
            }
        }
    }

    public static void evictPlayerFromTown(Player evicter, String playerToEvict) {
        if (!HuskTowns.getPlayerCache().hasLoaded()) {
            MessageManager.sendMessage(evicter, "error_cache_updating", "Player Data");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(evicter.getUniqueId(), connection)) {
                    MessageManager.sendMessage(evicter, "error_not_in_town");
                    return;
                }
                final Town evicterTown = getPlayerTown(evicter.getUniqueId(), connection);
                assert evicterTown != null;
                UUID uuidToEvict = getPlayerUUID(playerToEvict, connection);
                if (uuidToEvict == null) {
                    MessageManager.sendMessage(evicter, "error_invalid_player");
                    return;
                }
                TownRole evicterRole = getTownRole(evicter.getUniqueId(), connection);
                if (evicterRole == TownRole.RESIDENT) {
                    MessageManager.sendMessage(evicter, "error_insufficient_evict_privileges");
                    return;
                }
                final Town playerToEvictTown = getPlayerTown(uuidToEvict, connection);
                if (playerToEvictTown == null) {
                    MessageManager.sendMessage(evicter, "error_not_both_members");
                    return;
                }
                if (!playerToEvictTown.getName().equals(evicterTown.getName())) {
                    MessageManager.sendMessage(evicter, "error_not_both_members");
                    return;
                }
                if (evicter.getUniqueId() == uuidToEvict) {
                    MessageManager.sendMessage(evicter, "error_cant_evict_self");
                    return;
                }
                TownRole roleOfPlayerToEvict = getTownRole(uuidToEvict, connection);
                if (roleOfPlayerToEvict == TownRole.MAYOR) {
                    MessageManager.sendMessage(evicter, "error_cant_evict_mayor");
                    return;
                }
                if (evicterRole == TownRole.TRUSTED && roleOfPlayerToEvict == TownRole.TRUSTED) {
                    MessageManager.sendMessage(evicter, "error_cant_evict_other_trusted_member");
                    return;
                }

                clearPlayerTownData(uuidToEvict, connection);
                clearPlayerRoleData(uuidToEvict, connection);
                MessageManager.sendMessage(evicter, "you_evict_success", playerToEvict, playerToEvictTown.getName());

                // Send a notification to all town members
                for (UUID uuid : playerToEvictTown.getMembers().keySet()) {
                    if (uuid != evicter.getUniqueId()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            if (uuid == uuidToEvict) {
                                MessageManager.sendMessage(p, "have_been_evicted", playerToEvictTown.getName(), evicter.getName());
                            } else {
                                MessageManager.sendMessage(p, "player_evicted", playerToEvict, evicter.getName());
                            }

                        } else {
                            if (HuskTowns.getSettings().doBungee()) {
                                if (uuid == uuidToEvict) {
                                    CrossServerMessageHandler.getMessage(getPlayerName(uuid, connection), Message.MessageType.EVICTED_NOTIFICATION_YOURSELF,
                                            playerToEvictTown.getName(), evicter.getName()).send(evicter);
                                } else {
                                    CrossServerMessageHandler.getMessage(getPlayerName(uuid, connection), Message.MessageType.EVICTED_NOTIFICATION,
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!townExists(townName, connection)) {
                    MessageManager.sendMessage(player, "error_town_no_longer_exists");
                    return;
                }
                Town town = getTownFromName(townName, connection);
                assert town != null;
                if (inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_already_in_town");
                    return;
                }
                if (town.getMembers().size() + 1 > town.getMaxMembers()) {
                    MessageManager.sendMessage(player, "error_town_full", town.getName());
                    return;
                }
                setPlayerTownData(player.getUniqueId(), townName, connection);
                setPlayerRoleData(player.getUniqueId(), TownRole.RESIDENT, connection);
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
                                CrossServerMessageHandler.getMessage(getPlayerName(uuid, connection), Message.MessageType.PLAYER_HAS_JOINED_NOTIFICATION,
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                final Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;
                final String townName = town.getName();
                if (getTownRole(player.getUniqueId(), connection) == TownRole.MAYOR) {
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

    // Delete all of a town's claims from the database
    public static void deleteAllClaimData(String townName, Connection connection) throws SQLException {
        try (PreparedStatement deleteClaims = connection.prepareStatement(
                "DELETE FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE `town_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?);")) {
            deleteClaims.setString(1, townName);
            deleteClaims.executeUpdate();
        }

        HuskTowns.getClaimCache().removeAllClaims(townName);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                if (HuskTowns.getSettings().doBungee()) {
                    CrossServerMessageHandler.getMessage(Message.MessageType.TOWN_REMOVE_ALL_CLAIMS, townName).sendToAll(updateNotificationDispatcher);
                }
            }
        }
    }

    // Delete the table from SQL. Cascading deletion means all claims will be cleared & player town ID will be set to null
    public static void deleteTownData(String townName, Connection connection) throws SQLException {
        // Ensure foreign keys are ON for SQLite users
        if (HuskTowns.getSettings().getDatabaseType() == Settings.DatabaseType.SQLITE) {
            try (PreparedStatement ensurePragmaSet = connection.prepareStatement("PRAGMA foreign_keys = ON;")) {
                ensurePragmaSet.executeUpdate();
            }
        }

        // Clear the town roles of all members
        try (PreparedStatement clearPlayerRoles = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `town_role`=NULL WHERE `town_id`=(SELECT `id` FROM "
                        + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?);")) {
            clearPlayerRoles.setString(1, townName);
            clearPlayerRoles.executeUpdate();
        } finally {
            // Delete the town from database (triggers cascading nullification and deletion)
            try (PreparedStatement deleteTown = connection.prepareStatement(
                    "DELETE FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?;")) {
                deleteTown.setString(1, townName);
                deleteTown.executeUpdate();
            }
        }
    }

    public static void deleteAllTownClaims(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                final Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;
                if (getTownRole(player.getUniqueId(), connection) != TownRole.MAYOR) {
                    MessageManager.sendMessage(player, "error_insufficient_unclaim_all_privileges");
                    return;
                }
                final String townName = town.getName();

                // Delete all claims and if there is a town spawn set, delete that too.
                deleteAllClaimData(townName, connection);
                if (town.getTownSpawn() != null) {
                    deleteTownSpawnData(player, connection);
                    if (town.isSpawnPublic()) {
                        updateTownSpawnPrivacyData(townName, false, connection);
                    }
                }

                MessageManager.sendMessage(player, "town_unclaim_all_success");

                // Send a notification to all town members
                for (UUID uuid : town.getMembers().keySet()) {
                    if (!uuid.toString().equals(player.getUniqueId().toString())) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            if (p.getUniqueId() != player.getUniqueId()) {
                                MessageManager.sendMessage(p, "town_unclaim_all_notification", player.getName());
                            }

                        } else {
                            if (HuskTowns.getSettings().doBungee()) {
                                CrossServerMessageHandler.getMessage(getPlayerName(uuid, connection), Message.MessageType.REMOVE_ALL_CLAIMS_NOTIFICATION,
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

    public static void disbandTown(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                final Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;
                if (getTownRole(player.getUniqueId(), connection) != TownRole.MAYOR) {
                    MessageManager.sendMessage(player, "error_insufficient_disband_privileges");
                    return;
                }
                final String townName = town.getName();

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (EventCannon.fireEvent(new TownDisbandEvent(player, townName))) {
                        return;
                    }
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try (Connection connection1 = HuskTowns.getConnection()) {
                            deleteTownData(townName, connection1);
                            AutoClaimUtil.removeAutoClaimer(player);
                            MessageManager.sendMessage(player, "disband_town_success");

                            // Update caches
                            if (HuskTowns.getPlayerCache().hasLoaded()) {
                                HuskTowns.getPlayerCache().disbandReload(townName);
                            }
                            if (HuskTowns.getClaimCache().hasLoaded()) {
                                HuskTowns.getClaimCache().removeAllClaims(townName);
                            }
                            if (HuskTowns.getTownDataCache().hasLoaded()) {
                                HuskTowns.getTownDataCache().disbandReload(townName);
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
                                            CrossServerMessageHandler.getMessage(getPlayerName(uuid, connection1), Message.MessageType.DISBAND_NOTIFICATION,
                                                    player.getName(), town.getName()).send(player);

                                        }
                                    }
                                }
                            }
                            if (HuskTowns.getSettings().doBungee()) {
                                CrossServerMessageHandler.getMessage(Message.MessageType.TOWN_DISBAND, townName).sendToAll(player);
                            }
                        } catch (SQLException exception) {
                            plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
                        }
                    });
                });

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void depositMoney(Player player, double amountToDeposit) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!HuskTowns.getSettings().doEconomy()) {
                    MessageManager.sendMessage(player, "error_economy_disabled");
                    return;
                }
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                final Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;
                if (amountToDeposit <= 0) {
                    MessageManager.sendMessage(player, "error_invalid_amount");
                    return;
                }
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
                if (VaultIntegration.takeMoney(player, amountToDeposit)) {
                    DataManager.depositIntoCoffers(player.getUniqueId(), amountToDeposit, connection);
                    MessageManager.sendMessage(player, "money_deposited_success", VaultIntegration.format(amountToDeposit), VaultIntegration.format(town.getMoneyDeposited() + amountToDeposit));
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
                                MessageManager.sendMessage(p, "town_deposit_notification", player.getName(), VaultIntegration.format(amountToDeposit));
                            } else {
                                if (HuskTowns.getSettings().doBungee()) {
                                    CrossServerMessageHandler.getMessage(getPlayerName(uuid, connection), Message.MessageType.DEPOSIT_NOTIFICATION,
                                            player.getName(), VaultIntegration.format(amountToDeposit)).send(player);
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
                                CrossServerMessageHandler.getMessage(getPlayerName(uuid, connection), Message.MessageType.LEVEL_UP_NOTIFICATION,
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                Town demoterTown = getPlayerTown(player.getUniqueId(), connection);
                assert demoterTown != null;
                if (getTownRole(player.getUniqueId(), connection) != TownRole.MAYOR) {
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
                if (!town.getName().equals(demoterTown.getName())) {
                    MessageManager.sendMessage(player, "error_not_both_members");
                    return;
                }
                if (getTownRole(uuidToDemote, connection) == TownRole.MAYOR) {
                    MessageManager.sendMessage(player, "error_cant_demote_self");
                    return;
                }
                if (getTownRole(uuidToDemote, connection) == TownRole.RESIDENT) {
                    MessageManager.sendMessage(player, "error_cant_demote_resident");
                    return;
                }
                DataManager.setPlayerRoleData(uuidToDemote, TownRole.RESIDENT, connection);
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
                                    CrossServerMessageHandler.getMessage(getPlayerName(uuid, connection), Message.MessageType.DEMOTED_NOTIFICATION_YOURSELF,
                                            player.getName(), town.getName()).send(player);
                                } else {
                                    CrossServerMessageHandler.getMessage(getPlayerName(uuid, connection), Message.MessageType.DEMOTED_NOTIFICATION,
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(invitingPlayer.getUniqueId(), connection)) {
                    MessageManager.sendMessage(invitingPlayer, "error_not_in_town");
                    return;
                }
                if (getTownRole(invitingPlayer.getUniqueId(), connection) == TownRole.RESIDENT) {
                    MessageManager.sendMessage(invitingPlayer, "error_insufficient_invite_privileges");
                    return;
                }
                if (!playerNameExists(inviteeName, connection)) {
                    MessageManager.sendMessage(invitingPlayer, "error_invalid_player");
                    return;
                }
                final UUID inviteeUUID = getPlayerUUID(inviteeName, connection);
                assert inviteeUUID != null;
                if (inTown(inviteeUUID, connection)) {
                    MessageManager.sendMessage(invitingPlayer, "error_other_already_in_town", inviteeName);
                    return;
                }
                Town town = getPlayerTown(invitingPlayer.getUniqueId(), connection);
                assert town != null;
                if (town.getMembers().size() + 1 > town.getMaxMembers()) {
                    MessageManager.sendMessage(invitingPlayer, "error_town_full", town.getName());
                    return;
                }

                Player inviteePlayer = Bukkit.getPlayer(inviteeName);
                if (inviteePlayer != null) {
                    // Handle on server
                    InviteCommand.sendInvite(inviteePlayer, new TownInvite(invitingPlayer.getName(), town.getName()));
                    MessageManager.sendMessage(invitingPlayer, "invite_sent_success", inviteeName, town.getName());
                } else {
                    if (HuskTowns.getSettings().doBungee()) {
                        // Handle with Plugin Messages
                        TownInvite invite = new TownInvite(invitingPlayer.getName(), town.getName());
                        CrossServerMessageHandler.getMessage(inviteeName, Message.MessageType.INVITED_TO_JOIN,
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
                                CrossServerMessageHandler.getMessage(getPlayerName(uuid, connection), Message.MessageType.INVITED_NOTIFICATION,
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                Town oldMayorTown = getPlayerTown(player.getUniqueId(), connection);
                assert oldMayorTown != null;
                if (getTownRole(player.getUniqueId(), connection) != TownRole.MAYOR) {
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
                if (!town.getName().equals(oldMayorTown.getName())) {
                    MessageManager.sendMessage(player, "error_not_both_members");
                    return;
                }
                if (getTownRole(newMayorUUID, connection) == TownRole.MAYOR) {
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
                                    CrossServerMessageHandler.getMessage(getPlayerName(uuid, connection), Message.MessageType.TRANSFER_YOU_NOTIFICATION,
                                            player.getName(), town.getName()).send(player);
                                } else {
                                    CrossServerMessageHandler.getMessage(getPlayerName(uuid, connection), Message.MessageType.TRANSFER_NOTIFICATION,
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                Town promoterTown = getPlayerTown(player.getUniqueId(), connection);
                assert promoterTown != null;
                if (getTownRole(player.getUniqueId(), connection) != TownRole.MAYOR) {
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
                if (!town.getName().equals(promoterTown.getName())) {
                    MessageManager.sendMessage(player, "error_not_both_members");
                    return;
                }
                if (getTownRole(uuidToPromote, connection) == TownRole.MAYOR) {
                    MessageManager.sendMessage(player, "error_cant_promote_self");
                    return;
                }
                if (getTownRole(uuidToPromote, connection) == TownRole.TRUSTED) {
                    MessageManager.sendMessage(player, "error_cant_promote_trusted");
                    return;
                }
                DataManager.setPlayerRoleData(uuidToPromote, TownRole.TRUSTED, connection);
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
                                    CrossServerMessageHandler.getMessage(getPlayerName(uuid, connection), Message.MessageType.PROMOTED_NOTIFICATION_YOURSELF,
                                            player.getName(), town.getName()).send(player);
                                } else {
                                    CrossServerMessageHandler.getMessage(getPlayerName(uuid, connection), Message.MessageType.PROMOTED_NOTIFICATION,
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

    // Returns true if a player is online on the network
    private static boolean isPlayerOnline(String username) {
        for (String player : HuskTowns.getPlayerList().getPlayers()) {
            if (player.equals(username)) {
                return true;
            }
        }
        return false;
    }

    private static void sendTownInfo(Player player, Town town, Connection connection) throws SQLException {
        if (town.getName().equals(HuskTowns.getSettings().getAdminTownName())) {
            MessageManager.sendMessage(player, "admin_town_information");
            return;
        }
        StringBuilder mayorName = new StringBuilder().append(MessageManager.getRawMessage("town_overview_mayor"));
        StringBuilder trustedMembers = new StringBuilder().append(MessageManager.getRawMessage("town_overview_trustees"));
        StringBuilder residentMembers = new StringBuilder().append(MessageManager.getRawMessage("town_overview_residents"));

        // Format each player name in the player list
        for (UUID uuid : town.getMembers().keySet()) {
            String playerName = getPlayerName(uuid, connection);
            if (playerName == null) {
                continue;
            }


            // Show the username in green if the player is online
            final boolean isOnline = isPlayerOnline(playerName);

            String onlineNameString = isOnline ? "[" + playerName + "](green " : "[" + playerName + "](gray ";
            StringBuilder userString = new StringBuilder(onlineNameString);
            if (playerName.equals(player.getName())) {
                userString.append("bold ");
            }

            userString.append("show_text=");
            if (isOnline) {
                userString.append(MessageManager.getRawMessage("player_status_online_tooltip"));
            } else {
                userString.append(MessageManager.getRawMessage("player_status_offline_tooltip"));
            }
            userString.append("\n&7UUID: ").append(uuid);
            if (isOnline) {
                userString.append(" suggest_command=/msg ").append(playerName).append(" ");
            }

            switch (town.getMembers().get(uuid)) {
                case MAYOR -> mayorName.append(userString).append(")");
                case RESIDENT -> residentMembers.append(userString).append("), ");
                case TRUSTED -> trustedMembers.append(userString).append("), ");
            }
        }

        MessageManager.sendMessage(player, "town_overview_header", town.getName());
        MessageManager.sendMessage(player, "town_overview_level", Integer.toString(town.getLevel()));

        if (HuskTowns.getSettings().doEconomy()) {
            final double townCofferBalance = town.getMoneyDeposited();
            final double nextLevelRequirement = TownLimitsUtil.getNextLevelRequired(townCofferBalance);
            String nextLevelRequirementFormat;
            if (nextLevelRequirement > 0) {
                nextLevelRequirementFormat = VaultIntegration.format(nextLevelRequirement);
            } else {
                nextLevelRequirementFormat = MessageManager.getRawMessage("town_overview_coffers_max_level");
            }
            MessageManager.sendMessage(player, "town_overview_coffers", VaultIntegration.format(townCofferBalance), nextLevelRequirementFormat);
        }

        MessageManager.sendMessage(player, "town_overview_founded", town.getFormattedFoundedTime());
        player.spigot().sendMessage(new ComponentBuilder()
                .append(new MineDown(MessageManager.getRawMessage("town_overview_bio")).toComponent())
                .append(new MineDown(town.getBio()).disable(MineDownParser.Option.ADVANCED_FORMATTING).toComponent())
                .append("").reset()
                .create());

        if (HuskTowns.getTownBonusesCache().contains(town.getName())) {
            final int bonusClaims = HuskTowns.getTownBonusesCache().getBonusClaims(town.getName());
            if (bonusClaims > 0) {
                MessageManager.sendMessage(player, "town_overview_claims_bonus", town.getTownColorHex(), Integer.toString(town.getClaimedChunksNumber()), Integer.toString(town.getMaximumClaimedChunks()), Integer.toString(bonusClaims), town.getName());
            } else {
                MessageManager.sendMessage(player, "town_overview_claims", town.getTownColorHex(), Integer.toString(town.getClaimedChunksNumber()), Integer.toString(town.getMaximumClaimedChunks()), town.getName());
            }
        } else {
            MessageManager.sendMessage(player, "town_overview_claims", town.getTownColorHex(), Integer.toString(town.getClaimedChunksNumber()), Integer.toString(town.getMaximumClaimedChunks()), town.getName());
        }

        if (HuskTowns.getTownBonusesCache().contains(town.getName())) {
            int bonusMembers = HuskTowns.getTownBonusesCache().getBonusMembers(town.getName());
            if (bonusMembers > 0) {
                MessageManager.sendMessage(player, "town_overview_members_bonus", Integer.toString(town.getMembers().size()), Integer.toString(town.getMaxMembers()), Integer.toString(bonusMembers));
            } else {
                MessageManager.sendMessage(player, "town_overview_members", Integer.toString(town.getMembers().size()), Integer.toString(town.getMaxMembers()));
            }
        } else {
            MessageManager.sendMessage(player, "town_overview_members", Integer.toString(town.getMembers().size()), Integer.toString(town.getMaxMembers()));
        }

        player.spigot().sendMessage(new MineDown(mayorName.toString()).toComponent());
        player.spigot().sendMessage(new MineDown(trustedMembers.toString().replaceAll(", $", "")).toComponent());
        player.spigot().sendMessage(new MineDown(residentMembers.toString().replaceAll(", $", "")).toComponent());
    }

    public static void sendPlayerInfo(Player player, String playerName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                UUID playerUUID = getPlayerUUID(playerName, connection);
                if (playerUUID == null) {
                    MessageManager.sendMessage(player, "error_invalid_player");
                    return;
                }
                StringJoiner playerProfile = new StringJoiner("\n");
                playerProfile.add(MessageManager.getRawMessage("player_info_header", playerName));

                boolean isOnline = isPlayerOnline(playerName);
                if (isOnline) {
                    playerProfile.add(MessageManager.getRawMessage("player_status_online"));
                } else {
                    playerProfile.add(MessageManager.getRawMessage("player_status_offline"));
                }

                Town town = getPlayerTown(playerUUID, connection);
                if (town == null) {
                    playerProfile.add(MessageManager.getRawMessage("player_info_not_in_town"));
                } else {
                    playerProfile.add(MessageManager.getRawMessage("player_info_town", town.getName(), town.getTownColorHex(),
                            Integer.toString(town.getMembers().size()), Integer.toString(town.getMaxMembers())));
                    playerProfile.add(MessageManager.getRawMessage("player_info_town_role",
                            WordUtils.capitalizeFully(town.getMembers().get(playerUUID).name())));
                }

                player.spigot().sendMessage(new MineDown(playerProfile.toString()).toComponent());
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void sendTownInfoMenu(Player player, String townName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!townExists(townName, connection)) {
                    MessageManager.sendMessage(player, "error_invalid_town");
                    return;
                }
                Town town = getTownFromName(townName, connection);
                assert town != null;
                sendTownInfo(player, town, connection);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void sendTownInfoMenu(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "town_menu_no_town");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;
                sendTownInfo(player, town, connection);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void sendTownSettings(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "town_menu_no_town");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;
                sendTownSettings(player, town.getName());
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void sendTownSettings(Player player, String townName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!townExists(townName, connection)) {
                    MessageManager.sendMessage(player, "error_invalid_town");
                    return;
                }
                Town town = getTownFromName(townName, connection);
                assert town != null;
                final boolean canEditSettings = town.getMembers().containsKey(player.getUniqueId());
                if (!canEditSettings && !player.hasPermission("husktowns.command.town.settings.other")) {
                    MessageManager.sendMessage(player, "error_no_permission");
                    return;
                }
                MessageManager.sendMessage(player, "settings_menu_header", town.getName());
                ComponentBuilder settings = new ComponentBuilder()
                        .append(new MineDown(MessageManager.getRawMessage("settings_menu_bio", "")).toComponent())
                        .append(new MineDown(town.getBio()).disable(MineDownParser.Option.ADVANCED_FORMATTING).toComponent())
                        .append("").reset();
                if (canEditSettings) {
                    settings.append(new MineDown(" " + MessageManager.getRawMessage("settings_menu_edit", "/town bio ")).toComponent());
                }
                settings.append(new MineDown("\n" + MessageManager.getRawMessage("settings_menu_greeting")).toComponent())
                        .append(new MineDown(town.getGreetingMessage()).disable(MineDownParser.Option.ADVANCED_FORMATTING).toComponent())
                        .append("").reset();
                if (canEditSettings) {
                    settings.append(new MineDown(" " + MessageManager.getRawMessage("settings_menu_edit", "/town greeting ")).toComponent());
                }
                settings.append(new MineDown("\n" + MessageManager.getRawMessage("settings_menu_farewell")).toComponent())
                        .append(new MineDown(town.getFarewellMessage()).disable(MineDownParser.Option.ADVANCED_FORMATTING).toComponent())
                        .append("").reset();
                if (canEditSettings) {
                    settings.append(new MineDown(" " + MessageManager.getRawMessage("settings_menu_edit", "/town farewell ")).toComponent());
                }
                if (town.getTownSpawn() == null) {
                    settings.append(new MineDown("\n" + MessageManager.getRawMessage("settings_menu_town_spawn_not_set")).toComponent());
                    if (canEditSettings) {
                        settings.append(new MineDown(" " + MessageManager.getRawMessage("settings_menu_update_town_spawn_location")).toComponent());
                    }
                } else {
                    if (town.isSpawnPublic()) {
                        settings.append(new MineDown("\n" + MessageManager.getRawMessage("settings_menu_town_spawn_public")).toComponent());
                    } else {
                        settings.append(new MineDown("\n" + MessageManager.getRawMessage("settings_menu_town_spawn_private")).toComponent());
                    }
                    if (canEditSettings) {
                        settings.append(new MineDown(" " + MessageManager.getRawMessage("settings_menu_update_town_spawn_privacy")).toComponent());
                    }
                }
                settings.append("\n");
                player.spigot().sendMessage(settings.retain(ComponentBuilder.FormatRetention.NONE).create());
                player.spigot().sendMessage(new MineDown(Flag.getTownFlagMenu(town.getFlags(), town.getName(), canEditSettings)).toComponent());
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void sendTownBonusesList(CommandSender sender, String targetName, int pageNumber, boolean useCache) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                ArrayList<TownBonus> bonuses = new ArrayList<>();
                if (!useCache) {
                    String townName = targetName;
                    if (!townExists(targetName, connection)) {
                        if (playerNameExists(targetName, connection)) {
                            UUID playerUUID = getPlayerUUID(townName, connection);
                            if (playerUUID == null) {
                                MessageManager.sendMessage(sender, "error_town_bonus_invalid_target");
                                return;
                            }
                            if (inTown(playerUUID, connection)) {
                                Town town = getPlayerTown(playerUUID, connection);
                                assert town != null;
                                townName = town.getName();
                            } else {
                                MessageManager.sendMessage(sender, "error_town_bonus_invalid_target");
                                return;
                            }
                        } else {
                            MessageManager.sendMessage(sender, "error_town_bonus_invalid_target");
                            return;
                        }
                    }
                    bonuses.addAll(getTownBonuses(townName, connection));
                    if (sender instanceof Player player) {
                        TownBonusCommand.addCachedBonusList(player.getUniqueId(), townName, bonuses);
                    }
                } else {
                    Player player = (Player) sender;
                    bonuses.addAll(TownBonusCommand.getPlayerCachedBonusLists(player.getUniqueId(), targetName));
                }
                if (bonuses.isEmpty()) {
                    MessageManager.sendMessage(sender, "error_no_town_bonuses", targetName);
                    return;
                }
                ArrayList<String> bonusesListStrings = new ArrayList<>();
                for (TownBonus bonus : bonuses) {
                    StringBuilder bonusesList = new StringBuilder();
                    if (bonus.getApplierUUID() != null) {
                        bonusesList.append(MessageManager.getRawMessage("bonus_list_item_player_applier", bonus.getFormattedAppliedTime(), Integer.toString(bonus.getBonusClaims()), Integer.toString(bonus.getBonusMembers()), getPlayerName(bonus.getApplierUUID(), connection)));
                    } else {
                        bonusesList.append(MessageManager.getRawMessage("bonus_list_item_console_applier", bonus.getFormattedAppliedTime(), Integer.toString(bonus.getBonusClaims()), Integer.toString(bonus.getBonusMembers())));
                    }
                    bonusesListStrings.add(bonusesList.toString());
                }

                MessageManager.sendMessage(sender, "town_bonus_list_header", targetName, Integer.toString(bonuses.size()));

                PageChatList list = new PageChatList(bonusesListStrings, 10, "/townbonus view " + targetName + " -c");
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
        ArrayList<ClaimedChunk> claimedChunks = new ArrayList<>(town.getClaimedChunks());
        if (claimedChunks.isEmpty()) {
            MessageManager.sendMessage(player, "error_no_claims_list", town.getName());
            return;
        }
        ArrayList<String> claimListStrings = new ArrayList<>();
        for (ClaimedChunk chunk : claimedChunks) {
            StringBuilder claimList = new StringBuilder();
            if (chunk.getServer().equals(HuskTowns.getSettings().getServerID())) {
                claimList.append(MessageManager.getRawMessage("claim_list_item_viewable", town.getTownColorHex(), Integer.toString(chunk.getChunkX() * 16), Integer.toString(chunk.getChunkZ() * 16), chunk.getWorld(), chunk.getServer(), Integer.toString(chunk.getChunkX()), Integer.toString(chunk.getChunkZ())));
            } else {
                claimList.append(MessageManager.getRawMessage("claim_list_item_unviewable", town.getTownColorHex(), Integer.toString(chunk.getChunkX() * 16), Integer.toString(chunk.getChunkZ() * 16), chunk.getWorld(), chunk.getServer()));
            }
            claimListStrings.add(claimList.toString());
        }

        if (town.getName().equals(HuskTowns.getSettings().getAdminTownName())) {
            MessageManager.sendMessage(player, "admin_claim_list_header",
                    Integer.toString(town.getClaimedChunksNumber()), "∞");
        } else {
            MessageManager.sendMessage(player, "claim_list_header", town.getName(),
                    Integer.toString(town.getClaimedChunksNumber()), Integer.toString(town.getMaximumClaimedChunks()));
        }
        // -c flag indicates the list will be cached
        PageChatList list = new PageChatList(claimListStrings, 10, "/claimlist " + town.getName() + " -c");
        if (list.doesNotContainPage(pageNumber)) {
            MessageManager.sendMessage(player, "error_invalid_page_number");
            return;
        }
        player.spigot().sendMessage(list.getPage(pageNumber));
    }

    public static void setTownFlag(Player player, ClaimedChunk.ChunkType chunkType, String flagIdentifier, boolean value, boolean showSettingsMenu) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;
                setTownFlag(player, town.getName(), chunkType, flagIdentifier, value, showSettingsMenu);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void setTownFlag(Player player, String townName, ClaimedChunk.ChunkType chunkType, String flagIdentifier, boolean value, boolean showSettingsMenu) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!HuskTowns.getTownDataCache().hasLoaded() || !HuskTowns.getClaimCache().hasLoaded()) {
                MessageManager.sendMessage(player, "error_cache_updating", "town data");
                return;
            }
            try (Connection connection = HuskTowns.getConnection()) {
                if (!townExists(townName, connection)) {
                    MessageManager.sendMessage(player, "error_invalid_town");
                    return;
                }
                if (!player.hasPermission("husktowns.command.town.flag.other")) {
                    if (!inTown(player.getUniqueId(), connection)) {
                        MessageManager.sendMessage(player, "error_not_in_town");
                        return;
                    }
                    Town town = getPlayerTown(player.getUniqueId(), connection);
                    assert town != null;
                    if (!town.getName().equalsIgnoreCase(townName)) {
                        MessageManager.sendMessage(player, "error_no_permission");
                        return;
                    }
                    if (getTownRole(player.getUniqueId(), connection) == TownRole.RESIDENT) {
                        MessageManager.sendMessage(player, "error_insufficient_flag_privileges");
                        return;
                    }
                }
                for (Flag flag : getTownFlags(townName, connection).get(chunkType)) {
                    if (flag.getIdentifier().equalsIgnoreCase(flagIdentifier)) {
                        if (!player.hasPermission(flag.getSetPermission())) {
                            MessageManager.sendMessage(player, "error_no_flag_permission");
                            return;
                        }
                        flag.setFlag(value);
                        updateTownFlagData(townName, chunkType, flag, connection);
                        if (showSettingsMenu) {
                            sendTownSettings(player, townName);
                        } else {
                            MessageManager.sendMessage(player, "town_flag_update_success", flag.getDisplayName(), chunkType.name().toLowerCase(), Boolean.toString(value));
                        }
                        return;
                    }
                }
                MessageManager.sendMessage(player, "error_invalid_flag");
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });

    }

    public static void showClaimList(Player player, int pageNumber) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;
                ClaimListCommand.addCachedClaimList(player.getUniqueId(), town.getName(), town);
                sendClaimList(player, town, pageNumber);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void showClaimList(Player player, String townName, int pageNumber, boolean useCache) {
        if (useCache) {
            sendClaimList(player, ClaimListCommand.getPlayerCachedClaimLists(player.getUniqueId(), townName), pageNumber);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!townExists(townName, connection)) {
                    MessageManager.sendMessage(player, "error_invalid_town");
                    return;
                }
                Town town = getTownFromName(townName, connection);
                assert town != null;
                ClaimListCommand.addCachedClaimList(player.getUniqueId(), town.getName(), town);
                sendClaimList(player, town, pageNumber);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    private static Town getTownFromID(int townID, Connection connection) throws SQLException {
        try (PreparedStatement getTownRole = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `id`=?;")) {
            getTownRole.setInt(1, townID);
            ResultSet townRoleResults = getTownRole.executeQuery();
            if (townRoleResults != null) {
                if (townRoleResults.next()) {
                    final String townName = townRoleResults.getString("name");
                    final double money = townRoleResults.getDouble("money");
                    final Timestamp timestamp = townRoleResults.getTimestamp("founded");
                    final String greetingMessage = townRoleResults.getString("greeting_message");
                    final String farewellMessage = townRoleResults.getString("farewell_message");
                    final String bio = townRoleResults.getString("bio");
                    final TeleportationPoint spawnTeleportationPoint = getTeleportationPoint(townRoleResults.getInt("spawn_location_id"), connection);
                    final boolean townSpawnPrivacy = townRoleResults.getBoolean("is_spawn_public");
                    final HashSet<ClaimedChunk> claimedChunks = getClaimedChunks(townName, connection);
                    final HashMap<UUID, TownRole> members = getTownMembers(townName, connection);
                    final HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> flags = getTownFlags(townName, connection);
                    getTownRole.close();
                    return new Town(townName, claimedChunks, members, spawnTeleportationPoint, townSpawnPrivacy, money, greetingMessage, farewellMessage, bio, timestamp.toInstant().getEpochSecond(), flags);
                }
            }
        }
        return null;
    }

    public static Town getPlayerTown(UUID uuid, Connection connection) throws SQLException {
        try (PreparedStatement getTown = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getTownsTable() +
                        " WHERE `id`=(SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);")) {
            getTown.setString(1, uuid.toString());
            ResultSet townResults = getTown.executeQuery();
            if (townResults != null) {
                if (townResults.next()) {
                    final String townName = townResults.getString("name");
                    final double money = townResults.getDouble("money");
                    final Timestamp timestamp = townResults.getTimestamp("founded");
                    final String greetingMessage = townResults.getString("greeting_message");
                    final String farewellMessage = townResults.getString("farewell_message");
                    final String bio = townResults.getString("bio");
                    final TeleportationPoint spawnTeleportationPoint = getTeleportationPoint(townResults.getInt("spawn_location_id"), connection);
                    final boolean townSpawnPrivacy = townResults.getBoolean("is_spawn_public");
                    final HashSet<ClaimedChunk> claimedChunks = getClaimedChunks(townName, connection);
                    final HashMap<UUID, TownRole> members = getTownMembers(townName, connection);
                    final HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> flags = getTownFlags(townName, connection);
                    getTown.close();
                    return new Town(townName, claimedChunks, members, spawnTeleportationPoint, townSpawnPrivacy, money, greetingMessage, farewellMessage, bio, timestamp.toInstant().getEpochSecond(), flags);
                }
            }
        }
        return null;
    }

    private static TeleportationPoint getTeleportationPoint(int teleportationPointID, Connection connection) throws SQLException {
        try (PreparedStatement getTeleportationPoint = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getLocationsTable() + " WHERE `id`=?;")) {
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
        }
        return null;
    }

    public static Boolean getIsTeleporting(Player player, Connection connection) throws SQLException {
        try (PreparedStatement getPlayerTeleporting = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?;")) {
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
    }

    public static TeleportationPoint getPlayerDestination(Player player, Connection connection) throws SQLException {
        try (PreparedStatement getPlayerDestination = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getLocationsTable() + " WHERE `id`=(SELECT `teleport_destination_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);")) {
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
        }
        return null;
    }

    public static void setPlayerDestinationToSpawn(Player player, Connection connection) throws SQLException {
        Town town = getPlayerTown(player.getUniqueId(), connection);
        if (town != null) {
            try (PreparedStatement setPlayerDestinationStatement = connection.prepareStatement(
                    "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `teleport_destination_id`=(SELECT `spawn_location_id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) WHERE `uuid`=?;")) {
                setPlayerDestinationStatement.setString(1, town.getName());
                setPlayerDestinationStatement.setString(2, player.getUniqueId().toString());
                setPlayerDestinationStatement.executeUpdate();
            }
        }
    }

    public static void setPlayerTeleporting(Player player, boolean isTeleporting, Connection connection) throws SQLException {
        try (PreparedStatement setPlayerTeleportingStatement = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getPlayerTable() + " SET `is_teleporting`=? WHERE `uuid`=?;")) {
            setPlayerTeleportingStatement.setBoolean(1, isTeleporting);
            setPlayerTeleportingStatement.setString(2, player.getUniqueId().toString());
            setPlayerTeleportingStatement.executeUpdate();
        }
    }

    public static void deleteTownSpawnData(Player player, Connection connection) throws SQLException {
        try (PreparedStatement townSpawnData = connection.prepareStatement(
                "DELETE FROM " + HuskTowns.getSettings().getLocationsTable() + " WHERE `id`=(SELECT `spawn_location_id` FROM "
                        + HuskTowns.getSettings().getTownsTable() + " WHERE `id`=(SELECT `town_id` FROM "
                        + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?));")) {
            townSpawnData.setString(1, player.getUniqueId().toString());
            townSpawnData.executeUpdate();
        }
    }


    private static void setTownSpawnData(Player player, TeleportationPoint point, Connection connection) throws SQLException {
        try (PreparedStatement dataInsertionStatement = connection.prepareStatement(
                "INSERT INTO " + HuskTowns.getSettings().getLocationsTable() + " (server,world,x,y,z,yaw,pitch) VALUES(?,?,?,?,?,?,?);")) {
            dataInsertionStatement.setString(1, point.getServer());
            dataInsertionStatement.setString(2, point.getWorldName());
            dataInsertionStatement.setDouble(3, point.getX());
            dataInsertionStatement.setDouble(4, point.getY());
            dataInsertionStatement.setDouble(5, point.getZ());
            dataInsertionStatement.setFloat(6, point.getYaw());
            dataInsertionStatement.setFloat(7, point.getPitch());
            dataInsertionStatement.executeUpdate();
        }

        String lastInsertString;
        if (HuskTowns.getSettings().getDatabaseType() == Settings.DatabaseType.MYSQL) {
            lastInsertString = "LAST_INSERT_ID()";
        } else {
            lastInsertString = "last_insert_rowid()";
        }
        try (PreparedStatement townSpawnData = connection.prepareStatement(
                "UPDATE " + HuskTowns.getSettings().getTownsTable() + " SET `spawn_location_id`=(SELECT "
                        + lastInsertString + ") WHERE `id`=(SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable()
                        + " WHERE `uuid`=?);")) {
            townSpawnData.setString(1, player.getUniqueId().toString());
            townSpawnData.executeUpdate();
        }
    }

    private static void createAdminTown(Connection connection) throws SQLException {
        Town adminTown = new Town();
        addTownData(adminTown, connection);
        HuskTowns.getTownDataCache().setGreetingMessage(HuskTowns.getSettings().getAdminTownName(),
                MessageManager.getRawMessage("admin_claim_greeting_message"));
        HuskTowns.getTownDataCache().setFarewellMessage(HuskTowns.getSettings().getAdminTownName(),
                MessageManager.getRawMessage("admin_claim_farewell_message"));
        HuskTowns.getTownDataCache().setTownBio(HuskTowns.getSettings().getAdminTownName(),
                MessageManager.getRawMessage("admin_town_bio"));
    }

    public static void createAdminClaim(Player player, Location location, boolean showMap) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!townExists(HuskTowns.getSettings().getAdminTownName(), connection)) {
                    createAdminTown(connection);
                }

                ClaimedChunk chunk = new ClaimedChunk(player, HuskTowns.getSettings().getServerID(), location, HuskTowns.getSettings().getAdminTownName());
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
                MessageManager.sendMessage(player, "admin_claim_success", Integer.toString(chunk.getChunkX() * 16), Integer.toString(chunk.getChunkZ() * 16), Integer.toString(chunk.getChunkX()), Integer.toString(chunk.getChunkZ()));
                if (showMap) {
                    player.spigot().sendMessage(new MineDown("\n" + MapCommand.getMapAround(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ(),
                            player.getWorld().getName(), HuskTowns.getSettings().getAdminTownName(), true)).toComponent());
                }

                Bukkit.getScheduler().runTask(plugin, () -> ClaimViewerUtil.showParticles(player, 5, chunk));

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });

    }

    public static void createTown(Player player, String townName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
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
                    if (townName.toLowerCase().contains(name.toLowerCase()) || townName.equalsIgnoreCase(HuskTowns.getSettings().getAdminTownName())) {
                        MessageManager.sendMessage(player, "error_town_name_prohibited");
                        return;
                    }
                }
                // Check economy stuff
                if (HuskTowns.getSettings().doEconomy()) {
                    double creationCost = HuskTowns.getSettings().getTownCreationCost();
                    if (creationCost > 0) {
                        if (!VaultIntegration.takeMoney(player, creationCost)) {
                            MessageManager.sendMessage(player, "error_insufficient_funds_need", VaultIntegration.format(creationCost));
                            return;
                        }
                        MessageManager.sendMessage(player, "money_spent_notice", VaultIntegration.format(creationCost), "found a new town");
                    }
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (EventCannon.fireEvent(new TownCreateEvent(player, townName))) {
                        return;
                    }

                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try (Connection connection1 = HuskTowns.getConnection()) {
                            // Insert the town into the database
                            Town town = new Town(player, townName);
                            addTownData(town, connection1);
                            setPlayerTownData(player.getUniqueId(), townName, connection1);
                            setPlayerRoleData(player.getUniqueId(), TownRole.MAYOR, connection1);
                            HuskTowns.getPlayerCache().setPlayerName(player.getUniqueId(), player.getName());

                            HuskTowns.getTownDataCache().setGreetingMessage(townName,
                                    MessageManager.getRawMessage("default_greeting_message", town.getName()));
                            HuskTowns.getTownDataCache().setFarewellMessage(townName,
                                    MessageManager.getRawMessage("default_farewell_message", town.getName()));
                            HuskTowns.getTownDataCache().setTownBio(town.getName(),
                                    MessageManager.getRawMessage("default_town_bio", town.getName()));
                            MessageManager.sendMessage(player, "town_creation_success", town.getName());
                        } catch (SQLException exception) {
                            plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
                        }
                    });
                });

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void renameTown(Player player, String newTownName) {
        if (!HuskTowns.getTownBonusesCache().hasLoaded() || !HuskTowns.getTownDataCache().hasLoaded() || !HuskTowns.getClaimCache().hasLoaded() || !HuskTowns.getPlayerCache().hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", "all cached");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                // Check that the player is in a town
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;

                // Check that the player is mayor
                TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role != TownRole.MAYOR) {
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
                    if (newTownName.toLowerCase().contains(name.toLowerCase()) || newTownName.equalsIgnoreCase(HuskTowns.getSettings().getAdminTownName())) {
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
                        if (!VaultIntegration.takeMoney(player, renameCost)) {
                            MessageManager.sendMessage(player, "error_insufficient_funds_need", VaultIntegration.format(renameCost));
                            return;
                        }
                        MessageManager.sendMessage(player, "money_spent_notice", VaultIntegration.format(renameCost), "change the town name");
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
                                CrossServerMessageHandler.getMessage(getPlayerName(uuid, connection), Message.MessageType.RENAME_NOTIFICATION,
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
        final Location playerLocation = player.getLocation();
        final World playerWorld = player.getWorld();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                // Check that the player is in a town
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                // Check that the player is a trusted resident or mayor
                TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == TownRole.RESIDENT) {
                    MessageManager.sendMessage(player, "error_insufficient_set_spawn_privileges");
                    return;
                }
                // Check that the town message is of a valid length
                ClaimedChunk chunk = getClaimedChunk(HuskTowns.getSettings().getServerID(),
                        playerWorld.getName(),
                        playerLocation.getChunk().getX(),
                        playerLocation.getChunk().getZ(),
                        connection);
                if (chunk == null) {
                    MessageManager.sendMessage(player, "error_cant_set_spawn_outside_claim");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;
                if (!chunk.getTown().equals(town.getName())) {
                    MessageManager.sendMessage(player, "error_claim_not_member_of_town", chunk.getTown());
                    return;
                }

                // Check economy stuff
                if (HuskTowns.getSettings().doEconomy()) {
                    double farewellCost = HuskTowns.getSettings().getSetSpawnCost();
                    if (farewellCost > 0) {
                        if (!VaultIntegration.takeMoney(player, farewellCost)) {
                            MessageManager.sendMessage(player, "error_insufficient_funds_need", VaultIntegration.format(farewellCost));
                            return;
                        }
                        MessageManager.sendMessage(player, "money_spent_notice", VaultIntegration.format(farewellCost), "set the town spawn point");
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

    public static void teleportPlayerToOtherSpawn(Player player, String targetTown) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                // Check that the player is in a town
                if (!townExists(targetTown, connection)) {
                    MessageManager.sendMessage(player, "error_invalid_town");
                    return;
                }
                Town town = getTownFromName(targetTown, connection);
                assert town != null;
                // Check that the spawn has been set and that it is public
                TeleportationPoint spawn = town.getTownSpawn();
                if (spawn == null) {
                    MessageManager.sendMessage(player, "error_town_spawn_not_set");
                    return;
                }
                if (!town.isSpawnPublic()) {
                    MessageManager.sendMessage(player, "error_town_spawn_not_public");
                    return;
                }

                // Teleport the player to the spawn
                TeleportationHandler.teleportPlayer(player, spawn);
                MessageManager.sendMessage(player, "teleporting_you_to_other_town_spawn", town.getName());

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void teleportPlayerToSpawn(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                // Check that the player is in a town
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;
                TeleportationPoint spawn = town.getTownSpawn();
                if (spawn == null) {
                    MessageManager.sendMessage(player, "error_town_spawn_not_set");
                    return;
                }

                // Teleport the player to the spawn
                TeleportationHandler.teleportPlayer(player, spawn);
                MessageManager.sendMessage(player, "teleporting_you_to_town_spawn");

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }


    public static void executeTeleportToSpawn(Player player, TeleportationPoint point) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                DataManager.setPlayerDestinationToSpawn(player, connection);
                DataManager.setPlayerTeleporting(player, true, connection);
                CrossServerMessageHandler.movePlayerServer(player, point.getServer());
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void handleTeleportingPlayers(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
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

    public static void toggleTownPrivacy(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                // Check that the player is in a town
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                // Check that the player is a trusted resident or mayor
                TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == TownRole.RESIDENT) {
                    MessageManager.sendMessage(player, "error_insufficient_spawn_privacy_privileges");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;
                if (town.getTownSpawn() == null) {
                    MessageManager.sendMessage(player, "error_town_spawn_not_set");
                    return;
                }
                if (!town.isSpawnPublic()) {
                    // Make public
                    if (HuskTowns.getSettings().doEconomy()) {
                        double farewellCost = HuskTowns.getSettings().getMakeSpawnPublicCost();
                        if (farewellCost > 0) {
                            if (!VaultIntegration.takeMoney(player, farewellCost)) {
                                MessageManager.sendMessage(player, "error_insufficient_funds_need", VaultIntegration.format(farewellCost));
                                return;
                            }
                            MessageManager.sendMessage(player, "money_spent_notice", VaultIntegration.format(farewellCost), "make the town spawn public");
                        }
                    }

                    updateTownSpawnPrivacyData(town.getName(), true, connection);
                    MessageManager.sendMessage(player, "town_update_spawn_public");
                } else {
                    // Make private
                    updateTownSpawnPrivacyData(town.getName(), false, connection);
                    MessageManager.sendMessage(player, "town_update_spawn_private");
                }

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void updateTownBio(Player player, String newTownBio) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                // Check that the player is in a town
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                // Check that the player is a trusted resident or mayor
                TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == TownRole.RESIDENT) {
                    MessageManager.sendMessage(player, "error_insufficient_bio_privileges");
                    return;
                }
                // Check that the town bio is of a valid length
                if (newTownBio.length() > 255 || newTownBio.length() < 3) {
                    MessageManager.sendMessage(player, "error_town_bio_invalid_length");
                    return;
                }
                // Check economy stuff
                if (HuskTowns.getSettings().doEconomy()) {
                    double farewellCost = HuskTowns.getSettings().getUpdateBioCost();
                    if (farewellCost > 0) {
                        if (!VaultIntegration.takeMoney(player, farewellCost)) {
                            MessageManager.sendMessage(player, "error_insufficient_funds_need", VaultIntegration.format(farewellCost));
                            return;
                        }
                        MessageManager.sendMessage(player, "money_spent_notice", VaultIntegration.format(farewellCost), "update the town bio");
                    }
                }

                // Update the town name on the database & cache
                updateTownBioData(player.getUniqueId(), newTownBio, connection);
                MessageManager.sendMessage(player, "town_update_bio_success");
                player.spigot().sendMessage(new MineDown("&7\"" + newTownBio + "&7\"").disable(MineDownParser.Option.ADVANCED_FORMATTING).toComponent());

            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static CompletableFuture<Boolean> canPerformTownCommand(Player player, TownRole requiredRole) {
        CompletableFuture<Boolean> canPerform = new CompletableFuture<>();
        final UUID playerUUID = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (inTown(playerUUID, connection)) {
                    final TownRole playerRole = getTownRole(playerUUID, connection);
                    if (playerRole != null) {
                        if (playerRole.roleWeight >= requiredRole.roleWeight) {
                            canPerform.complete(true);
                        }
                    }
                }
                canPerform.complete(false);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred");
                canPerform.completeExceptionally(exception);
            }
        });
        return canPerform;
    }

    public static void updateTownFarewell(Player player, String newFarewellMessage) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                // Check that the player is in a town
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                // Check that the player is a trusted resident or mayor
                TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == TownRole.RESIDENT) {
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
                        if (!VaultIntegration.takeMoney(player, farewellCost)) {
                            MessageManager.sendMessage(player, "error_insufficient_funds_need", VaultIntegration.format(farewellCost));
                            return;
                        }
                        MessageManager.sendMessage(player, "money_spent_notice", VaultIntegration.format(farewellCost), "update the town farewell message");
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                // Check that the player is in a town
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                // Check that the player is a trusted resident or mayor
                TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == TownRole.RESIDENT) {
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
                        if (!VaultIntegration.takeMoney(player, greetingCost)) {
                            MessageManager.sendMessage(player, "error_insufficient_funds_need", VaultIntegration.format(greetingCost));
                            return;
                        }
                        MessageManager.sendMessage(player, "money_spent_notice", VaultIntegration.format(greetingCost), "update the town greeting message");
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

    public static TownRole getTownRole(UUID uuid, Connection connection) throws SQLException {
        try (PreparedStatement getTownRole = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE uuid=?;")) {
            getTownRole.setString(1, uuid.toString());
            ResultSet townRoleResults = getTownRole.executeQuery();
            if (townRoleResults != null) {
                if (townRoleResults.next()) {
                    final TownRole role = getTownRoleFromID(townRoleResults.getInt("town_role"));
                    getTownRole.close();
                    return role;
                } else {
                    getTownRole.close();
                    return null;
                }
            }
        }
        return null;
    }

    // Returns if a player is in a town
    private static boolean inTown(UUID uuid, Connection connection) throws SQLException {
        try (PreparedStatement alreadyInTownCheck = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=? AND `town_id` IS NOT NULL;")) {
            alreadyInTownCheck.setString(1, uuid.toString());
            ResultSet alreadyInTownResult = alreadyInTownCheck.executeQuery();
            if (alreadyInTownResult != null) {
                final boolean inTown = alreadyInTownResult.next();
                alreadyInTownCheck.close();
                return inTown;
            }
        }
        return false;
    }

    // Returns if a chunk is claimed
    private static boolean isClaimed(String server, String worldName, int chunkX, int chunkZ, Connection connection) throws SQLException {
        try (PreparedStatement checkClaimed = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE `chunk_x`=? AND `chunk_z`=? AND `world`=? AND `server`=?;")) {
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
        }
        return false;
    }

    private static void addAdminClaim(ClaimedChunk chunk, Connection connection) throws SQLException {
        try (PreparedStatement claimCreationStatement = connection.prepareStatement(
                "INSERT INTO " + HuskTowns.getSettings().getClaimsTable() + " (town_id,claim_time,claimer_id,server,world,chunk_x,chunk_z,chunk_type) " +
                        "VALUES((SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE name=?),?," +
                        "(SELECT `id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?),?,?,?,?,0);")) {
            claimCreationStatement.setString(1, HuskTowns.getSettings().getAdminTownName());
            claimCreationStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            claimCreationStatement.setString(3, chunk.getClaimerUUID().toString());
            claimCreationStatement.setString(4, chunk.getServer());
            claimCreationStatement.setString(5, chunk.getWorld());
            claimCreationStatement.setInt(6, chunk.getChunkX());
            claimCreationStatement.setInt(7, chunk.getChunkZ());
            claimCreationStatement.executeUpdate();
        }
        HuskTowns.getClaimCache().add(chunk);
    }

    private static void addClaimData(ClaimedChunk chunk, Connection connection) throws SQLException {
        try (PreparedStatement claimCreationStatement = connection.prepareStatement(
                "INSERT INTO " + HuskTowns.getSettings().getClaimsTable() + " (town_id,claim_time,claimer_id,server,world,chunk_x,chunk_z,chunk_type) " +
                        "VALUES((SELECT `town_id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE uuid=?),?," +
                        "(SELECT `id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?),?,?,?,?,0);")) {
            claimCreationStatement.setString(1, chunk.getClaimerUUID().toString());
            claimCreationStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            claimCreationStatement.setString(3, chunk.getClaimerUUID().toString());
            claimCreationStatement.setString(4, chunk.getServer());
            claimCreationStatement.setString(5, chunk.getWorld());
            claimCreationStatement.setInt(6, chunk.getChunkX());
            claimCreationStatement.setInt(7, chunk.getChunkZ());
            claimCreationStatement.executeUpdate();
            HuskTowns.getClaimCache().add(chunk);
        } catch (SQLIntegrityConstraintViolationException ignored) {
        }
    }

    private static void addBonusData(TownBonus bonus, String townName, Connection connection) throws SQLException {
        if (bonus.getApplierUUID() != null) {
            try (PreparedStatement bonusCreationStatement = connection.prepareStatement(
                    "INSERT INTO " + HuskTowns.getSettings().getBonusesTable() + " (town_id,applier_id,applied_time,bonus_claims,bonus_members) " +
                            "VALUES((SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE name=?)," +
                            "(SELECT `id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?),?,?,?);")) {
                bonusCreationStatement.setString(1, townName);
                bonusCreationStatement.setString(2, bonus.getApplierUUID().toString());
                bonusCreationStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                bonusCreationStatement.setInt(4, bonus.getBonusClaims());
                bonusCreationStatement.setInt(5, bonus.getBonusMembers());
                bonusCreationStatement.executeUpdate();
            }

        } else {
            try (PreparedStatement consoleBonusCreationStatement = connection.prepareStatement(
                    "INSERT INTO " + HuskTowns.getSettings().getBonusesTable() + " (town_id,applied_time,bonus_claims,bonus_members) " +
                            "VALUES((SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE name=?),?,?,?);")) {
                consoleBonusCreationStatement.setString(1, townName);
                consoleBonusCreationStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                consoleBonusCreationStatement.setInt(3, bonus.getBonusClaims());
                consoleBonusCreationStatement.setInt(4, bonus.getBonusMembers());
                consoleBonusCreationStatement.executeUpdate();
            }
        }

        HuskTowns.getTownBonusesCache().add(townName, bonus);
        if (HuskTowns.getSettings().doBungee()) {
            for (Player updateNotificationDispatcher : Bukkit.getOnlinePlayers()) {
                final UUID applierUUID = bonus.getApplierUUID();
                if (applierUUID != null) {
                    CrossServerMessageHandler.getMessage(Message.MessageType.ADD_TOWN_BONUS, townName, Integer.toString(bonus.getBonusClaims()), Integer.toString(bonus.getBonusMembers()), Long.toString(bonus.getAppliedTimestamp()), applierUUID.toString()).sendToAll(updateNotificationDispatcher);
                } else {
                    CrossServerMessageHandler.getMessage(Message.MessageType.ADD_TOWN_BONUS, townName, Integer.toString(bonus.getBonusClaims()), Integer.toString(bonus.getBonusMembers()), Long.toString(bonus.getAppliedTimestamp())).sendToAll(updateNotificationDispatcher);
                }
                return;
            }
        }
    }

    public static void claimChunk(Player player, Location claimLocation, boolean showMap) {
        final ClaimCache claimCache = HuskTowns.getClaimCache();
        if (!claimCache.hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", claimCache.getName());
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;

                ClaimedChunk chunk = new ClaimedChunk(player, HuskTowns.getSettings().getServerID(), claimLocation, town.getName());
                if (isClaimed(chunk.getServer(), chunk.getWorld(), chunk.getChunkX(), chunk.getChunkZ(), connection)) {
                    MessageManager.sendMessage(player, "error_already_claimed");
                    return;
                }

                TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == TownRole.RESIDENT) {
                    MessageManager.sendMessage(player, "error_insufficient_claim_privileges");
                    return;
                }

                for (String worldName : HuskTowns.getSettings().getUnClaimableWorlds()) {
                    if (player.getWorld().getName().equals(worldName)) {
                        MessageManager.sendMessage(player, "error_unclaimable_world");
                        return;
                    }
                }

                if (town.getClaimedChunks().size() >= town.getMaximumClaimedChunks()) {
                    MessageManager.sendMessage(player, "error_maximum_claims_made", Integer.toString(town.getMaximumClaimedChunks()));
                    return;
                }

                // Charge for setting the town spawn if needed
                if (HuskTowns.getSettings().doEconomy() && town.getClaimedChunks().size() == 0 && HuskTowns.getSettings().setTownSpawnInFirstClaim()) {
                    double spawnCost = HuskTowns.getSettings().getSetSpawnCost();
                    if (spawnCost > 0) {
                        if (!VaultIntegration.takeMoney(player, spawnCost)) {
                            MessageManager.sendMessage(player, "error_insufficient_funds_need", VaultIntegration.format(spawnCost));
                            return;
                        }
                        MessageManager.sendMessage(player, "money_spent_notice", VaultIntegration.format(spawnCost), "create a claim and set the town spawn point");
                    }
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (EventCannon.fireEvent(new ClaimEvent(player, chunk))) {
                        return;
                    }
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try (Connection connection1 = HuskTowns.getConnection()) {
                            addClaimData(chunk, connection1);
                            MessageManager.sendMessage(player, "claim_success", Integer.toString(chunk.getChunkX() * 16), Integer.toString(chunk.getChunkZ() * 16), Integer.toString(chunk.getChunkX()), Integer.toString(chunk.getChunkZ()));
                            if (showMap) {
                                player.spigot().sendMessage(new MineDown("\n" + MapCommand.getMapAround(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ(),
                                        player.getWorld().getName(), town.getName(), true)).toComponent());
                            }

                            if (town.getClaimedChunks().size() == 0 && HuskTowns.getSettings().setTownSpawnInFirstClaim()) {
                                setTownSpawnData(player, new TeleportationPoint(player.getLocation(), HuskTowns.getSettings().getServerID()), connection1);
                            }
                            Bukkit.getScheduler().runTask(plugin, () -> ClaimViewerUtil.showParticles(player, 5, chunk));
                        } catch (SQLException exception) {
                            plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
                        }
                    });
                });
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static UUID getPlayerUUID(String username, Connection connection) throws SQLException {
        try (PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE username=?;")) {
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
        }
        return null;
    }

    private static UUID getPlayerUUID(int playerID, Connection connection) throws SQLException {
        if (playerID == 0) {
            return null;
        }
        try (PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE id=?;")) {
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
        }
        return null;
    }

    private static ClaimedChunk.ChunkType getChunkType(int chunkTypeID) {
        return switch (chunkTypeID) {
            case 1 -> ClaimedChunk.ChunkType.FARM;
            case 2 -> ClaimedChunk.ChunkType.PLOT;
            default -> ClaimedChunk.ChunkType.REGULAR;
        };
    }

    private static Integer getIDFromChunkType(ClaimedChunk.ChunkType type) {
        return switch (type) {
            case FARM -> 1;
            case PLOT -> 2;
            default -> 0;
        };
    }

    private static void addPlotMemberData(ClaimedChunk chunk, UUID plotMember, Connection connection) throws SQLException {
        try (PreparedStatement addPlotMemberStatement = connection.prepareStatement("INSERT INTO " + HuskTowns.getSettings().getPlotMembersTable() + "(`claim_id`,`member_id`) VALUES ((SELECT `id` FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE `chunk_x`=? AND `chunk_z`=? AND `world`=? AND `server`=?),(SELECT `id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?));")) {
            addPlotMemberStatement.setInt(1, chunk.getChunkX());
            addPlotMemberStatement.setInt(2, chunk.getChunkZ());
            addPlotMemberStatement.setString(3, chunk.getWorld());
            addPlotMemberStatement.setString(4, chunk.getServer());
            addPlotMemberStatement.setString(5, plotMember.toString());
            addPlotMemberStatement.executeUpdate();
        }
        // Update in the cache (this does NOT need to be updated cross-server since the cache only holds claims on this server)
        HuskTowns.getClaimCache().getChunkAt(chunk.getChunkX(), chunk.getChunkZ(), chunk.getWorld()).addPlotMember(plotMember);
    }

    public static void addPlotMember(Player adder, ClaimedChunk claimedChunk, String newPlotMember) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(adder.getUniqueId(), connection)) {
                    MessageManager.sendMessage(adder, "error_not_in_town");
                    return;
                }
                if (claimedChunk == null) {
                    MessageManager.sendMessage(adder, "error_not_standing_on_claim");
                    return;
                }
                Town town = getPlayerTown(adder.getUniqueId(), connection);
                assert town != null;
                if (!town.getName().equals(claimedChunk.getTown())) {
                    MessageManager.sendMessage(adder, "error_claim_not_member_of_town", claimedChunk.getTown());
                    return;
                }
                if (claimedChunk.getChunkType() != ClaimedChunk.ChunkType.PLOT) {
                    MessageManager.sendMessage(adder, "error_not_a_plot");
                    return;
                }
                UUID plotChunkOwner = claimedChunk.getPlotChunkOwner();
                if (plotChunkOwner == null) {
                    MessageManager.sendMessage(adder, "error_plot_not_claimed");
                    return;
                }
                TownRole role = getTownRole(adder.getUniqueId(), connection);
                if (role == TownRole.RESIDENT) {
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
                assert targetPlayerUUID != null;
                if (getPlotMembers(claimedChunk, connection).contains(targetPlayerUUID)) {
                    MessageManager.sendMessage(adder, "error_already_plot_member");
                    return;
                }
                if (targetPlayerUUID.toString().equals(plotChunkOwner.toString())) {
                    MessageManager.sendMessage(adder, "error_plot_member_already_owner");
                    return;
                }
                addPlotMemberData(claimedChunk, targetPlayerUUID, connection);
                MessageManager.sendMessage(adder, "plot_member_added", newPlotMember, Integer.toString(claimedChunk.getChunkX() * 16), Integer.toString(claimedChunk.getChunkZ() * 16));
                Bukkit.getScheduler().runTask(plugin, () -> ClaimViewerUtil.showParticles(adder, 5, claimedChunk));
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    private static void removePlotMemberData(ClaimedChunk chunk, UUID plotMember, Connection connection) throws SQLException {
        try (PreparedStatement removePlotMemberStatement = connection.prepareStatement("DELETE FROM " + HuskTowns.getSettings().getPlotMembersTable() + " WHERE `claim_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE `chunk_x`=? AND `chunk_z`=? AND `world`=? AND `server`=?) AND `member_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `uuid`=?);")) {
            removePlotMemberStatement.setInt(1, chunk.getChunkX());
            removePlotMemberStatement.setInt(2, chunk.getChunkZ());
            removePlotMemberStatement.setString(3, chunk.getWorld());
            removePlotMemberStatement.setString(4, chunk.getServer());
            removePlotMemberStatement.setString(5, plotMember.toString());
            removePlotMemberStatement.executeUpdate();
        }
        // Update in the cache (this does NOT need to be updated cross-server since the cache only holds claims on this server)
        HuskTowns.getClaimCache().getChunkAt(chunk.getChunkX(), chunk.getChunkZ(), chunk.getWorld()).removePlotMember(plotMember);
    }

    public static void removePlotMember(Player remover, ClaimedChunk claimedChunk, String plotMemberToRemove) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(remover.getUniqueId(), connection)) {
                    MessageManager.sendMessage(remover, "error_not_in_town");
                    return;
                }
                if (claimedChunk == null) {
                    MessageManager.sendMessage(remover, "error_not_standing_on_claim");
                    return;
                }
                Town town = getPlayerTown(remover.getUniqueId(), connection);
                assert town != null;
                if (!town.getName().equals(claimedChunk.getTown())) {
                    MessageManager.sendMessage(remover, "error_claim_not_member_of_town", claimedChunk.getTown());
                    return;
                }
                if (claimedChunk.getChunkType() != ClaimedChunk.ChunkType.PLOT) {
                    MessageManager.sendMessage(remover, "error_not_a_plot");
                    return;
                }
                if (claimedChunk.getPlotChunkOwner() == null) {
                    MessageManager.sendMessage(remover, "error_plot_not_claimed");
                    return;
                }
                TownRole role = getTownRole(remover.getUniqueId(), connection);
                if (role == TownRole.RESIDENT) {
                    if (claimedChunk.getPlotChunkOwner() != remover.getUniqueId()) {
                        MessageManager.sendMessage(remover, "error_not_your_plot");
                        return;
                    }
                }
                if (!playerNameExists(plotMemberToRemove, connection)) {
                    MessageManager.sendMessage(remover, "error_invalid_player");
                    return;
                }
                UUID targetPlayerUUID = getPlayerUUID(plotMemberToRemove, connection);
                assert targetPlayerUUID != null;
                if (!getPlotMembers(claimedChunk, connection).contains(targetPlayerUUID)) {
                    MessageManager.sendMessage(remover, "error_not_a_plot_member");
                    return;
                }
                removePlotMemberData(claimedChunk, targetPlayerUUID, connection);
                MessageManager.sendMessage(remover, "plot_member_removed", plotMemberToRemove, Integer.toString(claimedChunk.getChunkX() * 16), Integer.toString(claimedChunk.getChunkZ() * 16));
                Bukkit.getScheduler().runTask(plugin, () -> ClaimViewerUtil.showParticles(remover, 5, claimedChunk));
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    private static void clearPlotMembers(ClaimedChunk chunk, Connection connection) throws SQLException {
        try (PreparedStatement deletePlotMembersStatement = connection.prepareStatement("DELETE FROM " + HuskTowns.getSettings().getPlotMembersTable() + " WHERE `claim_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE `chunk_x`=? AND `chunk_z`=? AND `world`=? AND `server`=?);")) {
            deletePlotMembersStatement.setInt(1, chunk.getChunkX());
            deletePlotMembersStatement.setInt(2, chunk.getChunkZ());
            deletePlotMembersStatement.setString(3, chunk.getWorld());
            deletePlotMembersStatement.setString(4, chunk.getServer());
            deletePlotMembersStatement.executeUpdate();
        }
    }

    private static HashSet<UUID> getPlotMembers(ClaimedChunk chunk, Connection connection) throws SQLException {
        HashSet<UUID> plotMembers = new HashSet<>();
        try (PreparedStatement getPlotMembersStatement = connection.prepareStatement("SELECT * FROM " + HuskTowns.getSettings().getPlotMembersTable() + " WHERE `claim_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE `chunk_x`=? AND `chunk_z`=? AND `world`=? AND `server`=?);")) {
            getPlotMembersStatement.setInt(1, chunk.getChunkX());
            getPlotMembersStatement.setInt(2, chunk.getChunkZ());
            getPlotMembersStatement.setString(3, chunk.getWorld());
            getPlotMembersStatement.setString(4, chunk.getServer());
            ResultSet resultSet = getPlotMembersStatement.executeQuery();
            if (resultSet != null) {
                while (resultSet.next()) {
                    plotMembers.add(getPlayerUUID(resultSet.getInt("member_id"), connection));
                }
            }
        }
        return plotMembers;
    }

    private static HashSet<UUID> getPlotMembers(int plotClaimID, Connection connection) throws SQLException {
        try (PreparedStatement getPlotMembersStatement = connection.prepareStatement("SELECT * FROM " + HuskTowns.getSettings().getPlotMembersTable() + " WHERE `claim_id`=?;")) {
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
        try (PreparedStatement checkClaimed = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE `chunk_x`=? AND `chunk_z`=? AND `world`=? AND `server`=?;")) {
            checkClaimed.setInt(1, chunkX);
            checkClaimed.setInt(2, chunkZ);
            checkClaimed.setString(3, worldName);
            checkClaimed.setString(4, server);
            ResultSet checkClaimedResult = checkClaimed.executeQuery();

            if (checkClaimedResult != null) {
                if (checkClaimedResult.next()) {
                    final ClaimedChunk.ChunkType chunkType = getChunkType(checkClaimedResult.getInt("chunk_type"));
                    final Timestamp timestamp = checkClaimedResult.getTimestamp("claim_time");
                    final Town town = getTownFromID(checkClaimedResult.getInt("town_id"), connection);
                    assert town != null;
                    final String townName = town.getName();
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
        }
        return null;
    }

    public static HashMap<UUID, TownRole> getTownMembers(String townName, Connection connection) throws SQLException {
        final HashMap<UUID, TownRole> members = new HashMap<>();
        try (PreparedStatement getMembers = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + " WHERE `town_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?);")) {
            getMembers.setString(1, townName);
            ResultSet memberResult = getMembers.executeQuery();

            if (memberResult != null) {
                while (memberResult.next()) {
                    members.put(UUID.fromString(memberResult.getString("uuid")),
                            getTownRoleFromID(memberResult.getInt("town_role")));
                }
            }
        }
        return members;
    }

    // Returns a list of a town's chunks on ALL servers
    public static HashSet<ClaimedChunk> getClaimedChunks(String townName, Connection connection) throws SQLException {
        HashSet<ClaimedChunk> chunks = new HashSet<>();
        try (PreparedStatement getChunks = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE `town_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) ORDER BY `claim_time` ASC;")) {
            getChunks.setString(1, townName);
            ResultSet chunkResults = getChunks.executeQuery();
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
        }
        return chunks;
    }

    private static ArrayList<Town> getTownsToCache() throws SQLException {
        final ArrayList<Town> townArrayList = new ArrayList<>();
        try (Connection connection = HuskTowns.getConnection()) {
            try (PreparedStatement towns = connection.prepareStatement("SELECT * FROM " + HuskTowns.getSettings().getTownsTable() + ";")) {
                ResultSet townResults = towns.executeQuery();
                if (townResults != null) {
                    HuskTowns.getTownDataCache().setItemsToLoad(getRowCount(HuskTowns.getSettings().getTownsTable() + ";"));
                    while (townResults.next()) {
                        try {
                            final String name = townResults.getString("name");
                            final double money = townResults.getDouble("money");
                            final Timestamp timestamp = townResults.getTimestamp("founded");
                            final String greetingMessage = townResults.getString("greeting_message");
                            final String farewellMessage = townResults.getString("farewell_message");
                            final String bio = townResults.getString("bio");
                            final TeleportationPoint spawnTeleportationPoint = getTeleportationPoint(townResults.getInt("spawn_location_id"), connection);
                            final boolean townSpawnPrivacy = townResults.getBoolean("is_spawn_public");
                            final HashSet<ClaimedChunk> claimedChunks = getClaimedChunks(name, connection);
                            final HashMap<UUID, TownRole> members = getTownMembers(name, connection);
                            final HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> flags = getTownFlags(name, connection);

                            // Log town cache loading process
                            HuskTowns.getTownDataCache().incrementItemsLoaded();
                            HuskTowns.getTownDataCache().setCurrentItemToLoadData("Town Data for " + name);
                            HuskTowns.getTownDataCache().log();

                            townArrayList.add(new Town(name, claimedChunks, members, spawnTeleportationPoint, townSpawnPrivacy, money, greetingMessage, farewellMessage, bio, timestamp.toInstant().getEpochSecond(), flags));
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.WARNING, "An exception occurred loading cached data for a town", e);
                        }
                    }
                }
            }
        }
        return townArrayList;
    }

    // Update the cache storing town messages and bio
    public static void updateTownDataCache() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Skip updating if it's already being processed
                HuskTowns.getTownDataCache().setStatus(CacheStatus.UPDATING);

                // Pull town data from the town table
                ArrayList<Town> townList = getTownsToCache();
                HuskTowns.getTownDataCache().clearItemsLoaded();
                for (Town town : townList) {
                    final String townName = town.getName();
                    HuskTowns.getTownDataCache().setGreetingMessage(townName, town.getGreetingMessage());
                    HuskTowns.getTownDataCache().setFarewellMessage(townName, town.getFarewellMessage());
                    HuskTowns.getTownDataCache().setTownBio(townName, town.getBio());
                    HuskTowns.getTownDataCache().setFlags(townName, town.getFlags());
                    if (town.isSpawnPublic()) {
                        if (town.getTownSpawn() != null) {
                            HuskTowns.getTownDataCache().addTownWithPublicSpawn(townName);
                        }
                    }
                }

                // Set the cache as having loaded
                HuskTowns.getTownDataCache().setStatus(CacheStatus.LOADED);
                HuskTowns.initializeLuckPermsIntegration(); // Initialize LuckPerms integration if all caches are loaded
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
                HuskTowns.getTownDataCache().setStatus(CacheStatus.ERROR);
            }
        });
    }

    // Returns claimed chunks on this server
    private static HashSet<ClaimedChunk> getClaimedChunksToCache(Connection connection) throws SQLException {
        try (PreparedStatement getChunks = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE `server`=?")) {
            getChunks.setString(1, HuskTowns.getSettings().getServerID());
            ResultSet resultSet = getChunks.executeQuery();

            final HashSet<ClaimedChunk> chunks = new HashSet<>();
            if (resultSet != null) {
                HuskTowns.getClaimCache().setItemsToLoad(getRowCount(HuskTowns.getSettings().getClaimsTable() + " WHERE `server`='" + HuskTowns.getSettings().getServerID() + "'"));
                while (resultSet.next()) {
                    final ClaimedChunk.ChunkType chunkType = getChunkType(resultSet.getInt("chunk_type"));
                    final String server = resultSet.getString("server");
                    final String world = resultSet.getString("world");
                    final int chunkX = resultSet.getInt("chunk_x");
                    final int chunkZ = resultSet.getInt("chunk_z");
                    final Timestamp timestamp = resultSet.getTimestamp("claim_time");
                    final UUID claimerUUID = getPlayerUUID(resultSet.getInt("claimer_id"), connection);
                    final Town town = getTownFromID(resultSet.getInt("town_id"), connection);
                    assert town != null;
                    final String townName = town.getName();

                    // Log claim cache loading process
                    HuskTowns.getClaimCache().incrementItemsLoaded();
                    HuskTowns.getClaimCache().setCurrentItemToLoadData("Chunk at " + world + "; " + chunkX + ", " + chunkZ);
                    HuskTowns.getClaimCache().log();

                    if (chunkType == ClaimedChunk.ChunkType.PLOT) {
                        final UUID plotOwnerUUID = getPlayerUUID(resultSet.getInt("plot_owner_id"), connection);
                        HashSet<UUID> plotMembers = new HashSet<>();
                        if (plotOwnerUUID != null) {
                            plotMembers = getPlotMembers(resultSet.getInt("id"), connection);
                        }
                        chunks.add(new ClaimedChunk(server, world, chunkX, chunkZ, claimerUUID, chunkType, plotOwnerUUID, plotMembers, townName, timestamp.toInstant().getEpochSecond()));
                    } else {
                        chunks.add(new ClaimedChunk(server, world, chunkX, chunkZ, claimerUUID, chunkType, townName, timestamp.toInstant().getEpochSecond()));
                    }
                }
            }
            return chunks;
        }
    }

    // Returns ALL claimed chunks on the server
    public static void updateClaimedChunkCache() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                HuskTowns.getClaimCache().setStatus(CacheStatus.UPDATING);
                plugin.getLogger().info("Loading claim data into cache...");

                final HashSet<ClaimedChunk> chunks = getClaimedChunksToCache(connection);
                HuskTowns.getClaimCache().clearItemsLoaded();
                for (ClaimedChunk chunk : chunks) {
                    HuskTowns.getClaimCache().add(chunk);
                }
                HuskTowns.getClaimCache().setStatus(CacheStatus.LOADED);
                plugin.getLogger().info("Claim data caching complete (took " + HuskTowns.getClaimCache().getTimeSinceInitialization() + " secs)");
                HuskTowns.initializeLuckPermsIntegration(); // Initialize LuckPerms integration if all caches are loaded
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
                HuskTowns.getClaimCache().setStatus(CacheStatus.ERROR);
            }
        });
    }

    // Remove the claim data and cache information
    private static void deleteClaimData(ClaimedChunk claimedChunk, Connection connection) throws SQLException {
        try (PreparedStatement claimRemovalStatement = connection.prepareStatement(
                "DELETE FROM " + HuskTowns.getSettings().getClaimsTable() + " WHERE `town_id`=" +
                        "(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) " +
                        "AND `server`=? AND `world`=? AND `chunk_x`=? AND `chunk_z`=?;")) {
            claimRemovalStatement.setString(1, claimedChunk.getTown());
            claimRemovalStatement.setString(2, claimedChunk.getServer());
            claimRemovalStatement.setString(3, claimedChunk.getWorld());
            claimRemovalStatement.setInt(4, claimedChunk.getChunkX());
            claimRemovalStatement.setInt(5, claimedChunk.getChunkZ());

            claimRemovalStatement.executeUpdate();
            HuskTowns.getClaimCache().remove(claimedChunk.getChunkX(), claimedChunk.getChunkZ(), claimedChunk.getWorld());
        }
    }

    public static void changeToFarm(Player player, ClaimedChunk claimedChunk) {
        ClaimCache cache = HuskTowns.getClaimCache();
        if (!cache.hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", cache.getName());
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                if (claimedChunk == null) {
                    MessageManager.sendMessage(player, "error_not_standing_on_claim");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;
                if (!town.getName().equals(claimedChunk.getTown())) {
                    MessageManager.sendMessage(player, "error_claim_not_member_of_town", claimedChunk.getTown());
                    return;
                }
                TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == TownRole.RESIDENT) {
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
                Bukkit.getScheduler().runTask(plugin, () -> ClaimViewerUtil.showParticles(player, 5, claimedChunk));
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(assignee.getUniqueId(), connection)) {
                    MessageManager.sendMessage(assignee, "error_not_in_town");
                    return;
                }
                if (claimedChunk == null) {
                    MessageManager.sendMessage(assignee, "error_not_standing_on_claim");
                    return;
                }
                Town assigneeTown = getPlayerTown(assignee.getUniqueId(), connection);
                assert assigneeTown != null;
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
                if (playerToBeAssignedTown == null) {
                    MessageManager.sendMessage(assignee, "error_claim_other_not_member_of_town",
                            playerNameToAssign, claimedChunk.getTown());
                    return;
                }
                if (!playerToBeAssignedTown.getName().equals(claimedChunk.getTown())) {
                    MessageManager.sendMessage(assignee, "error_claim_other_not_member_of_town",
                            playerNameToAssign, claimedChunk.getTown());
                    return;
                }
                if (getTownRole(assignee.getUniqueId(), connection) == TownRole.RESIDENT) {
                    MessageManager.sendMessage(assignee, "error_insufficient_assign_privileges");
                    return;
                }
                if (claimedChunk.getChunkType() != ClaimedChunk.ChunkType.PLOT) {
                    MessageManager.sendMessage(assignee, "error_not_a_plot");
                    return;
                }
                if (claimedChunk.getPlotChunkOwner() != null) {
                    MessageManager.sendMessage(assignee, "error_assign_plot_already_claimed", HuskTowns.getPlayerCache().getPlayerUsername(claimedChunk.getPlotChunkOwner()));
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
                Bukkit.getScheduler().runTask(plugin, () -> ClaimViewerUtil.showParticles(assignee, 5, claimedChunk));
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void sendTownList(Player player, TownListCommand.TownListOrderType orderBy, int pageNumber, boolean useCache) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                ArrayList<Town> townList = new ArrayList<>();
                if (useCache) {
                    townList.addAll(TownListCommand.getTownList(player.getUniqueId()));
                } else {
                    townList.addAll(switch (orderBy) {
                        case BY_NAME -> getTowns(connection, "name", true);
                        case BY_LEVEL, BY_WEALTH -> getTowns(connection, "money", false);
                        case BY_NEWEST -> getTowns(connection, "founded", false);
                        case BY_OLDEST -> getTowns(connection, "founded", true);
                    });
                    TownListCommand.addTownList(player.getUniqueId(), townList);
                }
                ArrayList<String> pages = new ArrayList<>();
                int adminTownAdjustmentSize = 0;
                for (Town town : townList) {
                    if (town.getName().equals(HuskTowns.getSettings().getAdminTownName())) {
                        adminTownAdjustmentSize = 1;
                        continue;
                    }
                    String mayorName = "";
                    for (UUID uuid : town.getMembers().keySet()) {
                        if (town.getMembers().get(uuid) == TownRole.MAYOR) {
                            mayorName = HuskTowns.getPlayerCache().getPlayerUsername(uuid);
                            break;
                        }
                    }
                    pages.add(MessageManager.getRawMessage("town_list_item", town.getName(), town.getTownColorHex(), mayorName, town.getBio().replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)"), Integer.toString(town.getMembers().size()), Integer.toString(town.getMaxMembers()), Integer.toString(town.getClaimedChunksNumber()), Integer.toString(town.getMaximumClaimedChunks()), Integer.toString(town.getLevel()), town.getFormattedFoundedTime()));
                }
                MessageManager.sendMessage(player, "town_list_header", orderBy.toString().toLowerCase().replace("_", " "), Integer.toString(townList.size() - adminTownAdjustmentSize));
                player.spigot().sendMessage(new PageChatList(pages, 10, "/townlist " + orderBy.toString().toLowerCase() + " -c").getPage(pageNumber));
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void claimPlot(Player player, ClaimedChunk claimedChunk) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                if (claimedChunk == null) {
                    MessageManager.sendMessage(player, "error_not_standing_on_claim");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;
                if (!town.getName().equals(claimedChunk.getTown())) {
                    MessageManager.sendMessage(player, "error_claim_not_member_of_town", claimedChunk.getTown());
                    return;
                }
                if (claimedChunk.getChunkType() != ClaimedChunk.ChunkType.PLOT) {
                    MessageManager.sendMessage(player, "error_not_a_plot");
                    return;
                }
                if (claimedChunk.getPlotChunkOwner() != null) {
                    MessageManager.sendMessage(player, "error_plot_already_claimed", HuskTowns.getPlayerCache().getPlayerUsername(claimedChunk.getPlotChunkOwner()));
                    return;
                }
                setPlotOwner(claimedChunk, player.getUniqueId(), connection);
                MessageManager.sendMessage(player, "claimed_plot_success", Integer.toString(claimedChunk.getChunkX()),
                        Integer.toString(claimedChunk.getChunkZ()), claimedChunk.getWorld());
                Bukkit.getScheduler().runTask(plugin, () -> ClaimViewerUtil.showParticles(player, 5, claimedChunk));
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static void unClaimPlot(Player player, ClaimedChunk claimedChunk) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                if (claimedChunk == null) {
                    MessageManager.sendMessage(player, "error_not_standing_on_claim");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;
                if (!town.getName().equals(claimedChunk.getTown())) {
                    MessageManager.sendMessage(player, "error_claim_not_member_of_town", claimedChunk.getTown());
                    return;
                }
                if (claimedChunk.getChunkType() != ClaimedChunk.ChunkType.PLOT) {
                    MessageManager.sendMessage(player, "error_not_a_plot");
                    return;
                }
                if (claimedChunk.getPlotChunkOwner() != player.getUniqueId()) {
                    TownRole unClaimerRole = getTownRole(player.getUniqueId(), connection);
                    if (unClaimerRole == TownRole.RESIDENT) {
                        MessageManager.sendMessage(player, "error_not_your_plot");
                        return;
                    }
                }
                clearPlotOwner(claimedChunk, connection);
                clearPlotMembers(claimedChunk, connection);
                MessageManager.sendMessage(player, "unclaimed_plot_success", claimedChunk.getTown());
                Bukkit.getScheduler().runTask(plugin, () -> ClaimViewerUtil.showParticles(player, 5, claimedChunk));
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                if (!inTown(player.getUniqueId(), connection)) {
                    MessageManager.sendMessage(player, "error_not_in_town");
                    return;
                }
                if (claimedChunk == null) {
                    MessageManager.sendMessage(player, "error_not_standing_on_claim");
                    return;
                }
                Town town = getPlayerTown(player.getUniqueId(), connection);
                assert town != null;
                if (!town.getName().equals(claimedChunk.getTown())) {
                    MessageManager.sendMessage(player, "error_claim_not_member_of_town", claimedChunk.getTown());
                    return;
                }
                TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == TownRole.RESIDENT) {
                    MessageManager.sendMessage(player, "error_insufficient_claim_privileges");
                    return;
                }
                if (claimedChunk.getChunkType() == ClaimedChunk.ChunkType.FARM) {
                    MessageManager.sendMessage(player, "error_already_farm_chunk");
                    return;
                }
                // Ensure plot data is cleared whenever the chunk is updated & switch the type
                clearPlotOwner(claimedChunk, connection);
                clearPlotMembers(claimedChunk, connection);
                if (claimedChunk.getChunkType() == ClaimedChunk.ChunkType.PLOT) {
                    setChunkType(claimedChunk, ClaimedChunk.ChunkType.REGULAR, connection);
                    MessageManager.sendMessage(player, "make_regular_success", Integer.toString(claimedChunk.getChunkX()), Integer.toString(claimedChunk.getChunkZ()));
                } else {
                    setChunkType(claimedChunk, ClaimedChunk.ChunkType.PLOT, connection);
                    MessageManager.sendMessage(player, "make_plot_success", Integer.toString(claimedChunk.getChunkX()), Integer.toString(claimedChunk.getChunkZ()));
                }
                Bukkit.getScheduler().runTask(plugin, () -> ClaimViewerUtil.showParticles(player, 5, claimedChunk));
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
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
                assert town != null;
                if (!town.getName().equals(claimedChunk.getTown())) {
                    if (player.hasPermission("husktowns.administrator.unclaim_any")) {
                        deleteClaimData(claimedChunk, connection);
                        MessageManager.sendMessage(player, "remove_claim_success_override", town.getName(), Integer.toString(claimedChunk.getChunkX()), Integer.toString(claimedChunk.getChunkZ()));
                        return;
                    }
                    MessageManager.sendMessage(player, "error_claim_not_member_of_town", claimedChunk.getTown());
                    return;
                }
                TownRole role = getTownRole(player.getUniqueId(), connection);
                if (role == TownRole.RESIDENT) {
                    if (player.hasPermission("husktowns.administrator.unclaim_any")) {
                        deleteClaimData(claimedChunk, connection);
                        MessageManager.sendMessage(player, "remove_claim_success_override", town.getName(), Integer.toString(claimedChunk.getChunkX()), Integer.toString(claimedChunk.getChunkZ()));
                        return;
                    }
                    MessageManager.sendMessage(player, "error_insufficient_claim_privileges");
                    return;
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (EventCannon.fireEvent(new UnClaimEvent(player, claimedChunk))) {
                        return;
                    }
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try (Connection connection1 = HuskTowns.getConnection()) {
                            deleteClaimData(claimedChunk, connection1);
                            MessageManager.sendMessage(player, "remove_claim_success", Integer.toString(claimedChunk.getChunkX()), Integer.toString(claimedChunk.getChunkZ()));

                            // Check if the town spawn was set within the claimed chunk and if so remove.
                            final Chunk chunk = player.getWorld().getChunkAt(claimedChunk.getChunkX(), claimedChunk.getChunkZ());
                            final TeleportationPoint townSpawn = town.getTownSpawn();
                            if (townSpawn != null) {
                                if (townSpawn.getLocation().getChunk() == chunk) {
                                    deleteTownSpawnData(player, connection1);
                                    updateTownSpawnPrivacyData(town.getName(), false, connection1);
                                    MessageManager.sendMessage(player, "spawn_removed_in_claim");
                                }
                            }
                        } catch (SQLException exception) {
                            plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
                        }
                    });
                });
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    private static void updateCachedBonuses(Connection connection) throws SQLException {
        try (PreparedStatement bonusesStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getBonusesTable() + ";")) {
            ResultSet resultSet = bonusesStatement.executeQuery();
            if (resultSet != null) {
                HuskTowns.getTownBonusesCache().setItemsToLoad(getRowCount(HuskTowns.getSettings().getBonusesTable() + ";"));
                while (resultSet.next()) {
                    final Town town = getTownFromID(resultSet.getInt("town_id"), connection);
                    if (town != null) {
                        HuskTowns.getTownBonusesCache().setCurrentItemToLoadData("Town bonus for " + town.getName());
                        HuskTowns.getTownBonusesCache().add(town.getName(),
                                new TownBonus(getPlayerUUID(resultSet.getInt("applier_id"), connection),
                                        resultSet.getInt("bonus_claims"),
                                        resultSet.getInt("bonus_members"),
                                        resultSet.getTimestamp("applied_time").toInstant().getEpochSecond()));
                        HuskTowns.getTownBonusesCache().log();
                    }
                }
            }
        }
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
        if (((bonus.getBonusClaims() == 0) && (bonus.getBonusMembers() == 0)) || bonus.getBonusMembers() < 0 || bonus.getBonusClaims() < 0) {
            MessageManager.sendMessage(sender, "error_invalid_town_bonus");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                String townName = targetName;
                if (!townExists(targetName, connection)) {
                    if (playerNameExists(targetName, connection)) {
                        UUID playerUUID = getPlayerUUID(townName, connection);
                        if (playerUUID == null) {
                            MessageManager.sendMessage(sender, "error_town_bonus_invalid_target");
                            return;
                        }
                        if (inTown(playerUUID, connection)) {
                            Town town = getPlayerTown(playerUUID, connection);
                            assert town != null;
                            townName = town.getName();
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                HuskTowns.getTownBonusesCache().setStatus(CacheStatus.UPDATING);
                updateCachedBonuses(connection);
                HuskTowns.getTownBonusesCache().setStatus(CacheStatus.LOADED);
                HuskTowns.initializeLuckPermsIntegration(); // Initialize LuckPerms integration if all caches are loaded
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
                HuskTowns.getTownBonusesCache().setStatus(CacheStatus.ERROR);
            }
        });
    }

    private static void deleteTownBonuses(String townName, Connection connection) throws SQLException {
        try (PreparedStatement deleteBonusesStatement = connection.prepareStatement(
                "DELETE FROM " + HuskTowns.getSettings().getBonusesTable() + " WHERE `town_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?);")) {
            deleteBonusesStatement.setString(1, townName);
            deleteBonusesStatement.executeUpdate();
        }
    }

    public static void clearTownBonuses(CommandSender sender, String targetName) {
        if (!HuskTowns.getPlayerCache().hasLoaded()) {
            MessageManager.sendMessage(sender, "error_cache_updating", "Player Data");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskTowns.getConnection()) {
                String townName = targetName;
                if (!townExists(targetName, connection)) {
                    if (playerNameExists(targetName, connection)) {
                        UUID playerUUID = getPlayerUUID(townName, connection);
                        if (playerUUID == null) {
                            MessageManager.sendMessage(sender, "error_town_bonus_invalid_target");
                            return;
                        }
                        if (inTown(playerUUID, connection)) {
                            Town town = getPlayerTown(playerUUID, connection);
                            assert town != null;
                            townName = town.getName();
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
                        CrossServerMessageHandler.getMessage(Message.MessageType.CLEAR_TOWN_BONUSES, townName).sendToAll(updateNotificationDispatcher);
                        break;
                    }
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", exception);
            }
        });
    }

    public static ArrayList<TownBonus> getTownBonuses(String townName, Connection connection) throws SQLException {
        try (PreparedStatement bonusesStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getBonusesTable() + " WHERE `town_id`=(SELECT `id` FROM " + HuskTowns.getSettings().getTownsTable() + " WHERE `name`=?) ORDER BY `applied_time` DESC;")) {
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
        }
        return null;
    }

    private static HashSet<UUID> getPlayersToCache(Connection connection) throws SQLException {
        final HashSet<UUID> players = new HashSet<>();
        try (PreparedStatement existStatement = connection.prepareStatement(
                "SELECT * FROM " + HuskTowns.getSettings().getPlayerTable() + ";")) {
            ResultSet resultSet = existStatement.executeQuery();
            if (resultSet != null) {
                HuskTowns.getPlayerCache().setItemsToLoad(getRowCount(HuskTowns.getSettings().getPlayerTable() + ";"));
                while (resultSet.next()) {
                    final String uuid = resultSet.getString("uuid");
                    HuskTowns.getPlayerCache().incrementItemsLoaded();
                    HuskTowns.getPlayerCache().setCurrentItemToLoadData("Player data for " + uuid);
                    HuskTowns.getPlayerCache().log();

                    players.add(UUID.fromString(uuid));
                }
            }
        }
        return players;
    }

    public static void updatePlayerCachedData() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            HuskTowns.getPlayerCache().setStatus(CacheStatus.UPDATING);
            try (Connection connection = HuskTowns.getConnection()) {
                final HashMap<UUID, String> namesToPut = new HashMap<>();
                final HashMap<UUID, String> townsToPut = new HashMap<>();
                final HashMap<UUID, TownRole> rolesToPut = new HashMap<>();
                final HashSet<UUID> playersToCache = getPlayersToCache(connection);
                HuskTowns.getPlayerCache().clearItemsLoaded();
                for (UUID uuid : playersToCache) {
                    namesToPut.put(uuid, getPlayerName(uuid, connection));
                    if (inTown(uuid, connection)) {
                        Town town = getPlayerTown(uuid, connection);
                        assert town != null;
                        townsToPut.put(uuid, town.getName());
                        rolesToPut.put(uuid, getTownRole(uuid, connection));
                    }
                }
                for (UUID uuid : namesToPut.keySet()) {
                    HuskTowns.getPlayerCache().setPlayerName(uuid, namesToPut.get(uuid));
                }
                for (UUID uuid : townsToPut.keySet()) {
                    HuskTowns.getPlayerCache().setPlayerTown(uuid, townsToPut.get(uuid));
                }
                for (UUID uuid : rolesToPut.keySet()) {
                    HuskTowns.getPlayerCache().setPlayerRole(uuid, rolesToPut.get(uuid));
                }
                HuskTowns.getPlayerCache().setStatus(CacheStatus.LOADED);
                HuskTowns.initializeLuckPermsIntegration(); // Initialize LuckPerms integration if all caches are loaded
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred: ", e);
                HuskTowns.getPlayerCache().setStatus(CacheStatus.ERROR);
            }
        });
    }

    private static int getRowCount(String sqlFrom) {
        try (Connection connection = HuskTowns.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) AS row_count FROM " + sqlFrom)) {
                ResultSet set = statement.executeQuery();
                if (set.next()) {
                    return set.getInt("row_count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}