package net.william278.husktowns.events;

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

public interface IPlayerEnterTownEvent extends UserEvent {

    @NotNull
    TownClaim getEnteredTownClaim();

    @NotNull
    default Town getEntered() {
        return getEnteredTownClaim().town();
    }

    @NotNull
    default Claim getEnteredClaim() {
        return getEnteredTownClaim().claim();
    }

    @NotNull
    Position getFromPosition();

    @NotNull
    Position getToPosition();

}
