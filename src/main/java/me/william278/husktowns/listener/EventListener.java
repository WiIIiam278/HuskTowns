package me.william278.husktowns.listener;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.cache.ClaimCache;
import me.william278.husktowns.object.cache.PlayerCache;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.object.town.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;

public class EventListener implements Listener {

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

    private static boolean sameClaimTown(Location location1, Location location2) {
        ClaimCache claimCache = HuskTowns.getClaimCache();

        ClaimedChunk chunk1 = claimCache.getChunkAt(location1.getChunk().getX(), location1.getChunk().getZ(), location1.getWorld().getName());
        ClaimedChunk chunk2 = claimCache.getChunkAt(location2.getChunk().getX(), location2.getChunk().getZ(), location2.getWorld().getName());

        String chunk1Town = "wild";
        String chunk2Town = "wild";

        if (chunk1 != null) {
            chunk1Town = chunk1.getTown();
        }

        if (chunk2 != null) {
            chunk2Town = chunk2.getTown();
        }

        return (chunk1Town.equals(chunk2Town));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // Synchronise mySQL player data
        DataManager.updatePlayerData(e.getPlayer());

        if (Bukkit.getOnlinePlayers().size() == 1) {
            HuskTowns.getClaimCache().reload();
            HuskTowns.getPlayerCache().reload();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        // Check when a player changes chunk
        if (!e.getFrom().getChunk().equals(e.getTo().getChunk())) {
            ClaimCache claims = HuskTowns.getClaimCache();

            Location toLocation = e.getTo();
            Location fromLocation = e.getFrom();

            ClaimedChunk toClaimedChunk = claims.getChunkAt(toLocation.getChunk().getX(),
                    toLocation.getChunk().getZ(), toLocation.getWorld().getName());
            ClaimedChunk fromClaimedChunk = claims.getChunkAt(fromLocation.getChunk().getX(),
                    fromLocation.getChunk().getZ(), fromLocation.getWorld().getName());

            if (toClaimedChunk == null && fromClaimedChunk == null) {
                return;
            }

            if (toClaimedChunk == null && fromClaimedChunk != null) {
                // Display ENTERING WILDERNESS message
                e.getPlayer().sendTitle("", MessageManager.getRawMessage("wilderness"), 10, 70, 20);
                return;
            }

            if (toClaimedChunk != null && fromClaimedChunk == null) {
                e.getPlayer().sendTitle("", toClaimedChunk.getTown(), 10, 70, 20);
                return;
            }

            if (!toClaimedChunk.getTown().equals(fromClaimedChunk.getTown())) {
                // Display ENTERING TOWN message
                e.getPlayer().sendTitle("", toClaimedChunk.getTown(), 10, 70, 20);
            }
        }
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
    public void onBlockFromTo(BlockFromToEvent e) {
        // Stop fluids from entering claims
        Material material = e.getBlock().getType();
        if (material == Material.LAVA || material == Material.WATER) {
            e.setCancelled(!sameClaimTown(
                    e.getBlock().getLocation(), e.getToBlock().getLocation()));
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
