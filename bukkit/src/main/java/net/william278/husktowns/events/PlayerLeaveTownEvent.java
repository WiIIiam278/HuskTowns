package net.william278.husktowns.events;

import net.william278.husktowns.town.Town;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player exists a town (by moving out of the town's boundaries)
 */
public class PlayerLeaveTownEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled;

    private final String leftTown; // You should make ClaimedChunk#getTown() return a Town object instead of a String
    private final Location fromLocation;
    private final Location toLocation;
    private final PlayerMoveEvent pme;

    public PlayerLeaveTownEvent(@NotNull Player who, @NotNull String leftTown, @NotNull PlayerMoveEvent pme) {
        super(who);

        this.leftTown = leftTown;
        this.fromLocation = pme.getFrom();
        this.toLocation = pme.getTo();
        this.pme = pme;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public String getLeftTown() {
        return leftTown;
    }

    public Location getFromLocation() {
        return fromLocation;
    }

    public Location getToLocation() {
        return toLocation;
    }

    public PlayerMoveEvent getPlayerMoveEvent() {
        return pme;
    }


}
