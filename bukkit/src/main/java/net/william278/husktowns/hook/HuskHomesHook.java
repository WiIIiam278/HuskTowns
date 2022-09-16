package net.william278.husktowns.hook;

import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.event.HomeSaveEvent;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.position.World;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.MessageManager;
import net.william278.husktowns.chunk.ClaimedChunk;
import net.william278.husktowns.teleport.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.time.Instant;
import java.util.UUID;

public class HuskHomesHook implements Listener {

    private static HuskHomesAPI huskHomesAPI;
    private static final HuskTowns plugin = HuskTowns.getInstance();

    public static boolean initialize() {
        if (!HuskTowns.getSettings().doHuskHomes) {
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
        huskHomesAPI = HuskHomesAPI.getInstance();
        return true;
    }

    public static void queueTeleport(Player player, TeleportationPoint point) {
        huskHomesAPI.teleportPlayer(huskHomesAPI.adaptUser(player),
                new Position(point.getX(), point.getY(), point.getZ(),
                        point.getYaw(), point.getPitch(),
                        new World(point.getWorldName(), UUID.randomUUID()),
                        new Server(point.getServer())), true);
    }

    @EventHandler
    public void onPlayerSetHome(HomeSaveEvent e) {
        final Player player = Bukkit.getPlayer(e.getHome().owner.uuid);
        final Location location = huskHomesAPI.getLocation(e.getHome());
        final String playerTown = HuskTowns.getPlayerCache().getPlayerTown(e.getHome().owner.uuid);
        final ClaimedChunk chunk = HuskTowns.getClaimCache().getChunkAt(location.getChunk().getX(),
                location.getChunk().getZ(), e.getHome().world.name);
        final String locale = e.getHome().meta.creationTime.getEpochSecond() - Instant.now().getEpochSecond() < 3
                ? "error_cannot_relocate_sethome" : "error_cannot_sethome";

        if (chunk != null) {
            if (playerTown != null) {
                if (!chunk.getTown().equals(playerTown)) {
                    if (player != null) {
                        MessageManager.sendMessage(player, locale, chunk.getTown());
                    }
                    e.setCancelled(true);
                }
            } else {
                if (player != null) {
                    MessageManager.sendMessage(player, locale, chunk.getTown());
                }
                e.setCancelled(true);
            }
        }
    }

}
