package net.william278.husktowns.events;

import org.jetbrains.annotations.NotNull;

/**
 * An event fired when a town is created
 */
public interface ITownCreateEvent extends UserEvent {

    /**
     * Get the name of the town to be created
     *
     * @return the name of the town to be created
     */
    @NotNull
    String getTownName();

}
