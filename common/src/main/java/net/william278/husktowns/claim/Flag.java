package net.william278.husktowns.claim;

/**
 * Various flag types
 */
public enum Flag {
    EXPLOSION_DAMAGE(false),
    FIRE_DAMAGE(false),
    MOB_GRIEFING(false),
    MONSTER_SPAWNING(true),
    PUBLIC_BUILD_ACCESS(false),
    PUBLIC_CONTAINER_ACCESS(false),
    PUBLIC_FARM_ACCESS(false),
    PUBLIC_INTERACT_ACCESS(false),
    PVP(false);

    private final boolean defaultValue;

    Flag(final boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }
}
