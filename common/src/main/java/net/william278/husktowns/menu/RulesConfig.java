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

package net.william278.husktowns.menu;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Flag;
import net.william278.husktowns.claim.Rules;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;

public class RulesConfig {

    private final HuskTowns plugin;
    private final Town town;
    private final CommandUser viewer;

    private RulesConfig(@NotNull HuskTowns plugin, @NotNull Town town, @NotNull CommandUser viewer) {
        this.plugin = plugin;
        this.town = town;
        this.viewer = viewer;
    }

    @NotNull
    public static RulesConfig of(@NotNull HuskTowns plugin, @NotNull Town town, @NotNull CommandUser viewer) {
        return new RulesConfig(plugin, town, viewer);
    }

    @NotNull
    public Component toComponent() {
        return getTitle()
            .append(getRules());
    }

    public void show() {
        viewer.sendMessage(toComponent());
    }

    @NotNull
    private Component getTitle() {
        return plugin.getLocales().getLocale("town_rules_config_title", town.getName())
            .map(mineDown -> mineDown.toComponent().appendNewline())
            .orElse(Component.empty());
    }

    @NotNull
    private Component getRules() {
        final Map<Flag, Map<Claim.Type, Boolean>> rules = new TreeMap<>();
        for (Map.Entry<Claim.Type, Rules> entry : town.getRules().entrySet()) {
            for (Map.Entry<Flag, Boolean> flag : entry.getValue().getCalculatedFlags(plugin.getFlags()).entrySet()) {
                if (!rules.containsKey(flag.getKey())) {
                    rules.put(flag.getKey(), new TreeMap<>());
                }
                rules.get(flag.getKey()).put(entry.getKey(), flag.getValue());
            }
        }
        return rules.entrySet().stream()
            .map(this::getRuleLine)
            .reduce(Component.empty(), Component::append);
    }

    @NotNull
    private Component getRuleLine(@NotNull Map.Entry<Flag, Map<Claim.Type, Boolean>> entry) {
        Component line = Component.newline();
        for (Claim.Type type : entry.getValue().keySet()) {
            line = line.append(getRuleFlag(entry.getKey(), type, entry.getValue().get(type)));
            for (int i = 0; i < 3; i++) {
                line = line.append(Component.space());
            }
        }

        final String flagName = entry.getKey().getName().toLowerCase();
        return line.append(plugin.getLocales().getLocale("town_rules_config_flag_name",
                plugin.getLocales().getRawLocale(("town_rule_name_" + flagName))
                    .orElse(flagName.replaceAll("_", " ")))
            .map(MineDown::toComponent).orElse(Component.empty()));
    }

    @NotNull
    private Component getRuleFlag(@NotNull Flag flag, @NotNull Claim.Type type, boolean value) {
        return plugin.getLocales().getLocale(value ? "town_rules_config_flag_true" : "town_rules_config_flag_false")
            .map(MineDown::toComponent).orElse(Component.empty())
            .hoverEvent(plugin.getLocales().getLocale("town_rules_config_flag_hover",
                type.name().toLowerCase()).map(MineDown::toComponent).orElse(Component.empty()))
            .clickEvent(ClickEvent.runCommand("/husktowns:town rules " + flag.getName().toLowerCase() + " "
                + type.name().toLowerCase() + " " + !value + " -m"));
    }

}
