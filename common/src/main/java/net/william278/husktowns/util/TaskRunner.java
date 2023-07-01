/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
