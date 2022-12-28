package net.william278.husktowns.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Fired just before a player creates a town. Cancellable
 */
public class TownCreateEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled;

    private final String townName;

    public TownCreateEvent(Player player, String townName) {
        super(player);
        this.townName = townName;
    }

    public String getTownName() {
        return townName;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}
