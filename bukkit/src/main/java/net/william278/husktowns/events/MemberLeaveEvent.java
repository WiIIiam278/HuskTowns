package net.william278.husktowns.events;

import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * {@inheritDoc}
 */
public class MemberLeaveEvent extends Event implements IMemberLeaveEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled = false;

    private final User user;
    private final Town town;
    private final Role role;
    private final LeaveReason leaveReason;

    public MemberLeaveEvent(@NotNull User user, @NotNull Town town, @NotNull Role role, @NotNull LeaveReason leaveReason) {
        this.user = user;
        this.town = town;
        this.role = role;
        this.leaveReason = leaveReason;
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
    public LeaveReason getLeaveReason() {
        return leaveReason;
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

    @Override
    @NotNull
    public User getUser() {
        return user;
    }

    @NotNull
    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(user.getUuid());
    }
}
