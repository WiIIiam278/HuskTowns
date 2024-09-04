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

import lombok.AllArgsConstructor;
import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
public abstract class HookManager {
    protected final HuskTowns plugin;
    protected final static HashSet<Hook> registeredHooks = new HashSet<>();

    public abstract void registerOnLoad();

    public abstract void registerOnEnable();

    public <T extends Hook> Optional<T> getHook(@NotNull Class<T> hookClass) {
        return registeredHooks.stream()
            .filter(hook -> hookClass.isAssignableFrom(hook.getClass()))
            .map(hookClass::cast)
            .findFirst();
    }

    public void registerHook(@NotNull Hook hook) {
        if (getHook(hook.getClass()).isPresent() || registeredHooks.stream().anyMatch(var1 ->
            Objects.equals(var1.getHookInfo().id(), hook.getHookInfo().id()))) {
            throw new RuntimeException("Hook with matching Class or ID already registered! ID: %s".formatted(hook.getHookInfo().id()));
        }
        registeredHooks.add(hook);
    }
}
