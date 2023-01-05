package net.william278.husktowns.events;

/**
 * Base implementation of a HuskTowns event
 *
 * @since 2.0
 */
public interface Event {

    /**
     * Set whether the event should be cancelled
     *
     * @param cancelled Whether the event should be cancelled
     */
    void setCancelled(boolean cancelled);

    /**
     * Get whether the event is cancelled
     *
     * @return Whether the event is cancelled
     */
    boolean isCancelled();

}
