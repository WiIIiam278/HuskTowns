package me.william278.husktowns.integrations;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.marker.Shape;
import de.bluecolored.bluemap.api.marker.*;
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

/**
 * This class provies a hook for the BlueMap API
 */
public class BlueMap {

    private static final HuskTowns plugin = HuskTowns.getInstance();
    private static final String MARKER_SET_ID = "husktowns.towns";
    private static final String MARKER_SET_LABEL = "Town Claims";
    private static final HashMap<ClaimedChunk, Boolean> queuedOperations = new HashMap<>();
    private static boolean queuedRemoveAllMarkers;

    public static void removeAllMarkers() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            BlueMapAPI.getInstance().ifPresentOrElse(api -> {
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
            }, () -> queuedRemoveAllMarkers = true);
        });
    }

    public static void removeMarkers(HashSet<ClaimedChunk> claimedChunks) {
        ClaimedChunk[] chunks = new ClaimedChunk[claimedChunks.size()];
        int i = 0;
        for (ClaimedChunk chunk : claimedChunks) {
            chunks[i] = chunk;
            i++;
        }
        removeMarker(chunks);
    }

    public static void removeMarker(ClaimedChunk... claimedChunks) {
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

    public static void addAllMarkers(HashSet<ClaimedChunk> claimedChunks) {
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
                                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                        api.getWorld(world.getUID()).ifPresent(blueMapWorld -> {
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
                                                marker.setDetail(DynMap.getTownPopup(claimedChunk));
                                            }
                                            try {
                                                markerAPI.save();
                                            } catch (IOException ignored) {
                                            }
                                        });
                                    });
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
            } );
        });
    }

    public static void addMarker(ClaimedChunk claimedChunk) {
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
                            marker.setDetail(DynMap.getTownPopup(claimedChunk));
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

    private static void executeQueuedOperations() {
        if (queuedRemoveAllMarkers) {
            removeAllMarkers();
        }
        for (ClaimedChunk chunk : queuedOperations.keySet()) {
            if (queuedOperations.get(chunk)) {
                addMarker(chunk);
            } else {
                removeMarker(chunk);
            }
        }
    }

    public static void initialize() {
        if (HuskTowns.getSettings().doBlueMap()) {
            Plugin blueMap = Bukkit.getPluginManager().getPlugin("BlueMap");
            if (blueMap == null) {
                HuskTowns.getSettings().setDoBlueMap(false);
                plugin.getConfig().set("integrations.bluemap.enabled", false);
                plugin.saveConfig();
                return;
            }
            if (!blueMap.isEnabled()) {
                HuskTowns.getSettings().setDoBlueMap(false);
                plugin.getConfig().set("integrations.bluemap.enabled", false);
                plugin.saveConfig();
                return;
            }
            queuedRemoveAllMarkers = false;

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                // Create Marker Set
                BlueMapAPI.onEnable(api -> {
                    try {
                        MarkerAPI markerAPI = api.getMarkerAPI();
                        MarkerSet markerSet = markerAPI.getMarkerSet(MARKER_SET_ID).orElse(markerAPI.createMarkerSet(MARKER_SET_ID));
                        markerSet.setLabel(MARKER_SET_LABEL);
                        markerAPI.save();
                        plugin.getLogger().info("Enabled BlueMap integration!");

                        executeQueuedOperations();
                    } catch (IOException e) {
                        plugin.getLogger().warning("An exception occurred initialising BlueMap.");
                        HuskTowns.getSettings().setDoBlueMap(false);
                        plugin.getConfig().set("integrations.bluemap.enabled", false);
                        plugin.saveConfig();
                    }
                });
            });

        }
    }

}
