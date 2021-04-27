package me.william278.husktowns.object.util;

import me.william278.husktowns.HuskTowns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

public class UpdateChecker {

    private final String currentVersion;
    private String latestVersion;
    private final HuskTowns plugin;

    public UpdateChecker(HuskTowns huskTowns) {
        this.plugin = huskTowns;
        this.currentVersion = plugin.getDescription().getVersion();

        try {
            // Numbers = Spigot Project ID todo UPDATE WHEN THE SPIGOT PAGE IS LIVE
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=83767");
            URLConnection urlConnection = url.openConnection();
            this.latestVersion = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())).readLine();
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "An IOException occurred when trying to check for updates.");
        }
    }

    public boolean isUpToDate() {
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
