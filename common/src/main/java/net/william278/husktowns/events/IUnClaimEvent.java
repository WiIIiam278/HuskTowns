package net.william278.husktowns.events;

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

/**
 * An event fired when a player removes a claimed chunk from their town
 */
public interface IUnClaimEvent extends UserEvent {

    /**
     * Get the town-claim mapping that was removed
     *
     * @return the {@link TownClaim} that was removed
     */
    @NotNull
    TownClaim getTownClaim();

    /**
     * Get the {@link Town} who unclaimed the chunk
     *
     * @return the {@link Town} who unclaimed the chunk
     */
    @NotNull
    default Town getTown() {
        return getTownClaim().town();
    }

    /**
     * Get the {@link Claim} that was removed
     *
     * @return the {@link Claim} that was removed
     */
    @NotNull
    default Claim getClaim() {
        return getTownClaim().claim();
    }

}
