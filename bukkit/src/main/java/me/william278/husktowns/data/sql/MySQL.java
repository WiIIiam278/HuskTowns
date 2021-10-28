package me.william278.husktowns.data.sql;

import com.zaxxer.hikari.HikariDataSource;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.flags.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class MySQL extends Database {

    final static String[] SQL_SETUP_STATEMENTS = {
            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getLocationsTable() + " (" +
                    "`id` integer AUTO_INCREMENT NOT NULL," +
                    "`server` varchar(64) NOT NULL," +
                    "`world` varchar(64) NOT NULL," +
                    "`x` double NOT NULL," +
                    "`y` double NOT NULL," +
                    "`z` double NOT NULL," +
                    "`yaw` float NOT NULL," +
                    "`pitch` float NOT NULL," +

                    "PRIMARY KEY (`id`)" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getTownsTable() + " (" +
                    "`id` integer AUTO_INCREMENT NOT NULL," +
                    "`name` varchar(16) NOT NULL," +
                    "`money` double NOT NULL," +
                    "`founded` timestamp NOT NULL," +
                    "`greeting_message` varchar(255) NOT NULL," +
                    "`farewell_message` varchar(255) NOT NULL," +
                    "`bio` varchar(255) NOT NULL," +
                    "`spawn_location_id` integer," +
                    "`is_spawn_public` boolean NOT NULL," +

                    "PRIMARY KEY (`id`)," +
                    "FOREIGN KEY (`spawn_location_id`) REFERENCES " + HuskTowns.getSettings().getLocationsTable() + " (`id`) ON DELETE SET NULL ON UPDATE NO ACTION" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getPlayerTable() + " (" +
                    "`id` integer AUTO_INCREMENT NOT NULL," +
                    "`username` varchar(16) NOT NULL," +
                    "`uuid` char(36) NOT NULL," +
                    "`town_id` integer," +
                    "`town_role` integer," +
                    "`is_teleporting` boolean NOT NULL DEFAULT 0," +
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
                    "`server` varchar(64) NOT NULL," +
                    "`world` varchar(64) NOT NULL," +
                    "`chunk_x` integer NOT NULL," +
                    "`chunk_z` integer NOT NULL," +
                    "`chunk_type` integer NOT NULL," +
                    "`plot_owner_id` integer," +

                    "UNIQUE KEY `" + HuskTowns.getSettings().getClaimsTable() + "_ix" + "` (`server`,`world`,`chunk_x`,`chunk_z`)," +
                    "PRIMARY KEY (`id`)," +
                    "FOREIGN KEY (`town_id`) REFERENCES " + HuskTowns.getSettings().getTownsTable() + " (`id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`claimer_id`) REFERENCES " + HuskTowns.getSettings().getPlayerTable() + " (`id`) ON DELETE SET NULL ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`plot_owner_id`) REFERENCES " + HuskTowns.getSettings().getPlayerTable() + " (`id`) ON DELETE SET NULL ON UPDATE NO ACTION" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getBonusesTable() + " (" +
                    "`id` integer AUTO_INCREMENT NOT NULL," +
                    "`town_id` integer NOT NULL," +
                    "`applier_id` integer," +
                    "`applied_time` timestamp NOT NULL," +
                    "`bonus_claims` integer NOT NULL," +
                    "`bonus_members` integer NOT NULL," +

                    "PRIMARY KEY (`id`)," +
                    "FOREIGN KEY (`town_id`) REFERENCES " + HuskTowns.getSettings().getTownsTable() + " (`id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`applier_id`) REFERENCES " + HuskTowns.getSettings().getPlayerTable() + " (`id`) ON DELETE SET NULL ON UPDATE NO ACTION" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getTownFlagsTable() + " (" +
                    "`town_id` integer NOT NULL," +
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

                    "PRIMARY KEY (`town_id`, `chunk_type`)," +
                    "FOREIGN KEY (`town_id`) REFERENCES " + HuskTowns.getSettings().getTownsTable() + "(`id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
                    ");",

            "CREATE TABLE IF NOT EXISTS " + HuskTowns.getSettings().getPlotMembersTable() + " (" +
                    "`claim_id` integer NOT NULL," +
                    "`member_id` integer NOT NULL," +

                    "PRIMARY KEY (`claim_id`, `member_id`)," +
                    "FOREIGN KEY (`claim_id`) REFERENCES " + HuskTowns.getSettings().getClaimsTable() + " (`id`) ON DELETE CASCADE ON UPDATE NO ACTION," +
                    "FOREIGN KEY (`member_id`) REFERENCES " + HuskTowns.getSettings().getPlayerTable() + " (`id`) ON DELETE CASCADE ON UPDATE NO ACTION" +
                    ");"

    };

    final String host = HuskTowns.getSettings().getHost();
    final int port = HuskTowns.getSettings().getPort();
    final String database = HuskTowns.getSettings().getDatabase();
    final String username = HuskTowns.getSettings().getUsername();
    final String password = HuskTowns.getSettings().getPassword();
    final String params = HuskTowns.getSettings().getConnectionParams();

    private HikariDataSource dataSource;

    public MySQL(HuskTowns instance) {
        super(instance);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void load() {
        // Create new HikariCP data source
        final String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + params;
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);

        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // Set various additional parameters
        dataSource.setMaximumPoolSize(hikariMaximumPoolSize);
        dataSource.setMinimumIdle(hikariMinimumIdle);
        dataSource.setMaxLifetime(hikariMaximumLifetime);
        dataSource.setKeepaliveTime(hikariKeepAliveTime);
        dataSource.setConnectionTimeout(hikariConnectionTimeOut);
        dataSource.setPoolName(DATA_POOL_NAME);

        // Create tables
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                for (String tableCreationStatement : SQL_SETUP_STATEMENTS) {
                    statement.execute(tableCreationStatement);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred creating tables on the MySQL database: ", e);
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
        plugin.getLogger().info("Remember to make backups of your HuskTowns Database before updating the plugin!");
    }
}
