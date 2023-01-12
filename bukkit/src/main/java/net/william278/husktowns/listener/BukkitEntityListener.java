package net.william278.husktowns.listener;

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Position;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public interface BukkitEntityListener extends BukkitListener {

    @EventHandler(ignoreCancelled = true)
    default void onBlockExplosion(@NotNull BlockExplodeEvent e) {
        final HashSet<Block> blocksToRemove = new HashSet<>();
        for (Block block : e.blockList()) {
            if (getListener().handler().cancelOperation(Operation.of(
                    Operation.Type.EXPLOSION_DAMAGE_TERRAIN,
                    getPosition(block.getLocation())))) {
                blocksToRemove.add(block);
            }
        }
        for (Block block : blocksToRemove) {
            e.blockList().remove(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    default void onEntityExplode(@NotNull EntityExplodeEvent e) {
        final HashSet<Block> blocksToRemove = new HashSet<>();
        for (Block block : e.blockList()) {
            if (getListener().handler().cancelOperation(Operation.of(
                    Operation.Type.MONSTER_DAMAGE_TERRAIN,
                    getPosition(block.getLocation())))) {
                blocksToRemove.add(block);
            }
        }
        for (Block block : blocksToRemove) {
            e.blockList().remove(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    default void onEntityChangeBlock(@NotNull EntityChangeBlockEvent e) {
        if (getPlugin().getSpecialTypes().isGriefingMob(e.getEntity().getType().getKey().toString())) {
            final Block block = e.getBlock();
            if (getListener().handler().cancelOperation(Operation.of(
                    Operation.Type.MONSTER_DAMAGE_TERRAIN,
                    getPosition(block.getLocation())))) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    default void onMobSpawn(@NotNull CreatureSpawnEvent e) {
        final Entity entity = e.getEntity();
        if (entity instanceof Monster) {
            final CreatureSpawnEvent.SpawnReason reason = e.getSpawnReason();
            if (reason == CreatureSpawnEvent.SpawnReason.NATURAL || reason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                final Position position = getPosition(entity.getLocation());
                if (getListener().handler().cancelOperation(Operation.of(
                        Operation.Type.MONSTER_SPAWN,
                        position))) {
                    e.setCancelled(true);
                    return;
                }

                // Boosted mob spawning in farms
                if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                    getPlugin().getClaimAt(position).ifPresent(claim -> {
                        if (claim.claim().getType() != Claim.Type.FARM) {
                            return;
                        }

                        final double chance = claim.town().getMobSpawnerRate(getPlugin());
                        if (doBoostRate(chance)) {
                            entity.getWorld().spawnEntity(e.getLocation(), e.getEntityType());
                        }
                    });
                }
            }
        }
    }

}
