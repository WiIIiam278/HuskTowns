package net.william278.husktowns.events;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Interface for firing plugin API events
 */
public interface EventDispatcher {
    /**
     * Fire an event synchronously, then run a callback asynchronously
     *
     * @param event    The event to fire
     * @param callback The callback to run after the event has been fired
     * @param <T>      The type of event to fire
     */
    default <T extends Event> void fireEvent(@NotNull T event, @Nullable Consumer<T> callback) {
        getPlugin().runSync(() -> {
            if (!fireIsCancelled(event) && callback != null) {
                getPlugin().runAsync(() -> callback.accept(event));
            }
        });
    }

    /**
     * Fire an event on this thread, and return whether the event was cancelled
     *
     * @param event The event to fire
     * @param <T>   The type of event to fire
     * @return Whether the event was cancelled
     */
    <T extends Event> boolean fireIsCancelled(@NotNull T event);

    @NotNull
    IClaimEvent getClaimEvent(@NotNull OnlineUser user, @NotNull TownClaim claim);

    @NotNull
    IUnClaimEvent getUnClaimEvent(@NotNull OnlineUser user, @NotNull TownClaim claim);

    @NotNull
    IUnClaimAllEvent getUnClaimAllEvent(@NotNull OnlineUser user, @NotNull Town town);

    @NotNull
    ITownCreateEvent getTownCreateEvent(@NotNull OnlineUser user, @NotNull String townName);

    @NotNull
    ITownDisbandEvent getTownDisbandEvent(@NotNull OnlineUser user, @NotNull Town town);

    @NotNull
    IMemberJoinEvent getMemberJoinEvent(@NotNull OnlineUser user, @NotNull Town town, @NotNull Role role,
                                        @NotNull IMemberJoinEvent.JoinReason joinReason);

    @NotNull
    IMemberLeaveEvent getMemberLeaveEvent(@NotNull User user, @NotNull Town town, @NotNull Role role,
                                          @NotNull IMemberLeaveEvent.LeaveReason leaveReason);

    @NotNull
    IMemberRoleChangeEvent getMemberRoleChangeEvent(@NotNull User user, @NotNull Town town, @NotNull Role oldRole, @NotNull Role newRole);

    @NotNull
    IPlayerEnterTownEvent getPlayerEnterTownEvent(@NotNull OnlineUser user, @NotNull TownClaim claim,
                                                  @NotNull Position fromPosition, @NotNull Position toPosition);

    @NotNull
    IPlayerLeaveTownEvent getPlayerLeaveTownEvent(@NotNull OnlineUser user, @NotNull TownClaim claim,
                                                  @NotNull Position fromPosition, @NotNull Position toPosition);

    @NotNull
    HuskTowns getPlugin();

}
