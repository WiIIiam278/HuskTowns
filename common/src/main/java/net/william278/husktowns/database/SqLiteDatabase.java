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
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.ServerWorld;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.Preferences;
import net.william278.husktowns.user.SavedUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.logging.Level;

public final class SqLiteDatabase extends Database {

    /**
     * The name of the database file
     */
    private static final String DATABASE_FILE_NAME = "HuskTownsData.db";

    /**
     * Path to the SQLite HuskTownsData.db file
     */
    private final File databaseFile;

    /**
     * The persistent SQLite database connection
     */
    private Connection connection;

    private Connection getConnection() throws SQLException {
        if (connection == null) {
            setConnection();
        } else if (connection.isClosed()) {
            setConnection();
        }
        return connection;
    }

    private void setConnection() {
        try {
            // Ensure that the database file exists
            if (databaseFile.createNewFile()) {
                plugin.log(Level.INFO, "Created the SQLite database file");
            }

            // Specify use of the JDBC SQLite driver
            Class.forName("org.sqlite.JDBC");

            // Set SQLite database properties
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            config.setEncoding(SQLiteConfig.Encoding.UTF8);
            config.setSynchronous(SQLiteConfig.SynchronousMode.FULL);

            // Establish the connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath(), config.toProperties());
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "An exception occurred creating the database file", e);
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "An SQL exception occurred initializing the SQLite database", e);
        } catch (ClassNotFoundException e) {
            plugin.log(Level.SEVERE, "Failed to load the necessary SQLite driver", e);
        }
    }

    public SqLiteDatabase(@NotNull HuskTowns plugin) {
        super(plugin);
        this.databaseFile = new File(plugin.getDataFolder(), DATABASE_FILE_NAME);
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

        // Backup database file
        this.backupFlatFile(databaseFile);

        // Create tables
        if (!isCreated()) {
            plugin.log(Level.INFO, "Creating SQLite database tables");
            try {
                executeScript(getConnection(), "sqlite_schema.sql");
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "Failed to create SQLite database tables");
                setLoaded(false);
                return;
            }
            setSchemaVersion(Migration.getLatestVersion());
            plugin.log(Level.INFO, "SQLite database tables created!");
            setLoaded(true);
            return;
        }

        // Perform migrations
        try {
            performMigrations(getConnection(), Type.SQLITE);
            setLoaded(true);
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to perform SQLite database migrations");
            setLoaded(false);
        }
    }

    @Override
    public boolean isCreated() {
        if (!databaseFile.exists()) {
            return false;
        }
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
            SELECT `uuid`
            FROM `%user_data%`
            LIMIT 1;"""))) {
            statement.executeQuery();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public int getSchemaVersion() {
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
            SELECT `schema_version`
            FROM `%meta_data%`
            LIMIT 1;"""))) {
            final ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("schema_version");
            }
        } catch (SQLException e) {
            plugin.log(Level.WARNING, "The database schema version could not be fetched; migrations will be carried out.");
        }
        return -1;
    }

    @Override
    public void setSchemaVersion(int version) {
        if (getSchemaVersion() == -1) {
            try (PreparedStatement insertStatement = getConnection().prepareStatement(format("""
                INSERT INTO `%meta_data%` (`schema_version`)
                VALUES (?);"""))) {
                insertStatement.setInt(1, version);
                insertStatement.executeUpdate();
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "Failed to insert schema version in table", e);
            }
            return;
        }

        try (PreparedStatement statement = getConnection().prepareStatement(format("""
            UPDATE `%meta_data%`
            SET `schema_version` = ?;"""))) {
            statement.setInt(1, version);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to update schema version in table", e);
        }
    }

    @Override
    public Optional<SavedUser> getUser(@NotNull UUID uuid) {
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
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
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to fetch user data from table by UUID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<SavedUser> getUser(@NotNull String username) {
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
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
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to fetch user data from table by username", e);
        }
        return Optional.empty();
    }

    @Override
    public List<SavedUser> getInactiveUsers(long daysInactive) {
        final List<SavedUser> inactiveUsers = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
            SELECT `uuid`, `username`, `last_login`, `preferences`
            FROM `%user_data%`
            WHERE datetime(`last_login` / 1000, 'unixepoch') < datetime('now', ?);"""))) {
            statement.setString(1, String.format("-%d days", daysInactive));
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
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to fetch list of inactive users", e);
            inactiveUsers.clear(); // Clear for safety to prevent any accidental data being returned
        }
        return inactiveUsers;
    }

    @Override
    public void createUser(@NotNull User user, @NotNull Preferences preferences) {
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
            INSERT INTO `%user_data%` (`uuid`, `username`, `last_login`, `preferences`)
            VALUES (?, ?, ?, ?)"""))) {
            statement.setString(1, user.getUuid().toString());
            statement.setString(2, user.getUsername());
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            statement.setBytes(4, plugin.getGson().toJson(preferences).getBytes(StandardCharsets.UTF_8));
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to create user in table", e);
        }
    }

    @Override
    public void updateUser(@NotNull User user, @NotNull OffsetDateTime lastLogin, @NotNull Preferences preferences) {
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
            UPDATE `%user_data%`
            SET `username` = ?, `last_login` = ?, `preferences` = ?
            WHERE `uuid` = ?"""))) {
            statement.setString(1, user.getUsername());
            statement.setTimestamp(2, Timestamp.valueOf(lastLogin.toLocalDateTime()));
            statement.setBytes(3, plugin.getGson().toJson(preferences).getBytes(StandardCharsets.UTF_8));
            statement.setString(4, user.getUuid().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to update user in table", e);
        }
    }

    @Override
    public void deleteAllUsers() {
        try (Statement statement = getConnection().createStatement()) {
            statement.execute(format("DELETE FROM `%user_data%`"));
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to delete all users from table", e);
        }
    }

    @Override
    public Optional<Town> getTown(int townId) {
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
            SELECT `id`, `data`
            FROM `%town_data%`
            WHERE `id` = ?"""))) {
            statement.setInt(1, townId);
            final ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                final Town town = plugin.getTownFromJson(
                    new String(resultSet.getBytes("data"), StandardCharsets.UTF_8)
                );
                town.setId(resultSet.getInt("id"));
                return Optional.of(town);
            }
        } catch (SQLException | JsonSyntaxException e) {
            plugin.log(Level.SEVERE, "Failed to fetch town data from table by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Town> getAllTowns() throws IllegalStateException {
        final List<Town> towns = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
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
        } catch (SQLException | JsonSyntaxException e) {
            throw new IllegalStateException("Failed to fetch all town data from table", e);
        }
        return towns;
    }

    @Override
    @NotNull
    public Town createTown(@NotNull String name, @NotNull User creator) {
        final Town town = Town.create(name, creator, plugin);
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
            INSERT INTO `%town_data%` (`name`, `data`)
            VALUES (?, ?)
            RETURNING `id`;"""))) {
            statement.setString(1, town.getName());
            statement.setBytes(2, plugin.getGson().toJson(town).getBytes(StandardCharsets.UTF_8));
            final ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                town.setId(resultSet.getInt("id"));
            }
        } catch (SQLException | JsonSyntaxException e) {
            plugin.log(Level.SEVERE, "Failed to create town in table", e);
        }
        return town;
    }

    @Override
    public void updateTown(@NotNull Town town) {
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
            UPDATE `%town_data%`
            SET `name` = ?, `data` = ?
            WHERE `id` = ?"""))) {
            statement.setString(1, town.getName());
            statement.setBytes(2, plugin.getGson().toJson(town).getBytes(StandardCharsets.UTF_8));
            statement.setInt(3, town.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to update town in table", e);
        }
    }

    @Override
    public void deleteTown(int townId) {
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
            DELETE FROM `%town_data%`
            WHERE `id` = ?"""))) {
            statement.setInt(1, townId);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to delete town from table", e);
        }
    }

    @Override
    public void deleteAllTowns() {
        try (Statement statement = getConnection().createStatement()) {
            statement.execute(format("DELETE FROM `%town_data%`"));
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to delete all towns from table", e);
        }
    }

    @Override
    public Map<World, ClaimWorld> getClaimWorlds(@NotNull String server) throws IllegalStateException {
        final Map<World, ClaimWorld> worlds = new HashMap<>();
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
            SELECT `id`, `world_uuid`, `world_name`, `world_environment`, `claims`
            FROM `%claim_data%`
            WHERE `server_name` = ?"""))) {
            statement.setString(1, server);
            final ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                final World world = World.of(UUID.fromString(resultSet.getString("world_uuid")),
                    resultSet.getString("world_name"),
                    resultSet.getString("world_environment"));
                final ClaimWorld claimWorld = plugin.getClaimWorldFromJson(
                    new String(resultSet.getBytes("claims"), StandardCharsets.UTF_8)
                );
                claimWorld.updateId(resultSet.getInt("id"));
                if (!plugin.getSettings().getGeneral().isUnclaimableWorld(world)) {
                    worlds.put(world, claimWorld);
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
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
            SELECT `id`, `server_name`, `world_uuid`, `world_name`, `world_environment`, `claims`
            FROM `%claim_data%`"""))) {
            final ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                final World world = World.of(UUID.fromString(resultSet.getString("world_uuid")),
                    resultSet.getString("world_name"),
                    resultSet.getString("world_environment"));
                final ClaimWorld claimWorld = plugin.getClaimWorldFromJson(
                    new String(resultSet.getBytes("claims"), StandardCharsets.UTF_8)
                );
                claimWorld.updateId(resultSet.getInt("id"));
                worlds.put(new ServerWorld(resultSet.getString("server_name"), world), claimWorld);
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
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
            INSERT INTO `%claim_data%` (`world_uuid`, `world_name`, `world_environment`, `server_name`, `claims`)
            VALUES (?, ?, ?, ?, ?)"""))) {
            statement.setString(1, world.getUuid().toString());
            statement.setString(2, world.getName());
            statement.setString(3, world.getEnvironment());
            statement.setString(4, plugin.getServerName());
            statement.setBytes(5, plugin.getGson().toJson(claimWorld).getBytes(StandardCharsets.UTF_8));
            claimWorld.updateId(statement.executeUpdate());
        } catch (SQLException | JsonSyntaxException e) {
            plugin.log(Level.SEVERE, "Failed to create claim world in table", e);
        }
        return claimWorld;
    }

    @Override
    public void updateClaimWorld(@NotNull ClaimWorld claimWorld) {
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
            UPDATE `%claim_data%`
            SET `claims` = ?
            WHERE `id` = ?"""))) {
            statement.setBytes(1, plugin.getGson().toJson(claimWorld).getBytes(StandardCharsets.UTF_8));
            statement.setInt(2, claimWorld.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to update claim world in table", e);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to close connection", e);
        }
    }

}
