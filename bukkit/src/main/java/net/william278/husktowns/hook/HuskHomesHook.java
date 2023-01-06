package net.william278.husktowns.hook;

import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.event.HomeSaveEvent;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.teleport.TimedTeleport;
import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.listener.Operation;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.logging.Level;

public class HuskHomesHook extends TeleportationHook implements Listener {
    @Nullable
    private HuskHomesAPI api;

    public HuskHomesHook(@NotNull HuskTowns plugin) {
        super(plugin, "HuskHomes");
    }

    @Override
    public void onEnable() {
        this.api = HuskHomesAPI.getInstance();
        Bukkit.getPluginManager().registerEvents(this, (BukkitHuskTowns) plugin);
        plugin.log(Level.INFO, "Enabled HuskHomes teleportation hook");
    }

    @Override
    public void teleport(@NotNull OnlineUser user, @NotNull Position position, @NotNull String server) {
        getHuskHomes().ifPresent(api -> api.teleportBuilder(api.adaptUser(((BukkitUser) user).getPlayer()))
                .setTarget(new net.william278.huskhomes.position.Position(
                        position.getX(),
                        position.getY(),
                        position.getZ(),
                        position.getYaw(),
                        position.getPitch(),
                        new net.william278.huskhomes.position.World(position.getWorld().getName(), position.getWorld().getUuid()),
                        new Server(server)
                ))
                .toTimedTeleport()
                .thenAccept(TimedTeleport::execute));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSetHome(@NotNull HomeSaveEvent event) {
        final Optional<? extends OnlineUser> user = plugin.getOnlineUsers().stream()
                .filter(online -> online.getUuid().equals(event.getHome().owner.uuid)).findFirst();
        if (user.isEmpty()) {
            return;
        }

        final Position position = Position.at(event.getHome().x, event.getHome().y, event.getHome().z,
                World.of(event.getHome().world.uuid, event.getHome().world.name,
                        event.getHome().world.environment.name().toLowerCase()));
        if (plugin.getOperationHandler().cancelOperation(Operation
                .of(user.get(), Operation.Type.BLOCK_INTERACT, position))) {
            event.setCancelled(true);
        }
    }

    private Optional<HuskHomesAPI> getHuskHomes() {
        return Optional.ofNullable(api);
    }

}
