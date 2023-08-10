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

package net.william278.husktowns.config;

import net.kyori.adventure.text.format.TextColor;
import net.william278.annotaml.YamlComment;
import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.database.Database;
import net.william278.husktowns.network.Broker;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃       HuskTowns Config       ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ Information: https://william278.net/project/husktowns
        ┣╸ Config Help: https://william278.net/docs/husktowns/config-files/
        ┗╸ Documentation: https://william278.net/docs/husktowns""")
public class Settings {

    // Top-level settings
    @YamlComment("Locale of the default language file to use. Docs: https://william278.net/docs/husktowns/translations")
    @YamlKey("language")
    private String language = "en-gb";

    @YamlComment("Whether to automatically check for plugin updates on startup")
    @YamlKey("check_for_updates")
    private boolean checkForUpdates = true;

    @YamlComment("Aliases to use for the /town command.")
    @YamlKey("aliases")
    private List<String> aliases = List.of(
            "t"
    );


    // Database settings
    @YamlComment("Type of database to use (SQLITE, MYSQL or MARIADB)")
    @YamlKey("database.type")
    private Database.Type databaseType = Database.Type.SQLITE;

    @YamlComment("Specify credentials here if you are using MYSQL or MARIADB as your database type")
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

    @YamlComment("MYSQL database Hikari connection pool properties. Don't modify this unless you know what you're doing!")
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

    @YamlComment("Names of tables to use on your database. Don't modify this unless you know what you're doing!")
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

    @YamlComment("The type of message broker to use for cross-server communication. Options: PLUGIN_MESSAGE, REDIS")
    @YamlKey("cross_server.messenger_type")
    private Broker.Type brokerType = Broker.Type.PLUGIN_MESSAGE;

    @YamlComment("Specify a common ID for grouping servers running HuskTowns on your proxy. Don't modify this unless you know what you're doing!")
    @YamlKey("cross_server.cluster_id")
    private String clusterId = "main";

    @YamlComment("Specify credentials here if you are using REDIS as your messenger_type. Docs: https://william278.net/docs/husktowns/redis-support/")
    @YamlKey("cross_server.redis.host")
    private String redisHost = "localhost";

    @YamlKey("cross_server.redis.port")
    private int redisPort = 6379;

    @YamlKey("cross_server.redis.password")
    private String redisPassword = "";

    @YamlKey("cross_server.redis.ssl")
    private boolean redisSsl = false;


    // General settings
    @YamlComment("How many items should be displayed per-page in chat menu lists")
    @YamlKey("general.list_items_per_page")
    private int listItemsPerPage = 6;

    @YamlComment("Which item to use for the inspector tool; the item that displays claim information when right-clicked.")
    @YamlKey("general.inspector_tool")
    private String inspectorTool = "minecraft:stick";

    @YamlComment("How far away the inspector tool can be used from a claim. (blocks)")
    @YamlKey("general.max_inspection_distance")
    private int maxInspectionDistance = 80;

    @YamlComment("The slot to display claim entry/teleportation notifications in. (ACTION_BAR, CHAT, TITLE, SUBTITLE, NONE)")
    @YamlKey("general.notification_slot")
    private Locales.Slot notificationSlot = Locales.Slot.ACTION_BAR;

    @YamlComment("The width and height of the claim map displayed in chat when runnign the /town map command.")
    @YamlKey("general.claim_map_width")
    private int claimMapWidth = 9;

    @YamlKey("general.claim_map_height")
    private int claimMapHeight = 9;

    @YamlComment("Whether town spawns should be automatically created when a town's first claim is made.")
    @YamlKey("general.first_claim_auto_setspawn")
    private boolean firstClaimAutoSetSpawn = false;

    @YamlComment("Whether to provide modern, rich TAB suggestions for commands (if available)")
    @YamlKey("general.brigadier_tab_completion")
    private boolean brigadierTabCompletion = true;

    @YamlComment("Whether to allow players to attack other players in their town.")
    @YamlKey("general.allow_friendly_fire")
    private boolean allowFriendlyFire = false;

    @YamlComment("A list of world names where claims cannot be created.")
    @YamlKey("general.unclaimable_worlds")
    private List<String> unclaimableWorlds = List.of(
            "world_nether",
            "world_the_end"
    );

    @YamlComment("A list of town names that cannot be used.")
    @YamlKey("general.prohibited_town_names")
    private List<String> prohibitedTownNames = List.of(
            "Administrators",
            "Moderators",
            "Mods",
            "Staff",
            "Server"
    );

    @YamlComment("Adds special advancements for town progression. Docs: https://william278.net/docs/husktowns/town-advancements/")
    @YamlKey("general.do_advancements")
    private boolean advancements = true;

    @YamlComment("Enable economy features. Requires Vault.")
    @YamlKey("general.economy_hook")
    private boolean economyHook = true;

    @YamlComment("Hook with LuckPerms to provide town permission contexts. Docs: https://william278.net/docs/husktowns/luckperms-contexts")
    @YamlKey("general.luckperms_contexts_hook")
    private boolean luckPermsHook = true;

    @YamlComment("Hook with PlaceholderAPI to provide placeholders. Docs: https://william278.net/docs/husktowns/placeholders")
    @YamlKey("general.placeholderapi_hook")
    private boolean placeholderAPIHook = true;

    @YamlComment("Use HuskHomes for improved teleportation")
    @YamlKey("general.huskhomes_hook")
    private boolean huskHomesHook = true;

    @YamlComment("Show town information on your Player Analytics web panel")
    @YamlKey("general.plan_hook")
    private boolean planHook = true;

    @YamlComment("Show claims on your server Dynmap or BlueMap. Docs: https://william278.net/docs/husktowns/map-hooks/")
    @YamlKey("general.web_map_hook.enabled")
    private boolean webMapHook = true;

    @YamlComment("The name of the marker set to use for claims on your web map")
    @YamlKey("general.web_map_hook.marker_set_name")
    private String webMapMarkerSetName = "Claims";


    // Town settings
    @YamlComment("Whether town names should be restricted by a regex. Set this to false to allow full UTF-8 names.")
    @YamlKey("towns.restrict_town_names")
    private boolean restrictTownNames = true;

    @YamlComment("Regex which town names must match. Names have a hard min/max length of 3-16 characters.")
    @YamlKey("towns.town_name_regex")
    private String townNameRegex = "[a-zA-Z0-9-_]*";

    @YamlComment("Whether town bios/greetings/farewells should be restricted. Set this to false to allow full UTF-8.")
    @YamlKey("towns.restrict_town_bios")
    private boolean restrictTownBios = true;

    @YamlComment("Regex which town bios/greeting/farewells must match. A hard limit of 256 characters is enforced.")
    @YamlKey("towns.town_meta_regex")
    private String townMetaRegex = "\\A\\p{ASCII}*\\z";

    @YamlComment("Require the level 1 cost as collateral when creating a town (this cost is otherwise ignored)")
    @YamlKey("towns.require_first_level_collateral")
    private boolean requireFirstLevelCollateral = false;

    @YamlComment("The minimum distance apart towns must be, in chunks")
    @YamlKey("towns.minimum_chunk_separation")
    private int minimumChunkSeparation = 0;

    @YamlComment("Require towns to have all their claims adjacent to each other")
    @YamlKey("towns.require_claim_adjacency")
    private boolean requireClaimAdjacency = false;

    @YamlComment("Whether to spawn particle effects when crop growth or mob spawning is boosted by a town's level")
    @YamlKey("towns.spawn_boost_particles")
    private boolean spawnBoostParticles = true;

    @YamlComment("Which particle effect to use for crop growth and mob spawning boosts")
    @YamlKey("towns.boost_particle")
    private String boostParticle = "spell_witch";


    // Admin Town settings
    @YamlComment("Admin Town settings for changing how admin claims look")
    @YamlKey("towns.admin_town.name")
    private String adminTownName = "Admin";

    @YamlKey("towns.admin_town.color")
    private String adminTownColor = "#ff0000";


    // Inactive claim pruning settings
    @YamlComment("Delete towns on startup who have had no members online within a certain number of days. Docs: https://william278.net/docs/husktowns/inactive-town-pruning/")
    @YamlKey("towns.prune_inactive_towns.prune_on_startup")
    private boolean automaticallyPruneInactiveTowns = false;

    @YamlComment("The number of days a town can be inactive before it will be deleted")
    @YamlKey("towns.prune_inactive_towns.prune_after_days")
    private int pruneInactiveTownDays = 90;


    @SuppressWarnings("unused")
    private Settings() {
    }


    @NotNull
    public String getLanguage() {
        return language;
    }

    @NotNull
    public List<String> getAlias() {
        return aliases;
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

    @NotNull
    public Locales.Slot getNotificationSlot() {
        return notificationSlot;
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

    public boolean doRestrictTownNames() {
        return restrictTownNames;
    }

    @NotNull
    public String getTownNameRegex() {
        return townNameRegex;
    }

    public boolean doRestrictTownBios() {
        return restrictTownBios;
    }

    @NotNull
    public String getTownMetaRegex() {
        return townMetaRegex;
    }

    public boolean doRequireFirstLevelCollateral() {
        return requireFirstLevelCollateral;
    }

    public int getMinimumChunkSeparation() {
        return minimumChunkSeparation;
    }

    public boolean doRequireClaimAdjacency() {
        return requireClaimAdjacency;
    }

    public boolean doSpawnBoostParticles() {
        return spawnBoostParticles;
    }

    @NotNull
    public String getBoostParticle() {
        return boostParticle;
    }

    public boolean doAutomaticallyPruneInactiveTowns() {
        return automaticallyPruneInactiveTowns;
    }

    public int getPruneInactiveTownDays() {
        return pruneInactiveTownDays;
    }

    @NotNull
    public String getAdminTownName() {
        return adminTownName;
    }

    @NotNull
    public TextColor getAdminTownColor() {
        return Objects.requireNonNull(
                TextColor.fromHexString(adminTownColor),
                "Invalid hex color code for admin town"
        );
    }

}
