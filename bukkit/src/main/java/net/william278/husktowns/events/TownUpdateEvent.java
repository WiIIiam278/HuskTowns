package net.william278.husktowns.events;

import net.william278.husktowns.town.Town;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TownUpdateEvent extends Event implements ITownUpdateEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Town town;

    public TownUpdateEvent(@NotNull Town town) {
        this.town = town;
    }

    @Override
    @NotNull
    public Town getTown() {
        return town;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
