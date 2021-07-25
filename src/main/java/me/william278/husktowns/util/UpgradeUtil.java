package me.william278.husktowns.util;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpgradeUtil {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    public static void checkNeededUpgrades() {
        FileConfiguration config = plugin.getConfig();

        final String configFileVersion = config.getString("config_file_version", "1.4.1");
        final String currentVersion = plugin.getDescription().getVersion();
        if (!configFileVersion.equalsIgnoreCase(currentVersion)) {
            if (configFileVersion.equals("1.4.1")) {
                addTownBioColumn();
            }

            plugin.getConfig().set("config_file_version", currentVersion);
            plugin.saveConfig();
        }
    }

    // Add the data column for town bios, introduced in v1.4.2
    private static void addTownBioColumn() {
        final String defaultTownBio = MessageManager.getRawMessage("default_town_bio");
        try (PreparedStatement statement = HuskTowns.getConnection().prepareStatement(
                "ALTER TABLE " + HuskTowns.getSettings().getTownsTable() + " ADD `bio` varchar(255) NOT NULL DEFAULT ?;")) {
            statement.setString(1, defaultTownBio);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("An SQL exception occurred adding the bio column ");
        }
    }

}
