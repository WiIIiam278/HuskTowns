package net.william278.husktowns.events;

import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * {@inheritDoc}
 */
public class MemberJoinEvent extends PlayerEvent implements IMemberJoinEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled = false;

    private final Town town;
    private final Role role;
    private final JoinReason joinReason;

    public MemberJoinEvent(@NotNull BukkitUser user, @NotNull Town town, @NotNull Role role, @NotNull JoinReason joinReason) {
        super(user.getPlayer());
        this.town = town;
        this.role = role;
        this.joinReason = joinReason;
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

    @Override
    @NotNull
    public JoinReason getJoinReason() {
        return joinReason;
    }

    @Override
    @NotNull
    public Role getMemberRole() {
        return role;
    }

    @Override
    @NotNull
    public Town getTown() {
        return town;
    }
}
