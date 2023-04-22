/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
 */

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
