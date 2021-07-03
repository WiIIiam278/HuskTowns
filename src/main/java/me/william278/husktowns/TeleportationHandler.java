package me.william278.husktowns;

import io.papermc.lib.PaperLib;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.integrations.HuskHomes;
import me.william278.husktowns.object.teleport.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

/**
 * This class handles the teleportation of players, either via HuskHomes or via a native method
 */
public class TeleportationHandler {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    public static void executeTeleport(Player player, TeleportationPoint point) {
        try {
            if (!point.getServer().equals(HuskTowns.getSettings().getServerID())) {
                if (HuskTowns.getSettings().doBungee()) {
                    DataManager.executeTeleportToSpawn(player, point);
                    return;
                }
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                PaperLib.teleportAsync(player, point.getLocation());
                player.playSound(player.getLocation(), HuskTowns.getSettings().getTeleportCompleteSound(), 1, 1);
                MessageManager.sendMessage(player, "teleporting_complete");
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "An exception occurred executing a teleport", e);
        }
    }

    // This converts a negative to a positive double, used in checking if a player has moved
    private static double makePositive(double d) {
        if (d < 0) {
            d = d * -1D;
        }
        return d;
    }

    // This returns if the player has lost health during a timed teleport
    public static boolean hasLostHealth(Player p, double initialTeleportHealth) {
        return p.getHealth() < initialTeleportHealth;
    }

    // This returns if the player has moved during a timed teleport
    public static boolean hasMoved(Player p, Location initialTeleportLocation) {
        Location currentLocation = p.getLocation();
        final double movementThreshold = 0.1;

        double xDiff = makePositive(initialTeleportLocation.getX() - currentLocation.getX());
        double yDiff = makePositive(initialTeleportLocation.getY() - currentLocation.getY());
        double zDiff = makePositive(initialTeleportLocation.getZ() - currentLocation.getZ());
        double totalDiff = xDiff + yDiff + zDiff;

        return totalDiff > movementThreshold;
    }

    private static void queueTeleport(Player player, TeleportationPoint point) {
        if (HuskTowns.getSettings().getTeleportWarmup() == 0 || player.hasPermission("husktowns.bypass_teleport_warmup")) {
            executeTeleport(player, point);
        } else {
            final int[] i = {HuskTowns.getSettings().getTeleportWarmup()+1};
            final Location playerLocation = player.getLocation();
            final double playerHealth = player.getHealth();
            MessageManager.sendMessage(player, "teleportation_warmup_notice",
                    Integer.toString(HuskTowns.getSettings().getTeleportWarmup()));

            new BukkitRunnable() {
                @Override
                public void run() {
                    Player executablePlayer = Bukkit.getPlayer(player.getUniqueId());
                    if (executablePlayer == null) {
                        cancel();
                        return;
                    }
                    if (hasMoved(executablePlayer, playerLocation)) {
                        cancel();
                        executablePlayer.playSound(executablePlayer.getLocation(), HuskTowns.getSettings().getTeleportCancelSound(), 1, 1);
                        MessageManager.sendMessage(player, "teleportation_cancelled_moved");
                        MessageManager.sendActionBar(player, "teleportation_cancelled");
                        return;
                    }
                    if (hasLostHealth(executablePlayer, playerHealth)) {
                        cancel();
                        executablePlayer.playSound(executablePlayer.getLocation(), HuskTowns.getSettings().getTeleportCancelSound(), 1, 1);
                        MessageManager.sendMessage(player, "teleportation_cancelled_damaged");
                        MessageManager.sendActionBar(player, "teleportation_cancelled");
                        return;
                    }
                    i[0] = i[0] -1;
                    if (i[0] == 0) {
                        executeTeleport(executablePlayer, point);
                        cancel();
                        return;
                    }
                    MessageManager.sendActionBar(executablePlayer, "teleporting_in", Integer.toString(i[0]));
                    executablePlayer.playSound(executablePlayer.getLocation(), HuskTowns.getSettings().getTeleportWarmupSound(), 1, 1);
                }
            }.runTaskTimer(plugin, 0, 20L);
        }
    }

    public static void teleportPlayer(Player player, TeleportationPoint point) {
        if (HuskTowns.getSettings().doHuskHomes()) {
            Bukkit.getScheduler().runTask(plugin, () -> HuskHomes.queueTeleport(player, point));
        } else {
            queueTeleport(player, point);
        }
    }

}
