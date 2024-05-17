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

package net.william278.husktowns.hook;

import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class BukkitHookManager extends HookManager {
    public BukkitHookManager(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @Override
    public void registerOnLoad() {
        plugin.log(Level.INFO, "Loading early hooks...");
        AtomicInteger loaded = new AtomicInteger();
        registeredHooks.stream().filter(Hook::isDisabled)
                .filter((hook) -> hook.getHookInfo().register() == PluginHook.Register.ON_LOAD)
                .forEach((hook) -> {
                    hook.enable();
                    loaded.getAndIncrement();
                });
        plugin.log(Level.INFO, "Successfully loaded %s hooks".formatted(loaded.get()));
    }

    @Override
    public void registerOnEnable() {
        plugin.log(Level.INFO, "Loading hooks...");
        AtomicInteger loaded = new AtomicInteger();
        registeredHooks.stream().filter(Hook::isDisabled)
                .filter((hook) -> hook.getHookInfo().register() == PluginHook.Register.ON_ENABLE)
                .forEach((hook) -> {
                    hook.enable();
                    loaded.getAndIncrement();
                });
        plugin.log(Level.INFO, "Successfully loaded %s hooks".formatted(loaded.get()));
    }

    @Override
    public void registerDelayed() {
        plugin.log(Level.INFO, "Loading late hooks...");
        AtomicInteger loaded = new AtomicInteger();
        registeredHooks.stream().filter(Hook::isDisabled)
                .filter((hook) -> hook.getHookInfo().register() == PluginHook.Register.DELAYED)
                .forEach((hook) -> {
                    hook.enable();
                    loaded.getAndIncrement();
                });
        plugin.log(Level.INFO, "Successfully loaded %s hooks".formatted(loaded.get()));
    }
}
