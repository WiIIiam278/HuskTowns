package net.william278.husktowns.events;

import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

/**
 * An event that involves an {@link OnlineUser}
 */
public interface OnlineUserEvent extends UserEvent {

    /**
     * Get the online user involved in the event
     *
     * @return the {@link OnlineUser} involved in the event
     */
    @Override
    @NotNull
    OnlineUser getUser();

}
