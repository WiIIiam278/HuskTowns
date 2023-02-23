package net.william278.husktowns.events;

import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class UnClaimAllEvent extends PlayerEvent implements IUnClaimAllEvent, Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled = false;
    private final Town town;

    public UnClaimAllEvent(@NotNull BukkitUser user, @NotNull Town town) {
        super(user.getPlayer());
        this.town = town;
    }

    @Override
    @NotNull
    public OnlineUser getUser() {
        return BukkitUser.adapt(getPlayer());
    }

    @Override
    @NotNull
    public Town getTown() {
        return town;
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
