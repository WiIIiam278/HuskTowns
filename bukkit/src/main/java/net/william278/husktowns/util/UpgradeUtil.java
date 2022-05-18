package net.william278.husktowns.util;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.MessageManager;
import net.william278.husktowns.data.DataManager;
import net.william278.husktowns.chunk.ClaimedChunk;
import net.william278.husktowns.flags.Flag;
import net.william278.husktowns.flags.PublicFarmAccessFlag;
import net.william278.husktowns.town.Town;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

public class UpgradeUtil {

    private static final HuskTowns plugin = HuskTowns.getInstance();
    private static boolean isUpgrading = false;

    public static boolean getIsUpgrading() {
        return isUpgrading;
    }

    public static void checkNeededUpgrades() {
        FileConfiguration config = plugin.getConfig();

        final String configFileVersion = config.getString("config_file_version", "1.4.1");
        final String currentVersion = plugin.getDescription().getVersion();
        if (!configFileVersion.equalsIgnoreCase(currentVersion)) {
            switch (configFileVersion) {
                case "1.4.1":
                    isUpgrading = true;
                    addTownBioColumn();
                case "1.4.2":
                case "1.4.3":
                    isUpgrading = true;
                    addTownFlags();
                case "1.5":
                case "1.5.1":
                case "1.5.2":
                case "1.5.3":
                case "1.5.4":
                case "1.5.5":
                case "1.5.6":
                    isUpgrading = true;
                    addFarmFlag();
            }
            if (isUpgrading) {
                isUpgrading = false;
                if (HuskTowns.getTownDataCache() != null) {
                    if (HuskTowns.getTownDataCache().hasLoaded()) {
                        HuskTowns.getTownDataCache().reload();
                    }
                }
                if (HuskTowns.getPlayerCache() != null) {
                    if (HuskTowns.getPlayerCache().hasLoaded()) {
                        HuskTowns.getPlayerCache().reload();
                    }
                }
                if (HuskTowns.getClaimCache() != null) {
                    if (HuskTowns.getClaimCache().hasLoaded()) {
                        HuskTowns.getPlayerCache().reload();
                    }
                }
                if (HuskTowns.getTownBonusesCache() != null) {
                    if (HuskTowns.getTownBonusesCache().hasLoaded()) {
                        HuskTowns.getTownBonusesCache().reload();
                    }
                }
            }
            plugin.getConfig().set("config_file_version", currentVersion);
            plugin.saveConfig();
        }
    }

    // Populate town flags table with default flags for each town, introduced in v1.5
    private static void addTownFlags() {
        final HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> defaultClaimFlags = HuskTowns.getSettings().defaultClaimFlags;
        final HashMap<ClaimedChunk.ChunkType, HashSet<Flag>> adminClaimFlags = HuskTowns.getSettings().adminClaimFlags;
        try (Connection connection = HuskTowns.getConnection()) {
            int rowCount;
            try (PreparedStatement count = connection.prepareStatement("SELECT COUNT(*) AS row_count FROM " + HuskTowns.getSettings().townFlagsTable + ";")) {
                ResultSet set = count.executeQuery();
                set.next();
                rowCount = set.getInt("row_count");
            }
            if (rowCount == 0) {
                final ArrayList<Town> towns = DataManager.getTowns(connection, "name", true);
                for (Town town : towns) {
                    if (town.getName().equalsIgnoreCase(HuskTowns.getSettings().adminTownName)) {
                        DataManager.addTownFlagData(town.getName(), adminClaimFlags, false, connection);
                    } else {
                        DataManager.addTownFlagData(town.getName(), defaultClaimFlags, false, connection);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "An SQL error occurred inserting flag data", e);
        } catch (IllegalStateException e) {
            plugin.getLogger().log(Level.SEVERE, "An IllegalStateException occurred inserting flag data", e);
        }
    }

    // Add the data column for town bios, introduced in v1.4.2
    private static void addTownBioColumn() {
        final String defaultTownBio = MessageManager.getRawMessage("default_town_bio");
        try (Connection connection = HuskTowns.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "ALTER TABLE " + HuskTowns.getSettings().townsTable
                            + " ADD `bio` varchar(255) NOT NULL DEFAULT ?, "
                            + "ADD `is_spawn_public` boolean NOT NULL DEFAULT 0;")) {
                statement.setString(1, defaultTownBio);
                statement.executeUpdate();
                plugin.getLogger().info("Your HuskTowns database has been successfully upgraded to support the features of HuskTowns 1.4.2");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("An SQL exception occurred adding the bio column ");
        }
    }

    // Add the Player Farm Access flag introduced in v1.6
    private static void addFarmFlag() {
        try (Connection connection = HuskTowns.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "ALTER TABLE " + HuskTowns.getSettings().townFlagsTable
                            + " ADD `" + PublicFarmAccessFlag.FLAG_IDENTIFIER + "` boolean NOT NULL DEFAULT 0;")) {
                statement.executeUpdate();
                plugin.getLogger().info("Your HuskTowns database has been successfully upgraded to support the features of HuskTowns 1.6");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("An SQL exception occurred adding the farm access flag column ");
        }
    }

}
