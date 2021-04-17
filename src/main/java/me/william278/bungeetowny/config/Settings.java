package me.william278.bungeetowny.config;

import org.bukkit.configuration.file.FileConfiguration;

public class Settings {

    // ID of the server
    private final String serverID;

    public Settings(FileConfiguration config) {
        serverID = config.getString("serverID");
    }

    public String getServerID() {
        return serverID;
    }
}
