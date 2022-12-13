package net.william278.husktowns.config;

import net.william278.annotaml.YamlComment;
import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃       HuskTowns Config       ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ Information: https://william278.net/project/husktowns
        ┗╸ Documentation: https://william278.net/docs/husktowns""")
public class Settings {

    // Top-level settings
    public String language = "en-gb";

    @YamlKey("check_for_updates")
    public boolean checkForUpdates = true;

    @YamlKey("debug_logging")
    public boolean debugLogging = false;


    // Database settings
    @YamlComment("Database connection settings")
    @YamlKey("database.type")
    public DatabaseType databaseType = DatabaseType.SQLITE;

    @YamlKey("database.mysql.credentials.host")
    public String mySqlHost = "localhost";

    @YamlKey("database.mysql.credentials.port")
    public int mySqlPort = 3306;

    @YamlKey("database.mysql.credentials.database")
    public String mySqlDatabase = "HuskHomes";

    @YamlKey("database.mysql.credentials.username")
    public String mySqlUsername = "root";

    @YamlKey("database.mysql.credentials.password")
    public String mySqlPassword = "pa55w0rd";

    @YamlKey("database.mysql.credentials.parameters")
    public String mySqlConnectionParameters = "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8";

    @YamlComment("MySQL connection pool properties")
    @YamlKey("database.mysql.connection_pool.size")
    public int mySqlConnectionPoolSize = 12;

    @YamlKey("database.mysql.connection_pool.idle")
    public int mySqlConnectionPoolIdle = 12;

    @YamlKey("database.mysql.connection_pool.lifetime")
    public long mySqlConnectionPoolLifetime = 1800000;

    @YamlKey("database.mysql.connection_pool.keepalive")
    public long mySqlConnectionPoolKeepAlive = 30000;

    @YamlKey("database.mysql.connection_pool.timeout")
    public long mySqlConnectionPoolTimeout = 20000;

    @YamlKey("database.table_names")
    public Map<String, String> tableNames = Map.of(
            TableName.USER_DATA.name().toLowerCase(), TableName.USER_DATA.defaultName,
            TableName.TOWN_DATA.name().toLowerCase(), TableName.TOWN_DATA.defaultName,
            TableName.MEMBER_DATA.name().toLowerCase(), TableName.MEMBER_DATA.defaultName,
            TableName.CLAIM_DATA.name().toLowerCase(), TableName.CLAIM_DATA.defaultName
    );

    @NotNull
    public String getTableName(@NotNull TableName tableName) {
        return Optional.ofNullable(tableNames.get(tableName.name().toLowerCase())).orElse(tableName.defaultName);
    }

    /**
     * Identifies types of databases
     */
    public enum DatabaseType {
        MYSQL("MySQL"),
        SQLITE("SQLite");

        @NotNull
        public final String displayName;

        DatabaseType(@NotNull String displayName) {
            this.displayName = displayName;
        }
    }

    /**
     * Represents the names of tables in the database
     */
    public enum TableName {
        USER_DATA("husktowns_users"),
        TOWN_DATA("husktowns_towns"),
        MEMBER_DATA("husktowns_members"),
        CLAIM_DATA("husktowns_claims");
        private final String defaultName;

        TableName(@NotNull String defaultName) {
            this.defaultName = defaultName;
        }

        @NotNull
        public static TableName match(@NotNull String placeholder) throws IllegalArgumentException {
            return TableName.valueOf(placeholder.replaceAll("_data", "").toUpperCase());
        }
    }

    @SuppressWarnings("unused")
    private Settings() {
    }
}
