package me.william278.husktowns.util;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.object.flag.Flag;
import me.william278.husktowns.object.town.Town;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public class UpgradeUtil {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    public static void checkNeededUpgrades() {
        FileConfiguration config = plugin.getConfig();

        final String configFileVersion = config.getString("config_file_version", "1.4.1");
        final String currentVersion = plugin.getDescription().getVersion();
        if (!configFileVersion.equalsIgnoreCase(currentVersion)) {
            switch (configFileVersion) {
                case "1.4.1":
                    addTownBioColumn();
                case "1.4.2":
                case "1.4.3":
                    addTownFlags();
            }
            plugin.getConfig().set("config_file_version", currentVersion);
            plugin.saveConfig();
        }
    }

    // Populate town flags table with default flags for each town, introduced in v1.5
    private static void addTownFlags() {
        final HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> defaultClaimFlags = HuskTowns.getSettings().getDefaultClaimFlags();
        final HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> adminClaimFlags = HuskTowns.getSettings().getAdminClaimFlags();
        Connection connection = HuskTowns.getConnection();
        try {
            int rowCount;
            try(PreparedStatement count = connection.prepareStatement("SELECT COUNT(*) AS row_count FROM " + HuskTowns.getSettings().getTownFlagsTable() + ";")) {
                ResultSet set = count.executeQuery();
                set.next();
                rowCount = set.getInt("row_count");
            }
            if (rowCount == 0) {
                for (Town town : DataManager.getTowns(connection, "name", true)) {
                    if (town.getName().equalsIgnoreCase(HuskTowns.getSettings().getAdminTownName())) {
                        DataManager.addTownFlagData(town.getName(), adminClaimFlags, connection);
                    } else {
                        DataManager.addTownFlagData(town.getName(), defaultClaimFlags, connection);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("An SQL exception occurred adding default town flags");
        }
    }

    // Add the data column for town bios, introduced in v1.4.2
    private static void addTownBioColumn() {
        final String defaultTownBio = MessageManager.getRawMessage("default_town_bio");
        try (PreparedStatement statement = HuskTowns.getConnection().prepareStatement(
                "ALTER TABLE " + HuskTowns.getSettings().getTownsTable()
                        + " ADD `bio` varchar(255) NOT NULL DEFAULT ?, "
                        + "ADD `is_spawn_public` boolean NOT NULL DEFAULT 0;")) {
            statement.setString(1, defaultTownBio);
            statement.executeUpdate();
            plugin.getLogger().info("Your HuskTowns database has been successfully upgraded to support the features of HuskTowns 1.4.2");
        } catch (SQLException e) {
            plugin.getLogger().severe("An SQL exception occurred adding the bio column ");
        }
    }

}
