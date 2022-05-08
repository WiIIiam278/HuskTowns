package net.william278.husktowns.cache;

/**
 * Identifies the current status of a cache
 */
public enum CacheStatus {
    /**
     * The cache has not been loaded yet
     */
    UNINITIALIZED,
    /**
     * The cache is in the process of being loaded from the database
     */
    UPDATING,
    /**
     * The cache is ready and has been loaded from the database
     */
    LOADED,
    /**
     * The cache initialization has failed and reached an error state
     */
    ERROR
}
