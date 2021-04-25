package me.william278.husktowns.data.sql;

import me.william278.husktowns.HuskTowns;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class MySQL extends Database {

    final static String[] SQL_SETUP_STATEMENTS = {
            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getLocationsTable() + " (" +
                    "`id` integer AUTO_INCREMENT NOT NULL," +
                    "`server` text NOT NULL," +
                    "`world` text NOT NULL," +
                    "`x` double NOT NULL," +
                    "`y` double NOT NULL," +
                    "`z` double NOT NULL," +
                    "`yaw` float NOT NULL," +
                    "`pitch` float NOT NULL," +

                    "PRIMARY KEY (`id`)" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getTownsTable() + " (" +
                    "`id` integer AUTO_INCREMENT NOT NULL," +
                    "`name` varchar(16) NOT NULL UNIQUE," +
                    "`money` double NOT NULL," +
                    "`founded` timestamp NOT NULL," +
                    "`greeting_message` varchar(255) NOT NULL," +
                    "`farewell_message` varchar(255) NOT NULL," +
                    "`spawn_location_id` integer," +

                    "PRIMARY KEY (`id`)," +
                    "FOREIGN KEY (`spawn_location_id`) REFERENCES " + HuskTowns.getSettings().getTownsTable() + " (`id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getPlayerTable() + " (" +
                    "`id` integer AUTO_INCREMENT NOT NULL," +
                    "`username` varchar(16) NOT NULL," +
                    "`uuid` char(36) NOT NULL UNIQUE," +
                    "`town_id` integer," +
                    "`town_role` integer," +
                    "`is_teleporting` boolean NOT NULL," +
                    "`teleport_destination_id` integer," +

                    "PRIMARY KEY (`id`)," +
                    "FOREIGN KEY (`town_id`) REFERENCES " + HuskTowns.getSettings().getTownsTable() + " (`id`) ON DELETE SET NULL ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`teleport_destination_id`) REFERENCES " + HuskTowns.getSettings().getLocationsTable() + " (`id`) ON DELETE SET NULL ON UPDATE NO ACTION" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getClaimsTable() + " (" +
                    "`id` integer AUTO_INCREMENT NOT NULL," +
                    "`town_id` integer NOT NULL," +
                    "`claim_time` timestamp NOT NULL," +
                    "`claimer_id` integer," +
                    "`server` text NOT NULL," +
                    "`world` text NOT NULL," +
                    "`chunk_x` integer NOT NULL," +
                    "`chunk_z` integer NOT NULL," +
                    "`chunk_type` integer NOT NULL," +
                    "`plot_owner_id` integer," +

                    "PRIMARY KEY (`id`)," +
                    "FOREIGN KEY (`town_id`) REFERENCES " + HuskTowns.getSettings().getTownsTable() + " (`id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`claimer_id`) REFERENCES " + HuskTowns.getSettings().getPlayerTable() + " (`id`) ON DELETE SET NULL ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`plot_owner_id`) REFERENCES " + HuskTowns.getSettings().getPlayerTable() + " (`id`) ON DELETE SET NULL ON UPDATE NO ACTION" +
                    ");"
    };

    final String host = HuskTowns.getSettings().getHost();
    final int port = HuskTowns.getSettings().getPort();
    final String database = HuskTowns.getSettings().getDatabase();
    final String username = HuskTowns.getSettings().getUsername();
    final String password = HuskTowns.getSettings().getPassword();
    final String params = HuskTowns.getSettings().getConnectionParams();

    private Connection connection;

    public MySQL(HuskTowns instance) {
        super(instance);
    }

    @Override
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                try {
                    synchronized (HuskTowns.getInstance()) {
                        Class.forName("com.mysql.jdbc.Driver");
                        connection = (DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + params, username, password));
                    }
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, "An exception occurred initialising the mySQL database: ", ex);
                } catch (ClassNotFoundException ex) {
                    plugin.getLogger().log(Level.SEVERE, "The mySQL JBDC library is missing! Please download and place this in the /lib folder.");
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
