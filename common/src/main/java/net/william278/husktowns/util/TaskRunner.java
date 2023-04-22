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

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnusedReturnValue")
public interface TaskRunner {

    /**
     * Run a task asynchronously
     *
     * @param runnable The task to run
     * @return The ID of the async task
     */
    int runAsync(@NotNull Runnable runnable);

    /**
     * Run a task synchronously on the server thread
     *
     * @param runnable The task to run
     * @return The ID of the synchronous task
     */
    int runSync(@NotNull Runnable runnable);

    /**
     * Run a task asynchronously continuously for a period
     *
     * @param runnable The task to run
     * @param delay    The delay in ticks before the task is run
     * @param period   The period in ticks for the task to run
     * @return The ID of the async task
     */
    int runTimedAsync(@NotNull Runnable runnable, long delay, long period);

    /**
     * Cancel a task
     *
     * @param taskId The ID of the task to cancel
     */
    void cancelTask(int taskId);

}
