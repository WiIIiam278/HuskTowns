package net.william278.husktowns.events;

import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * {@inheritDoc}
 */
public class TownCreateEvent extends PlayerEvent implements ITownCreateEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean isCancelled = false;
    private String townName;

    public TownCreateEvent(@NotNull BukkitUser user, @NotNull String townName) {
        super(user.getPlayer());
        this.townName = townName;
    }

    @Override
    @NotNull
    public String getTownName() {
        return townName;
    }

    @Override
    public void setTownName(@NotNull String townName) {
        this.townName = townName;
    }

    @Override
    @NotNull
    public OnlineUser getUser() {
        return BukkitUser.adapt(getPlayer());
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
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
