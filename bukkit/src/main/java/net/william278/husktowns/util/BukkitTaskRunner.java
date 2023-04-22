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

package net.william278.husktowns.util;

import net.william278.husktowns.BukkitHuskTowns;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public interface BukkitTaskRunner extends TaskRunner {

    @Override
    default int runAsync(@NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), runnable).getTaskId();
    }

    @Override
    default int runSync(@NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTask(getPlugin(), runnable).getTaskId();
    }

    @Override
    default int runTimedAsync(@NotNull Runnable runnable, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(getPlugin(), runnable, delay, period).getTaskId();
    }

    @Override
    default void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    @NotNull
    BukkitHuskTowns getPlugin();

}
