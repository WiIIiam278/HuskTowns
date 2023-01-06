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

    public boolean isNotEnabled() {
        return !enabled;
    }

    @NotNull
    public String getName() {
        return name;
    }

}
