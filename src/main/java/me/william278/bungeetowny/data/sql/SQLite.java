package me.william278.bungeetowny.data.sql;

import me.william278.bungeetowny.HuskTowns;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQLite extends Database {

    final static String[] SQL_SETUP_STATEMENTS = {
            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getLocationsTable() + " (" +
                    "`id` integer PRIMARY KEY," +
                    "`server` text NOT NULL," +
                    "`world` text NOT NULL," +
                    "`x` double NOT NULL," +
                    "`y` double NOT NULL," +
                    "`z` double NOT NULL," +
                    "`yaw` float NOT NULL," +
                    "`pitch` float NOT NULL" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getTownsTable() + " (" +
                    "`id` integer PRIMARY KEY," +
                    "`name` varchar(16) NOT NULL UNIQUE," +
                    "`money` double NOT NULL," +
                    "`founded` timestamp NOT NULL," +
                    "`greeting_message` varchar(255) NOT NULL," +
                    "`farewell_message` varchar(255) NOT NULL," +
                    "`spawn_location_id` integer," +

                    "FOREIGN KEY (`spawn_location_id`) REFERENCES " + HuskTowns.getSettings().getTownsTable() + " (`id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getPlayerTable() + " (" +
                    "`id` integer PRIMARY KEY," +
                    "`username` varchar(16) NOT NULL," +
                    "`uuid` char(36) NOT NULL UNIQUE," +
                    "`town_id` integer," +
                    "`town_role` integer," +
                    "`is_teleporting` boolean NOT NULL," +
                    "`teleport_destination_id` integer," +

                    "FOREIGN KEY (`town_id`) REFERENCES " + HuskTowns.getSettings().getTownsTable() + " (`id`) ON DELETE SET NULL ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`teleport_destination_id`) REFERENCES " + HuskTowns.getSettings().getLocationsTable() + " (`id`) ON DELETE SET NULL ON UPDATE NO ACTION" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getClaimsTable() + " (" +
                    "`id` integer PRIMARY KEY," +
                    "`town_id` integer NOT NULL," +
                    "`claim_time` timestamp NOT NULL," +
                    "`claimer_id` integer," +
                    "`server` text NOT NULL," +
                    "`world` text NOT NULL," +
                    "`chunk_x` integer NOT NULL," +
                    "`chunk_z` integer NOT NULL," +
                    "`chunk_type` integer NOT NULL," +
                    "`plot_owner_id` integer," +

                    "FOREIGN KEY (`town_id`) REFERENCES " + HuskTowns.getSettings().getTownsTable() + " (`id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`claimer_id`) REFERENCES " + HuskTowns.getSettings().getPlayerTable() + " (`id`) ON DELETE SET NULL ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`plot_owner_id`) REFERENCES " + HuskTowns.getSettings().getPlayerTable() + " (`id`) ON DELETE SET NULL ON UPDATE NO ACTION" +
                    ");"
    };

    private static final String DATABASE_NAME = "HuskTownsData";

    private Connection connection;

    public SQLite(HuskTowns instance) {
        super(instance);
    }

    @Override
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
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
                try {
                    Class.forName("org.sqlite.JDBC");
                    connection = (DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + DATABASE_NAME + ".db"));
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, "An exception occurred initialising the SQLite database", ex);
                } catch (ClassNotFoundException ex) {
                    plugin.getLogger().log(Level.SEVERE, "The SQLite JBDC library is missing! Please download and place this in the /lib folder.");
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "An error occurred checking the status of the SQL connection: ", exception);
        }
        return connection;
    }

    @Override
    public void load() {
        connection = getConnection();
        try {
            Statement statement = connection.createStatement();
            for (String tableCreationStatement : SQL_SETUP_STATEMENTS) {
                statement.execute(tableCreationStatement);
            }
            statement.close();
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred creating tables: ", exception);
            exception.printStackTrace();
        }

        initialize();
    }
}
