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
import org.jetbrains.annotations.NotNull;
import space.arim.morepaperlib.scheduling.GracefulScheduling;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

public interface BukkitTaskRunner extends TaskRunner {

    @Override
    default int runAsync(@NotNull Runnable runnable) {
        final int taskId = getTasks().size();
        getTasks().put(taskId, getScheduler().asyncScheduler().run(runnable));
        return taskId;
    }

    @Override
    default int runSync(@NotNull Runnable runnable) {
        final int taskId = getTasks().size();
        getTasks().put(taskId, getScheduler().globalRegionalScheduler().run(runnable));
        return taskId;
    }

    @Override
    default int runTimedAsync(@NotNull Runnable runnable, long delay, long period) {
        final int taskId = getTasks().size();
        getTasks().put(taskId, getScheduler().asyncScheduler().runAtFixedRate(
                runnable,
                Duration.ZERO,
                getDurationTicks(period)
        ));
        return taskId;
    }

    @Override
    default void cancelTask(int taskId) {
        if (getTasks().containsKey(taskId)) {
            getTasks().get(taskId).cancel();
        }
    }

    @NotNull
    default Duration getDurationTicks(long ticks) {
        return Duration.of(ticks * 50, ChronoUnit.MILLIS);
    }

    @NotNull
    BukkitHuskTowns getPlugin();

    @NotNull
    GracefulScheduling getScheduler();

    @NotNull
    ConcurrentHashMap<Integer, ScheduledTask> getTasks();

}
