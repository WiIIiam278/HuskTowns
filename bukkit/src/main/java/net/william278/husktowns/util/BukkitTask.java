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

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.arim.morepaperlib.scheduling.AsynchronousScheduler;
import space.arim.morepaperlib.scheduling.AttachedScheduler;
import space.arim.morepaperlib.scheduling.GracefulScheduling;
import space.arim.morepaperlib.scheduling.RegionalScheduler;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public interface BukkitTask extends Task {

    class Sync extends Task.Sync implements BukkitTask {

        private ScheduledTask task;
        private final @Nullable OnlineUser user;

        protected Sync(@NotNull HuskTowns plugin, @NotNull Runnable runnable,
                       @Nullable OnlineUser user, long delayTicks) {
            super(plugin, runnable, delayTicks);
            this.user = user;
        }

        @Override
        public void cancel() {
            if (task != null && !cancelled) {
                task.cancel();
            }
            super.cancel();
        }

        @Override
        public void run() {
            if (isPluginDisabled()) {
                runnable.run();
                return;
            }
            if (cancelled) {
                return;
            }

            // Use entity-specific scheduler if user is not null
            if (user != null) {
                final AttachedScheduler scheduler = ((BukkitHuskTowns) getPlugin()).getUserSyncScheduler(user);
                if (delayTicks > 0) {
                    this.task = scheduler.runDelayed(runnable, null, delayTicks);
                } else {
                    this.task = scheduler.run(runnable, null);
                }
                return;
            }

            // Or default to the global scheduler
            final RegionalScheduler scheduler = ((BukkitHuskTowns) getPlugin()).getSyncScheduler();
            if (delayTicks > 0) {
                this.task = scheduler.runDelayed(runnable, delayTicks);
            } else {
                this.task = scheduler.run(runnable);
            }
        }
    }

    class Async extends Task.Async implements BukkitTask {

        private ScheduledTask task;

        protected Async(@NotNull HuskTowns plugin, @NotNull Runnable runnable, long delayTicks) {
            super(plugin, runnable, delayTicks);
        }

        @Override
        public void cancel() {
            if (task != null && !cancelled) {
                task.cancel();
            }
            super.cancel();
        }

        @Override
        public void run() {
            if (isPluginDisabled()) {
                runnable.run();
                return;
            }
            if (cancelled) {
                return;
            }

            final AsynchronousScheduler scheduler = ((BukkitHuskTowns) getPlugin()).getAsyncScheduler();
            if (delayTicks > 0) {
                this.task = scheduler.runDelayed(
                    runnable,
                    Duration.of(delayTicks * 50L, ChronoUnit.MILLIS)
                );
            } else {
                this.task = scheduler.run(runnable);
            }
        }
    }

    class Repeating extends Task.Repeating implements BukkitTask {

        private ScheduledTask task;

        protected Repeating(@NotNull HuskTowns plugin, @NotNull Runnable runnable, long repeatingTicks) {
            super(plugin, runnable, repeatingTicks);
        }

        @Override
        public void cancel() {
            if (task != null && !cancelled) {
                task.cancel();
            }
            super.cancel();
        }

        @Override
        public void run() {
            if (isPluginDisabled()) {
                return;
            }

            if (!cancelled) {
                final AsynchronousScheduler scheduler = ((BukkitHuskTowns) getPlugin()).getAsyncScheduler();
                this.task = scheduler.runAtFixedRate(
                    runnable, Duration.ZERO,
                    Duration.of(repeatingTicks * 50L, ChronoUnit.MILLIS)
                );
            }
        }
    }

    // Returns if the Bukkit HuskTowns plugin is disabled
    default boolean isPluginDisabled() {
        return !((BukkitHuskTowns) getPlugin()).isEnabled();
    }

    interface Supplier extends Task.Supplier {

        @NotNull
        @Override
        default Task.Sync getSyncTask(@NotNull Runnable runnable, @Nullable OnlineUser user, long delayTicks) {
            return new Sync(getPlugin(), runnable, user, delayTicks);
        }

        @NotNull
        @Override
        default Task.Async getAsyncTask(@NotNull Runnable runnable, long delayTicks) {
            return new Async(getPlugin(), runnable, delayTicks);
        }

        @NotNull
        @Override
        default Task.Repeating getRepeatingTask(@NotNull Runnable runnable, long repeatingTicks) {
            return new Repeating(getPlugin(), runnable, repeatingTicks);
        }

        @Override
        @NotNull
        default <T> Task.Sync runSync(T e, @NotNull Runnable runnable) {
            final BukkitHuskTowns bukkit = (BukkitHuskTowns) getPlugin();

            final BukkitTask.Sync task = new BukkitTask.Sync(bukkit, runnable, null, 0) {

                private ScheduledTask scheduled;

                @Override
                public void run() {
                    if (isPluginDisabled() || cancelled) {
                        return;
                    }

                    final GracefulScheduling scheduler = ((BukkitHuskTowns) getPlugin()).getScheduler();

                    if (e instanceof Entity entity) {
                        scheduled = scheduler.entitySpecificScheduler(entity).run(runnable, null);
                    } else if (e instanceof Location location) {
                        scheduled = scheduler.regionSpecificScheduler(location).run(runnable);
                    } else if (e instanceof Chunk chunk) {
                        scheduled = scheduler.regionSpecificScheduler(chunk.getWorld(), chunk.getX(), chunk.getZ()).run(runnable);
                    } else {
                        scheduled = scheduler.globalRegionalScheduler().run(runnable);
                    }
                }

                @Override
                public void cancel() {
                    if (!cancelled) {
                        if (scheduled != null) {
                            scheduled.cancel();
                        }
                        super.cancel();
                    }
                }
            };

            task.run();
            return task;
        }

        default void runSync(@NotNull Entity entity, @NotNull Runnable runnable) {
            runSync((Object) entity, runnable);
        }

        @NotNull
        default Task.Sync runSync(@NotNull Location location, @NotNull Runnable runnable) {
            return runSync((Object) location, runnable);
        }

        @Override
        @NotNull
        default Task.Sync runSync(Position position, @NotNull Runnable runnable) {
            return runSync(new Location(Bukkit.getWorld(position.getWorld().getName()),
                    position.getX(), position.getY(), position.getZ()), runnable);
        }

        @Override
        default void cancelTasks() {
            ((BukkitHuskTowns) getPlugin()).getScheduler().cancelGlobalTasks();
        }
    }

}
