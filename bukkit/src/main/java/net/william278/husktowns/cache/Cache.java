package net.william278.husktowns.cache;

import net.william278.husktowns.HuskTowns;

import java.time.Instant;

public class Cache {

    private CacheStatus status;
    private final String name;
    private long initializationTime; // When this cache was last loaded
    private int itemsLoaded; // Number of items currently loaded in the cache
    private int itemsToLoad; // Number of items to load into the cache
    private String currentItemToLoadData;

    // A cache object that contains some metadata about the cache
    public Cache(String name) {
        this.name = name;
        status = CacheStatus.UNINITIALIZED;
        resetInitializationTime();
        itemsLoaded = 0;
        itemsToLoad = 0;
        currentItemToLoadData = "N/A";
    }

    // Log cache loading progress
    public void log() {
        if (HuskTowns.getSettings().logCacheLoading()) {
            HuskTowns.getInstance().getLogger().info("[" + name + " Cache] Loading item: " + currentItemToLoadData + " (" + itemsLoaded + "/" + itemsToLoad + ")");
        }
    }

    public int getItemsToLoad() {
        return itemsToLoad;
    }

    public void setItemsToLoad(int itemsToLoad) {
        this.itemsToLoad = itemsToLoad;
    }

    public String getCurrentItemToLoadData() {
        return currentItemToLoadData;
    }

    public void setCurrentItemToLoadData(String currentItemToLoadData) {
        this.currentItemToLoadData = currentItemToLoadData;
    }

    public void clearItemsLoaded() {
        itemsLoaded = 0;
    }

    public void incrementItemsLoaded() {
        itemsLoaded++;
    }

    public void incrementItemsLoaded(int amount) {
        itemsLoaded = itemsLoaded + amount;
    }

    public void setItemsLoaded(int itemsLoaded) {
        this.itemsLoaded = itemsLoaded;
    }

    public void decrementItemsLoaded() {
        itemsLoaded--;
    }

    public void decrementItemsLoaded(int amount) {
        itemsLoaded = itemsLoaded - amount;
    }

    public int getItemsLoaded() {
        return itemsLoaded;
    }

    public boolean hasLoaded() {
        return status == CacheStatus.LOADED;
    }

    public void setStatus(CacheStatus status) {
        this.status = status;
    }

    public CacheStatus getStatus() {
        return status;
    }

    public void resetInitializationTime() {
        initializationTime = Instant.now().getEpochSecond();
    }

    public long getTimeSinceInitialization() {
        return Instant.now().getEpochSecond() - initializationTime;
    }

    public String getName() {
        return name;
    }

    public String getIllegalAccessMessage() {
        return "Exception attempting to access unloaded " + getName() + " cache (current state: " + getStatus().toString() + ")";
    }

    /**
     * Signals that a cache has been accessed when it has not yet loaded
     */
    public static class CacheNotLoadedException extends IllegalStateException {

        public CacheNotLoadedException(String message) {
            super(message);
        }

    }
}
