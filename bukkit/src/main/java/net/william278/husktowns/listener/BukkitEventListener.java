package net.william278.husktowns.listener;

import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

public class BukkitEventListener extends EventListener implements BukkitBlockMoveListener, BukkitBreakListener,
        BukkitInteractListener, BukkitJoinQuitListener, BukkitMoveListener, BukkitPlaceListener, BukkitEntityListener,
        BukkitFireListener, BukkitEntityDamageEvent {

    public BukkitEventListener(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public HuskTowns getPlugin() {
        return plugin;
    }

    @Override
    @NotNull
    public EventListener getHandler() {
        return this;
    }
}
