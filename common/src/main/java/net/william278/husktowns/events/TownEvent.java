package net.william278.husktowns.events;

import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link UserEvent} that involves a {@link Town}
 */
public interface TownEvent extends UserEvent {

    /**
     * Get the town involved in the event
     *
     * @return the {@link Town} involved in the event
     */
    @NotNull
    Town getTown();

}
