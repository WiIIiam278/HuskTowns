package net.william278.husktowns.listener;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class BukkitEventListener extends EventListener implements Listener {

    public BukkitEventListener(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e) {
        super.onPlayerJoin(BukkitUser.adapt(e.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent e) {
        super.onPlayerQuit(BukkitUser.adapt(e.getPlayer()));
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR) {
            final Material item = e.getPlayer().getInventory().getItemInMainHand().getType();
            if (item == Material.matchMaterial(plugin.getSettings().inspectorTool)) {
                e.setCancelled(true);
                final Block location = e.getPlayer().getTargetBlockExact(60, FluidCollisionMode.NEVER);
                if (location != null) {
                    final World world = World.of(location.getWorld().getUID(), location.getWorld().getName(),
                            location.getWorld().getEnvironment().name().toLowerCase());
                    final Position position = Position.at(location.getX(), location.getY(), location.getZ(), world);
                    super.onPlayerInspect(BukkitUser.adapt(e.getPlayer()), position);
                    return;
                }
            }
        }
    }

}
