package net.william278.husktowns.hook;

import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

public abstract class Hook {

    protected final HuskTowns plugin;
    private final String name;
    private boolean enabled = false;

    protected Hook(@NotNull HuskTowns plugin, @NotNull String name) {
        this.plugin = plugin;
        this.name = name;
    }

    /**
     * Fired when a hook is enabled
     */
    protected abstract void onEnable();

    /**
     * Enable the hook
     */
    public final void enable() {
        this.onEnable();
        this.enabled = true;
    }

    /**
     * Get if the hook is disabled
     *
     * @return {@code true} if the hook is disabled, {@code false} otherwise
     */
    public boolean isDisabled() {
        return !enabled;
    }

    /**
     * Get the name of the hook
     *
     * @return the name of the hook
     */
    @NotNull
    public String getName() {
        return name;
    }

}
