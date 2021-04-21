package me.william278.bungeetowny.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;

public class Settings {

    // ID of the server
    private final String serverID;

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

        serverID = config.getString("serverID");

        databaseType = config.getString("database.type");

        playerTable = config.getString("database.table_names.player_table");
        townsTable = config.getString("database.table_names.towns_table");
        claimsTable = config.getString("database.table_names.claims_table");
        locationsTable = config.getString("database.table_names.locations_table");

        levelRequirements.addAll(config.getDoubleList("level_deposit_requirements"));
        maxClaims.addAll(config.getIntegerList("level_max_claims"));
        maxMembers.addAll(config.getIntegerList("level_max_members"));

        host = config.getString("database.connection.host");
        port = config.getInt("database.connection.port");
        database = config.getString("database.connection.database");
        username = config.getString("database.connection.username");
        password = config.getString("database.connection.password");
        connectionParams = config.getString("database.connection.params");
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

    public String getServerID() {
        return serverID;
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
