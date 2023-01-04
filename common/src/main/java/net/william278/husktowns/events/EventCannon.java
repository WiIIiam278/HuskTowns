package net.william278.husktowns.events;

import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Interface for firing plugin API events
 */
public interface EventCannon {

    Optional<? extends IClaimEvent> fireClaimEvent(@NotNull OnlineUser user, @NotNull TownClaim claim);

    Optional<? extends IUnClaimEvent> fireUnClaimEvent(@NotNull OnlineUser user, @NotNull TownClaim claim);

    Optional<? extends IPlayerEnterTownEvent> firePlayerEnterTownEvent(@NotNull OnlineUser user, @NotNull TownClaim claim,
                                                                       @NotNull Position fromPosition, @NotNull Position toPosition);

    Optional<? extends IPlayerLeaveTownEvent> firePlayerLeaveTownEvent(@NotNull OnlineUser user, @NotNull TownClaim claim,
                                                                       @NotNull Position fromPosition, @NotNull Position toPosition);

    Optional<? extends ITownCreateEvent> fireTownCreateEvent(@NotNull OnlineUser user, @NotNull String townName);

    Optional<? extends ITownDisbandEvent> fireTownDisbandEvent(@NotNull OnlineUser user, @NotNull Town town);

}
