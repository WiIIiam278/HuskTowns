package me.william278.husktowns.integrations.map;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.marker.*;
import de.bluecolored.bluemap.api.marker.Shape;
import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.object.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BlueMap extends Map {

    private static final HuskTowns plugin = HuskTowns.getInstance();
    private static final HashMap<ClaimedChunk, Boolean> queuedOperations = new HashMap<>();
    private static boolean queuedClearMarkers;

    private void executeQueuedOperations() {
        if (queuedClearMarkers) {
            clearMarkers();
        }
        HashSet<ClaimedChunk> chunksToAdd = new HashSet<>();
        HashSet<ClaimedChunk> chunksToRemove = new HashSet<>();
        for (ClaimedChunk chunk : queuedOperations.keySet()) {
            if (queuedOperations.get(chunk)) {
                chunksToAdd.add(chunk);
            } else {
                chunksToRemove.add(chunk);
            }
        }
        addMarkers(chunksToAdd);
        removeMarkers(chunksToRemove);
    }

    @Override
    public void initialize() {
        Plugin blueMap = Bukkit.getPluginManager().getPlugin("BlueMap");
        if (blueMap == null) {
            HuskTowns.getSettings().setDoMapIntegration(false);
            return;
        }
        if (!blueMap.isEnabled()) {
            HuskTowns.getSettings().setDoMapIntegration(false);
            return;
        }
        queuedClearMarkers = false;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Create Marker Set
            BlueMapAPI.onEnable(api -> {
                try {
                    MarkerAPI markerAPI = api.getMarkerAPI();
                    MarkerSet markerSet = markerAPI.getMarkerSet(MARKER_SET_ID).orElse(markerAPI.createMarkerSet(MARKER_SET_ID));
                    markerSet.setLabel(HuskTowns.getSettings().getMapMarkerSetName());
                    markerAPI.save();
                    plugin.getLogger().info("Enabled BlueMap integration!");

                    executeQueuedOperations();
                } catch (IOException e) {
                    plugin.getLogger().warning("An exception occurred initialising BlueMap; it has been disabled.");
                    HuskTowns.getSettings().setDoMapIntegration(false);
                }
            });
        });
    }

    @Override
    public void addMarker(ClaimedChunk claimedChunk) {
        // Check that the claimed chunk is in a valid world
        World world = Bukkit.getWorld(claimedChunk.getWorld());
        if (world == null) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Insert the marker with the API if available
            BlueMapAPI.getInstance().ifPresentOrElse(api -> {
                try {
                    MarkerAPI markerAPI = api.getMarkerAPI();
                    markerAPI.getMarkerSet(MARKER_SET_ID).ifPresent(markerSet -> api.getWorld(world.getUID()).ifPresent(blueMapWorld -> {
                        double xCoord = claimedChunk.getChunkX() * 16;
                        double zCoord = claimedChunk.getChunkZ() * 16;
                        Shape shape = Shape.createRect(xCoord, zCoord, xCoord + 16, zCoord + 16);
                        String markerId = "husktowns.claim." + claimedChunk.getServer() + "." + claimedChunk.getWorld() + "." + claimedChunk.getChunkX() + "." + claimedChunk.getChunkZ();
                        for (BlueMapMap map : blueMapWorld.getMaps()) {
                            Color townColor = Town.getTownColor(claimedChunk.getTown());
                            Color color = new Color(townColor.getRed(), townColor.getGreen(), townColor.getBlue(), 130);
                            ShapeMarker marker = markerSet.createShapeMarker(markerId, map, shape, world.getSeaLevel());
                            marker.setLineWidth(0);
                            marker.setColors(color, color);
                            marker.setLabel(claimedChunk.getTown());
                            marker.setDetail(getClaimInfoWindow(claimedChunk));
                        }
                        try {
                            markerAPI.save();
                        } catch (IOException ignored) {
                        }
                    }));
                } catch (IOException ignored) {
                }
            }, () -> queuedOperations.put(claimedChunk, true));
        });
    }

    @Override
    public void removeMarker(ClaimedChunk claimedChunk) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Remove the marker
            BlueMapAPI.getInstance().ifPresentOrElse(api -> {
                try {
                    MarkerAPI markerAPI = api.getMarkerAPI();
                    markerAPI.getMarkerSet(MARKER_SET_ID).ifPresent(markerSet -> {
                        String markerId = "husktowns.claim." + claimedChunk.getServer() + "." + claimedChunk.getWorld() + "." + claimedChunk.getChunkX() + "." + claimedChunk.getChunkZ();
                        markerSet.removeMarker(markerId);
                        try {
                            markerAPI.save();
                        } catch (IOException ignored) {
                        }
                    });
                } catch (IOException ignored) {
                }
            }, () -> queuedOperations.put(claimedChunk, false));
        });
    }

    @Override
    public void addMarkers(Set<ClaimedChunk> claimedChunks) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Insert the marker with the API if available
            BlueMapAPI.getInstance().ifPresentOrElse(api -> {
                try {
                    MarkerAPI markerAPI = api.getMarkerAPI();
                    markerAPI.getMarkerSet(MARKER_SET_ID).ifPresent(markerSet -> {
                        for (ClaimedChunk claimedChunk : claimedChunks) {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                World world = Bukkit.getWorld(claimedChunk.getWorld());
                                if (world != null) {
                                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> api.getWorld(world.getUID()).ifPresent(blueMapWorld -> {
                                        double xCoord = claimedChunk.getChunkX() * 16;
                                        double zCoord = claimedChunk.getChunkZ() * 16;
                                        Shape shape = Shape.createRect(xCoord, zCoord, xCoord + 16, zCoord + 16);
                                        String markerId = "husktowns.claim." + claimedChunk.getServer() + "." + claimedChunk.getWorld() + "." + claimedChunk.getChunkX() + "." + claimedChunk.getChunkZ();
                                        for (BlueMapMap map : blueMapWorld.getMaps()) {
                                            Color townColor = Town.getTownColor(claimedChunk.getTown());
                                            Color color = new Color(townColor.getRed(), townColor.getGreen(), townColor.getBlue(), 130);
                                            ShapeMarker marker = markerSet.createShapeMarker(markerId, map, shape, world.getSeaLevel());
                                            marker.setLineWidth(0);
                                            marker.setColors(color, color);
                                            marker.setLabel(claimedChunk.getTown());
                                            marker.setDetail(getClaimInfoWindow(claimedChunk));
                                        }
                                        try {
                                            markerAPI.save();
                                        } catch (IOException ignored) {
                                        }
                                    }));
                                }
                            });
                        }
                    });
                } catch (IOException ignored) {
                }
            }, () -> {
                for (ClaimedChunk claimedChunk : claimedChunks) {
                    queuedOperations.put(claimedChunk, true);
                }
            });
        });
    }

    @Override
    public void removeMarkers(Set<ClaimedChunk> claimedChunks) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Remove the marker
            BlueMapAPI.getInstance().ifPresentOrElse(api -> {
                try {
                    MarkerAPI markerAPI = api.getMarkerAPI();
                    markerAPI.getMarkerSet(MARKER_SET_ID).ifPresent(markerSet -> {
                        for (ClaimedChunk claimedChunk : claimedChunks) {
                            String markerId = "husktowns.claim." + claimedChunk.getServer() + "." + claimedChunk.getWorld() + "." + claimedChunk.getChunkX() + "." + claimedChunk.getChunkZ();
                            markerSet.removeMarker(markerId);
                            try {
                                markerAPI.save();
                            } catch (IOException ignored) {
                            }
                        }
                    });
                } catch (IOException ignored) {
                }
            }, () -> {
                for (ClaimedChunk claimedChunk : claimedChunks) {
                    queuedOperations.put(claimedChunk, false);
                }
            });
        });
    }

    @Override
    public void clearMarkers() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> BlueMapAPI.getInstance().ifPresentOrElse(api -> {
            try {
                MarkerAPI markerAPI = api.getMarkerAPI();
                markerAPI.getMarkerSet(MARKER_SET_ID).ifPresent(markerSet -> {
                    for (Marker marker : markerSet.getMarkers()) {
                        markerSet.removeMarker(marker);
                    }
                });
                markerAPI.save();
            } catch (IOException ignored) {
            }
        }, () -> queuedClearMarkers = true));
    }
}
