/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
 */

package net.william278.husktowns.listener;

import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
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

    // Handle player interaction with blocks.
    // We must not ignoreCancelled here as clicking air fires this event in a cancelled state.
    @EventHandler
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
                }

                // Check against containers, switches and other block interactions
                final Block block = e.getClickedBlock();
                if (block != null && e.useInteractedBlock() != Event.Result.DENY) {
                    if (getListener().handler().cancelOperation(Operation.of(
                            BukkitUser.adapt(e.getPlayer()),
                            block.getBlockData() instanceof Openable || block.getState() instanceof InventoryHolder ? Operation.Type.CONTAINER_OPEN
                                    : getPlugin().getSpecialTypes().isFarmBlock(block.getType().getKey().toString()) ? Operation.Type.FARM_BLOCK_INTERACT
                                    : block.getBlockData() instanceof Switch ? Operation.Type.REDSTONE_INTERACT
                                    : Operation.Type.BLOCK_INTERACT,
                            getPosition(block.getLocation()),
                            e.getHand() != EquipmentSlot.OFF_HAND
                    ))) {
                        e.setUseInteractedBlock(Event.Result.DENY);
                        if (e.getItem() != null && e.getItem().getType() != Material.AIR) {
                            e.setUseItemInHand(Event.Result.DENY);
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
                        if (getListener().handler().cancelOperation(Operation.of(
                                BukkitUser.adapt(e.getPlayer()),
                                Operation.Type.REDSTONE_INTERACT,
                                getPosition(block.getLocation())
                        ))) {
                            e.setUseInteractedBlock(Event.Result.DENY);
                        }
                        return;
                    }

                    if (getListener().handler().cancelOperation(Operation.of(
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
        if (e.useItemInHand() == Event.Result.DENY) {
            return true;
        }

        final Material item = e.getPlayer().getInventory().getItemInMainHand().getType();
        if (item == Material.matchMaterial(getPlugin().getSettings().getInspectorTool())) {
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DENY);

            final int maxInspectionDistance = getPlugin().getSettings().getMaxInspectionDistance();
            final Block location = e.getPlayer().getTargetBlockExact(maxInspectionDistance, FluidCollisionMode.NEVER);
            if (location != null) {
                final World world = World.of(location.getWorld().getUID(), location.getWorld().getName(),
                        location.getWorld().getEnvironment().name().toLowerCase());
                final Position position = Position.at(location.getX(), location.getY(), location.getZ(), world);
                getListener().onPlayerInspect(BukkitUser.adapt(e.getPlayer()), position);
            }
            return true;
        }

        if (item.getKey().toString().toLowerCase().contains(SPAWN_EGG_NAME)) {
            if (getListener().handler().cancelOperation(Operation.of(
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
        if (e.getRightClicked() instanceof Player) {
            return;
        }
        if (e.getHand() == EquipmentSlot.HAND) {
            if (getListener().handler().cancelOperation(Operation.of(
                    BukkitUser.adapt(e.getPlayer()),
                    Operation.Type.ENTITY_INTERACT,
                    getPosition(e.getRightClicked().getLocation())
            ))) {
                e.setCancelled(true);
            }
        } else if (e.getHand() == EquipmentSlot.OFF_HAND) {
            if (getListener().handler().cancelOperation(Operation.of(
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
        if (getListener().handler().cancelOperation(Operation.of(
                BukkitUser.adapt(e.getPlayer()),
                Operation.Type.ENTITY_INTERACT,
                getPosition(e.getRightClicked().getLocation())
        ))) {
            e.setCancelled(true);
        }
    }

}
