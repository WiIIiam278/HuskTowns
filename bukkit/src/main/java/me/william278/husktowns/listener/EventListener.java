package me.william278.husktowns.listener;

import de.themoep.minedown.MineDown;
import de.themoep.minedown.MineDownParser;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.util.AccessManager;
import me.william278.husktowns.commands.TownChatCommand;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.cache.ClaimCache;
import me.william278.husktowns.cache.PlayerCache;
import me.william278.husktowns.cache.TownDataCache;
import me.william278.husktowns.chunk.ClaimedChunk;
import me.william278.husktowns.flags.Flag;
import me.william278.husktowns.town.Town;
import me.william278.husktowns.util.AutoClaimUtil;
import me.william278.husktowns.util.ClaimViewerUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
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
     Returns whether to cancel an action based on the location data and flags
     */
    public static boolean cancelPlayerAction(Player player, Location location, ActionType actionType, boolean sendMessage) {
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
            switch (AccessManager.getPlayerAccess(player, actionType, chunk, true)) {
                case CANNOT_PERFORM_ACTION_ADMIN_CLAIM:
                case CANNOT_PERFORM_ACTION_DIFFERENT_TOWN:
                case CANNOT_PERFORM_ACTION_NOT_IN_TOWN:
                    if (sendMessage) {
                        MessageManager.sendMessage(player, "error_claimed_by", chunk.getTown());
                    }
                    return true;
                case CANNOT_PERFORM_ACTION_RESIDENT:
                    if (sendMessage) {
                        MessageManager.sendMessage(player, "error_claimed_trusted");
                    }
                    return true;
                case CAN_PERFORM_ACTION_IGNORING_CLAIMS:
                    if (sendMessage) {
                        MessageManager.sendActionBar(player, "action_bar_warning_ignoring_claims");
                    }
                    return false;
                default:
                    return false;
            }
        } else {
            return !Flag.isActionAllowed(location, actionType);
        }
    }

    private static boolean cancelDamageChunkAction(Chunk damagedChunk, Chunk damagerChunk) {
        final ClaimCache claimCache = HuskTowns.getClaimCache();
        if (!claimCache.hasLoaded()) {
            return true;
        }

        final ClaimedChunk damagedClaim = claimCache.getChunkAt(damagedChunk.getX(), damagedChunk.getZ(), damagedChunk.getWorld().getName());
        final ClaimedChunk damagerClaim = claimCache.getChunkAt(damagerChunk.getX(), damagerChunk.getZ(), damagerChunk.getWorld().getName());

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
    private static boolean cancelPvpAction(Player combatant, Player defendant, ActionType pvpActionType) {
        if (HuskTowns.ignoreClaimPlayers.contains(combatant.getUniqueId())) {
            return false;
        }
        // Allow players to hurt themselves!
        if (combatant.getUniqueId() == defendant.getUniqueId()) {
            return false;
        }

        final ClaimCache claimCache = HuskTowns.getClaimCache();
        final PlayerCache playerCache = HuskTowns.getPlayerCache();
        if (!claimCache.hasLoaded()) {
            MessageManager.sendMessage(combatant, "error_cache_updating", claimCache.getName());
            return true;
        }
        if (!playerCache.hasLoaded()) {
            MessageManager.sendMessage(combatant, "error_cache_updating", playerCache.getName());
            return true;
        }

        final Location pvpLocation = defendant.getLocation();
        if (!Flag.isActionAllowed(pvpLocation, pvpActionType)) {
            MessageManager.sendMessage(combatant, "cannot_pvp_here");
            return true;
        }

        if (HuskTowns.getSettings().doBlockPvpFriendlyFire()) {
            final String combatantTown = playerCache.getPlayerTown(combatant.getUniqueId());
            final String defendantTown = playerCache.getPlayerTown(defendant.getUniqueId());
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

    private static boolean areChunksInSameTown(Location location1, Location location2) {
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
        // Synchronise mySQL and cached data
        DataManager.updatePlayerData(e.getPlayer());

        // Update caches for bungee users if this is the first player to join
        if (Bukkit.getOnlinePlayers().size() == 1 && HuskTowns.getSettings().doBungee()) {
            HuskTowns.getClaimCache().reload();
            HuskTowns.getPlayerCache().reload();
            HuskTowns.getTownDataCache().reload();
            HuskTowns.getTownBonusesCache().reload();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        // Check when a player changes chunk
        if (!e.getFrom().getChunk().equals(e.getTo().getChunk())) {
            final ClaimCache claimCache = HuskTowns.getClaimCache();
            final TownDataCache messageCache = HuskTowns.getTownDataCache();
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
                if (AutoClaimUtil.isAutoClaiming(player)) {
                    AutoClaimUtil.autoClaim(player, e.getTo());
                }
                return;
            }

            // When a goes from a town to wilderness
            if (toClaimedChunk == null) {
                if (AutoClaimUtil.isAutoClaiming(player)) {
                    AutoClaimUtil.autoClaim(player, e.getTo());
                } else {
                    MessageManager.sendActionBar(player, "wilderness");
                    try {
                        ComponentBuilder builder = new ComponentBuilder();
                        builder.append(new MineDown(MessageManager.getRawMessage("farewell_message_prefix",
                                        fromClaimedChunk.getTown())).toComponent())
                                .append("").reset()
                                .append(new MineDown(messageCache.getFarewellMessage(fromClaimedChunk.getTown()))
                                        .disable(MineDownParser.Option.ADVANCED_FORMATTING).toComponent());
                        player.spigot().sendMessage(builder.create());
                    } catch (NullPointerException ignored) {}
                }
                return;
            }

            // When the player goes from wilderness to a town
            if (fromClaimedChunk == null) {
                sendTownGreetingMessage(player, messageCache, toClaimedChunk);
                return;
            }

            // When the player goes from one town to another
            if (!toClaimedChunk.getTown().equals(fromClaimedChunk.getTown())) {
                sendTownGreetingMessage(player, messageCache, toClaimedChunk);
            }
        }
    }

    // Send a greeting message to a player
    private void sendTownGreetingMessage(Player player, TownDataCache messageCache, ClaimedChunk toClaimedChunk) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new MineDown("&"
                + Town.getTownColorHex(toClaimedChunk.getTown()) + "&" + toClaimedChunk.getTown()).toComponent());
        try {
            ComponentBuilder builder = new ComponentBuilder();
            builder.append(new MineDown(MessageManager.getRawMessage("greeting_message_prefix",
                            toClaimedChunk.getTown())).toComponent())
                    .append("").reset()
                    .append(new MineDown(messageCache.getGreetingMessage(toClaimedChunk.getTown()))
                            .disable(MineDownParser.Option.ADVANCED_FORMATTING).toComponent());

            player.spigot().sendMessage(builder.create());
        } catch (NullPointerException ignored) {
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPlaceBlock(BlockPlaceEvent e) {
        if (cancelPlayerAction(e.getPlayer(), e.getBlock().getLocation(), ActionType.PLACE_BLOCK, true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBreakBlock(BlockBreakEvent e) {
        if (cancelPlayerAction(e.getPlayer(), e.getBlock().getLocation(), ActionType.BREAK_BLOCK, true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerEmptyBucket(PlayerBucketEmptyEvent e) {
        if (cancelPlayerAction(e.getPlayer(), e.getBlock().getLocation(), ActionType.EMPTY_BUCKET, true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFillBucket(PlayerBucketFillEvent e) {
        if (cancelPlayerAction(e.getPlayer(), e.getBlock().getLocation(), ActionType.FILL_BUCKET, true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent e) {
        if (cancelPlayerAction(e.getPlayer(), e.getEntity().getLocation(), ActionType.PLACE_HANGING_ENTITY, true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player) {
            if (cancelPlayerAction((Player) e.getRemover(), e.getEntity().getLocation(), ActionType.BREAK_HANGING_ENTITY, true)) {
                e.setCancelled(true);
            }
        } else {
            Entity damagedEntity = e.getEntity();
            Entity damagingEntity = e.getRemover();
            if (damagingEntity instanceof Projectile damagingProjectile) {
                if (damagingProjectile.getShooter() instanceof Player) {
                    if (cancelPlayerAction((Player) damagingProjectile.getShooter(), e.getEntity().getLocation(), ActionType.BREAK_HANGING_ENTITY_PROJECTILE, true)) {
                        e.setCancelled(true);
                    }
                } else {
                    Chunk damagingEntityChunk;
                    if (damagingProjectile.getShooter() instanceof BlockProjectileSource dispenser) {
                        damagingEntityChunk = dispenser.getBlock().getLocation().getChunk();
                    } else {
                        LivingEntity damagingProjectileShooter = (LivingEntity) damagingProjectile.getShooter();
                        if (!Flag.isActionAllowed(damagedEntity.getLocation(), ActionType.MOB_GRIEF_WORLD)) {
                            e.setCancelled(true);
                            return;
                        }
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
                            e.setUseInteractedBlock(Event.Result.DENY);
                            e.setUseItemInHand(Event.Result.DENY);
                            if (e.getPlayer().isSneaking()) {
                                ClaimViewerUtil.inspectNearbyChunks(e.getPlayer(), location);
                            } else {
                                ClaimViewerUtil.inspectChunk(e.getPlayer(), location);
                            }
                        } else {
                            MessageManager.sendMessage(e.getPlayer(), "inspect_chunk_too_far");
                        }
                    } else if (item.getType().toString().toLowerCase(Locale.ENGLISH).contains("spawn_egg")) {
                        if (e.useItemInHand() == Event.Result.DENY) {
                            return;
                        }
                        if (cancelPlayerAction(e.getPlayer(), e.getPlayer().getEyeLocation(), ActionType.USE_SPAWN_EGG, true)) {
                            e.setUseItemInHand(Event.Result.DENY);
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
                        e.setUseInteractedBlock(Event.Result.DENY);
                        e.setUseItemInHand(Event.Result.DENY);
                        if (e.getPlayer().isSneaking()) {
                            ClaimViewerUtil.inspectNearbyChunks(e.getPlayer(), e.getClickedBlock().getLocation());
                        } else {
                            ClaimViewerUtil.inspectChunk(e.getPlayer(), e.getClickedBlock().getLocation());
                        }
                        return;
                    } else if (e.getPlayer().getInventory().getItemInMainHand().getType().toString().toLowerCase(Locale.ENGLISH).contains("spawn_egg")) {
                        if (e.useItemInHand() == Event.Result.DENY) {
                            return;
                        }
                        if (cancelPlayerAction(e.getPlayer(), e.getPlayer().getEyeLocation(), ActionType.USE_SPAWN_EGG, true)) {
                            e.setUseItemInHand(Event.Result.DENY);
                        }
                    }
                    if (e.useInteractedBlock() == Event.Result.DENY) {
                        return;
                    }
                    Block block = e.getClickedBlock();
                    if (block != null) {
                        if (block.getBlockData() instanceof Openable || block.getState() instanceof InventoryHolder) {
                            if (cancelPlayerAction(e.getPlayer(), e.getClickedBlock().getLocation(), ActionType.OPEN_CONTAINER, true)) {
                                e.setUseInteractedBlock(Event.Result.DENY);
                            }
                        } else if (block.getBlockData() instanceof Switch) {
                            if (cancelPlayerAction(e.getPlayer(), e.getClickedBlock().getLocation(), ActionType.INTERACT_REDSTONE, true)) {
                                e.setUseInteractedBlock(Event.Result.DENY);
                            }
                        }
                    }
                }
                return;
            case PHYSICAL:
                if (e.useInteractedBlock() == Event.Result.DENY) {
                    return;
                }
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
                            if (cancelPlayerAction(e.getPlayer(), e.getClickedBlock().getLocation(), ActionType.INTERACT_REDSTONE, false)) {
                                e.setUseInteractedBlock(Event.Result.DENY);
                            }
                            return;
                        case AIR:
                            return;
                        default:
                            if (cancelPlayerAction(e.getPlayer(), e.getClickedBlock().getLocation(), ActionType.INTERACT_BLOCKS, true)) {
                                e.setUseInteractedBlock(Event.Result.DENY);
                            }
                    }
                }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
        // Stop fluids from entering claims
        Material material = e.getBlock().getType();
        if (material == Material.LAVA || material == Material.WATER) {
            if (!areChunksInSameTown(e.getBlock().getLocation(), e.getToBlock().getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (e.getHand() == EquipmentSlot.HAND) {
            if (cancelPlayerAction(e.getPlayer(), e.getRightClicked().getLocation(), ActionType.ENTITY_INTERACTION, true)) {
                e.setCancelled(true);
            }
        } else if (e.getHand() == EquipmentSlot.OFF_HAND) {
            if (cancelPlayerAction(e.getPlayer(), e.getRightClicked().getLocation(), ActionType.ENTITY_INTERACTION, false)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplosion(BlockExplodeEvent e) {
        HashSet<Block> blocksToRemove = new HashSet<>();
        for (Block block : e.blockList()) {
            if (!Flag.isActionAllowed(block.getLocation(), ActionType.BLOCK_EXPLOSION_DAMAGE)) {
                blocksToRemove.add(block);
            }
        }
        for (Block block : blocksToRemove) {
            e.blockList().remove(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        HashSet<Block> blocksToRemove = new HashSet<>();
        for (Block block : e.blockList()) {
            if (!Flag.isActionAllowed(block.getLocation(), ActionType.MOB_EXPLOSION_DAMAGE)) {
                blocksToRemove.add(block);
            }
        }
        for (Block block : blocksToRemove) {
            e.blockList().remove(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
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
                if (!Flag.isActionAllowed(block.getLocation(), ActionType.MOB_EXPLOSION_DAMAGE)) {
                    e.setCancelled(true);
                }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            if (e.getEntity() instanceof Player) {
                if (cancelPvpAction((Player) e.getDamager(), (Player) e.getEntity(), ActionType.PVP)) {
                    e.setCancelled(true);
                }
            } else {
                if (HuskTowns.getSettings().allowKillingHostilesEverywhere()) {
                    if (e.getEntity() instanceof Monster) {
                        return;
                    }
                }
                if (cancelPlayerAction((Player) e.getDamager(), e.getEntity().getLocation(), ActionType.PVE, true)) {
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
                    if (e.getEntity() instanceof Player) {
                        if (cancelPvpAction((Player) damagingProjectile.getShooter(), (Player) e.getEntity(), ActionType.PVP_PROJECTILE)) {
                            e.setCancelled(true);
                        }
                    } else if (cancelPlayerAction((Player) damagingProjectile.getShooter(), e.getEntity().getLocation(), ActionType.PVE_PROJECTILE, true)) {
                        e.setCancelled(true);
                    }
                } else {
                    Chunk damagingEntityChunk;
                    if (damagingProjectile.getShooter() instanceof BlockProjectileSource dispenser) {
                        damagingEntityChunk = dispenser.getBlock().getLocation().getChunk();
                    } else {
                        LivingEntity damagingProjectileShooter = (LivingEntity) damagingProjectile.getShooter();
                        if (damagedEntity instanceof Monster || damagedEntity instanceof Player) {
                            return;
                        }
                        if (!Flag.isActionAllowed(damagedEntity.getLocation(), ActionType.MOB_GRIEF_WORLD)) {
                            e.setCancelled(true);
                            return;
                        }
                        damagingEntityChunk = damagingProjectileShooter.getLocation().getChunk();
                    }
                    Chunk damagedEntityChunk = damagedEntity.getLocation().getChunk();
                    if (cancelDamageChunkAction(damagedEntityChunk, damagingEntityChunk)) {
                        e.setCancelled(true);
                    }
                }
            } else {
                // Cancel explosion damage to friendly entities
                if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
                    if (!(e.getEntity() instanceof Monster)) {
                        if (!Flag.isActionAllowed(e.getEntity().getLocation(), ActionType.MOB_EXPLOSION_DAMAGE)) {
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerArmorStand(PlayerArmorStandManipulateEvent e) {
        if (cancelPlayerAction(e.getPlayer(), e.getRightClicked().getLocation(), ActionType.ARMOR_STAND_MANIPULATE, true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        if (!Flag.isActionAllowed(e.getBlock().getLocation(), ActionType.FIRE_DAMAGE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireSpread(BlockIgniteEvent e) {
        if (!Flag.isActionAllowed(e.getBlock().getLocation(), ActionType.FIRE_SPREAD)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (HuskTowns.getSettings().doToggleableTownChat()) {
            Player player = e.getPlayer();
            if (HuskTowns.townChatPlayers.contains(player.getUniqueId())) {
                PlayerCache playerCache = HuskTowns.getPlayerCache();
                if (!playerCache.hasLoaded()) {
                    MessageManager.sendMessage(player, "error_cache_updating", playerCache.getName());
                    return;
                }
                String town = playerCache.getPlayerTown(player.getUniqueId());
                if (town == null) {
                    HuskTowns.townChatPlayers.remove(player.getUniqueId());
                    return;
                }
                e.setCancelled(true);
                TownChatCommand.sendTownChatMessage(player, town, e.getMessage());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent e) {
        final Entity entity = e.getEntity();
        if (entity instanceof Monster) {
            if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL || e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                if (!Flag.isActionAllowed(e.getLocation(), ActionType.MONSTER_SPAWN)) {
                    e.setCancelled(true);
                    entity.remove();
                }
            }
        }
    }

}