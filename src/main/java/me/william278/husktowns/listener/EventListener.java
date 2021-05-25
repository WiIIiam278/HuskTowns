package me.william278.husktowns.listener;

import de.themoep.minedown.MineDown;
import de.themoep.minedown.MineDownParser;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.cache.ClaimCache;
import me.william278.husktowns.object.cache.PlayerCache;
import me.william278.husktowns.object.chunk.ChunkType;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.object.town.Town;
import me.william278.husktowns.object.town.TownRole;
import me.william278.husktowns.util.AutoClaimUtil;
import me.william278.husktowns.util.ClaimViewerUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

public class EventListener implements Listener {

    private static final double MAX_RAYTRACE_DISTANCE = 60D;

    // Returns whether or not to cancel an action based on claim properties
    public static boolean cancelAction(Player player, Location location, boolean sendMessage) {
        ClaimCache claimCache = HuskTowns.getClaimCache();
        PlayerCache playerCache = HuskTowns.getPlayerCache();

        if (claimCache.isUpdating() && claimCache.getAllChunks().isEmpty()) {
            MessageManager.sendMessage(player, "error_cache_updating");
            return true;
        }

        ClaimedChunk chunk = claimCache.getChunkAt(location.getChunk().getX(), location.getChunk().getZ(), location.getWorld().getName());

        if (chunk != null) {
            if (!playerCache.isPlayerInTown(player.getUniqueId())) {
                if (sendMessage) {
                    MessageManager.sendMessage(player, "error_claimed_by", chunk.getTown());
                }
                return true;
            }
            if (chunk.getTown().equals(HuskTowns.getSettings().getAdminTownName())) {
                return !player.hasPermission("husktowns.administrator");
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
        if (claimCache.isUpdating() && claimCache.getAllChunks().isEmpty()) {
            return true;
        }

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

    // Blocks PvP dependant on plugin settings
    private static boolean cancelPvp(Player combatant, Player defendant) {
        World combatantWorld = combatant.getWorld();
        if (HuskTowns.getSettings().blockPvpInUnClaimableWorlds()) {
            for (String unClaimableWorld : HuskTowns.getSettings().getUnClaimableWorlds()) {
                if (combatantWorld.getName().equals(unClaimableWorld)) {
                    MessageManager.sendMessage(combatant, "cannot_pvp_here");
                    return true;
                }
            }
        }
        ClaimCache claimCache = HuskTowns.getClaimCache();
        if (claimCache.isUpdating() && claimCache.getAllChunks().isEmpty()) {
            MessageManager.sendMessage(combatant, "error_cache_updating");
            return true;
        }
        ClaimedChunk combatantChunk = claimCache.getChunkAt(combatant.getLocation().getChunk().getX(), combatant.getLocation().getChunk().getZ(), combatant.getLocation().getChunk().getWorld().getName());
        ClaimedChunk defendantChunk = claimCache.getChunkAt(defendant.getLocation().getChunk().getX(), defendant.getLocation().getChunk().getZ(), defendant.getLocation().getChunk().getWorld().getName());
        if (HuskTowns.getSettings().blockPvpInClaims()) {
            if (combatantChunk != null || defendantChunk != null) {
                MessageManager.sendMessage(combatant, "cannot_pvp_here");
                return true;
            }
        }
        if (HuskTowns.getSettings().blockPvpOutsideClaims()) {
            if (combatantChunk == null || defendantChunk == null) {
                MessageManager.sendMessage(combatant, "cannot_pvp_here");
                return true;
            }
        }
        if (HuskTowns.getSettings().blockPvpFriendlyFire()) {
            String combatantTown = HuskTowns.getPlayerCache().getTown(combatant.getUniqueId());
            String defendantTown = HuskTowns.getPlayerCache().getTown(defendant.getUniqueId());
            if (combatantTown != null) {
                if (defendantTown != null) {
                    if (defendantTown.equals(combatantTown)) {
                        MessageManager.sendMessage(combatant, "cannot_pvp_friendly_fire", defendant.getName(), combatantTown);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean sameClaimTown(Location location1, Location location2) {
        ClaimCache claimCache = HuskTowns.getClaimCache();
        if (claimCache.isUpdating() && claimCache.getAllChunks().isEmpty()) {
            return false;
        }

        ClaimedChunk chunk1 = claimCache.getChunkAt(location1.getChunk().getX(),
                location1.getChunk().getZ(), location1.getWorld().getName());
        ClaimedChunk chunk2 = claimCache.getChunkAt(location2.getChunk().getX(),
                location2.getChunk().getZ(), location2.getWorld().getName());

        String chunk1Town = "Wilderness";
        String chunk2Town = "Wilderness";

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

        // Update caches for bungee users if this is the first player to join
        if (Bukkit.getOnlinePlayers().size() == 1 && HuskTowns.getSettings().doBungee()) {
            HuskTowns.getClaimCache().reload();
            HuskTowns.getPlayerCache().reload();
            HuskTowns.getTownMessageCache().reload();
            HuskTowns.getTownBonusesCache().reload();
        }
    }

    private static void sendFarewellMessage(Player player, String farewellMessage, String townName) {
        MessageManager.sendMessage(player, "farewell_message_format", townName, farewellMessage);
    }

    private static void sendGreetingMessage(Player player, String greetingMessage, String townName) {
        ArrayList<BaseComponent> message = new ArrayList<>(Arrays.asList(new MineDown(MessageManager.getRawMessage("greeting_message_format",
                "&" + Town.getTownColorHex(townName) + "&", townName)).toComponent()));
        message.addAll(Arrays.asList(new MineDown(greetingMessage).urlDetection(false).disable(MineDownParser.Option.ADVANCED_FORMATTING).toComponent()));
        BaseComponent[] messageArray = new BaseComponent[message.size()];
        int i = 0;
        for (BaseComponent baseComponent : message) {
            messageArray[i] = baseComponent;
            i++;
        }
        player.spigot().sendMessage(messageArray);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

        // Check when a player changes chunk
        if (!e.getFrom().getChunk().equals(e.getTo().getChunk())) {
            ClaimCache claims = HuskTowns.getClaimCache();
            if (claims.isUpdating()) {
                return;
            }

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
                try {
                    MessageManager.sendMessage(e.getPlayer(), "farewell_message_prefix", fromClaimedChunk.getTown(), HuskTowns.getTownMessageCache().getFarewellMessage(fromClaimedChunk.getTown()));
                } catch (NullPointerException ignored) {
                }

                AutoClaimUtil.autoClaim(e.getPlayer());
                return;
            }

            // When the player goes from wilderness to a town
            if (fromClaimedChunk == null) {
                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new MineDown("&"
                        + Town.getTownColorHex(toClaimedChunk.getTown()) + "&" + toClaimedChunk.getTown()).toComponent());
                try {
                    MessageManager.sendMessage(e.getPlayer(), "greeting_message_prefix", toClaimedChunk.getTown(), HuskTowns.getTownMessageCache().getGreetingMessage(toClaimedChunk.getTown()));
                } catch (NullPointerException ignored) {
                }
                return;
            }

            // When the player goes from one town to another
            if (!toClaimedChunk.getTown().equals(fromClaimedChunk.getTown())) {
                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new MineDown("&"
                        + Town.getTownColorHex(toClaimedChunk.getTown()) + "&" + toClaimedChunk.getTown()).toComponent());
                try {
                    MessageManager.sendMessage(e.getPlayer(), "greeting_message_prefix", toClaimedChunk.getTown(), HuskTowns.getTownMessageCache().getGreetingMessage(toClaimedChunk.getTown()));
                } catch (NullPointerException ignored) {
                }
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
                if (e.getHand() == EquipmentSlot.HAND) {
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
                    Block block = e.getClickedBlock();
                    if (block != null) {
                        if (block.getBlockData() instanceof Openable || block.getState() instanceof InventoryHolder) {
                            e.setCancelled(cancelAction(e.getPlayer(), e.getClickedBlock().getLocation(), true));
                        }
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
        if (e.getHand() == EquipmentSlot.HAND) {
            e.setCancelled(cancelAction(e.getPlayer(), e.getRightClicked().getLocation(), true));
        } else if (e.getHand() == EquipmentSlot.OFF_HAND) {
            e.setCancelled(cancelAction(e.getPlayer(), e.getRightClicked().getLocation(), false));
        }
    }

    // Returns whether or not a block can take damage
    private boolean removeFromExplosion(Location location) {
        World world = location.getWorld();
        ClaimedChunk blockChunk = HuskTowns.getClaimCache().getChunkAt(location.getChunk().getX(),
                location.getChunk().getZ(), location.getChunk().getWorld().getName());
        if (blockChunk != null) {
            if (HuskTowns.getSettings().disableExplosionsInClaims()) {
                return blockChunk.getChunkType() != ChunkType.FARM || !HuskTowns.getSettings().allowExplosionsInFarmChunks();
            } else {
                return true;
            }
        }
        if (HuskTowns.getSettings().getUnClaimableWorlds().contains(world.getName())) {
            switch (HuskTowns.getSettings().getUnClaimableWorldsExplosionRule()) {
                case EVERYWHERE:
                    return false;
                case NOWHERE:
                    return true;
                case ABOVE_SEA_LEVEL:
                    return (location.getBlockY() > world.getSeaLevel());
            }
        } else {
            switch (HuskTowns.getSettings().getClaimableWorldsExplosionRule()) {
                case EVERYWHERE:
                    return false;
                case NOWHERE:
                    return true;
                case ABOVE_SEA_LEVEL:
                    return (location.getBlockY() > world.getSeaLevel());
            }
        }
        return true;
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent e) {
        HashSet<Block> blocksToRemove = new HashSet<>();
        for (Block block : e.blockList()) {
            if (removeFromExplosion(block.getLocation())) {
                blocksToRemove.add(block);
            }
        }
        for (Block block : blocksToRemove) {
            e.blockList().remove(block);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        HashSet<Block> blocksToRemove = new HashSet<>();
        for (Block block : e.blockList()) {
            if (removeFromExplosion(block.getLocation())) {
                blocksToRemove.add(block);
            }
        }
        for (Block block : blocksToRemove) {
            e.blockList().remove(block);
        }
    }

    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            if (e.getEntity() instanceof Player) {
                e.setCancelled(cancelPvp((Player) e.getDamager(), (Player) e.getEntity()));
            } else {
                if (HuskTowns.getSettings().allowKillingHostilesEverywhere()) {
                    if (e.getEntity() instanceof Monster) {
                        return;
                    }
                }
                e.setCancelled(cancelAction((Player) e.getDamager(), e.getEntity().getLocation(), true));
            }
        } else {
            Entity damagedEntity = e.getEntity();
            Entity damagingEntity = e.getDamager();
            if (HuskTowns.getSettings().allowKillingHostilesEverywhere()) {
                if (damagedEntity instanceof Monster) {
                    return;
                }
            }
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
            } else if (e.getDamager() instanceof Explosive) {
                Explosive explosive = (Explosive) e.getDamager();
                if (removeFromExplosion(explosive.getLocation())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerArmorStand(PlayerArmorStandManipulateEvent e) {
        e.setCancelled(cancelAction(e.getPlayer(), e.getRightClicked().getLocation(), true));
    }
}
