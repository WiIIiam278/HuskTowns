package net.william278.husktowns.events;

import net.william278.husktowns.chunk.ClaimedChunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Fired just before a player claims land for their town. Cancellable
 */
public class ClaimEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled;

    private final ClaimedChunk claimedChunk;

    public ClaimEvent(Player player, ClaimedChunk chunk) {
        super(player);
        this.claimedChunk = chunk;
    }

    public ClaimedChunk getClaimedChunk() {
        return claimedChunk;
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
