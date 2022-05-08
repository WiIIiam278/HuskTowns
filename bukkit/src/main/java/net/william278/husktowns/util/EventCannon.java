package net.william278.husktowns.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class EventCannon {

    /**
     * Fires an event, returns {@code true} if it is cancelled. Must be executed on main thread
     *
     * @param event The {@link Event} to fire
     * @return {@code true} if the event is cancelled; false otherwise
     */
    public static boolean fireEvent(Event event) {
        Bukkit.getPluginManager().callEvent(event);
        if (event instanceof Cancellable cancellable) {
            return cancellable.isCancelled();
        }
        return false;
    }

}
