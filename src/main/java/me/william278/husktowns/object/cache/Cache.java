package me.william278.husktowns.object.cache;

import java.time.Instant;

public class Cache {

    private CacheStatus status;
    private final String name;
    private final long initializationTime;

    // A cache object that contains some metadata about the cache
    public Cache(String name) {
        this.name = name;
        status = CacheStatus.UNINITIALIZED;
        initializationTime = Instant.now().getEpochSecond();
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

    public long getTimeSinceInitialization() {
        return Instant.now().getEpochSecond() - initializationTime;
    }

    public String getName() {
        return name;
    }

    public enum CacheStatus {
        UNINITIALIZED,
        UPDATING,
        LOADED,
        ERROR
    }
}
