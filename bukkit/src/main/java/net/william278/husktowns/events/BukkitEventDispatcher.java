package net.william278.husktowns.events;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public interface BukkitEventDispatcher extends EventDispatcher {

    @Override
    default <T extends Event> boolean fireIsCancelled(@NotNull T event) {
        Bukkit.getPluginManager().callEvent((org.bukkit.event.Event) event);
        return event instanceof Cancellable cancellable && cancellable.isCancelled();
    }

    @Override
    @NotNull
    default IClaimEvent getClaimEvent(@NotNull OnlineUser user, @NotNull TownClaim claim) {
        return new ClaimEvent((BukkitUser) user, claim);
    }

    @Override
    @NotNull
    default IUnClaimEvent getUnClaimEvent(@NotNull OnlineUser user, @NotNull TownClaim claim) {
        return new UnClaimEvent((BukkitUser) user, claim);
    }

    @Override
    @NotNull
    default IUnClaimAllEvent getUnClaimAllEvent(@NotNull OnlineUser user, @NotNull Town town) {
        return new UnClaimAllEvent((BukkitUser) user, town);
    }

    @Override
    @NotNull
    default ITownCreateEvent getTownCreateEvent(@NotNull OnlineUser user, @NotNull String townName) {
        return new TownCreateEvent((BukkitUser) user, townName);
    }

    @Override
    @NotNull
    default ITownDisbandEvent getTownDisbandEvent(@NotNull OnlineUser user, @NotNull Town town) {
        return new TownDisbandEvent((BukkitUser) user, town);
    }

    @Override
    @NotNull
    default IMemberJoinEvent getMemberJoinEvent(@NotNull OnlineUser user, @NotNull Town town, @NotNull Role role,
                                                @NotNull IMemberJoinEvent.JoinReason joinReason) {
        return new MemberJoinEvent((BukkitUser) user, town, role, joinReason);
    }

    @Override
    @NotNull
    default IMemberLeaveEvent getMemberLeaveEvent(@NotNull User user, @NotNull Town town, @NotNull Role role,
                                                  @NotNull IMemberLeaveEvent.LeaveReason leaveReason) {
        return new MemberLeaveEvent(user, town, role, leaveReason);
    }

    @Override
    @NotNull
    default IMemberRoleChangeEvent getMemberRoleChangeEvent(@NotNull User user, @NotNull Town town, @NotNull Role oldRole, @NotNull Role newRole) {
        return new MemberRoleChangeEvent(user, town, oldRole, newRole);
    }

    @Override
    @NotNull
    default IPlayerEnterTownEvent getPlayerEnterTownEvent(@NotNull OnlineUser user, @NotNull TownClaim claim,
                                                          @NotNull Position fromPosition, @NotNull Position toPosition) {
        return new PlayerEnterTownEvent((BukkitUser) user, claim, fromPosition, toPosition);
    }

    @Override
    @NotNull
    default IPlayerLeaveTownEvent getPlayerLeaveTownEvent(@NotNull OnlineUser user, @NotNull TownClaim claim,
                                                          @NotNull Position fromPosition, @NotNull Position toPosition) {
        return new PlayerLeaveTownEvent((BukkitUser) user, claim, fromPosition, toPosition);
    }

    @NotNull
    BukkitHuskTowns getPlugin();

}
