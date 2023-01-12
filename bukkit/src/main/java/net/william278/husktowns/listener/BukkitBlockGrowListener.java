package net.william278.husktowns.listener;

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Position;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockGrowEvent;
import org.jetbrains.annotations.NotNull;

public interface BukkitBlockGrowListener extends BukkitListener {

    // Boosted crop growth in farms
    @EventHandler(ignoreCancelled = true)
    default void onBlockGrow(@NotNull BlockGrowEvent e) {
        if (!(e.getBlock().getBlockData() instanceof Ageable ageable) || ageable.getAge() >= ageable.getMaximumAge()) {
            return;
        }

        final Position position = getPosition(e.getBlock().getLocation());
        getPlugin().getClaimAt(position).ifPresent(claim -> {
            if (claim.claim().getType() != Claim.Type.FARM) {
                return;
            }

            final double chance = getPlugin().getLevels().getCropGrowthRateBonus(claim.town().getLevel());
            if (doBoostRate(chance)) {
                ageable.setAge(Math.min(ageable.getAge() + 2, ageable.getMaximumAge()));
                e.setCancelled(true);
            }
        });
    }

}
