package net.william278.husktowns.events;

import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

/**
 * An event that involves a {@link OnlineUser}
 */
public interface UserEvent extends Event {

    /**
     * Get the user involved in the event
     * @return the {@link OnlineUser} involved in the event
     */
    @NotNull
    OnlineUser getUser();

}
