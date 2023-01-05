package net.william278.husktowns.listener;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public interface BukkitListener extends Listener {
    @NotNull
    HuskTowns getPlugin();

    @NotNull
    EventListener getListener();

    @NotNull
    default Position getPosition(@NotNull Location location) {
        final World world = World.of(Objects.requireNonNull(location.getWorld()).getUID(),
                location.getWorld().getName(),
                location.getWorld().getEnvironment().name().toLowerCase());
        return Position.at(location.getX(), location.getY(), location.getZ(), world);
    }

    default Optional<Player> getPlayerSource(@Nullable Entity e) {
        if (e == null) {
            return Optional.empty();
        }
        if (e instanceof Player player) {
            return Optional.of(player);
        }
        if (e instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return Optional.of(player);
        }
        return e.getPassengers().stream()
                .filter(p -> p instanceof Player)
                .map(p -> (Player) p)
                .findFirst();
    }

}
