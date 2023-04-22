/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
 */

package net.william278.husktowns.config;

import net.william278.annotaml.YamlComment;
import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.network.Broker;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
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
    @YamlKey("language")
    private String language = "en-gb";

    @YamlKey("check_for_updates")
    private boolean checkForUpdates = true;


    // Database settings
    @YamlComment("Database connection settings")
    @YamlKey("database.type")
    private Database.Type databaseType = Database.Type.SQLITE;

    @YamlKey("database.mysql.credentials.host")
    private String mySqlHost = "localhost";

    @YamlKey("database.mysql.credentials.port")
    private int mySqlPort = 3306;

    @YamlKey("database.mysql.credentials.database")
    private String mySqlDatabase = "HuskTowns";

    @YamlKey("database.mysql.credentials.username")
    private String mySqlUsername = "root";

    @YamlKey("database.mysql.credentials.password")
    private String mySqlPassword = "pa55w0rd";

    @YamlKey("database.mysql.credentials.parameters")
    private String mySqlConnectionParameters = "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8";

    @YamlComment("MySQL connection pool properties")
    @YamlKey("database.mysql.connection_pool.size")
    private int mySqlConnectionPoolSize = 10;

    @YamlKey("database.mysql.connection_pool.idle")
    private int mySqlConnectionPoolIdle = 10;

    @YamlKey("database.mysql.connection_pool.lifetime")
    private long mySqlConnectionPoolLifetime = 1800000;

    @YamlKey("database.mysql.connection_pool.keepalive")
    private long mySqlConnectionPoolKeepAlive = 30000;

    @YamlKey("database.mysql.connection_pool.timeout")
    private long mySqlConnectionPoolTimeout = 20000;

    @YamlKey("database.table_names")
    private Map<String, String> tableNames = Map.of(
            Database.Table.USER_DATA.name().toLowerCase(), Database.Table.USER_DATA.getDefaultName(),
            Database.Table.TOWN_DATA.name().toLowerCase(), Database.Table.TOWN_DATA.getDefaultName(),
            Database.Table.CLAIM_DATA.name().toLowerCase(), Database.Table.CLAIM_DATA.getDefaultName()
    );


    // Cross-server settings
    @YamlComment("Synchronise towns across a proxy network. Requires MySQL. Don't forget to update server.yml")
    @YamlKey("cross_server.enabled")
    private boolean crossServer = false;

    @YamlKey("cross_server.messenger_type")
    private Broker.Type brokerType = Broker.Type.PLUGIN_MESSAGE;

    @YamlComment("Sub-network cluster identifier. Don't edit this unless you know what you're doing")
    @YamlKey("cross_server.cluster_id")
    private String clusterId = "main";

    @YamlComment("Redis connection properties")
    @YamlKey("cross_server.redis.host")
    private String redisHost = "localhost";

    @YamlKey("cross_server.redis.port")
    private int redisPort = 6379;

    @YamlKey("cross_server.redis.password")
    private String redisPassword = "";

    @YamlKey("cross_server.redis.ssl")
    private boolean redisSsl = false;


    // General settings
    @YamlComment("General system settings")
    @YamlKey("general.list_items_per_page")
    private int listItemsPerPage = 6;

    @YamlKey("general.inspector_tool")
    private String inspectorTool = "minecraft:stick";

    @YamlKey("general.max_inspection_distance")
    private int maxInspectionDistance = 80;

    @YamlKey("general.claim_map_width")
    private int claimMapWidth = 9;

    @YamlKey("general.claim_map_height")
    private int claimMapHeight = 9;

    @YamlKey("general.first_claim_auto_setspawn")
    private boolean firstClaimAutoSetSpawn = false;

    @YamlKey("general.brigadier_tab_completion")
    private boolean brigadierTabCompletion = true;

    @YamlKey("general.allow_friendly_fire")
    private boolean allowFriendlyFire = false;

    @YamlKey("general.unclaimable_worlds")
    private List<String> unclaimableWorlds = List.of(
            "world_nether",
            "world_the_end"
    );

    @YamlKey("general.prohibited_town_names")
    private List<String> prohibitedTownNames = List.of(
            "Administrators",
            "Moderators",
            "Mods",
            "Staff",
            "Server"
    );

    @YamlComment("Add special advancements for town progression to your server")
    @YamlKey("general.do_advancements")
    private boolean advancements = true;

    @YamlComment("Enable economy features. Requires Vault or RedisEconomy")
    @YamlKey("general.economy_hook")
    private boolean economyHook = true;

    @YamlComment("Provide permission contexts via LuckPerms")
    @YamlKey("general.luckperms_contexts_hook")
    private boolean luckPermsHook = true;

    @YamlComment("Use PlaceholderAPI for placeholders")
    @YamlKey("general.placeholderapi_hook")
    private boolean placeholderAPIHook = true;

    @YamlComment("Use HuskHomes for improved teleportation")
    @YamlKey("general.huskhomes_hook")
    private boolean huskHomesHook = true;

    @YamlComment("Show town information on your Player Analytics web panel")
    @YamlKey("general.plan_hook")
    private boolean planHook = true;

    @YamlComment("Show claims on your server Dynmap or BlueMap")
    @YamlKey("general.web_map_hook.enabled")
    private boolean webMapHook = true;

    @YamlKey("general.web_map_hook.marker_set_name")
    private String webMapMarkerSetName = "Claims";


    // Town settings
    @YamlComment("Town settings. Check rules.yml, roles.yml and levels.yml for more settings")
    @YamlKey("towns.allow_unicode_names")
    private boolean allowUnicodeNames = false;

    @YamlKey("towns.allow_unicode_bios")
    private boolean allowUnicodeMeta = true;

    @YamlComment("The minimum distance apart towns must be, in chunks")
    @YamlKey("towns.minimum_chunk_separation")
    private int minimumChunkSeparation = 0;

    @YamlComment("Require towns to have all their claims adjacent to each other")
    @YamlKey("towns.require_claim_adjacency")
    private boolean requireClaimAdjacency = false;

    // Admin Town settings
    @YamlComment("Admin Town settings for changing how admin claims look")
    @YamlKey("towns.admin_town.name")
    private String adminTownName = "Admin";

    @YamlKey("towns.admin_town.color")
    private String adminTownColor = "#ff0000";

    @SuppressWarnings("unused")
    private Settings() {
    }

    @NotNull
    public String getLanguage() {
        return language;
    }

    public boolean doCheckForUpdates() {
        return checkForUpdates;
    }

    @NotNull
    public Database.Type getDatabaseType() {
        return databaseType;
    }

    @NotNull
    public String getMySqlHost() {
        return mySqlHost;
    }

    public int getMySqlPort() {
        return mySqlPort;
    }

    @NotNull
    public String getMySqlDatabase() {
        return mySqlDatabase;
    }

    @NotNull
    public String getMySqlUsername() {
        return mySqlUsername;
    }

    @NotNull
    public String getMySqlPassword() {
        return mySqlPassword;
    }

    @NotNull
    public String getMySqlConnectionParameters() {
        return mySqlConnectionParameters;
    }

    public int getMySqlConnectionPoolSize() {
        return mySqlConnectionPoolSize;
    }

    public int getMySqlConnectionPoolIdle() {
        return mySqlConnectionPoolIdle;
    }

    public long getMySqlConnectionPoolLifetime() {
        return mySqlConnectionPoolLifetime;
    }

    public long getMySqlConnectionPoolKeepAlive() {
        return mySqlConnectionPoolKeepAlive;
    }

    public long getMySqlConnectionPoolTimeout() {
        return mySqlConnectionPoolTimeout;
    }

    @NotNull
    public String getTableName(@NotNull Database.Table tableName) {
        return Optional.ofNullable(tableNames.get(tableName.name().toLowerCase())).orElse(tableName.getDefaultName());
    }

    public boolean doCrossServer() {
        return crossServer;
    }

    @NotNull
    public Broker.Type getBrokerType() {
        return brokerType;
    }

    @NotNull
    public String getClusterId() {
        return clusterId;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    @NotNull
    public String getRedisPassword() {
        return redisPassword;
    }

    public boolean useRedisSsl() {
        return redisSsl;
    }

    public int getListItemsPerPage() {
        return listItemsPerPage;
    }

    @NotNull
    public String getInspectorTool() {
        return inspectorTool;
    }

    public int getMaxInspectionDistance() {
        return maxInspectionDistance;
    }

    public int getClaimMapWidth() {
        return claimMapWidth;
    }

    public int getClaimMapHeight() {
        return claimMapHeight;
    }

    public boolean doFirstClaimAutoSetSpawn() {
        return firstClaimAutoSetSpawn;
    }

    public boolean doBrigadierTabCompletion() {
        return brigadierTabCompletion;
    }

    public boolean doAllowFriendlyFire() {
        return allowFriendlyFire;
    }

    public boolean isUnclaimableWorld(@NotNull World world) {
        return unclaimableWorlds.stream().anyMatch(world.getName()::equalsIgnoreCase);
    }

    public boolean isTownNameAllowed(@NotNull String name) {
        return prohibitedTownNames.stream().noneMatch(name::equalsIgnoreCase);
    }

    public boolean doAdvancements() {
        return advancements;
    }

    public boolean doEconomyHook() {
        return economyHook;
    }

    public boolean doLuckPermsHook() {
        return luckPermsHook;
    }

    public boolean doPlaceholderAPIHook() {
        return placeholderAPIHook;
    }

    public boolean doHuskHomesHook() {
        return huskHomesHook;
    }

    public boolean doPlanHook() {
        return planHook;
    }

    public boolean doWebMapHook() {
        return webMapHook;
    }

    @NotNull
    public String getWebMapMarkerSetName() {
        return webMapMarkerSetName;
    }

    public boolean doAllowUnicodeNames() {
        return allowUnicodeNames;
    }

    public boolean doAllowUnicodeMeta() {
        return allowUnicodeMeta;
    }

    public int getMinimumChunkSeparation() {
        return minimumChunkSeparation;
    }

    public boolean doRequireClaimAdjacency() {
        return requireClaimAdjacency;
    }

    @NotNull
    public String getAdminTownName() {
        return adminTownName;
    }

    @NotNull
    public Color getAdminTownColor() {
        return Color.decode(adminTownColor);
    }

}
