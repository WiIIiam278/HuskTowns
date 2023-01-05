package net.william278.husktowns.events;

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

/**
 * An event fired when a player claims a chunk for their town
 */
public interface IClaimEvent extends UserEvent {

    /**
     * Get the town-claim mapping that was made
     *
     * @return the {@link TownClaim} that was made
     */
    @NotNull
    TownClaim getTownClaim();

    /**
     * Get the {@link Town} who claimed the chunk
     *
     * @return the {@link Town} who claimed the chunk
     */
    @NotNull
    default Town getTown() {
        return getTownClaim().town();
    }

    /**
     * Get the {@link Claim} that was made
     *
     * @return the {@link Claim} that was made
     */
    @NotNull
    default Claim getClaim() {
        return getTownClaim().claim();
    }

}
