/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
 */

package net.william278.husktowns.database;

import com.google.gson.JsonSyntaxException;
import com.zaxxer.hikari.HikariDataSource;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.ServerWorld;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.Preferences;
import net.william278.husktowns.user.SavedUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.logging.Level;

public final class MySqlDatabase extends Database {

    /**
     * Name of the Hikari connection pool
     */
    private static final String DATA_POOL_NAME = "HuskTownsHikariPool";

    /**
     * The Hikari data source
     */
    private HikariDataSource dataSource;

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void setConnection() {
        final Settings settings = plugin.getSettings();

        // Create jdbc driver connection url
        final String jdbcUrl = "jdbc:mysql://" + settings.getMySqlHost() + ":" + settings.getMySqlPort() + "/"
                + settings.getMySqlDatabase() + settings.getMySqlConnectionParameters();
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);

        // Authenticate
        dataSource.setUsername(settings.getMySqlUsername());
        dataSource.setPassword(settings.getMySqlPassword());

        // Set connection pool options
        dataSource.setMaximumPoolSize(settings.getMySqlConnectionPoolSize());
        dataSource.setMinimumIdle(settings.getMySqlConnectionPoolIdle());
        dataSource.setMaxLifetime(settings.getMySqlConnectionPoolLifetime());
        dataSource.setKeepaliveTime(settings.getMySqlConnectionPoolKeepAlive());
        dataSource.setConnectionTimeout(settings.getMySqlConnectionPoolTimeout());
        dataSource.setPoolName(DATA_POOL_NAME);

        // Set additional connection pool properties
        dataSource.setDataSourceProperties(new Properties() {{
            put("cachePrepStmts", "true");
            put("prepStmtCacheSize", "250");
            put("prepStmtCacheSqlLimit", "2048");
            put("useServerPrepStmts", "true");
            put("useLocalSessionState", "true");
            put("useLocalTransactionState", "true");
            put("rewriteBatchedStatements", "true");
            put("cacheResultSetMetadata", "true");
            put("cacheServerConfiguration", "true");
            put("elideSetAutoCommits", "true");
            put("maintainTimeStats", "false");
        }});
    }

    public MySqlDatabase(@NotNull HuskTowns plugin) {
        super(plugin, "mysql_schema.sql");
    }

    @Override
    public void initialize() throws RuntimeException {
        // Establish connection
        this.setConnection();

        // Create tables
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                for (String tableCreationStatement : getSchema()) {
                    statement.execute(tableCreationStatement);
                }
            }
            setLoaded(true);
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to create MySQL database tables");
            setLoaded(false);
        }
    }

    @Override
    public Optional<SavedUser> getUser(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `uuid`, `username`, `last_login`, `preferences`
                    FROM `%user_data%`
                    WHERE uuid = ?"""))) {
                statement.setString(1, uuid.toString());
                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    final String name = resultSet.getString("username");
                    final String preferences = new String(resultSet.getBytes("preferences"), StandardCharsets.UTF_8);
                    return Optional.of(new SavedUser(
                            User.of(uuid, name),
                            resultSet.getTimestamp("last_login").toLocalDateTime()
                                    .atOffset(OffsetDateTime.now().getOffset()),
                            plugin.getGson().fromJson(preferences, Preferences.class)
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to fetch user data from table by UUID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<SavedUser> getUser(@NotNull String username) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `uuid`, `username`, `last_login`, `preferences`
                    FROM `%user_data%`
                    WHERE `username` = ?"""))) {
                statement.setString(1, username);
                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    final UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                    final String name = resultSet.getString("username");
                    final String preferences = new String(resultSet.getBytes("preferences"), StandardCharsets.UTF_8);
                    return Optional.of(new SavedUser(
                            User.of(uuid, name),
                            resultSet.getTimestamp("last_login").toLocalDateTime()
                                    .atOffset(OffsetDateTime.now().getOffset()),
                            plugin.getGson().fromJson(preferences, Preferences.class)
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to fetch user data from table by username", e);
        }
        return Optional.empty();
    }

    @Override
    public List<SavedUser> getInactiveUsers(int daysInactive) {
        final List<SavedUser> inactiveUsers = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `uuid`, `username`, `last_login`, `preferences`
                    FROM `%user_data%`
                    WHERE `last_login` < DATE_SUB(NOW(), INTERVAL ? DAY);"""))) {
                statement.setInt(1, daysInactive);
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                    final String name = resultSet.getString("username");
                    final String preferences = new String(resultSet.getBytes("preferences"), StandardCharsets.UTF_8);
                    inactiveUsers.add(new SavedUser(
                            User.of(uuid, name),
                            resultSet.getTimestamp("last_login").toLocalDateTime()
                                    .atOffset(OffsetDateTime.now().getOffset()),
                            plugin.getGson().fromJson(preferences, Preferences.class)
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to fetch list of inactive users", e);
        }
        return inactiveUsers;
    }

    @Override
    public void createUser(@NotNull User user, @NotNull Preferences preferences) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    INSERT INTO `%user_data%` (`uuid`, `username`, `last_login`, `preferences`)
                    VALUES (?, ?, ?)"""))) {
                statement.setString(1, user.getUuid().toString());
                statement.setString(2, user.getUsername());
                statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                statement.setBytes(4, plugin.getGson().toJson(preferences).getBytes(StandardCharsets.UTF_8));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to create user in table", e);
        }
    }

    @Override
    public void updateUser(@NotNull User user, @NotNull Preferences preferences) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    UPDATE `%user_data%`
                    SET `username` = ?, `last_login` = ?, `preferences` = ?
                    WHERE `uuid` = ?"""))) {
                statement.setString(1, user.getUsername());
                statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                statement.setBytes(2, plugin.getGson().toJson(preferences).getBytes(StandardCharsets.UTF_8));
                statement.setString(3, user.getUuid().toString());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to update user in table", e);
        }
    }

    @Override
    public void deleteAllUsers() {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    DELETE FROM `%user_data%`"""))) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to delete all users from table", e);
        }
    }

    @Override
    public Optional<Town> getTown(int townId) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `id`, `data`
                    FROM `%town_data%`
                    WHERE `id` = ?"""))) {
                statement.setInt(1, townId);
                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    final String data = new String(resultSet.getBytes("data"), StandardCharsets.UTF_8);
                    final Town town = plugin.getGson().fromJson(data, Town.class);
                    town.setId(resultSet.getInt("id"));
                    return Optional.of(town);
                }
            }
        } catch (SQLException | JsonSyntaxException e) {
            plugin.log(Level.SEVERE, "Failed to fetch town data from table by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Town> getAllTowns() {
        final List<Town> towns = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `id`, `data`
                    FROM `%town_data%`"""))) {
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final String data = new String(resultSet.getBytes("data"), StandardCharsets.UTF_8);
                    final Town town = plugin.getGson().fromJson(data, Town.class);
                    if (town != null) {
                        town.setId(resultSet.getInt("id"));
                        towns.add(town);
                    }
                }
            }
        } catch (SQLException | JsonSyntaxException e) {
            plugin.log(Level.SEVERE, "Failed to fetch list of towns from table", e);
        }
        return towns;
    }

    @Override
    @NotNull
    public Town createTown(@NotNull String name, @NotNull User creator) {
        final Town town = Town.create(name, creator, plugin);
        town.addMember(creator.getUuid(), plugin.getRoles().getMayorRole());
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    INSERT INTO `%town_data%` (`name`, `data`)
                    VALUES (?, ?)"""), Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, town.getName());
                statement.setBytes(2, plugin.getGson().toJson(town).getBytes(StandardCharsets.UTF_8));
                statement.executeUpdate();

                final ResultSet insertedRow = statement.getGeneratedKeys();
                if (insertedRow.next()) {
                    town.setId(insertedRow.getInt(1));
                }
            }
        } catch (SQLException | JsonSyntaxException e) {
            plugin.log(Level.SEVERE, "Failed to create town in table", e);
        }
        return town;
    }

    @Override
    public void updateTown(@NotNull Town town) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    UPDATE `%town_data%`
                    SET `name` = ?, `data` = ?
                    WHERE `id` = ?"""))) {
                statement.setString(1, town.getName());
                statement.setBytes(2, plugin.getGson().toJson(town).getBytes(StandardCharsets.UTF_8));
                statement.setInt(3, town.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to update town in table", e);
        }
    }

    @Override
    public void deleteTown(int townId) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    DELETE FROM `%town_data%`
                    WHERE `id` = ?"""))) {
                statement.setInt(1, townId);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to delete town from table", e);
        }
    }

    @Override
    public void deleteAllTowns() {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    DELETE FROM `%town_data%`"""))) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to delete all towns from table", e);
        }
    }

    @Override
    public Map<World, ClaimWorld> getClaimWorlds(@NotNull String server) {
        final Map<World, ClaimWorld> worlds = new HashMap<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `id`, `world_uuid`, `world_name`, `world_environment`, `claims`
                    FROM `%claim_data%`
                    WHERE `server_name` = ?"""))) {
                statement.setString(1, server);
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final String data = new String(resultSet.getBytes("claims"), StandardCharsets.UTF_8);
                    final World world = World.of(UUID.fromString(resultSet.getString("world_uuid")),
                            resultSet.getString("world_name"),
                            resultSet.getString("world_environment"));
                    final ClaimWorld claimWorld = plugin.getGson().fromJson(data, ClaimWorld.class);
                    claimWorld.updateId(resultSet.getInt("id"));
                    if (!plugin.getSettings().isUnclaimableWorld(world)) {
                        worlds.put(world, claimWorld);
                    }
                }
            }
        } catch (SQLException | JsonSyntaxException e) {
            plugin.log(Level.SEVERE, "Failed to fetch map of server claim worlds from table", e);
        }
        return worlds;
    }

    @Override
    public Map<ServerWorld, ClaimWorld> getAllClaimWorlds() {
        final Map<ServerWorld, ClaimWorld> worlds = new HashMap<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `id`, `server_name`, `world_uuid`, `world_name`, `world_environment`, `claims`
                    FROM `%claim_data%`"""))) {
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final String data = new String(resultSet.getBytes("claims"), StandardCharsets.UTF_8);
                    final World world = World.of(UUID.fromString(resultSet.getString("world_uuid")),
                            resultSet.getString("world_name"),
                            resultSet.getString("world_environment"));
                    final ClaimWorld claimWorld = plugin.getGson().fromJson(data, ClaimWorld.class);
                    claimWorld.updateId(resultSet.getInt("id"));
                    worlds.put(new ServerWorld(resultSet.getString("server_name"), world), claimWorld);
                }
            }
        } catch (SQLException | JsonSyntaxException e) {
            plugin.log(Level.SEVERE, "Failed to fetch map of all claim worlds from table", e);
        }
        return worlds;
    }

    @Override
    @NotNull
    public ClaimWorld createClaimWorld(@NotNull World world) {
        final ClaimWorld claimWorld = ClaimWorld.of(0, new HashMap<>(), new ArrayList<>());
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    INSERT INTO `%claim_data%` (`world_uuid`, `world_name`, `world_environment`, `server_name`, `claims`)
                    VALUES (?, ?, ?, ?, ?)"""), Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, world.getUuid().toString());
                statement.setString(2, world.getName());
                statement.setString(3, world.getEnvironment());
                statement.setString(4, plugin.getServerName());
                statement.setBytes(5, plugin.getGson().toJson(claimWorld).getBytes(StandardCharsets.UTF_8));
                statement.executeUpdate();

                final ResultSet insertedRow = statement.getGeneratedKeys();
                if (insertedRow.next()) {
                    claimWorld.updateId(insertedRow.getInt(1));
                }
            }
        } catch (SQLException | JsonSyntaxException e) {
            plugin.log(Level.SEVERE, "Failed to create claim world in table", e);
        }
        return claimWorld;
    }

    @Override
    public void updateClaimWorld(@NotNull ClaimWorld claimWorld) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    UPDATE `%claim_data%`
                    SET `claims` = ?
                    WHERE `id` = ?"""))) {
                statement.setBytes(1, plugin.getGson().toJson(claimWorld).getBytes(StandardCharsets.UTF_8));
                statement.setInt(2, claimWorld.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to update claim world in table", e);
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}
