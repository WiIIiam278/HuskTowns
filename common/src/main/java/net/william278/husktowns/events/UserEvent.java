package net.william278.husktowns.events;

import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

public interface UserEvent extends Event {

    @NotNull
    OnlineUser getUser();

}
