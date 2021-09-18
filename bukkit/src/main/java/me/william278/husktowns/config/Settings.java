package me.william278.husktowns.config;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.chunk.ClaimedChunk;
import me.william278.husktowns.flags.*;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Settings {

    // Locale options
    public final String language;

    // General options
    private final long inviteExpiryTime;
    private Material inspectionTool;
    private final ArrayList<String> unClaimableWorlds = new ArrayList<>();
    private final ArrayList<String> prohibitedTownNames = new ArrayList<>();
    private final int teleportWarmup;
    private final Sound teleportWarmupSound;
    private final Sound teleportCompleteSound;
    private final Sound teleportCancelSound;
    private final boolean setTownSpawnInFirstClaim;
    private final String adminTownName;
    private final String adminTownColor;
    private final int townMapSquareRadius;
    private final boolean doTownChat;
    private final boolean doToggleableTownChat;
    private final boolean allowKillingHostilesEverywhere;
    private final boolean fallbackOnDatabaseIfCacheFailed;
    private final boolean blockPvpFriendlyFire;
    private final boolean logCacheLoading;

    // Flag options & defaults
    private final static HashMap<ClaimedChunk.ChunkType,HashSet<Flag>> defaultClaimFlags = new HashMap<>();
    private static HashSet<Flag> wildernessFlags = new HashSet<>();
    private static HashSet<Flag> unClaimableWorldFlags = new HashSet<>();
    private static final HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> adminClaimFlags = new HashMap<>();

    // Help menu options
    private final boolean hideCommandsFromHelpMenuWithoutPermission;
    private final boolean hideHuskTownsCommandFromHelpMenu;

    // Economy integration
    private boolean doEconomy;
    private final double depositNotificationThreshold;
    private final double townCreationCost;
    private final double greetingCost;
    private final double farewellCost;
    private final double updateBioCost;
    private final double setSpawnCost;
    private final double renameCost;
    private final double makeSpawnPublicCost;

    // LuckPerms context provider integration
    private boolean doLuckPerms;

    // HuskHomes integration
    private boolean doHuskHomes;
    private final boolean disableHuskHomesSetHomeInOtherTown;

    // Map integration
    private boolean doMapIntegration;
    private final String mapIntegrationPlugin;
    private final boolean useTownColorsOnMap;
    private final String mapTownColor;
    private final double mapClaimFillOpacity;
    private final double mapClaimStrokeOpacity;
    private final int mapClaimStrokeWeight;
    private final String mapMarkerSetName;

    // Bungee options
    private final String serverID;
    private final int clusterID;
    private final boolean doBungee;

    // Data storage settings
    private final String databaseType;
    private final String playerTable;
    private final String townsTable;
    private final String claimsTable;
    private final String locationsTable;
    private final String bonusesTable;
    private final String plotMembersTable;
    private final String townFlagsTable;

    // Level thresholds and bonuses
    private final ArrayList<Double> levelRequirements = new ArrayList<>();
    private final ArrayList<Integer> maxClaims = new ArrayList<>();
    private final ArrayList<Integer> maxMembers = new ArrayList<>();

    // mySQL credentials
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String connectionParams;

    public Settings(FileConfiguration config) {
        language = config.getString("language", "en-gb");

        inviteExpiryTime = config.getLong("general_options.invite_expiry", 120L);
        inspectionTool = Material.matchMaterial(config.getString("general_options.claim_inspection_tool", "stick"));
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
        townCreationCost = config.getDouble("integrations.economy.town_creation_cost", 150D);
        greetingCost = config.getDouble("integrations.economy.welcome_message_cost", 0D);
        farewellCost = config.getDouble("integrations.economy.farewell_message_cost", 0D);
        updateBioCost = config.getDouble("integrations.economy.update_bio_cost", 0D);
        setSpawnCost = config.getDouble("integrations.economy.set_spawn_cost", 50D);
        renameCost = config.getDouble("integrations.economy.town_rename_cost", 100D);
        makeSpawnPublicCost = config.getDouble("integrations.economy.make_spawn_public_cost", 25D);

        doLuckPerms = config.getBoolean("integrations.luckperms.enabled", true);

        doHuskHomes = config.getBoolean("integrations.huskhomes.enabled", true);
        disableHuskHomesSetHomeInOtherTown = config.getBoolean("integrations.huskhomes.block_sethome_in_other_towns", true);

        doMapIntegration = config.getBoolean("integrations.map.enabled", true);
        mapIntegrationPlugin = config.getString("integrations.map.plugin", "dynmap");
        useTownColorsOnMap = config.getBoolean("integrations.map.use_town_colors", true);
        mapTownColor = config.getString("integrations.map.default_town_color", "#4af7c9");
        mapClaimFillOpacity = config.getDouble("integrations.map.claim_fill_opacity", 0.5D);
        mapClaimStrokeOpacity = config.getDouble("integrations.map.claim_stroke_opacity", 0);
        mapClaimStrokeWeight = config.getInt("integrations.map.claim_stroke_weight", 1);
        mapMarkerSetName = config.getString("integrations.map.marker_set_name", "Towns");


        doBungee = config.getBoolean("bungee_options.enable_bungee_mode", false);
        serverID = config.getString("bungee_options.server_id", "server");
        clusterID = config.getInt("bungee_options.cluster_id", 0);

        databaseType = config.getString("data_storage_options.storage_type", "SQLite");

        playerTable = config.getString("data_storage_options.table_names.player_table", "husktowns_players");
        townsTable = config.getString("data_storage_options.table_names.towns_table", "husktowns_towns");
        claimsTable = config.getString("data_storage_options.table_names.claims_table", "husktowns_claims");
        locationsTable = config.getString("data_storage_options.table_names.locations_table", "husktowns_locations");
        bonusesTable = config.getString("data_storage_options.table_names.bonuses_table", "husktowns_bonus");
        plotMembersTable = config.getString("data_storage_options.table_names.plot_members_table", "husktowns_plot_members");
        townFlagsTable = config.getString("data_storage_options.table_names.town_flags_table", "husktowns_flags");

        levelRequirements.addAll(config.getDoubleList("town_levelling.level_deposit_requirements"));
        maxClaims.addAll(config.getIntegerList("town_levelling.level_max_claims"));
        maxMembers.addAll(config.getIntegerList("town_levelling.level_max_members"));

        host = config.getString("data_storage_options.mysql_credentials.host", "localhost");
        port = config.getInt("data_storage_options.mysql_credentials.port", 3306);
        database = config.getString("data_storage_options.mysql_credentials.database", "HuskTowns");
        username = config.getString("data_storage_options.mysql_credentials.username", "root");
        password = config.getString("data_storage_options.mysql_credentials.password", "pa55w0rd");
        connectionParams = config.getString("data_storage_options.mysql_credentials.params", "?autoReconnect=true&useSSL=false");
    }

    private HashSet<Flag> getFlags(FileConfiguration config, String configKeyPath) {
        HashSet<Flag> flags = new HashSet<>();
        flags.add(new ExplosionDamageFlag(config.getBoolean(configKeyPath + "." + ExplosionDamageFlag.FLAG_IDENTIFIER)));
        flags.add(new FireDamageFlag(config.getBoolean(configKeyPath + "." + FireDamageFlag.FLAG_IDENTIFIER)));
        flags.add(new MobGriefingFlag(config.getBoolean(configKeyPath + "." + MobGriefingFlag.FLAG_IDENTIFIER)));
        flags.add(new MonsterSpawningFlag(config.getBoolean(configKeyPath + "." + MonsterSpawningFlag.FLAG_IDENTIFIER)));
        flags.add(new PvpFlag(config.getBoolean(configKeyPath + "." + PvpFlag.FLAG_IDENTIFIER)));
        flags.add(new PublicInteractAccessFlag(config.getBoolean(configKeyPath + "." + PublicInteractAccessFlag.FLAG_IDENTIFIER)));
        flags.add(new PublicContainerAccessFlag(config.getBoolean(configKeyPath + "." + PublicContainerAccessFlag.FLAG_IDENTIFIER)));
        flags.add(new PublicBuildAccessFlag(config.getBoolean(configKeyPath + "." + PublicBuildAccessFlag.FLAG_IDENTIFIER)));
        return flags;
    }

    public String getLanguage() {
        return language;
    }

    public String getServerID() {
        return serverID;
    }

    public int getClusterID() {
        return clusterID;
    }

    public boolean doBungee() {
        return doBungee;
    }

    public long getInviteExpiryTime() {
        return inviteExpiryTime;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public String getPlayerTable() {
        return playerTable;
    }

    public String getTownsTable() {
        return townsTable;
    }

    public String getClaimsTable() {
        return claimsTable;
    }

    public String getLocationsTable() {
        return locationsTable;
    }

    public String getBonusesTable() {
        return bonusesTable;
    }

    public String getPlotMembersTable() {
        return plotMembersTable;
    }

    public String getTownFlagsTable() { return townFlagsTable; }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getConnectionParams() {
        return connectionParams;
    }

    public ArrayList<Double> getLevelRequirements() {
        return levelRequirements;
    }

    public ArrayList<Integer> getMaxClaims() {
        return maxClaims;
    }

    public ArrayList<Integer> getMaxMembers() {
        return maxMembers;
    }

    public ArrayList<String> getUnClaimableWorlds() {
        return unClaimableWorlds;
    }

    public boolean doEconomy() {
        return doEconomy;
    }

    public void setDoEconomy(boolean doEconomy) {
        this.doEconomy = doEconomy;
    }

    public double getDepositNotificationThreshold() {
        return depositNotificationThreshold;
    }

    public double getTownCreationCost() {
        return townCreationCost;
    }

    public double getGreetingCost() {
        return greetingCost;
    }

    public double getFarewellCost() {
        return farewellCost;
    }

    public double getUpdateBioCost() { return updateBioCost; }

    public double getRenameCost() {
        return renameCost;
    }

    public boolean doLuckPerms() { return doLuckPerms; }

    public boolean doHuskHomes() {
        return doHuskHomes;
    }

    public void setHuskHomes(boolean doHuskHomes) {
        this.doHuskHomes = doHuskHomes;
    }

    public boolean disableHuskHomesSetHomeInOtherTown() {
        return disableHuskHomesSetHomeInOtherTown;
    }

    public double getSetSpawnCost() {
        return setSpawnCost;
    }

    public boolean doMapIntegration() {
        return doMapIntegration;
    }

    public void setDoMapIntegration(boolean doMapIntegration) {
        this.doMapIntegration = doMapIntegration;
    }

    public String getMapIntegrationPlugin() {
        return mapIntegrationPlugin;
    }

    public String getMapMarkerSetName() {
        return mapMarkerSetName;
    }

    public boolean useTownColorsOnMap() {
        return useTownColorsOnMap;
    }

    public String getMapTownColor() {
        return mapTownColor;
    }

    public int getTeleportWarmup() {
        return teleportWarmup;
    }

    public Sound getTeleportWarmupSound() {
        return teleportWarmupSound;
    }

    public Sound getTeleportCompleteSound() {
        return teleportCompleteSound;
    }

    public Sound getTeleportCancelSound() {
        return teleportCancelSound;
    }

    public double getMapFillOpacity() {
        return mapClaimFillOpacity;
    }

    public double getMapStrokeOpacity() {
        return mapClaimStrokeOpacity;
    }

    public int getMapStrokeWeight() {
        return mapClaimStrokeWeight;
    }

    public Material getInspectionTool() {
        return inspectionTool;
    }

    public boolean setTownSpawnInFirstClaim() {
        return setTownSpawnInFirstClaim;
    }

    public String getAdminTownName() {
        return adminTownName;
    }

    public String getAdminTownColor() {
        return adminTownColor;
    }

    public ArrayList<String> getProhibitedTownNames() {
        return prohibitedTownNames;
    }

    public boolean doTownChat() {
        return doTownChat;
    }

    public boolean doToggleableTownChat() {
        return doToggleableTownChat;
    }

    public boolean hideCommandsFromHelpMenuWithoutPermission() {
        return hideCommandsFromHelpMenuWithoutPermission;
    }

    public boolean hideHuskTownsCommandFromHelpMenu() {
        return hideHuskTownsCommandFromHelpMenu;
    }

    public boolean allowKillingHostilesEverywhere() {
        return allowKillingHostilesEverywhere;
    }

    public boolean isFallbackOnDatabaseIfCacheFailed() {
        return fallbackOnDatabaseIfCacheFailed;
    }

    public int getTownMapSquareRadius() {
        return townMapSquareRadius;
    }

    public double getMakeSpawnPublicCost() {
        return makeSpawnPublicCost;
    }

    public boolean doBlockPvpFriendlyFire() { return blockPvpFriendlyFire; }

    public boolean logCacheLoading() { return logCacheLoading; }

    public HashSet<Flag> getWildernessFlags() {
        return wildernessFlags;
    }

    public HashSet<Flag> getUnClaimableWorldFlags() {
        return unClaimableWorldFlags;
    }

    public HashMap<ClaimedChunk.ChunkType,HashSet<Flag>> getDefaultClaimFlags() {
        return defaultClaimFlags;
    }

    public HashMap<ClaimedChunk.ChunkType,HashSet<Flag>> getAdminClaimFlags() {
        return adminClaimFlags;
    }

}