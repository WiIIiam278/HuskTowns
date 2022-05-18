package net.william278.husktowns.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.chunk.ClaimedChunk;
import net.william278.husktowns.flags.*;
import net.william278.husktowns.town.TownRole;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.*;
import java.util.logging.Level;

public class Settings {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    // Locale options
    public final String language;

    // Check for plugin updates on startup
    public final boolean startupCheckForUpdates;

    // General options
    public final long inviteExpiryTime;
    public Material inspectionTool;
    public final ArrayList<String> unClaimableWorlds = new ArrayList<>();
    public final ArrayList<String> prohibitedTownNames = new ArrayList<>();
    public final int teleportWarmup;
    public final Sound teleportWarmupSound;
    public final Sound teleportCompleteSound;
    public final Sound teleportCancelSound;
    public final boolean setTownSpawnInFirstClaim;
    public final String adminTownName;
    public final String adminTownColor;
    public final int townMapSquareRadius;
    public final boolean doTownChat;
    public final boolean doToggleableTownChat;
    public final boolean allowKillingHostilesEverywhere;
    public final boolean fallbackOnDatabaseIfCacheFailed;
    public final boolean blockPvpFriendlyFire;
    public final boolean logCacheLoading;

    public final int minimumTownChunkSeparation;

    // Flag options & defaults
    public final HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> defaultClaimFlags = new HashMap<>();
    public HashSet<Flag> wildernessFlags;
    public HashSet<Flag> unClaimableWorldFlags;
    public final HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> adminClaimFlags = new HashMap<>();

    // Help menu options
    public final boolean hideCommandsFromHelpMenuWithoutPermission;
    public final boolean hideHuskTownsCommandFromHelpMenu;

    // Economy integration
    public boolean doEconomy;
    public final double depositNotificationThreshold;
    public final double townCreationCost;
    public final double greetingCost;
    public final double farewellCost;
    public final double updateBioCost;
    public final double setSpawnCost;
    public final double renameCost;
    public final double makeSpawnPublicCost;

    // LuckPerms context provider integration
    public final boolean doLuckPerms;

    // HuskHomes integration
    public boolean doHuskHomes;
    public final boolean disableHuskHomesSetHomeInOtherTown;

    // Map integration
    public boolean doMapIntegration;
    public final String mapIntegrationPlugin;
    public final boolean useTownColorsOnMap;
    public final String mapTownColor;
    public final double mapClaimFillOpacity;
    public final double mapClaimStrokeOpacity;
    public final int mapClaimStrokeWeight;
    public final String mapMarkerSetName;

    // Cross server names and auto-completion
    public final boolean autoCompletePlayerNames;

    // Bungee options
    public final String serverId;
    public final int clusterId;
    public final boolean doBungee;
    public MessengerType messengerType;

    // Data storage settings
    public DatabaseType databaseType;
    public final String playerTable;
    public final String townsTable;
    public final String claimsTable;
    public final String locationsTable;
    public final String bonusesTable;
    public final String plotMembersTable;
    public final String townFlagsTable;

    // Level thresholds and bonuses
    public final ArrayList<Double> levelRequirements = new ArrayList<>();
    public final ArrayList<Integer> maxClaims = new ArrayList<>();
    public final ArrayList<Integer> maxMembers = new ArrayList<>();

    // Redis connection settings
    public final int redisPort;
    public final String redisHost;
    public final String redisPassword;
    public final boolean redisSsl;


    // mySQL credentials
    public final String databaseHost;
    public final int databasePort;
    public final String databaseName;
    public final String databaseUsername;
    public final String databasePassword;
    public final String databaseConnectionParams;

    // Hikari connection pool options
    public final int hikariMaximumPoolSize;
    public final int hikariMinimumIdle;
    public final long hikariMaximumLifetime;
    public final long hikariKeepAliveTime;
    public final long hikariConnectionTimeOut;

    public Settings(YamlDocument config) {
        language = config.getString("language", "en-gb");
        startupCheckForUpdates = config.getBoolean("check_for_updates", true);

        inviteExpiryTime = config.getLong("general_options.invite_expiry", 120L);

        inspectionTool = Material.matchMaterial(Objects.requireNonNull(config.getString("general_options.claim_inspection_tool", "stick")));
        if (inspectionTool == null) {
            inspectionTool = Material.STICK;
            HuskTowns.getInstance().getLogger().warning("An invalid material was specified for the claim inspection tool; defaulting to a stick.");
        }

        unClaimableWorlds.addAll(config.getStringList("general_options.unclaimable_worlds"));
        prohibitedTownNames.addAll(config.getStringList("general_options.prohibited_town_names"));
        teleportWarmup = config.getInt("general_options.teleport_warmup_secs", 5);
        teleportWarmupSound = Sound.valueOf(config.getString("general_options.teleport_warmup_sound", "BLOCK_NOTE_BLOCK_BANJO"));
        teleportCompleteSound = Sound.valueOf(config.getString("general_options.teleport_complete_sound", "ENTITY_ENDERMAN_TELEPORT"));
        teleportCancelSound = Sound.valueOf(config.getString("general_options.teleport_cancel_sound", "ENTITY_ITEM_BREAK"));
        setTownSpawnInFirstClaim = config.getBoolean("general_options.set_town_spawn_in_first_claim", true);
        adminTownName = config.getString("general_options.admin_town_name", "Administrators");
        adminTownColor = config.getString("general_options.admin_town_color", "#ff7e5e");
        doTownChat = config.getBoolean("general_options.enable_town_chat", true);
        doToggleableTownChat = (doTownChat && config.getBoolean("general_options.toggelable_town_chat", true));
        allowKillingHostilesEverywhere = config.getBoolean("general_options.allow_killing_hostiles_everywhere", true);
        fallbackOnDatabaseIfCacheFailed = config.getBoolean("general_options.use_database_fallback_on_cache_fail", false);
        townMapSquareRadius = config.getInt("general_options.town_map_square_radius", 5);
        blockPvpFriendlyFire = config.getBoolean("general_options.block_pvp_friendly_fire", true);
        logCacheLoading = config.getBoolean("general_options.log_cache_loading", false);
        minimumTownChunkSeparation = config.getInt("general_options.minimum_town_chunk_separation", 0);

        TownRole.townRoles = getTownRoles(config);
        Collections.sort(TownRole.townRoles);

        defaultClaimFlags.put(ClaimedChunk.ChunkType.REGULAR, getFlags(config, "flag_options.default_town_flags.regular_chunks"));
        defaultClaimFlags.put(ClaimedChunk.ChunkType.FARM, getFlags(config, "flag_options.default_town_flags.farm_chunks"));
        defaultClaimFlags.put(ClaimedChunk.ChunkType.PLOT, getFlags(config, "flag_options.default_town_flags.plot_chunks"));
        wildernessFlags = getFlags(config, "flag_options.wilderness_flags");
        unClaimableWorldFlags = getFlags(config, "flag_options.unclaimable_world_flags");
        adminClaimFlags.put(ClaimedChunk.ChunkType.REGULAR, getFlags(config, "flag_options.admin_claim_flags"));
        adminClaimFlags.put(ClaimedChunk.ChunkType.FARM, getFlags(config, "flag_options.admin_claim_flags"));
        adminClaimFlags.put(ClaimedChunk.ChunkType.PLOT, getFlags(config, "flag_options.admin_claim_flags"));

        hideCommandsFromHelpMenuWithoutPermission = config.getBoolean("general_options.help_menu.hide_commands_without_permission", true);
        hideHuskTownsCommandFromHelpMenu = config.getBoolean("general_options.help_menu.hide_husktowns_command", false);

        doEconomy = config.getBoolean("integrations.economy.enabled", true);
        depositNotificationThreshold = config.getDouble("integrations.economy.deposit_notification_threshold", 0.01);
        townCreationCost = config.getDouble("integrations.economy.town_creation_cost", 150d);
        greetingCost = config.getDouble("integrations.economy.welcome_message_cost", 0d);
        farewellCost = config.getDouble("integrations.economy.farewell_message_cost", 0d);
        updateBioCost = config.getDouble("integrations.economy.update_bio_cost", 0d);
        setSpawnCost = config.getDouble("integrations.economy.town_set_spawn_cost", 50d);
        renameCost = config.getDouble("integrations.economy.town_rename_cost", 100d);
        makeSpawnPublicCost = config.getDouble("integrations.economy.make_spawn_public_cost", 25d);

        doLuckPerms = config.getBoolean("integrations.luckperms.enabled", true);

        doHuskHomes = config.getBoolean("integrations.huskhomes.enabled", true);
        disableHuskHomesSetHomeInOtherTown = config.getBoolean("integrations.huskhomes.block_sethome_in_other_towns", true);

        doMapIntegration = config.getBoolean("integrations.map.enabled", true);
        mapIntegrationPlugin = config.getString("integrations.map.plugin", "dynmap");
        useTownColorsOnMap = config.getBoolean("integrations.map.use_town_colors", true);
        mapTownColor = config.getString("integrations.map.default_town_color", "#4af7c9");
        mapClaimFillOpacity = config.getDouble("integrations.map.claim_fill_opacity", 0.5d);
        mapClaimStrokeOpacity = config.getDouble("integrations.map.claim_stroke_opacity", 0d);
        mapClaimStrokeWeight = config.getInt("integrations.map.claim_stroke_weight", 1);
        mapMarkerSetName = config.getString("integrations.map.marker_set_name", "Towns");

        autoCompletePlayerNames = config.getBoolean("general_options.auto_complete_usernames", true);

        doBungee = config.getBoolean("bungee_options.enable_bungee_mode", false);
        serverId = config.getString("bungee_options.server_id", "server");
        clusterId = config.getInt("bungee_options.cluster_id", 0);
        final String messengerTypeConfig = config.getString("bungee_options.messenger_type", "plugin_message");
        try {
            messengerType = messengerTypeConfig.equalsIgnoreCase("pluginmessage") ? MessengerType.PLUGIN_MESSAGE : MessengerType.valueOf(messengerTypeConfig.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid messenger type specified; defaulting to Plugin Message.");
            messengerType = MessengerType.PLUGIN_MESSAGE;
        }

        redisHost = config.getString("bungee_options.redis_credentials.host", "localhost");
        redisPort = config.getInt("bungee_options.redis_credentials.port", 6379);
        redisPassword = config.getString("bungee_options.redis_credentials.password", "");
        redisSsl = config.getBoolean("bungee_options.redis_credentials.use_ssl", false);

        try {
            databaseType = DatabaseType.valueOf(config.getString("data_storage_options.storage_type", "SQLite").toUpperCase());
        } catch (IllegalArgumentException e) {
            databaseType = DatabaseType.SQLITE;
            plugin.getLogger().warning("Invalid database type specified; defaulting to SQLite.");
        }
        playerTable = config.getString("data_storage_options.table_names.player_table", "husktowns_players");
        townsTable = config.getString("data_storage_options.table_names.towns_table", "husktowns_towns");
        claimsTable = config.getString("data_storage_options.table_names.claims_table", "husktowns_claims");
        locationsTable = config.getString("data_storage_options.table_names.locations_table", "husktowns_locations");
        bonusesTable = config.getString("data_storage_options.table_names.bonuses_table", "husktowns_bonus");
        plotMembersTable = config.getString("data_storage_options.table_names.plot_members_table", "husktowns_plot_members");
        townFlagsTable = config.getString("data_storage_options.table_names.town_flags_table", "husktowns_flags");

        levelRequirements.addAll(config.getDoubleList("town_levelling.level_deposit_requirements"));
        maxClaims.addAll(config.getIntList("town_levelling.level_max_claims"));
        maxMembers.addAll(config.getIntList("town_levelling.level_max_members"));

        databaseHost = config.getString("data_storage_options.mysql_credentials.host", "localhost");
        databasePort = config.getInt("data_storage_options.mysql_credentials.port", 3306);
        databaseName = config.getString("data_storage_options.mysql_credentials.database", "HuskTowns");
        databaseUsername = config.getString("data_storage_options.mysql_credentials.username", "root");
        databasePassword = config.getString("data_storage_options.mysql_credentials.password", "pa55w0rd");
        databaseConnectionParams = config.getString("data_storage_options.mysql_credentials.params", "?autoReconnect=true&useSSL=false");

        hikariMaximumPoolSize = config.getInt("data_storage_options.connection_pool_options.maximum_pool_size", 10);
        hikariMinimumIdle = config.getInt("data_storage_options.connection_pool_options.minimum_idle", 10);
        hikariMaximumLifetime = config.getLong("data_storage_options.connection_pool_options.maximum_lifetime", 1800000L);
        hikariKeepAliveTime = config.getLong("data_storage_options.connection_pool_options.keepalive_time", 0L);
        hikariConnectionTimeOut = config.getLong("data_storage_options.connection_pool_options.connection_timeout", 5000L);
    }

    private HashSet<Flag> getFlags(YamlDocument config, String configKeyPath) {
        HashSet<Flag> flags = new HashSet<>();
        flags.add(new ExplosionDamageFlag(config.getBoolean(configKeyPath + "." + ExplosionDamageFlag.FLAG_IDENTIFIER)));
        flags.add(new FireDamageFlag(config.getBoolean(configKeyPath + "." + FireDamageFlag.FLAG_IDENTIFIER)));
        flags.add(new MobGriefingFlag(config.getBoolean(configKeyPath + "." + MobGriefingFlag.FLAG_IDENTIFIER)));
        flags.add(new MonsterSpawningFlag(config.getBoolean(configKeyPath + "." + MonsterSpawningFlag.FLAG_IDENTIFIER)));
        flags.add(new PvpFlag(config.getBoolean(configKeyPath + "." + PvpFlag.FLAG_IDENTIFIER)));
        flags.add(new PublicInteractAccessFlag(config.getBoolean(configKeyPath + "." + PublicInteractAccessFlag.FLAG_IDENTIFIER)));
        flags.add(new PublicContainerAccessFlag(config.getBoolean(configKeyPath + "." + PublicContainerAccessFlag.FLAG_IDENTIFIER)));
        flags.add(new PublicBuildAccessFlag(config.getBoolean(configKeyPath + "." + PublicBuildAccessFlag.FLAG_IDENTIFIER)));
        flags.add(new PublicFarmAccessFlag(config.getBoolean(configKeyPath + "." + PublicFarmAccessFlag.FLAG_IDENTIFIER)));
        return flags;
    }

    private ArrayList<TownRole> getTownRoles(YamlDocument config) {
        final ArrayList<TownRole> roles = new ArrayList<>();
        for (String roleIdentifier : config.getSection("town_roles").getRoutesAsStrings(false)) {
            final int roleWeight = config.getInt("town_roles." + roleIdentifier + ".weight", 0);
            final String displayName = config.getString("town_roles." + roleIdentifier + ".display_name", "");
            final List<TownRole.RolePrivilege> townPrivileges = new ArrayList<>();
            for (String privilege : config.getStringList("town_roles." + roleIdentifier + ".town_privileges")) {
                try {
                    townPrivileges.add(TownRole.RolePrivilege.valueOf(privilege.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.SEVERE, "Invalid role privilege specified in config; " + privilege);
                }
            }
            roles.add(new TownRole(roleWeight, roleIdentifier, displayName, townPrivileges));
        }
        return roles;
    }

    public enum DatabaseType {
        SQLITE,
        MYSQL
    }

    public enum MessengerType {
        PLUGIN_MESSAGE,
        REDIS
    }

}