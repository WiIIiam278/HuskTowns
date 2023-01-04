package net.william278.husktowns.events;

import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class TownDisbandEvent extends PlayerEvent implements ITownDisbandEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean isCancelled = false;
    private final Town town;

    public TownDisbandEvent(@NotNull BukkitUser user, @NotNull Town town) {
        super(user.getPlayer());
        this.town = town;
    }

    @Override
    @NotNull
    public Town getTown() {
        return town;
    }

    /**
     * Get the name of the town being disbanded
     *
     * @return The name of the town being disbanded
     * @deprecated Use {@link #getTown()} and {@link Town#getName()} instead
     */
    @NotNull
    @Deprecated(since = "2.0")
    public String getTownName() {
        return town.getName();
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
