package net.william278.husktowns.hook;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.town.Town;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class DynmapHook extends MapHook {

    @Nullable
    private DynmapCommonAPI dynmapApi;

    public DynmapHook(@NotNull HuskTowns plugin) {
        super(plugin, "Dynmap");
        DynmapCommonAPIListener.register(new DynmapCommonAPIListener() {
            @Override
            public void apiEnabled(DynmapCommonAPI dynmapCommonAPI) {
                dynmapApi = dynmapCommonAPI;
                if (plugin.isLoaded()) {
                    onEnable();
                }
            }
        });
    }

    @Override
    public void onEnable() {
        getDynmap().ifPresent(api -> {
            clearAllMarkers();
            getMarkerSet();

            for (Town town : plugin.getTowns()) {
                setClaimMarkers(town);
            }
        });
    }

    private void addMarker(@NotNull TownClaim claim, @NotNull World world, @NotNull MarkerSet markerSet) {
        final Chunk chunk = claim.claim().getChunk();

        // Get the corner coordinates
        double[] x = new double[4];
        double[] z = new double[4];
        for (int i = 0; i < 4; i++) {
            x[i] = chunk.getX() * 16 + (i % 2 == 0 ? 0 : 16);
            z[i] = chunk.getZ() * 16 + (i < 2 ? 0 : 16);
        }

        // Define the marker
        final AreaMarker marker = markerSet.createAreaMarker(getClaimMarkerKey(claim, world),
                claim.town().getName(), false, world.getName(), x, z, false);
        double markerY = 64;
        marker.setRangeY(markerY, markerY);

        // Set the fill and stroke colors
        final int color = claim.town().getColor().getRGB();
        marker.setFillStyle(0.5f, color);
        marker.setLineStyle(1, 1, color);
        marker.setLabel(claim.town().getName());
    }

    private void removeMarker(@NotNull TownClaim claim, @NotNull World world) {
        getMarkerSet().ifPresent(markerSet -> markerSet.getMarkers()
                .removeIf(marker -> marker.getMarkerID().equals(getClaimMarkerKey(claim, world))));
    }


    @Override
    public void setClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        plugin.runSync(() -> getMarkerSet().ifPresent(markerSet -> addMarker(claim, world, markerSet)));
    }

    @Override
    public void removeClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        plugin.runSync(() -> getMarkerSet().ifPresent(markerSet -> removeMarker(claim, world)));
    }

    @Override
    public void setClaimMarkers(@NotNull Town town) {
        plugin.runSync(() -> getMarkerSet().ifPresent(markerSet -> {
            for (World world : plugin.getWorlds()) {
                plugin.getClaimWorld(world).ifPresent(claimWorld -> {
                    for (Claim claim : claimWorld.getClaims().getOrDefault(town.getId(), List.of())) {
                        addMarker(new TownClaim(town, claim), world, markerSet);
                    }
                });
            }
        }));
    }

    @Override
    public void removeClaimMarkers(@NotNull Town town) {
        final String removalKey = plugin.getKey(town.getName().toLowerCase()).toString();
        plugin.runSync(() -> getMarkerSet().ifPresent(markerSet -> markerSet.getMarkers()
                .removeIf(marker -> marker.getMarkerID().startsWith(removalKey))));
    }

    @Override
    public void clearAllMarkers() {
        plugin.runSync(() -> getMarkerSet().ifPresent(markerSet -> markerSet.getMarkers().clear()));
    }

    @NotNull
    private String getClaimMarkerKey(@NotNull TownClaim claim, @NotNull World world) {
        return plugin.getKey(
                claim.town().getName().toLowerCase(),
                Integer.toString(claim.claim().getChunk().getX()),
                Integer.toString(claim.claim().getChunk().getZ()),
                world.getUuid().toString()
        ).toString();
    }

    private Optional<DynmapCommonAPI> getDynmap() {
        return Optional.ofNullable(dynmapApi);
    }

    private Optional<MarkerSet> getMarkerSet() {
        return getDynmap().map(api -> {
            MarkerSet markerSet = api.getMarkerAPI().getMarkerSet(getMarkerSetKey());
            if (markerSet == null) {
                markerSet = api.getMarkerAPI().createMarkerSet(getMarkerSetKey(), plugin.getSettings().webMapMarkerSetName,
                        api.getMarkerAPI().getMarkerIcons(), false);
            } else {
                markerSet.setMarkerSetLabel(plugin.getSettings().webMapMarkerSetName);
            }
            return markerSet;
        });
    }

}
