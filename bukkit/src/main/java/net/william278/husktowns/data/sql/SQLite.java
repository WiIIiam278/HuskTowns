package net.william278.husktowns.data.sql;

import com.zaxxer.hikari.HikariDataSource;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.flags.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;

public class SQLite extends Database {

    final static String[] SQL_SETUP_STATEMENTS = {
            "PRAGMA foreign_keys = ON;",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getLocationsTable() + " (" +
                    "`id` integer PRIMARY KEY," +
                    "`server` varchar(64) NOT NULL," +
                    "`world` varchar(64) NOT NULL," +
                    "`x` double NOT NULL," +
                    "`y` double NOT NULL," +
                    "`z` double NOT NULL," +
                    "`yaw` float NOT NULL," +
                    "`pitch` float NOT NULL" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getTownsTable() + " (" +
                    "`id` integer PRIMARY KEY," +
                    "`name` varchar(16) NOT NULL," +
                    "`money` double NOT NULL," +
                    "`founded` timestamp NOT NULL," +
                    "`greeting_message` varchar(255) NOT NULL," +
                    "`farewell_message` varchar(255) NOT NULL," +
                    "`bio` varchar(255) NOT NULL," +
                    "`spawn_location_id` integer REFERENCES " + HuskTowns.getSettings().getLocationsTable() + " (`id`) ON DELETE SET NULL," +
                    "`is_spawn_public` boolean NOT NULL" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getPlayerTable() + " (" +
                    "`id` integer PRIMARY KEY," +
                    "`username` varchar(16) NOT NULL," +
                    "`uuid` char(36) NOT NULL UNIQUE," +
                    "`town_id` integer REFERENCES " + HuskTowns.getSettings().getTownsTable() + " (`id`) ON DELETE SET NULL," +
                    "`town_role` integer," +
                    "`is_teleporting` boolean NOT NULL DEFAULT 0," +
                    "`teleport_destination_id` integer REFERENCES " + HuskTowns.getSettings().getLocationsTable() + " (`id`) ON DELETE SET NULL" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getClaimsTable() + " (" +
                    "`id` integer PRIMARY KEY," +
                    "`town_id` integer NOT NULL REFERENCES " + HuskTowns.getSettings().getTownsTable() + " (`id`) ON DELETE CASCADE," +
                    "`claim_time` timestamp NOT NULL," +
                    "`claimer_id` integer REFERENCES " + HuskTowns.getSettings().getPlayerTable() + " (`id`) ON DELETE SET NULL," +
                    "`server` varchar(64) NOT NULL," +
                    "`world` varchar(64) NOT NULL," +
                    "`chunk_x` integer NOT NULL," +
                    "`chunk_z` integer NOT NULL," +
                    "`chunk_type` integer NOT NULL," +
                    "`plot_owner_id` integer REFERENCES " + HuskTowns.getSettings().getPlayerTable() + " (`id`) ON DELETE SET NULL" +
                    ");",

            "CREATE UNIQUE INDEX IF NOT EXISTS " + HuskTowns.getSettings().getClaimsTable() + "_ix" + " ON " +  HuskTowns.getSettings().getClaimsTable()  + "(server, world, chunk_x, chunk_z);",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getBonusesTable() + " (" +
                    "`id` integer PRIMARY KEY," +
                    "`town_id` integer NOT NULL REFERENCES " + HuskTowns.getSettings().getTownsTable() + " (`id`) ON DELETE CASCADE," +
                    "`applier_id` integer REFERENCES " + HuskTowns.getSettings().getPlayerTable() + " (`id`) ON DELETE SET NULL," +
                    "`applied_time` timestamp NOT NULL," +
                    "`bonus_claims` integer NOT NULL," +
                    "`bonus_members` integer NOT NULL" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getTownFlagsTable() + " (" +
                    "`town_id` integer NOT NULL REFERENCES " + HuskTowns.getSettings().getTownsTable() + "(`id`) ON DELETE CASCADE," +
                    "`chunk_type` integer NOT NULL," +
                    "`" + ExplosionDamageFlag.FLAG_IDENTIFIER + "` boolean NOT NULL," +
                    "`" + FireDamageFlag.FLAG_IDENTIFIER + "` boolean NOT NULL," +
                    "`" + MobGriefingFlag.FLAG_IDENTIFIER + "` boolean NOT NULL," +
                    "`" + MonsterSpawningFlag.FLAG_IDENTIFIER + "` boolean NOT NULL," +
                    "`" + PvpFlag.FLAG_IDENTIFIER + "` boolean NOT NULL," +
                    "`" + PublicInteractAccessFlag.FLAG_IDENTIFIER + "` boolean NOT NULL," +
                    "`" + PublicContainerAccessFlag.FLAG_IDENTIFIER + "` boolean NOT NULL," +
                    "`" + PublicBuildAccessFlag.FLAG_IDENTIFIER + "` boolean NOT NULL," +
                    "`" + PublicFarmAccessFlag.FLAG_IDENTIFIER + "` boolean NOT NULL," +
                    "PRIMARY KEY (`town_id`, `chunk_type`)" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getPlotMembersTable() + " (" +
                    "`claim_id` integer NOT NULL REFERENCES " + HuskTowns.getSettings().getClaimsTable() + " (`id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
                    "`member_id` integer NOT NULL REFERENCES " + HuskTowns.getSettings().getPlayerTable() + " (`id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
                    "PRIMARY KEY (`claim_id`, `member_id`)" +
                    ");"
    };

    private static final String DATABASE_NAME = "HuskTownsData";

    private HikariDataSource dataSource;

    public SQLite(HuskTowns instance) {
        super(instance);
    }

    // Create the database file if it does not exist yet
    private void createDatabaseFileIfNotExist() {
        File databaseFile = new File(plugin.getDataFolder(), DATABASE_NAME + ".db");
        if (!databaseFile.exists()) {
            try {
                if (!databaseFile.createNewFile()) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to write new file: " + DATABASE_NAME + ".db (file already exists)");
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "An error occurred writing a file: " + DATABASE_NAME + ".db (" + e.getCause() + ")");
            }
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void load() {
        // Make SQLite database file
        createDatabaseFileIfNotExist();

        // Create new HikariCP data source
        final String jdbcUrl = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + DATABASE_NAME + ".db";
        dataSource = new HikariDataSource();
        dataSource.setDataSourceClassName("org.sqlite.SQLiteDataSource");
        dataSource.addDataSourceProperty("url", jdbcUrl);

        // Set various additional parameters
        dataSource.setMaximumPoolSize(hikariMaximumPoolSize);
        dataSource.setMinimumIdle(hikariMinimumIdle);
        dataSource.setMaxLifetime(hikariMaximumLifetime);
        dataSource.setKeepaliveTime(hikariKeepAliveTime);
        dataSource.setConnectionTimeout(hikariConnectionTimeOut);
        dataSource.setPoolName(DATA_POOL_NAME);

        // Create tables & perform setup
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                for (String tableCreationStatement : SQL_SETUP_STATEMENTS) {
                    statement.execute(tableCreationStatement);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred creating tables on the SQLite database: ", e);
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Override
    public void backup() {
        final String BACKUPS_FOLDER_NAME = "database-backups";
        final String backupFileName = DATABASE_NAME + "Backup_" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SS")
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(Instant.now()).replaceAll(" ", "-") + ".db";
        final File databaseFile = new File(plugin.getDataFolder(), DATABASE_NAME + ".db");
        if (new File(plugin.getDataFolder(), BACKUPS_FOLDER_NAME).mkdirs()) {
            plugin.getLogger().info("Created backups directory in HuskTowns plugin data folder.");
        }
        final File backUpFile = new File(plugin.getDataFolder(), BACKUPS_FOLDER_NAME + File.separator + backupFileName);
        try {
            Files.copy(databaseFile.toPath(), backUpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("Created a backup of your database.");
        } catch (IOException iox) {
            plugin.getLogger().log(Level.WARNING, "An error occurred making a database backup", iox);
        }
    }
}
