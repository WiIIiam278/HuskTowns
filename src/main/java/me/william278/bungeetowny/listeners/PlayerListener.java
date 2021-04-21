package me.william278.bungeetowny.listeners;

import me.william278.bungeetowny.HuskTowns;
import me.william278.bungeetowny.MessageManager;
import me.william278.bungeetowny.data.DataManager;
import me.william278.bungeetowny.object.ClaimCache;
import me.william278.bungeetowny.object.PlayerCache;
import me.william278.bungeetowny.object.chunk.ClaimedChunk;
import me.william278.bungeetowny.object.town.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    // Returns whether or not to cancel an action based on claim properties
    public static boolean cancelAction(Player player, Location location) {
        ClaimCache claimCache = HuskTowns.getClaimCache();
        PlayerCache playerCache = HuskTowns.getPlayerCache();

        ClaimedChunk chunk = claimCache.getChunkAt(location.getChunk().getX(), location.getChunk().getZ(), location.getWorld().getName());

        if (chunk != null) {
            if (!playerCache.containsPlayer(player.getUniqueId())) {
                MessageManager.sendMessage(player, "error_claimed_by", chunk.getTown());
                return true;
            }
            if (!chunk.getTown().equals(playerCache.getTown(player.getUniqueId()))) {
                MessageManager.sendMessage(player, "error_claimed_by", chunk.getTown());
                return true;
            } else {
                switch (chunk.getChunkType()) {
                    case REGULAR:
                        if (playerCache.getRole(player.getUniqueId()) == TownRole.RESIDENT) {
                            MessageManager.sendMessage(player, "error_claimed_trusted");
                            return true;
                        }
                        break;
                    case PLOT:
                        if (!chunk.getPlotChunkOwner().equals(player.getUniqueId())) {
                            if (playerCache.getRole(player.getUniqueId()) == TownRole.RESIDENT) {
                                MessageManager.sendMessage(player, "error_plot_claim", Bukkit.getOfflinePlayer(chunk.getPlotChunkOwner()).getName());
                                return true;
                            }
                        }
                        break;
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // Synchronise mySQL player data
        DataManager.updatePlayerData(e.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        HuskTowns.getPlayerCache().removePlayer(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent e) {
        e.setCancelled(cancelAction(e.getPlayer(), e.getBlock().getLocation()));
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent e) {
        e.setCancelled(cancelAction(e.getPlayer(), e.getBlock().getLocation()));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null) {
            if (!e.getClickedBlock().getType().isAir()) {
                e.setCancelled(cancelAction(e.getPlayer(), e.getClickedBlock().getLocation()));
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        e.setCancelled(cancelAction(e.getPlayer(), e.getRightClicked().getLocation()));
    }

    @EventHandler
    public void onPlayerDamageEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            e.setCancelled(cancelAction((Player) e.getDamager(), e.getEntity().getLocation()));
        }
    }

    @EventHandler
    public void onPlayerArmorStand(PlayerArmorStandManipulateEvent e) {
        e.setCancelled(cancelAction(e.getPlayer(), e.getRightClicked().getLocation()));
    }
}
