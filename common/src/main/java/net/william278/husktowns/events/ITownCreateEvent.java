package net.william278.husktowns.events;

import org.jetbrains.annotations.NotNull;

public interface ITownCreateEvent extends UserEvent {

    @NotNull
    String getTownName();

}
