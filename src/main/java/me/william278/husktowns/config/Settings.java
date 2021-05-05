package me.william278.husktowns.config;

import me.william278.husktowns.HuskTowns;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;

public class Settings {

    // Locale options
    public final String language;

    // General options
    private final long inviteExpiryTime;
    private Material inspectionTool;
    private final ArrayList<String> unclaimableWorlds = new ArrayList<>();

    // Economy integration
    private boolean doEconomy;
    private final double depositNotificationThreshold;
    private final double townCreationCost;
    private final double greetingCost;
    private final double farewellCost;
    private final double setSpawnCost;

    // Dynmap integration
    private boolean doDynmap;
    private final boolean useTownColorsOnDynmap;
    private final String defaultTownColor;
    private final boolean displayTownSpawnMarkersOnDynmap;
    private final String townSpawnMarker;
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
        unclaimableWorlds.addAll(config.getStringList("general_options.unclaimable_worlds"));

        doEconomy = config.getBoolean("integrations.economy.enabled");
        depositNotificationThreshold = config.getDouble("integrations.economy.deposit_notification_threshold");
        townCreationCost = config.getDouble("integrations.economy.town_creation_cost");
        greetingCost = config.getDouble("integrations.economy.welcome_message_cost");
        farewellCost = config.getDouble("integrations.economy.farewell_message_cost");
        setSpawnCost = config.getDouble("integrations.economy.set_spawn_cost");

        doDynmap = config.getBoolean("integrations.dynmap.enabled");
        useTownColorsOnDynmap = config.getBoolean("integrations.dynmap.use_town_colors");
        displayTownSpawnMarkersOnDynmap = config.getBoolean("integrations.dynmap.display_town_spawn_markers");
        defaultTownColor = config.getString("integrations.dynmap.default_town_color");
        townSpawnMarker = config.getString("integrations.dynmap.town_spawn_marker");
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

        levelRequirements.addAll(config.getDoubleList("level_deposit_requirements"));
        maxClaims.addAll(config.getIntegerList("level_max_claims"));
        maxMembers.addAll(config.getIntegerList("level_max_members"));

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

    public ArrayList<String> getUnclaimableWorlds() { return unclaimableWorlds; }

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

    public double getSetSpawnCost() {
        return setSpawnCost;
    }

    public boolean doDynmap() {
        return doDynmap;
    }

    public void setDoDynmap(boolean doDynmap) {
        this.doDynmap = doDynmap;
    }

    public boolean useTownColorsOnDynmap() {
        return useTownColorsOnDynmap;
    }

    public String getDefaultTownColor() {
        return defaultTownColor;
    }

    public boolean displayTownSpawnMarkersOnDynmap() {
        return displayTownSpawnMarkersOnDynmap;
    }

    public String getTownSpawnMarker() {
        return townSpawnMarker;
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
}
