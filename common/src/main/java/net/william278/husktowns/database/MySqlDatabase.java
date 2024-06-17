/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.database;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
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

    private static final String DATA_POOL_NAME = "HuskTownsHikariPool";
    private final String flavor;
    private final String driverClass;
    private HikariDataSource dataSource;

    public MySqlDatabase(@NotNull HuskTowns plugin) {
        super(plugin);
        this.flavor = plugin.getSettings().getDatabase().getType() == Type.MARIADB
            ? "mariadb" : "mysql";
        this.driverClass = plugin.getSettings().getDatabase().getType() == Type.MARIADB
            ? "org.mariadb.jdbc.Driver" : "com.mysql.cj.jdbc.Driver";
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void setConnection() {
        // Initialize the Hikari pooled connection
        final Settings.DatabaseSettings databaseSettings = plugin.getSettings().getDatabase();
        final Settings.DatabaseSettings.DatabaseCredentials credentials = databaseSettings.getCredentials();

        dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setJdbcUrl(String.format("jdbc:%s://%s:%s/%s%s",
            flavor,
            credentials.getHost(),
            credentials.getPort(),
            credentials.getDatabase(),
            credentials.getParameters()
        ));

        // Authenticate with the database
        dataSource.setUsername(credentials.getUsername());
        dataSource.setPassword(credentials.getPassword());

        // Set connection pool options
        final Settings.DatabaseSettings.PoolOptions poolOptions = databaseSettings.getConnectionPool();
        dataSource.setMaximumPoolSize(poolOptions.getSize());
        dataSource.setMinimumIdle(poolOptions.getIdle());
        dataSource.setMaxLifetime(poolOptions.getLifetime());
        dataSource.setKeepaliveTime(poolOptions.getKeepalive());
        dataSource.setConnectionTimeout(poolOptions.getTimeout());
        dataSource.setPoolName(DATA_POOL_NAME);

        // Set additional connection pool properties
        final Properties properties = new Properties();
        properties.putAll(
            Map.of("cachePrepStmts", "true",
                "prepStmtCacheSize", "250",
                "prepStmtCacheSqlLimit", "2048",
                "useServerPrepStmts", "true",
                "useLocalSessionState", "true",
                "useLocalTransactionState", "true"
            ));
        properties.putAll(
            Map.of(
                "rewriteBatchedStatements", "true",
                "cacheResultSetMetadata", "true",
                "cacheServerConfiguration", "true",
                "elideSetAutoCommits", "true",
                "maintainTimeStats", "false")
        );
        dataSource.setDataSourceProperties(properties);
    }

    @Override
    protected void executeScript(@NotNull Connection connection, @NotNull String name) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String schemaStatement : getScript(name)) {
                statement.execute(schemaStatement);
            }
        }
    }

    @Override
    public void initialize() throws RuntimeException {
        // Establish connection
        this.setConnection();

        // Create tables
        final Database.Type type = plugin.getSettings().getDatabase().getType();
        if (!isCreated()) {
            plugin.log(Level.INFO, String.format("Creating %s database tables", type.getDisplayName()));
            try (Connection connection = getConnection()) {
                executeScript(connection, String.format("%s_schema.sql", flavor));
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, String.format("Failed to create %s database tables", type.getDisplayName()), e);
                setLoaded(false);
                return;
            }
            setSchemaVersion(Migration.getLatestVersion());
            plugin.log(Level.INFO, String.format("Created %s database tables", type.getDisplayName()));
            setLoaded(true);
            return;
        }

        // Perform migrations
        try {
            performMigrations(getConnection(), type);
            setLoaded(true);
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, String.format("Failed to perform %s database migrations", type.getDisplayName()), e);
            setLoaded(false);
        }
    }

    // Select a table to check if the database has been created
    @Override
    public boolean isCreated() {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                SELECT `uuid`
                FROM `%user_data%`
                LIMIT 1;"""))) {
                statement.executeQuery();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public int getSchemaVersion() {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                SELECT `schema_version`
                FROM `%meta_data%`
                LIMIT 1;"""))) {
                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt("schema_version");
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.WARNING, "The database schema version could not be fetched; migrations will be carried out.");
        }
        return -1;
    }

    @Override
    public void setSchemaVersion(int version) {
        if (getSchemaVersion() == -1) {
            try (Connection connection = getConnection()) {
                try (PreparedStatement insertStatement = connection.prepareStatement(format("""
                    INSERT INTO `%meta_data%` (`schema_version`)
                    VALUES (?)"""))) {
                    insertStatement.setInt(1, version);
                    insertStatement.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "Failed to insert schema version in table", e);
            }
            return;
        }

        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                UPDATE `%meta_data%`
                SET `schema_version` = ?;"""))) {
                statement.setInt(1, version);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to update schema version in table", e);
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
                        plugin.getPreferencesFromJson(preferences)
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
                        plugin.getPreferencesFromJson(preferences)
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to fetch user data from table by username", e);
        }
        return Optional.empty();
    }

    @Override
    public List<SavedUser> getInactiveUsers(long daysInactive) {
        final List<SavedUser> inactiveUsers = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                SELECT `uuid`, `username`, `last_login`, `preferences`
                FROM `%user_data%`
                WHERE `last_login` < DATE_SUB(NOW(), INTERVAL ? DAY);"""))) {
                statement.setLong(1, daysInactive);
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                    final String name = resultSet.getString("username");
                    final String preferences = new String(resultSet.getBytes("preferences"), StandardCharsets.UTF_8);
                    inactiveUsers.add(new SavedUser(
                        User.of(uuid, name),
                        resultSet.getTimestamp("last_login").toLocalDateTime()
                            .atOffset(OffsetDateTime.now().getOffset()),
                        plugin.getPreferencesFromJson(preferences)
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to fetch list of inactive users", e);
            inactiveUsers.clear(); // Clear for safety to prevent any accidental data being returned
        }
        return inactiveUsers;
    }

    @Override
    public void createUser(@NotNull User user, @NotNull Preferences preferences) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                INSERT INTO `%user_data%` (`uuid`, `username`, `last_login`, `preferences`)
                VALUES (?, ?, ?, ?)"""))) {
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
    public void updateUser(@NotNull User user, @NotNull OffsetDateTime lastLogin, @NotNull Preferences preferences) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                UPDATE `%user_data%`
                SET `username` = ?, `last_login` = ?, `preferences` = ?
                WHERE `uuid` = ?"""))) {
                statement.setString(1, user.getUsername());
                statement.setTimestamp(2, Timestamp.valueOf(lastLogin.toLocalDateTime()));
                statement.setBytes(3, plugin.getGson().toJson(preferences).getBytes(StandardCharsets.UTF_8));
                statement.setString(4, user.getUuid().toString());
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
                    final Town town = plugin.getTownFromJson(data);
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
    public List<Town> getAllTowns() throws IllegalStateException {
        final List<Town> towns = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                SELECT `id`, `data`
                FROM `%town_data%`"""))) {
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final Town town = plugin.getTownFromJson(
                        new String(resultSet.getBytes("data"), StandardCharsets.UTF_8)
                    );
                    town.setId(resultSet.getInt("id"));
                    towns.add(town);
                }
            }
        } catch (SQLException | JsonSyntaxException e) {
            throw new IllegalStateException("Failed to fetch all town data from table", e);
        }
        return towns;
    }

    @Override
    @NotNull
    public Town createTown(@NotNull String name, @NotNull User creator) {
        final Town town = Town.create(name, creator, plugin);
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
    public Map<World, ClaimWorld> getClaimWorlds(@NotNull String server) throws IllegalStateException {
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
                    final ClaimWorld claimWorld = plugin.getClaimWorldFromJson(data);
                    claimWorld.updateId(resultSet.getInt("id"));
                    if (!plugin.getSettings().getGeneral().isUnclaimableWorld(world)) {
                        worlds.put(world, claimWorld);
                    }
                }
            }
        } catch (SQLException | JsonSyntaxException e) {
            throw new IllegalStateException(String.format("Failed to fetch claim world map for %s", server), e);
        }
        return worlds;
    }

    @Override
    public Map<ServerWorld, ClaimWorld> getAllClaimWorlds() throws IllegalStateException {
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
                    final ClaimWorld claimWorld = plugin.getClaimWorldFromJson(data);
                    claimWorld.updateId(resultSet.getInt("id"));
                    worlds.put(new ServerWorld(resultSet.getString("server_name"), world), claimWorld);
                }
            }
        } catch (SQLException | JsonSyntaxException e) {
            throw new IllegalStateException("Failed to fetch map of all claim worlds", e);
        }
        return worlds;
    }

    @Override
    @NotNull
    public ClaimWorld createClaimWorld(@NotNull World world) {
        final ClaimWorld claimWorld = ClaimWorld.of(0, Maps.newConcurrentMap(), Queues.newConcurrentLinkedQueue());
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
