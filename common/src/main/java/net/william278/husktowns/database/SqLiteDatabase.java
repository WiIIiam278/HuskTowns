package net.william278.husktowns.database;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class SqLiteDatabase extends Database {

    /**
     * The name of the database file
     */
    private static final String DATABASE_FILE_NAME = "HuskTownsData.db";

    /**
     * Path to the SQLite HuskHomesData.db file
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
        super(plugin, "sqlite_schema.sql");
        this.databaseFile = new File(plugin.getDataFolder(), DATABASE_FILE_NAME);
    }

    @Override
    public void initialize() throws RuntimeException {
        // Establish connection
        this.setConnection();

        // Create tables
        try (Statement statement = getConnection().createStatement()) {
            for (String tableCreationStatement : getSchema()) {
                statement.execute(tableCreationStatement);
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to create SQLite database tables", e);
        }
    }

    @Override
    public Optional<User> getUser(@NotNull UUID uuid) {
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
                SELECT * FROM `%user_data%`
                WHERE uuid = ?"""))) {
            statement.setString(1, uuid.toString());
            final ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(User.of(uuid, resultSet.getString("username")));
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to fetch user data from table", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> getUser(@NotNull String username) {
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
                SELECT * FROM `%user_data%`
                WHERE `username` = ?"""))) {
            statement.setString(1, username);
            final ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                final UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                final String name = resultSet.getString("username");
                return Optional.of(User.of(uuid, name));
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to fetch user data from table", e);
        }
        return Optional.empty();
    }

    @Override
    public void createUser(@NotNull User user) {
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
                INSERT INTO `%user_data%` (`uuid`, `username`)
                VALUES (?, ?)"""))) {
            statement.setString(1, user.getUuid().toString());
            statement.setString(2, user.getUsername());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to create user in table", e);
        }
    }

    @Override
    public void updateUser(@NotNull User user) {
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
                UPDATE `%user_data%`
                SET `username` = ?
                WHERE `uuid` = ?"""))) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getUuid().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to update user in table", e);
        }
    }

    @Override
    public Optional<Town> getTown(@NotNull UUID townUuid) {
        try (PreparedStatement statement = getConnection().prepareStatement(format("""
                SELECT * FROM `%town_data%`
                WHERE `uuid` = ?"""))) {
            statement.setString(1, townUuid.toString());
            final ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {

            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to fetch town data from table", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Town> getTown(@NotNull String townName) {
        return Optional.empty();
    }

    @Override
    protected void createTown(@NotNull Town town) {

    }

    @Override
    public void updateTown(@NotNull Town town) {

    }

    @Override
    public void deleteTown(@NotNull UUID townUuid) {

    }

    @Override
    public Optional<ClaimWorld> getClaimWorld(@NotNull World world, @NotNull String server) {
        return Optional.empty();
    }

    @Override
    @NotNull
    public ClaimWorld createClaimWorld(@NotNull World world) {
        return null;
    }

    @Override
    public void updateClaimWorld(@NotNull ClaimWorld claimWorld) {

    }

    @Override
    public void deleteClaimWorld(@NotNull World world, @NotNull String server) {

    }

    @Override
    public Optional<Member> getMember(@NotNull UUID userUuid) {
        return Optional.empty();
    }

    @Override
    public void createMember(@NotNull Member member) {

    }

    @Override
    public void updateMember(@NotNull Member member) {

    }

    @Override
    public void deleteMember(@NotNull UUID userUuid) {

    }
}
