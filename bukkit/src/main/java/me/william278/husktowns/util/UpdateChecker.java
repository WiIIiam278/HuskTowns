package me.william278.husktowns.util;

import me.william278.husktowns.HuskTowns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

public class UpdateChecker {

    private final static int SPIGOT_PROJECT_ID = 92672;

    private final String currentVersion;
    private String latestVersion;
    private final HuskTowns plugin;

    public UpdateChecker(HuskTowns huskTowns) {
        this.plugin = huskTowns;
        this.currentVersion = plugin.getDescription().getVersion();

        try {
            final URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + SPIGOT_PROJECT_ID);
            URLConnection urlConnection = url.openConnection();
            this.latestVersion = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())).readLine();
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check for updates: An IOException occurred.");
            this.latestVersion = "Unknown";
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check for updates: An exception occurred.");
            this.latestVersion = "Unknown";
        }
    }

    public boolean isUpToDate() {
        if (latestVersion.equalsIgnoreCase("Unknown")) {
            return true;
        }
        return latestVersion.equals(currentVersion);
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void logToConsole() {
        if (!isUpToDate()) {
            plugin.getLogger().log(Level.WARNING, "A new version of HuskTowns is available: Version "
                    + latestVersion + " (Currently running: " + currentVersion + ")");
        }
    }
}
