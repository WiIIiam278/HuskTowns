package net.william278.husktowns.events;

import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * {@inheritDoc}
 */
public class PostTownCreateEvent extends PlayerEvent implements IPostTownCreateEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Town town;

    public PostTownCreateEvent(@NotNull BukkitUser user, @NotNull Town town) {
        super(user.getPlayer());
        this.town = town;
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

    @NotNull
    @Override
    public OnlineUser getUser() {
        return BukkitUser.adapt(player);
    }

    @NotNull
    @Override
    public Town getTown() {
        return town;
    }

}
