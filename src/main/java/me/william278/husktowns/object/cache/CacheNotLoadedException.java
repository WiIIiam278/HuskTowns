package me.william278.husktowns.object.cache;

/**
 * Signals that a cache has been accessed when it has not yet loaded
 */
public class CacheNotLoadedException extends IllegalStateException {

    public CacheNotLoadedException() {
        super();
    }

    public CacheNotLoadedException(String message) {
        super(message);
    }

    public CacheNotLoadedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheNotLoadedException(Throwable cause) {
        super(cause);
    }
}
