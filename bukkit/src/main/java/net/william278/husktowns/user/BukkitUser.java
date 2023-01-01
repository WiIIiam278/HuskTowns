package net.william278.husktowns.user;

import io.papermc.lib.PaperLib;
import net.kyori.adventure.audience.Audience;
import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public final class BukkitUser extends OnlineUser {

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
        Bukkit.getScheduler().runTaskLater(plugin,
                () -> player.sendPluginMessage(BukkitHuskTowns.getInstance(), channel, message), 5L);
    }

    @Override
    public void spawnMarkerParticle(@NotNull Position position, @NotNull Color color, int count) {
        player.spawnParticle(Particle.REDSTONE, new Location(player.getWorld(), position.getX(), position.getY() + 1.1d, position.getZ()),
                1, new Particle.DustOptions(org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue()), 1));
    }

    @Override
    @NotNull
    public Audience getAudience() {
        return plugin.getAudiences().player(player);
    }

    @Override
    public void teleportTo(@NotNull Position position) {
        PaperLib.teleportAsync(player, new Location(Bukkit.getWorld(position.getWorld().getUuid()),
                position.getX(), position.getY(), position.getZ(), position.getYaw(), position.getPitch()));
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        //return player.hasPermission(permission); todo debug only
        return true;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

}
