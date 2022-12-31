package net.william278.husktowns.listener;

import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public interface BukkitInteractListener extends BukkitListener {

    String SPAWN_EGG_NAME = "spawn_egg";

    @EventHandler(ignoreCancelled = true)
    default void onPlayerInteract(@NotNull PlayerInteractEvent e) {
        switch (e.getAction()) {
            case RIGHT_CLICK_AIR -> {
                if (e.getHand() == EquipmentSlot.HAND) {
                    handleRightClick(e);
                }
            }
            case RIGHT_CLICK_BLOCK -> {
                if (e.getHand() == EquipmentSlot.HAND) {
                    if (handleRightClick(e)) {
                        return;
                    }

                    // Check against containers and switches
                    final Block block = e.getClickedBlock();
                    if (block != null && e.useInteractedBlock() != Event.Result.DENY) {
                        if (block.getBlockData() instanceof Openable || block.getState() instanceof InventoryHolder) {
                            if (getHandler().cancelOperation(Operation.of(
                                    BukkitUser.adapt(e.getPlayer()),
                                    Operation.Type.CONTAINER_OPEN,
                                    getPosition(block.getLocation())
                            ))) {
                                e.setUseInteractedBlock(Event.Result.DENY);
                            }
                        } else if (block.getBlockData() instanceof Switch) {
                            if (getHandler().cancelOperation(Operation.of(
                                    BukkitUser.adapt(e.getPlayer()),
                                    Operation.Type.REDSTONE_INTERACT,
                                    getPosition(block.getLocation())
                            ))) {
                                e.setUseInteractedBlock(Event.Result.DENY);
                            }
                        }
                    }
                }
            }
            case PHYSICAL -> {
                if (e.useInteractedBlock() == Event.Result.DENY) {
                    return;
                }
                final Block block = e.getClickedBlock();
                if (block != null && block.getType() != Material.AIR) {
                    if (getPlugin().getSpecialTypes().isPressureSensitiveBlock(block.getType().getKey().toString())) {
                        if (getHandler().cancelOperation(Operation.of(
                                BukkitUser.adapt(e.getPlayer()),
                                Operation.Type.REDSTONE_INTERACT,
                                getPosition(block.getLocation())
                        ))) {
                            e.setUseInteractedBlock(Event.Result.DENY);
                        }
                        return;
                    }

                    if (getHandler().cancelOperation(Operation.of(
                            BukkitUser.adapt(e.getPlayer()),
                            Operation.Type.BLOCK_INTERACT,
                            getPosition(block.getLocation())
                    ))) {
                        e.setUseInteractedBlock(Event.Result.DENY);
                    }
                }
            }
        }
    }

    // Handle inspecting and spawn egg usage
    private boolean handleRightClick(@NotNull PlayerInteractEvent e) {
        final Material item = e.getPlayer().getInventory().getItemInMainHand().getType();
        if (item == Material.matchMaterial(getPlugin().getSettings().inspectorTool)) {
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DENY);

            final int maxInspectionDistance = getPlugin().getSettings().maxInspectionDistance;
            final Block location = e.getPlayer().getTargetBlockExact(maxInspectionDistance, FluidCollisionMode.NEVER);
            if (location != null) {
                final World world = World.of(location.getWorld().getUID(), location.getWorld().getName(),
                        location.getWorld().getEnvironment().name().toLowerCase());
                final Position position = Position.at(location.getX(), location.getY(), location.getZ(), world);
                getHandler().onPlayerInspect(BukkitUser.adapt(e.getPlayer()), position);
            }
            return true;
        }
        if (item.getKey().toString().toLowerCase().contains(SPAWN_EGG_NAME)) {
            if (e.useItemInHand() == Event.Result.DENY) {
                return true;
            }
            if (getHandler().cancelOperation(Operation.of(
                    BukkitUser.adapt(e.getPlayer()),
                    Operation.Type.USE_SPAWN_EGG,
                    getPosition(e.getPlayer().getLocation())
            ))) {
                e.setUseItemInHand(Event.Result.DENY);
            }
            return true;
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    default void onPlayerInteractEntity(@NotNull PlayerInteractEntityEvent e) {
        if (e.getHand() == EquipmentSlot.HAND || e.getHand() == EquipmentSlot.OFF_HAND) {
            if (getHandler().cancelOperation(Operation.of(
                    BukkitUser.adapt(e.getPlayer()),
                    Operation.Type.ENTITY_INTERACT,
                    getPosition(e.getRightClicked().getLocation())
            ))) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    default void onPlayerArmorStand(@NotNull PlayerArmorStandManipulateEvent e) {
        if (getHandler().cancelOperation(Operation.of(
                BukkitUser.adapt(e.getPlayer()),
                Operation.Type.ENTITY_INTERACT,
                getPosition(e.getRightClicked().getLocation())
        ))) {
            e.setCancelled(true);
        }
    }

}
