package net.william278.husktowns.listener;

import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

public class BukkitEventListener extends EventListener implements BukkitBlockMoveListener, BukkitBreakListener,
        BukkitInteractListener, BukkitJoinListener, BukkitMoveListener, BukkitPlaceListener, BukkitEntityListener,
        BukkitFireListener, BukkitEntityDamageEvent, BukkitChatListener, BukkitBlockGrowListener {

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
    public EventListener getListener() {
        return this;
    }
}
