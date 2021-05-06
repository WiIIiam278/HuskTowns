package me.william278.husktowns;

import me.william278.husktowns.integration.HuskHomes;
import me.william278.husktowns.object.teleport.TeleportationPoint;
import org.bukkit.entity.Player;

/**
 * This class handles the teleportation of players, either via HuskHomes or via a native method
 */
public class TeleportationHandler {

    private static void queueTeleport(Player player, TeleportationPoint point) {
        //todo native method of teleporting
    }

    public static void teleportPlayer(Player player, TeleportationPoint point) {
        if (HuskTowns.getSettings().doHuskHomes()) {
            HuskHomes.queueTeleport(player, point);
        } else {
            queueTeleport(player, point);
        }
    }

}
