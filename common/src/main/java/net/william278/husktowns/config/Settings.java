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
    private Map<String, String> tableNames = Map.of(
            Database.Table.USER_DATA.name().toLowerCase(), Database.Table.USER_DATA.defaultName,
            Database.Table.TOWN_DATA.name().toLowerCase(), Database.Table.TOWN_DATA.defaultName,
            Database.Table.CLAIM_DATA.name().toLowerCase(), Database.Table.CLAIM_DATA.defaultName
    );


    // Cross-server settings
    @YamlComment("Synchronise towns across a proxy network. Requires MySQL. Don't forget to update server.yml")
    @YamlKey("cross_server.enabled")
    public boolean crossServer = false;

    @YamlKey("cross_server.messenger_type")
    public Broker.Type brokerType = Broker.Type.PLUGIN_MESSAGE;

    @YamlComment("Sub-network cluster identifier. Don't edit this unless you know what you're doing")
    @YamlKey("cross_server.cluster_id")
    public String clusterId = "main";

    @YamlComment("General system settings")
    @YamlKey("general.list_items_per_page")
    public int listItemsPerPage = 6;

    @YamlKey("general.inspector_tool")
    public String inspectorTool = "minecraft:stick";

    @YamlKey("general.max_inspection_distance")
    public int maxInspectionDistance = 80;

    @YamlKey("general.claim_map_width")
    public int claimMapWidth = 9;

    @YamlKey("general.claim_map_height")
    public int claimMapHeight = 9;


    // Town settings
    @YamlComment("Town settings. Check rules.yml, roles.yml and levels.yml for more settings")
    @YamlKey("towns.allow_unicode_names")
    public boolean allowUnicodeNames = false;

    @YamlKey("towns.allow_unicode_bios")
    public boolean allowUnicodeMeta = true;

    @YamlKey("towns.admin_town_name")
    public String adminTownName = "Admin";


    @SuppressWarnings("unused")
    private Settings() {
    }

    @NotNull
    public String getTableName(@NotNull Database.Table tableName) {
        return Optional.ofNullable(tableNames.get(tableName.name().toLowerCase())).orElse(tableName.defaultName);
    }
}
