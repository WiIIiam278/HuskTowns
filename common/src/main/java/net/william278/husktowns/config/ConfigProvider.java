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

package net.william278.husktowns.config;

import org.jetbrains.annotations.NotNull;

public interface ConfigProvider {

    @NotNull
    Settings getSettings();

    void setSettings(@NotNull Settings settings);

    @NotNull
    Locales getLocales();

    void setLocales(@NotNull Locales locales);

    @NotNull
    Roles getRoles();

    void setRoles(@NotNull Roles roles);

    @NotNull
    RulePresets getRulePresets();

    void setRulePresets(@NotNull RulePresets rulePresets);

    @NotNull
    Flags getFlags();

    void setFlags(@NotNull Flags flags);

    @NotNull
    Levels getLevels();

    void setLevels(@NotNull Levels levels);

    @NotNull
    String getServerName();

    void setServer(@NotNull Server server);

}
