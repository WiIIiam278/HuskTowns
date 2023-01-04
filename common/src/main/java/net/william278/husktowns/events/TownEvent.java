package net.william278.husktowns.events;

import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

public interface TownEvent extends UserEvent {

    @NotNull
    Town getTown();

}
