package net.william278.husktowns.integrations;

import me.william278.huskhomes2.api.HuskHomesAPI;
import me.william278.huskhomes2.api.events.PlayerRelocateHomeEvent;
import me.william278.huskhomes2.api.events.PlayerSetHomeEvent;
import me.william278.huskhomes2.teleport.points.Home;
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

public class HuskHomesIntegration implements Listener {

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
        huskHomesAPI = me.william278.huskhomes2.api.HuskHomesAPI.getInstance();
        return true;
    }

    public static void queueTeleport(Player player, TeleportationPoint point) {
        huskHomesAPI.teleportPlayer(player, point.toHuskHomes(), true);
    }

    @EventHandler
    public void onPlayerSetHome(PlayerSetHomeEvent e) {
        Location location = e.getHome().getLocation();
        String playerTown = HuskTowns.getPlayerCache().getPlayerTown(e.getPlayer().getUniqueId());
        ClaimedChunk chunk = HuskTowns.getClaimCache().getChunkAt(location.getChunk().getX(),
                location.getChunk().getZ(), e.getHome().getWorldName());

        if (chunk != null) {
            if (playerTown != null) {
                if (!chunk.getTown().equals(playerTown)) {
                    MessageManager.sendMessage(e.getPlayer(), "error_cannot_sethome", chunk.getTown());
                    e.setCancelled(true);
                }
            } else {
                MessageManager.sendMessage(e.getPlayer(), "error_cannot_sethome", chunk.getTown());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void updateHomeLocation(PlayerRelocateHomeEvent e) {
        Home home = e.getHome();
        if (!home.getServer().equals(HuskTowns.getSettings().getServerID()) || Bukkit.getWorld(home.getWorldName()) == null) {
            return;
        }

        Location location = e.getNewTeleportationPoint().getLocation();
        String playerTown = HuskTowns.getPlayerCache().getPlayerTown(e.getPlayer().getUniqueId());
        ClaimedChunk chunk = HuskTowns.getClaimCache().getChunkAt(location.getChunk().getX(),
                location.getChunk().getZ(), e.getNewTeleportationPoint().getWorldName());

        if (chunk != null) {
            if (playerTown != null) {
                if (!chunk.getTown().equals(playerTown)) {
                    MessageManager.sendMessage(e.getPlayer(), "error_cannot_relocate_sethome", chunk.getTown());
                    e.setCancelled(true);
                }
            } else {
                MessageManager.sendMessage(e.getPlayer(), "error_cannot_relocate_sethome", chunk.getTown());
                e.setCancelled(true);
            }
        }
    }

}
