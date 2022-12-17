package net.william278.husktowns.hook;

import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.event.HomeSaveEvent;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.teleport.TimedTeleport;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.MessageManager;
import net.william278.husktowns.chunk.ClaimedChunk;
import net.william278.husktowns.teleport.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Instant;
import java.util.UUID;

public class HuskHomesHook implements Listener {

    private final HuskHomesAPI huskHomes;

    public HuskHomesHook() {
        this.huskHomes = HuskHomesAPI.getInstance();
    }

    public void queueTeleport(Player player, TeleportationPoint point) {
        huskHomes.teleportBuilder(huskHomes.adaptUser(player))
                .setTarget(new Position(point.getX(), point.getY(), point.getZ(),
                        point.getYaw(), point.getPitch(),
                        new World(point.getWorldName(), UUID.randomUUID()),
                        new Server(point.getServer())))
                .toTimedTeleport()
                .thenAccept(TimedTeleport::execute);
    }

    @EventHandler
    public void onPlayerSetHome(HomeSaveEvent e) {
        final Player player = Bukkit.getPlayer(e.getHome().owner.uuid);
        final Location location = huskHomes.getLocation(e.getHome());
        final String playerTown = HuskTowns.getPlayerCache().getPlayerTown(e.getHome().owner.uuid);
        final ClaimedChunk chunk = HuskTowns.getClaimCache().getChunkAt(location.getChunk().getX(),
                location.getChunk().getZ(), e.getHome().world.name);
        final String locale = e.getHome().meta.creationTime.getEpochSecond() - Instant.now().getEpochSecond() < 3
                ? "error_cannot_relocate_sethome" : "error_cannot_sethome";

        if (chunk != null) {
            if (playerTown != null) {
                if (!chunk.getTown().equals(playerTown)) {
                    if (player != null) {
                        MessageManager.sendMessage(player, locale, chunk.getTown());
                    }
                    e.setCancelled(true);
                }
            } else {
                if (player != null) {
                    MessageManager.sendMessage(player, locale, chunk.getTown());
                }
                e.setCancelled(true);
            }
        }
    }

}
