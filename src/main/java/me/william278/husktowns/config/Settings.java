package me.william278.husktowns.config;

import me.william278.husktowns.HuskTowns;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Locale;

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
    private final boolean disableMobSpawningInAdminClaims;
    private final boolean allowPublicAccessToFarmChunks;

    // Help menu options
    private final boolean hideCommandsFromHelpMenuWithoutPermission;
    private final boolean hideHuskTownsCommandFromHelpMenu;

    // PvP Options
    private final boolean blockPvpInClaims;
    private final boolean blockPvpFriendlyFire;
    private final boolean blockPvpOutsideClaims;
    private final boolean blockPvpInUnClaimableWorlds;

    // Explosion damage options
    private final boolean disableExplosionsInClaims;
    private final boolean allowExplosionsInFarmChunks;
    private final ExplosionRule claimableWorldsExplosionRule;
    private final ExplosionRule unClaimableWorldsExplosionRule;

    // Economy integration
    private boolean doEconomy;
    private final double depositNotificationThreshold;
    private final double townCreationCost;
    private final double greetingCost;
    private final double farewellCost;
    private final double updateBioCost;
    private final double setSpawnCost;
    private final double renameCost;

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
        disableMobSpawningInAdminClaims = config.getBoolean("general_options.disable_mob_spawning_in_admin_claims", true);
        allowPublicAccessToFarmChunks = config.getBoolean("general_options.allow_public_access_to_farm_chunks", false);

        hideCommandsFromHelpMenuWithoutPermission = config.getBoolean("general_options.help_menu.hide_commands_without_permission", true);
        hideHuskTownsCommandFromHelpMenu = config.getBoolean("general_options.help_menu.hide_husktowns_command", false);

        disableExplosionsInClaims = config.getBoolean("explosion_damage_options.disable_explosions_in_claims", true);
        allowExplosionsInFarmChunks = config.getBoolean("explosion_damage_options.allow_explosions_in_farm_chunks", true);
        claimableWorldsExplosionRule = ExplosionRule.valueOf(config.getString("explosion_damage_options.claimable_worlds_explosion_rule", "ABOVE_SEA_LEVEL").toUpperCase(Locale.ENGLISH));
        unClaimableWorldsExplosionRule = ExplosionRule.valueOf(config.getString("explosion_damage_options.unclaimable_worlds_explosion_rule", "EVERYWHERE").toUpperCase(Locale.ENGLISH));

        blockPvpInClaims = config.getBoolean("pvp_options.block_pvp_in_claims", true);
        blockPvpFriendlyFire = config.getBoolean("pvp_options.block_friendly_fire", true);
        blockPvpOutsideClaims = config.getBoolean("pvp_options.block_pvp_outside_claims", true);
        blockPvpInUnClaimableWorlds = config.getBoolean("pvp_options.block_pvp_in_unclaimable_worlds", false);

        doEconomy = config.getBoolean("integrations.economy.enabled", true);
        depositNotificationThreshold = config.getDouble("integrations.economy.deposit_notification_threshold", 0.01);
        townCreationCost = config.getDouble("integrations.economy.town_creation_cost", 150D);
        greetingCost = config.getDouble("integrations.economy.welcome_message_cost", 0D);
        farewellCost = config.getDouble("integrations.economy.farewell_message_cost", 0D);
        updateBioCost = config.getDouble("integrations.economy.update_bio_cost", 0D);
        setSpawnCost = config.getDouble("integrations.economy.set_spawn_cost", 50D);
        renameCost = config.getDouble("integrations.economy.town_rename_cost", 100D);

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

    public boolean blockPvpInClaims() {
        return blockPvpInClaims;
    }

    public boolean blockPvpFriendlyFire() {
        return blockPvpFriendlyFire;
    }

    public boolean blockPvpOutsideClaims() {
        return blockPvpOutsideClaims;
    }

    public boolean blockPvpInUnClaimableWorlds() {
        return blockPvpInUnClaimableWorlds;
    }

    public boolean disableExplosionsInClaims() {
        return disableExplosionsInClaims;
    }

    public boolean allowExplosionsInFarmChunks() {
        return allowExplosionsInFarmChunks;
    }

    public ExplosionRule getClaimableWorldsExplosionRule() {
        return claimableWorldsExplosionRule;
    }

    public ExplosionRule getUnClaimableWorldsExplosionRule() {
        return unClaimableWorldsExplosionRule;
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

    public boolean allowPublicAccessToFarmChunks() {
        return allowPublicAccessToFarmChunks;
    }

    public boolean disableMobSpawningInAdminClaims() {
        return disableMobSpawningInAdminClaims;
    }

    public enum ExplosionRule {
        EVERYWHERE,
        NOWHERE,
        ABOVE_SEA_LEVEL
    }
}