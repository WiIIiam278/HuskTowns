package net.william278.husktowns.config;

import net.william278.annotaml.YamlComment;
import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.network.Broker;
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
    public Database.Type databaseType = Database.Type.SQLITE;

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
    public int mySqlConnectionPoolSize = 10;

    @YamlKey("database.mysql.connection_pool.idle")
    public int mySqlConnectionPoolIdle = 10;

    @YamlKey("database.mysql.connection_pool.lifetime")
    public long mySqlConnectionPoolLifetime = 1800000;

    @YamlKey("database.mysql.connection_pool.keepalive")
    public long mySqlConnectionPoolKeepAlive = 30000;

    @YamlKey("database.mysql.connection_pool.timeout")
    public long mySqlConnectionPoolTimeout = 20000;

    @YamlKey("database.table_names")
    public Map<String, String> tableNames = Map.of(
            Database.Table.USER_DATA.name().toLowerCase(), Database.Table.USER_DATA.defaultName,
            Database.Table.TOWN_DATA.name().toLowerCase(), Database.Table.TOWN_DATA.defaultName,
            Database.Table.CLAIM_DATA.name().toLowerCase(), Database.Table.CLAIM_DATA.defaultName
    );

    @NotNull
    public String getTableName(@NotNull Database.Table tableName) {
        return Optional.ofNullable(tableNames.get(tableName.name().toLowerCase())).orElse(tableName.defaultName);
    }


    // Cross-server settings
    @YamlComment("Enable teleporting across proxied servers. Requires MySQL")
    @YamlKey("cross_server.enabled")
    public boolean crossServer = false;

    @YamlKey("cross_server.messenger_type")
    public Broker.Type brokerType = Broker.Type.PLUGIN_MESSAGE;

    @YamlKey("cross_server.cluster_id")
    public String clusterId = "main";

    @YamlComment("General settings")
    @YamlKey("general.list_items_per_page")
    public int listItemsPerPage = 12;

    @YamlKey("general.inspector_tool")
    public String inspectorTool = "minecraft:stick";

    // Town settings
    @YamlComment("Town settings")
    @YamlKey("towns.allow_unicode_names")
    public boolean allowUnicodeNames = false;

    @YamlKey("towns.allow_unicode_bios")
    public boolean allowUnicodeMeta = true;


    @SuppressWarnings("unused")
    private Settings() {
    }
}
