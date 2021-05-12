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
    private final double setSpawnCost;
    private final double renameCost;

    // HuskHomes integration
    private boolean doHuskHomes;
    private final boolean disableHuskHomesSetHomeInOtherTown;

    // Dynmap integration
    private boolean doDynMap;
    private final boolean useTownColorsOnDynMap;
    private final String defaultTownColor;
    private final double fillOpacity;
    private final double strokeOpacity;
    private final int strokeWeight;

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
        language = config.getString("language");

        inviteExpiryTime = config.getLong("general_options.invite_expiry");
        String inspectionToolString = config.getString("general_options.claim_inspection_tool");
        if (inspectionToolString == null) {
            inspectionToolString = "stick";
            HuskTowns.getInstance().getLogger().warning("No material was specified for the claim inspection tool; defaulting to a stick.");
        }
        inspectionTool = Material.matchMaterial(inspectionToolString);
        if (inspectionTool == null) {
            inspectionTool = Material.STICK;
            HuskTowns.getInstance().getLogger().warning("An invalid material was specified for the claim inspection tool; defaulting to a stick.");
        }
        unClaimableWorlds.addAll(config.getStringList("general_options.unclaimable_worlds"));
        prohibitedTownNames.addAll(config.getStringList("general_options.prohibited_town_names"));
        teleportWarmup = config.getInt("general_options.teleport_warmup_secs");
        teleportWarmupSound = Sound.valueOf(config.getString("general_options.teleport_warmup_sound"));
        teleportCompleteSound = Sound.valueOf(config.getString("general_options.teleport_complete_sound"));
        teleportCancelSound = Sound.valueOf(config.getString("general_options.teleport_cancel_sound"));
        setTownSpawnInFirstClaim = config.getBoolean("general_options.set_town_spawn_in_first_claim");
        adminTownName = config.getString("general_options.admin_town_name");
        adminTownColor = config.getString("general_options.admin_town_color");

        disableExplosionsInClaims = config.getBoolean("explosion_damage_options.disable_explosions_in_claims");
        allowExplosionsInFarmChunks = config.getBoolean("explosion_damage_options.allow_explosions_in_farm_chunks");
        claimableWorldsExplosionRule = ExplosionRule.valueOf(config.getString("explosion_damage_options.claimable_worlds_explosion_rule").toUpperCase(Locale.ENGLISH));
        unClaimableWorldsExplosionRule = ExplosionRule.valueOf(config.getString("explosion_damage_options.unclaimable_worlds_explosion_rule").toUpperCase(Locale.ENGLISH));

        blockPvpInClaims = config.getBoolean("pvp_options.block_pvp_in_claims");
        blockPvpFriendlyFire = config.getBoolean("pvp_options.block_friendly_fire");
        blockPvpOutsideClaims = config.getBoolean("pvp_options.block_pvp_outside_claims");
        blockPvpInUnClaimableWorlds = config.getBoolean("pvp_options.block_pvp_in_unclaimable_worlds");

        doEconomy = config.getBoolean("integrations.economy.enabled");
        depositNotificationThreshold = config.getDouble("integrations.economy.deposit_notification_threshold");
        townCreationCost = config.getDouble("integrations.economy.town_creation_cost");
        greetingCost = config.getDouble("integrations.economy.welcome_message_cost");
        farewellCost = config.getDouble("integrations.economy.farewell_message_cost");
        setSpawnCost = config.getDouble("integrations.economy.set_spawn_cost");
        renameCost = config.getDouble("integrations.economy.town_rename_cost");

        doHuskHomes = config.getBoolean("integrations.huskhomes.enabled");
        disableHuskHomesSetHomeInOtherTown = config.getBoolean("integrations.huskhomes.block_sethome_in_other_towns");

        doDynMap = config.getBoolean("integrations.dynmap.enabled");
        useTownColorsOnDynMap = config.getBoolean("integrations.dynmap.use_town_colors");
        defaultTownColor = config.getString("integrations.dynmap.default_town_color");
        fillOpacity = config.getDouble("integrations.dynmap.claim_fill_opacity");
        strokeOpacity = config.getDouble("integrations.dynmap.claim_stroke_opacity");
        strokeWeight = config.getInt("integrations.dynmap.claim_stroke_weight");

        serverID = config.getString("bungee_options.server_id");
        clusterID = config.getInt("bungee_options.cluster_id");
        doBungee = config.getBoolean("bungee_options.enable_bungee_mode");

        databaseType = config.getString("data_storage_options.storage_type");

        playerTable = config.getString("data_storage_options.table_names.player_table");
        townsTable = config.getString("data_storage_options.table_names.towns_table");
        claimsTable = config.getString("data_storage_options.table_names.claims_table");
        locationsTable = config.getString("data_storage_options.table_names.locations_table");

        levelRequirements.addAll(config.getDoubleList("town_levelling.level_deposit_requirements"));
        maxClaims.addAll(config.getIntegerList("town_levelling.level_max_claims"));
        maxMembers.addAll(config.getIntegerList("town_levelling.level_max_members"));

        host = config.getString("data_storage_options.mysql_credentials.host");
        port = config.getInt("data_storage_options.mysql_credentials.port");
        database = config.getString("data_storage_options.mysql_credentials.database");
        username = config.getString("data_storage_options.mysql_credentials.username");
        password = config.getString("data_storage_options.mysql_credentials.password");
        connectionParams = config.getString("data_storage_options.mysql_credentials.params");
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

    public boolean doDynMap() {
        return doDynMap;
    }

    public void setDoDynMap(boolean doDynMap) {
        this.doDynMap = doDynMap;
    }

    public boolean useTownColorsOnDynMap() {
        return useTownColorsOnDynMap;
    }

    public String getDefaultTownColor() {
        return defaultTownColor;
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

    public double getDynmapFillOpacity() {
        return fillOpacity;
    }

    public double getDynmapStrokeOpacity() {
        return strokeOpacity;
    }

    public int getDynmapStrokeWeight() {
        return strokeWeight;
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
}