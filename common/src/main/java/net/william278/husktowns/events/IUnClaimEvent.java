package net.william278.husktowns.events;

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

public interface IUnClaimEvent extends UserEvent {

    @NotNull
    TownClaim getTownClaim();

    @NotNull
    default Town getTown() {
        return getTownClaim().town();
    }

    @NotNull
    default Claim getClaim() {
        return getTownClaim().claim();
    }

}
