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
public class MemberRoleChangeEvent extends Event implements IMemberRoleChangeEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled = false;

    private final Town town;
    private final User user;
    private final Role oldRole;
    private final Role newRole;

    public MemberRoleChangeEvent(@NotNull User user, @NotNull Town town, @NotNull Role oldRole, @NotNull Role newRole) {
        super();
        this.user = user;
        this.town = town;
        this.oldRole = oldRole;
        this.newRole = newRole;
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
    public Role getMemberRole() {
        return oldRole;
    }

    @Override
    @NotNull
    public Town getTown() {
        return town;
    }

    @Override
    @NotNull
    public Role getNewRole() {
        return newRole;
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