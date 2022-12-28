package net.william278.husktowns.hook;

import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

public abstract class Hook {

    protected final HuskTowns plugin;
    private final String name;

    protected Hook(@NotNull HuskTowns plugin, @NotNull String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public abstract void onEnable();

    @NotNull
    public String getName() {
        return name;
    }

}
