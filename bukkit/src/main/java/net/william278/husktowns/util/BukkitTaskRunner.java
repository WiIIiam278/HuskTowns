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
