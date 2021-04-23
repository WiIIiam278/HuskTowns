package me.william278.husktowns.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;

public class Settings {

    // Locale options
    public final String language;

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
}
