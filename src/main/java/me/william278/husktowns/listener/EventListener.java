package me.william278.husktowns.listener;

import de.themoep.minedown.MineDown;
import de.themoep.minedown.MineDownParser;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.cache.ClaimCache;
import me.william278.husktowns.object.cache.PlayerCache;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.object.town.Town;
import me.william278.husktowns.object.town.TownRole;
import me.william278.husktowns.util.AutoClaimUtil;
import me.william278.husktowns.util.ClaimViewerUtil;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.*;
import org.bukkit.block.Container;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.RayTraceResult;

import java.util.Locale;

public class EventListener implements Listener {

    private static final double MAX_RAYTRACE_DISTANCE = 60D;

    // Returns whether or not to cancel an action based on claim properties
    public static boolean cancelAction(Player player, Location location, boolean sendMessage) {
        ClaimCache claimCache = HuskTowns.getClaimCache();
        PlayerCache playerCache = HuskTowns.getPlayerCache();

        ClaimedChunk chunk = claimCache.getChunkAt(location.getChunk().getX(), location.getChunk().getZ(), location.getWorld().getName());

        if (chunk != null) {
            if (!playerCache.containsPlayer(player.getUniqueId())) {
                if (sendMessage) {
                    MessageManager.sendMessage(player, "error_claimed_by", chunk.getTown());
                }
                return true;
            }
            if (!chunk.getTown().equals(playerCache.getTown(player.getUniqueId()))) {
                if (sendMessage) {
                    MessageManager.sendMessage(player, "error_claimed_by", chunk.getTown());
                }
                return true;
            } else {
                switch (chunk.getChunkType()) {
                    case REGULAR:
                        if (playerCache.getRole(player.getUniqueId()) == TownRole.RESIDENT) {
                            if (sendMessage) {
                                MessageManager.sendMessage(player, "error_claimed_trusted");
                            }
                            return true;
                        }
                        break;
                    case PLOT:
                        if (!chunk.getPlotChunkOwner().equals(player.getUniqueId())) {
                            if (playerCache.getRole(player.getUniqueId()) == TownRole.RESIDENT) {
                                if (sendMessage) {
                                    MessageManager.sendMessage(player, "error_plot_claim",
                                            Bukkit.getOfflinePlayer(chunk.getPlotChunkOwner()).getName());
                                }
                                return true;
                            }
                        }
                        break;
                }
            }
        }
        return false;
    }

    private static boolean cancelDamageChunkAction(Chunk damagedChunk, Chunk damagerChunk) {
        ClaimCache claimCache = HuskTowns.getClaimCache();
        ClaimedChunk damagedClaim = claimCache.getChunkAt(damagedChunk.getX(), damagedChunk.getZ(), damagedChunk.getWorld().getName());
        ClaimedChunk damagerClaim = claimCache.getChunkAt(damagerChunk.getX(), damagerChunk.getZ(), damagerChunk.getWorld().getName());

        if (damagedClaim == null) {
            return damagerClaim != null;
        } else {
            if (damagerClaim == null) {
                return true;
            } else {
                return !damagedClaim.getTown().equals(damagerClaim.getTown());
            }
        }
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

        // Update caches if this is the first player to join
        if (Bukkit.getOnlinePlayers().size() == 1) {
            HuskTowns.getClaimCache().reload();
            HuskTowns.getPlayerCache().reload();
        }

        // Handle teleporting players
        if (HuskTowns.getSettings().doBungee()) {
            DataManager.handleTeleportingPlayers(e.getPlayer());
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

            // When a player travels through the wilderness
            if (toClaimedChunk == null && fromClaimedChunk == null) {
                AutoClaimUtil.autoClaim(e.getPlayer());
                return;
            }

            // When a goes from a town to wilderness
            if (toClaimedChunk == null) {
                MessageManager.sendActionBar(e.getPlayer(), "wilderness");
                e.getPlayer().spigot().sendMessage(new MineDown(HuskTowns.getTownMessageCache()
                        .getFarewellMessage(fromClaimedChunk.getTown()))
                        .urlDetection(false).disable(MineDownParser.Option.ADVANCED_FORMATTING)
                        .toComponent());
                AutoClaimUtil.autoClaim(e.getPlayer());
                return;
            }

            // When the player goes from wilderness to a town
            if (fromClaimedChunk == null) {
                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new MineDown("&"
                        + Town.getTownColorHex(toClaimedChunk.getTown()) + "&" + toClaimedChunk.getTown()).toComponent());
                e.getPlayer().spigot().sendMessage(new MineDown(HuskTowns.getTownMessageCache()
                        .getGreetingMessage(toClaimedChunk.getTown()))
                        .urlDetection(false).disable(MineDownParser.Option.ADVANCED_FORMATTING)
                        .toComponent());
                return;
            }

            // When the player goes from one town to another
            if (!toClaimedChunk.getTown().equals(fromClaimedChunk.getTown())) {
                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new MineDown("&"
                        + Town.getTownColorHex(toClaimedChunk.getTown()) + "&" + toClaimedChunk.getTown()).toComponent());
                e.getPlayer().spigot().sendMessage(new MineDown(HuskTowns.getTownMessageCache()
                        .getGreetingMessage(toClaimedChunk.getTown()))
                        .urlDetection(false).disable(MineDownParser.Option.ADVANCED_FORMATTING)
                        .toComponent());
            }
        }
    }

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent e) {
        e.setCancelled(cancelAction(e.getPlayer(), e.getBlock().getLocation(), true));
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent e) {
        e.setCancelled(cancelAction(e.getPlayer(), e.getBlock().getLocation(), true));
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent e) {
        e.setCancelled(cancelAction(e.getPlayer(), e.getEntity().getLocation(), true));
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player) {
            e.setCancelled(cancelAction((Player) e.getRemover(), e.getEntity().getLocation(), true));
        } else {
            Entity damagedEntity = e.getEntity();
            Entity damagingEntity = e.getRemover();
            if (damagingEntity instanceof Projectile) {
                Projectile damagingProjectile = (Projectile) damagingEntity;
                if (damagingProjectile.getShooter() instanceof Player) {
                    e.setCancelled(cancelAction((Player) damagingProjectile.getShooter(), e.getEntity().getLocation(), true));
                } else {
                    Chunk damagingEntityChunk;
                    if (damagingProjectile.getShooter() instanceof BlockProjectileSource) {
                        BlockProjectileSource dispenser = (BlockProjectileSource) damagingProjectile.getShooter();
                        damagingEntityChunk = dispenser.getBlock().getLocation().getChunk();
                    } else {
                        LivingEntity damagingProjectileShooter = (LivingEntity) damagingProjectile.getShooter();
                        damagingEntityChunk = damagingProjectileShooter.getLocation().getChunk();
                    }
                    Chunk damagedEntityChunk = damagedEntity.getLocation().getChunk();
                    e.setCancelled(cancelDamageChunkAction(damagedEntityChunk, damagingEntityChunk));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        switch (e.getAction()) {
            case RIGHT_CLICK_AIR:
                ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
                if (item.getType() == HuskTowns.getSettings().getInspectionTool()) {
                    RayTraceResult result = e.getPlayer().rayTraceBlocks(MAX_RAYTRACE_DISTANCE, FluidCollisionMode.NEVER);
                    if (result != null) {
                        if (result.getHitBlock() == null) {
                            MessageManager.sendMessage(e.getPlayer(), "inspect_chunk_too_far");
                            return;
                        }
                        Location location = result.getHitBlock().getLocation();
                        e.setCancelled(true);
                        ClaimViewerUtil.inspectChunk(e.getPlayer(), location);
                    } else {
                        MessageManager.sendMessage(e.getPlayer(), "inspect_chunk_too_far");
                    }
                } else if (item.getType().toString().toLowerCase(Locale.ENGLISH).contains("spawn_egg")) {
                    e.setCancelled(cancelAction(e.getPlayer(), e.getPlayer().getEyeLocation(), true));
                }
                return;
            case LEFT_CLICK_BLOCK:
            case LEFT_CLICK_AIR:
                return;
            case RIGHT_CLICK_BLOCK:
                if (e.getHand() == EquipmentSlot.HAND) {
                    if (e.getPlayer().getInventory().getItemInMainHand().getType() == HuskTowns.getSettings().getInspectionTool()) {
                        if (e.getClickedBlock() == null) {
                            return;
                        }
                        e.setCancelled(true);
                        ClaimViewerUtil.inspectChunk(e.getPlayer(), e.getClickedBlock().getLocation());
                        return;
                    } else if (e.getPlayer().getInventory().getItemInMainHand().getType().toString().toLowerCase(Locale.ENGLISH).contains("spawn_egg")) {
                        e.setCancelled(cancelAction(e.getPlayer(), e.getPlayer().getEyeLocation(), true));
                    }
                    if (e.getClickedBlock() instanceof Door || e.getClickedBlock() instanceof Container) {
                        e.setCancelled(cancelAction(e.getPlayer(), e.getClickedBlock().getLocation(), true));
                    }
                }
                return;
            case PHYSICAL:
                if (e.getClickedBlock() != null) {
                    switch (e.getClickedBlock().getType()) {
                        case POLISHED_BLACKSTONE_PRESSURE_PLATE:
                        case ACACIA_PRESSURE_PLATE:
                        case BIRCH_PRESSURE_PLATE:
                        case CRIMSON_PRESSURE_PLATE:
                        case DARK_OAK_PRESSURE_PLATE:
                        case HEAVY_WEIGHTED_PRESSURE_PLATE:
                        case JUNGLE_PRESSURE_PLATE:
                        case LIGHT_WEIGHTED_PRESSURE_PLATE:
                        case OAK_PRESSURE_PLATE:
                        case SPRUCE_PRESSURE_PLATE:
                        case STONE_PRESSURE_PLATE:
                        case WARPED_PRESSURE_PLATE:
                        case TRIPWIRE:
                            e.setCancelled(cancelAction(e.getPlayer(), e.getClickedBlock().getLocation(), false));
                            return;
                        case AIR:
                            return;
                        default:
                            e.setCancelled(cancelAction(e.getPlayer(), e.getClickedBlock().getLocation(), true));
                    }
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
        e.setCancelled(cancelAction(e.getPlayer(), e.getRightClicked().getLocation(), true));
    }

    // todo disable mob griefing in claims and above sea level (and add options)

    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            e.setCancelled(cancelAction((Player) e.getDamager(), e.getEntity().getLocation(), true));
        } else {
            Entity damagedEntity = e.getEntity();
            Entity damagingEntity = e.getDamager();
            if (e.getDamager() instanceof Projectile) {
                Projectile damagingProjectile = (Projectile) damagingEntity;
                if (damagingProjectile.getShooter() instanceof Player) {
                    e.setCancelled(cancelAction((Player) damagingProjectile.getShooter(), e.getEntity().getLocation(), true));
                } else {
                    Chunk damagingEntityChunk;
                    if (damagingProjectile.getShooter() instanceof BlockProjectileSource) {
                        BlockProjectileSource dispenser = (BlockProjectileSource) damagingProjectile.getShooter();
                        damagingEntityChunk = dispenser.getBlock().getLocation().getChunk();
                    } else {
                        LivingEntity damagingProjectileShooter = (LivingEntity) damagingProjectile.getShooter();
                        damagingEntityChunk = damagingProjectileShooter.getLocation().getChunk();
                    }
                    Chunk damagedEntityChunk = damagedEntity.getLocation().getChunk();
                    e.setCancelled(cancelDamageChunkAction(damagedEntityChunk, damagingEntityChunk));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerArmorStand(PlayerArmorStandManipulateEvent e) {
        e.setCancelled(cancelAction(e.getPlayer(), e.getRightClicked().getLocation(), true));
    }
}
