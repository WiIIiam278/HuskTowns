package net.william278.husktowns.hook;

import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.teleport.TimedTeleport;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.logging.Level;

public class HuskHomesHook extends TeleportationHook {
    @Nullable
    private HuskHomesAPI api;

    public HuskHomesHook(@NotNull HuskTowns plugin) {
        super(plugin, "HuskHomes");
    }

    @Override
    public void onEnable() {
        this.api = HuskHomesAPI.getInstance();
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
                        new World(position.getWorld().getName(), position.getWorld().getUuid()),
                        new Server(server)
                ))
                .toTimedTeleport()
                .thenAccept(TimedTeleport::execute));
    }

    private Optional<HuskHomesAPI> getHuskHomes() {
        return Optional.ofNullable(api);
    }
}
