package net.william278.husktowns.manager;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.war.Declaration;
import net.william278.husktowns.war.War;
import net.william278.husktowns.war.WarSystem;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WarManager implements WarSystem {

    private final HuskTowns plugin;
    private final List<War> activeWars;
    private final List<Declaration> pendingDeclarations;

    public WarManager(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
        this.activeWars = new ArrayList<>();
        this.pendingDeclarations = new ArrayList<>();
    }

    @NotNull
    @Override
    public List<War> getActiveWars() {
        return activeWars;
    }

    @NotNull
    @Override
    public List<Declaration> getPendingDeclarations() {
        return pendingDeclarations;
    }

    @NotNull
    @Override
    @ApiStatus.Internal
    public HuskTowns getPlugin() {
        return plugin;
    }
}
