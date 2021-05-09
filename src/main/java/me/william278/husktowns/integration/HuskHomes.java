package me.william278.husktowns.integration;

import me.william278.huskhomes2.api.HuskHomesAPI;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.object.teleport.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HuskHomes {

    private static HuskHomesAPI huskHomesAPI;
    private static final HuskTowns plugin = HuskTowns.getInstance();

    public static boolean initialize() {
        if (!HuskTowns.getSettings().doHuskHomes()) {
            return false;
        }
        Plugin huskHomesPlugin = Bukkit.getPluginManager().getPlugin("HuskHomes");
        if (huskHomesPlugin == null) {
            plugin.getConfig().set("integrations.huskhomes.enabled", false);
            plugin.saveConfig();
            return false;
        }
        if (!huskHomesPlugin.isEnabled()) {
            plugin.getConfig().set("integrations.huskhomes.enabled", false);
            plugin.saveConfig();
            return false;
        }
        huskHomesAPI = me.william278.huskhomes2.HuskHomes.getInstance().getAPI();
        return true;
    }

    public static void queueTeleport(Player player, TeleportationPoint point) {
        huskHomesAPI.teleportPlayer(player, point.toHuskHomes(), true);
    }

}
