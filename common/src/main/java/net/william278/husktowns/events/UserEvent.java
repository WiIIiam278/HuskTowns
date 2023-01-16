package net.william278.husktowns.events;

import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * An event that involves a {@link User}
 */
public interface UserEvent extends Event {

    /**
     * Get the user involved in the event
     *
     * @return the {@link User} involved in the event
     */
    @NotNull
    User getUser();

}
