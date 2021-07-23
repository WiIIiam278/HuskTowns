package me.william278.husktowns.listeners;

import de.themoep.minedown.MineDown;
import de.themoep.minedown.MineDownParser;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.commands.TownChatCommand;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.cache.ClaimCache;
import me.william278.husktowns.object.cache.PlayerCache;
import me.william278.husktowns.object.cache.TownMessageCache;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.object.town.Town;
import me.william278.husktowns.util.AutoClaimUtil;
import me.william278.husktowns.util.ClaimViewerUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
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
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.util.RayTraceResult;

import java.util.HashSet;
import java.util.Locale;

public class EventListener implements Listener {

    private static final double MAX_RAYTRACE_DISTANCE = 60D;

    /*
     Returns whether or not to cancel an action based on claim properties
     */
    public static boolean cancelAction(Player player, Location location, boolean sendMessage) {
        ClaimCache claimCache = HuskTowns.getClaimCache();
        PlayerCache playerCache = HuskTowns.getPlayerCache();

        if (!claimCache.hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", claimCache.getName());
            return true;
        }
        if (!playerCache.hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", playerCache.getName());
            return true;
        }

        ClaimedChunk chunk = claimCache.getChunkAt(location.getChunk().getX(), location.getChunk().getZ(), location.getWorld().getName());
        if (chunk != null) {
            switch (chunk.getPlayerAccess(player)) {
                case CANNOT_BUILD_ADMIN_CLAIM:
                case CANNOT_BUILD_DIFFERENT_TOWN:
                case CANNOT_BUILD_NOT_IN_TOWN:
                    if (sendMessage) {
                        MessageManager.sendMessage(player, "error_claimed_by", chunk.getTown());
                    }
                    return true;
                case CANNOT_BUILD_RESIDENT:
                    if (sendMessage) {
                        MessageManager.sendMessage(player, "error_claimed_trusted");
                    }
                    return true;
                case CAN_BUILD_ADMIN_CLAIM_ACCESS:
                    if (sendMessage) {
                        MessageManager.sendActionBar(player, "action_bar_warning_ignoring_claims");
                    }
                    return false;
                default:
                    return false;
            }
        }
        return false;
    }

    private static boolean cancelDamageChunkAction(Chunk damagedChunk, Chunk damagerChunk) {
        ClaimCache claimCache = HuskTowns.getClaimCache();
        if (!claimCache.hasLoaded()) {
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
        if (HuskTowns.ignoreClaimPlayers.contains(combatant.getUniqueId())) {
            return false;
        }

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
        PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (!claimCache.hasLoaded()) {
            MessageManager.sendMessage(combatant, "error_cache_updating", claimCache.getName());
            return true;
        }
        if (!playerCache.hasLoaded()) {
            MessageManager.sendMessage(combatant, "error_cache_updating", playerCache.getName());
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
            final String combatantTown = playerCache.getTown(combatant.getUniqueId());
            final String defendantTown = playerCache.getTown(defendant.getUniqueId());
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
        if (!claimCache.hasLoaded()) {
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

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        // Check when a player changes chunk
        if (!e.getFrom().getChunk().equals(e.getTo().getChunk())) {
            final ClaimCache claimCache = HuskTowns.getClaimCache();
            final TownMessageCache messageCache = HuskTowns.getTownMessageCache();
            if (!claimCache.hasLoaded()) {
                return;
            }
            if (!messageCache.hasLoaded()) {
                return;
            }

            Location toLocation = e.getTo();
            Location fromLocation = e.getFrom();

            ClaimedChunk toClaimedChunk = claimCache.getChunkAt(toLocation.getChunk().getX(),
                    toLocation.getChunk().getZ(), toLocation.getWorld().getName());
            ClaimedChunk fromClaimedChunk = claimCache.getChunkAt(fromLocation.getChunk().getX(),
                    fromLocation.getChunk().getZ(), fromLocation.getWorld().getName());

            // When a player travels through the wilderness
            if (toClaimedChunk == null && fromClaimedChunk == null) {
                AutoClaimUtil.autoClaim(e.getPlayer(), e.getTo());
                return;
            }

            // When a goes from a town to wilderness
            if (toClaimedChunk == null) {
                MessageManager.sendActionBar(e.getPlayer(), "wilderness");
                try {
                    ComponentBuilder builder = new ComponentBuilder();
                    builder.append(new MineDown(MessageManager.getRawMessage("farewell_message_prefix",
                            fromClaimedChunk.getTown())).toComponent());
                    builder.append(new MineDown(messageCache.getFarewellMessage(fromClaimedChunk.getTown()))
                            .disable(MineDownParser.Option.ADVANCED_FORMATTING).toComponent());
                    e.getPlayer().spigot().sendMessage(builder.create());
                } catch (NullPointerException ignored) {
                }

                AutoClaimUtil.autoClaim(e.getPlayer(), e.getTo());
                return;
            }

            // When the player goes from wilderness to a town
            if (fromClaimedChunk == null) {
                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new MineDown("&"
                        + Town.getTownColorHex(toClaimedChunk.getTown()) + "&" + toClaimedChunk.getTown()).toComponent());
                try {
                    ComponentBuilder builder = new ComponentBuilder();
                    builder.append(new MineDown(MessageManager.getRawMessage("greeting_message_prefix",
                            toClaimedChunk.getTown())).toComponent());
                    builder.append(new MineDown(messageCache.getGreetingMessage(toClaimedChunk.getTown()))
                            .disable(MineDownParser.Option.ADVANCED_FORMATTING).toComponent());
                    e.getPlayer().spigot().sendMessage(builder.create());
                } catch (NullPointerException ignored) {
                }
                return;
            }

            // When the player goes from one town to another
            if (!toClaimedChunk.getTown().equals(fromClaimedChunk.getTown())) {
                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new MineDown("&"
                        + Town.getTownColorHex(toClaimedChunk.getTown()) + "&" + toClaimedChunk.getTown()).toComponent());
                try {
                    ComponentBuilder builder = new ComponentBuilder();
                    builder.append(new MineDown(MessageManager.getRawMessage("greeting_message_prefix",
                            toClaimedChunk.getTown())).toComponent());
                    builder.append(new MineDown(messageCache.getGreetingMessage(toClaimedChunk.getTown()))
                            .disable(MineDownParser.Option.ADVANCED_FORMATTING).toComponent());
                    e.getPlayer().spigot().sendMessage(builder.create());
                } catch (NullPointerException ignored) {
                }
            }
        }
    }

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (cancelAction(e.getPlayer(), e.getBlock().getLocation(), true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (cancelAction(e.getPlayer(), e.getBlock().getLocation(), true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerEmptyBucket(PlayerBucketEmptyEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (cancelAction(e.getPlayer(), e.getBlock().getLocation(), true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerFillBucket(PlayerBucketFillEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (cancelAction(e.getPlayer(), e.getBlock().getLocation(), true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (cancelAction(e.getPlayer(), e.getEntity().getLocation(), true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player) {
            if (cancelAction((Player) e.getRemover(), e.getEntity().getLocation(), true)) {
                e.setCancelled(true);
            }
        } else {
            Entity damagedEntity = e.getEntity();
            Entity damagingEntity = e.getRemover();
            if (damagingEntity instanceof Projectile) {
                Projectile damagingProjectile = (Projectile) damagingEntity;
                if (damagingProjectile.getShooter() instanceof Player) {
                    if (cancelAction((Player) damagingProjectile.getShooter(), e.getEntity().getLocation(), true)) {
                        e.setCancelled(true);
                    }
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
                    if (cancelDamageChunkAction(damagedEntityChunk, damagingEntityChunk)) {
                        e.setCancelled(true);
                    }
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
                        if (cancelAction(e.getPlayer(), e.getPlayer().getEyeLocation(), true)) {
                            e.setCancelled(true);
                        }
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
                        if (cancelAction(e.getPlayer(), e.getPlayer().getEyeLocation(), true)) {
                            e.setCancelled(true);
                        }
                    }
                    Block block = e.getClickedBlock();
                    if (block != null) {
                        if (block.getBlockData() instanceof Openable || block.getState() instanceof InventoryHolder) {
                            if (cancelAction(e.getPlayer(), e.getClickedBlock().getLocation(), true)) {
                                e.setCancelled(true);
                            }
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
                            if (cancelAction(e.getPlayer(), e.getClickedBlock().getLocation(), false)) {
                                e.setCancelled(true);
                            }
                            return;
                        case AIR:
                            return;
                        default:
                            if (cancelAction(e.getPlayer(), e.getClickedBlock().getLocation(), true)) {
                                e.setCancelled(true);
                            }
                    }
                }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent e) {
        // Stop fluids from entering claims
        Material material = e.getBlock().getType();
        if (material == Material.LAVA || material == Material.WATER) {
            if (!sameClaimTown(e.getBlock().getLocation(), e.getToBlock().getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (e.getHand() == EquipmentSlot.HAND) {
            if (cancelAction(e.getPlayer(), e.getRightClicked().getLocation(), true)) {
                e.setCancelled(true);
            }
        } else if (e.getHand() == EquipmentSlot.OFF_HAND) {
            if (cancelAction(e.getPlayer(), e.getRightClicked().getLocation(), false)) {
                e.setCancelled(true);
            }
        }
    }

    // Returns whether or not a block can take damage
    private boolean removeFromExplosion(Location location) {
        if (!HuskTowns.getClaimCache().hasLoaded()) {
            return true;
        }
        World world = location.getWorld();
        ClaimedChunk blockChunk = HuskTowns.getClaimCache().getChunkAt(location.getChunk().getX(),
                location.getChunk().getZ(), location.getChunk().getWorld().getName());
        if (blockChunk != null) {
            if (HuskTowns.getSettings().disableExplosionsInClaims()) {
                return blockChunk.getChunkType() != ClaimedChunk.ChunkType.FARM || !HuskTowns.getSettings().allowExplosionsInFarmChunks();
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
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        Block block = e.getBlock();
        switch (e.getEntity().getType()) {
            case WITHER_SKULL:
            case WITHER:
            case ENDER_DRAGON:
            case ENDERMAN:
            case RAVAGER:
            case SNOWMAN:
            case RABBIT:
                if (removeFromExplosion(block.getLocation())) {
                    e.setCancelled(true);
                }
        }
    }

    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getDamager() instanceof Player) {
            if (e.getEntity() instanceof Player) {
                if (cancelPvp((Player) e.getDamager(), (Player) e.getEntity())) {
                    e.setCancelled(true);
                }
            } else {
                if (HuskTowns.getSettings().allowKillingHostilesEverywhere()) {
                    if (e.getEntity() instanceof Monster) {
                        return;
                    }
                }
                if (cancelAction((Player) e.getDamager(), e.getEntity().getLocation(), true)) {
                    e.setCancelled(true);
                }
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
                    if (cancelAction((Player) damagingProjectile.getShooter(), e.getEntity().getLocation(), true)) {
                        e.setCancelled(true);
                    }
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
                    if (cancelDamageChunkAction(damagedEntityChunk, damagingEntityChunk)) {
                        e.setCancelled(true);
                    }
                }
            } else {
                if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
                    if (!(e.getEntity() instanceof Monster)) {
                        if (removeFromExplosion(e.getEntity().getLocation())) {
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerArmorStand(PlayerArmorStandManipulateEvent e) {
        if (cancelAction(e.getPlayer(), e.getRightClicked().getLocation(), true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (HuskTowns.getSettings().doToggleableTownChat()) {
            Player player = e.getPlayer();
            if (HuskTowns.townChatPlayers.contains(player.getUniqueId())) {
                PlayerCache playerCache = HuskTowns.getPlayerCache();
                if (!playerCache.hasLoaded()) {
                    MessageManager.sendMessage(player, "error_cache_updating", playerCache.getName());
                    return;
                }
                String town = playerCache.getTown(player.getUniqueId());
                if (town == null) {
                    HuskTowns.townChatPlayers.remove(player.getUniqueId());
                    return;
                }
                e.setCancelled(true);
                TownChatCommand.sendTownChatMessage(player, town, e.getMessage());
            }
        }
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof Monster) {
            if (HuskTowns.getClaimCache().hasLoaded()) {
                if (HuskTowns.getSettings().disableMobSpawningInAdminClaims()) {
                    final ClaimedChunk chunk = HuskTowns.getClaimCache().getChunkAt(e.getLocation().getChunk().getX(), e.getLocation().getChunk().getZ(), e.getLocation().getWorld().getName());
                    if (chunk != null) {
                        if (chunk.getTown().equalsIgnoreCase(HuskTowns.getSettings().getAdminTownName())) {
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
}
