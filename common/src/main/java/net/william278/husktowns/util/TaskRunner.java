package net.william278.husktowns.util;

import org.jetbrains.annotations.NotNull;

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
