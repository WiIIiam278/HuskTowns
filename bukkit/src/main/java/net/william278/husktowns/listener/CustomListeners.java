package net.william278.husktowns.listener;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Optional;

public class CustomListeners implements Listener {

    private final BukkitHuskTowns plugin;

    public CustomListeners(BukkitHuskTowns plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onTeleport(PlayerTeleportEvent e) {
        BukkitUser user = BukkitUser.adapt(e.getPlayer());
        final Optional<Town> userTown = plugin.getUserTown(user).map(Member::town);

        Location l = e.getTo();
        World w = World.of(l.getWorld().getUID(), l.getWorld().getName(), l.getWorld().getEnvironment().name());
        Position pos = Position.at(l.getX(), l.getY(), l.getZ(), w);

        TownClaim spawnClaim = plugin.getClaimAt(pos).orElse(null);

        plugin.getUserPreferences(user.getUuid()).ifPresent(spawnPref -> {
            if (spawnPref.isTownFly()) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> user.setFlying(spawnClaim != null && spawnClaim.town() == userTown.orElse(null)), 1L);
            }
        });
    }

}
