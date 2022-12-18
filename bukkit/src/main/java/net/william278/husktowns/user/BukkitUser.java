package net.william278.husktowns.user;

import net.kyori.adventure.audience.Audience;
import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BukkitUser extends OnlineUser {

    private static final BukkitHuskTowns plugin = BukkitHuskTowns.getInstance();
    private final Player player;

    private BukkitUser(@NotNull Player player) {
        super(player.getUniqueId(), player.getName());
        this.player = player;
    }

    @NotNull
    public static BukkitUser adapt(@NotNull Player player) {
        return new BukkitUser(player);
    }

    @Override
    @NotNull
    public Chunk getChunk() {
        return Chunk.at(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());
    }

    @Override
    @NotNull
    public Position getPosition() {
        return Position.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(),
                getWorld(), player.getLocation().getYaw(), player.getLocation().getPitch());
    }

    @Override
    @NotNull
    public World getWorld() {
        return World.of(player.getWorld().getUID(), player.getWorld().getName(), player.getWorld().getEnvironment().name());
    }

    @Override
    public void sendPluginMessage(@NotNull String channel, byte[] message) {
        player.sendPluginMessage(BukkitHuskTowns.getInstance(), channel, message);
    }

    @Override
    protected Audience getAudience() {
        return plugin.getAudiences().player(player);
    }
}
