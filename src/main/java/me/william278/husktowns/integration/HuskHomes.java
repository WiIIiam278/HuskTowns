package me.william278.husktowns.integration;

import me.william278.huskhomes2.api.HuskHomesAPI;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.object.teleport.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HuskHomes {

    private static HuskHomesAPI huskHomesAPI;

    public static boolean initialize() {
        if (!HuskTowns.getSettings().doHuskHomes()) {
            return false;
        }
        Plugin huskHomesPlugin = Bukkit.getPluginManager().getPlugin("HuskHomes");
        if (huskHomesPlugin == null) {
            return false;
        }
        if (!huskHomesPlugin.isEnabled()) {
            return false;
        }
        huskHomesAPI = me.william278.huskhomes2.HuskHomes.getInstance().getAPI();
        return true;
    }

    public static void queueTeleport(Player player, TeleportationPoint point) {
        huskHomesAPI.teleportPlayer(player, point.toHuskHomes(), true);
    }

}
