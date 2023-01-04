package net.william278.husktowns.events;

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

public interface IPlayerLeaveTownEvent extends UserEvent {

    @NotNull
    TownClaim getLeftTownClaim();

    @NotNull
    default Town getLeft() {
        return getLeftTownClaim().town();
    }

    @NotNull
    default Claim getLeftClaim() {
        return getLeftTownClaim().claim();
    }

    @NotNull
    Position getFromPosition();

    @NotNull
    Position getToPosition();

}
