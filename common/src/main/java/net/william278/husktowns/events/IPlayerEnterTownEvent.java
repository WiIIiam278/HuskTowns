package net.william278.husktowns.events;

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

/**
 * An event fired when a walks into a chunk claimed by a town
 */
@SuppressWarnings("unused")
public interface IPlayerEnterTownEvent extends OnlineUserEvent {

    /**
     * Get the town-claim mapping of the claimed chunk the player entered
     *
     * @return the {@link TownClaim} of the claimed chunk the player entered
     */
    @NotNull
    TownClaim getEnteredTownClaim();

    /**
     * Get the {@link Town} who owns the claimed chunk the player entered
     *
     * @return the {@link Town} who owns the claimed chunk the player entered
     */
    @NotNull
    default Town getEntered() {
        return getEnteredTownClaim().town();
    }

    /**
     * Get the actual {@link Claim} that the player entered
     *
     * @return the {@link Claim} that the player entered
     */
    @NotNull
    default Claim getEnteredClaim() {
        return getEnteredTownClaim().claim();
    }

    /**
     * Get the {@link Position} the player entered from
     *
     * @return the {@link Position} the player entered from
     */
    @NotNull
    Position getFromPosition();

    /**
     * Get the new {@link Position} of the player after entering the chunk
     *
     * @return the new {@link Position} of the player after entering the chunk
     */
    @NotNull
    Position getToPosition();

}
