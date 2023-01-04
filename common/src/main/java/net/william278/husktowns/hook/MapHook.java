package net.william278.husktowns.hook;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

public abstract class MapHook extends Hook {

    protected MapHook(@NotNull HuskTowns plugin, @NotNull String name) {
        super(plugin, name);
    }

    public abstract void setClaimMarker(@NotNull TownClaim claim, @NotNull World world);

    public abstract void removeClaimMarker(@NotNull TownClaim claim, @NotNull World world);

    public abstract void setClaimMarkers(@NotNull Town town);

    public abstract void removeClaimMarkers(@NotNull Town town);

    public abstract void clearAllMarkers();

    @NotNull
    protected final String getMarkerSetKey() {
        return plugin.getKey(getName().toLowerCase(), "markers").toString();
    }

}
