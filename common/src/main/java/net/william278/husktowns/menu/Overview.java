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
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Spawn;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.SavedUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class Overview {

    private final HuskTowns plugin;
    private final Town town;
    private final CommandUser viewer;

    private Overview(@NotNull Town town, @NotNull CommandUser viewer, @NotNull HuskTowns plugin) {
        this.town = town;
        this.viewer = viewer;
        this.plugin = plugin;
    }

    @NotNull
    public static Overview of(@NotNull Town town, @NotNull CommandUser viewer, @NotNull HuskTowns plugin) {
        return new Overview(town, viewer, plugin);
    }


    public void show() {
        this.viewer.sendMessage(toComponent());
    }

    @NotNull
    public Component toComponent() {
        return getTitle()
            .append(getMeta())
            .append(getBio())
            .append(getWarStatus())
            .appendNewline()
            .append(getStats())
            .append(getSpawn())
            .appendNewline()
            .append(getButtons());
    }

    @NotNull
    private Component getTitle() {
        return plugin.getLocales().getLocale("town_overview_title",
                town.getName(), Integer.toString(town.getId()))
            .map(mineDown -> mineDown.toComponent().appendNewline())
            .orElse(Component.empty());
    }

    @NotNull
    private Component getMeta() {
        return plugin.getLocales().getLocale("town_overview_meta",
                town.getFoundedTime().format(DateTimeFormatter.ofPattern("dd MMM, yyyy")),
                town.getFoundedTime().format(DateTimeFormatter.ofPattern("dd MMM, yyyy, HH:mm:ss")),
                plugin.getDatabase().getUser(town.getMayor())
                    .map(SavedUser::user).map(User::getUsername).orElse("?"))
            .map(mineDown -> mineDown.toComponent().appendNewline())
            .orElse(Component.empty());
    }

    @NotNull
    private Component getBio() {
        return town.getBio().map(bio -> plugin.getLocales().getLocale("town_overview_bio",
                plugin.getLocales().truncateText(bio, 45), bio)
            .map(mineDown -> mineDown.toComponent().appendNewline())
            .orElse(Component.empty())).orElse(Component.empty());
    }

    @NotNull
    private Component getWarStatus() {
        return town.getCurrentWar().map(war -> plugin.getLocales().getLocale("town_overview_at_war",
                war.getDefending() == town.getId()
                    ? war.getAttacking(plugin).getName() : war.getDefending(plugin).getName()
            )
            .map(mineDown -> mineDown.toComponent().appendNewline())
            .orElse(Component.empty())).orElse(Component.empty());
    }

    @NotNull
    private Component getStats() {
        return plugin.getLocales().getLocale("town_overview_stats",
                Integer.toString(town.getLevel()),
                plugin.formatMoney(town.getMoney()),
                Integer.toString(town.getClaimCount()),
                Integer.toString(town.getMaxClaims(plugin)),
                town.getColorRgb(),
                Integer.toString(town.getBonusClaims()),
                Integer.toString(town.getMembers().size()),
                Integer.toString(town.getMaxMembers(plugin)),
                Integer.toString(town.getBonusMembers()),
                town.getLevel() >= plugin.getLevels().getMaxLevel()
                    ? plugin.getLocales().getNotApplicable()
                    : plugin.formatMoney(plugin.getLevels().getLevelUpCost(town.getLevel())))
            .map(mineDown -> mineDown.toComponent().appendNewline())
            .orElse(Component.empty());
    }

    @NotNull
    private Component getSpawn() {
        if (!isViewerMember() && !town.getSpawn().map(Spawn::isPublic).orElse(false)) {
            return Component.empty();
        }
        return town.getSpawn().map(spawn -> plugin.getLocales().getLocale("town_overview_spawn",
                new DecimalFormat("0.0").format(spawn.getPosition().getX()),
                new DecimalFormat("0.0").format(spawn.getPosition().getY()),
                new DecimalFormat("0.0").format(spawn.getPosition().getZ()),
                spawn.getPosition().getWorld().getName(),
                new DecimalFormat("0.00").format(spawn.getPosition().getPitch()),
                new DecimalFormat("0.00").format(spawn.getPosition().getYaw()))
            .map(mineDown -> mineDown.toComponent()
                .append(spawn.isPublic() ? plugin.getLocales().getLocale("town_overview_spawn_public")
                    .map(MineDown::toComponent).orElse(Component.empty())
                    : plugin.getLocales().getLocale("town_overview_spawn_private")
                    .map(MineDown::toComponent).orElse(Component.empty()))
                .appendNewline())
            .orElse(Component.empty())).orElse(Component.empty());
    }

    @NotNull
    private Component getButtons() {
        return getViewButtons()
            .append(getEditButtons())
            .append(getSpawnButtons());
    }

    @NotNull
    private Component getViewButtons() {
        return plugin.getLocales().getLocale("town_button_group_view")
            .map(mineDown -> mineDown.toComponent().append(Component.space())).orElse(Component.empty())
            .append(plugin.getLocales().getLocale("town_button_members", town.getName())
                .map(m -> m.toComponent().append(Component.space())).orElse(Component.empty()))
            .append(plugin.getLocales().getLocale("town_button_claims",
                    town.getName(), town.getColorRgb())
                .map(m -> m.toComponent().append(Component.space())).orElse(Component.empty())
                .append(plugin.getSettings().getTowns().getRelations().isEnabled()
                    ? plugin.getLocales().getLocale("town_button_relations", town.getName())
                    .map(MineDown::toComponent).orElse(Component.empty())
                    : Component.empty())
                .appendNewline());
    }

    @NotNull
    private Component getEditButtons() {
        if (!isViewerMember() || !hasPrivilege(Privilege.SET_BIO) && !hasPrivilege(Privilege.SET_GREETING)
            && !hasPrivilege(Privilege.SET_FAREWELL) && !hasPrivilege(Privilege.SET_RULES)) {
            return Component.empty();
        }
        return plugin.getLocales().getLocale("town_button_group_edit")
            .map(mineDown -> mineDown.toComponent().append(Component.space())).orElse(Component.empty())
            .append(hasPrivilege(Privilege.SET_BIO) ? plugin.getLocales().getLocale("town_button_bio",
                    town.getName())
                .map(mineDown -> mineDown.toComponent().append(Component.space()))
                .orElse(Component.empty()) : Component.empty())
            .append(hasPrivilege(Privilege.SET_GREETING) ? plugin.getLocales().getLocale("town_button_greeting",
                    town.getName())
                .map(mineDown -> mineDown.toComponent().append(Component.space()))
                .orElse(Component.empty()) : Component.empty())
            .append(hasPrivilege(Privilege.SET_FAREWELL) ? plugin.getLocales().getLocale("town_button_farewell",
                    town.getName())
                .map(mineDown -> mineDown.toComponent().append(Component.space()))
                .orElse(Component.empty()) : Component.empty())
            .append(hasPrivilege(Privilege.SET_RULES) ? plugin.getLocales().getLocale("town_button_rules",
                    town.getName())
                .map(MineDown::toComponent)
                .orElse(Component.empty()) : Component.empty())
            .appendNewline();
    }

    @NotNull
    private Component getSpawnButtons() {
        if (town.getSpawn().isEmpty()) {
            return Component.empty();
        }
        final Spawn spawn = town.getSpawn().get();
        if (!isViewerMember() && !spawn.isPublic()) {
            return Component.empty();
        }
        return plugin.getLocales().getLocale("town_button_group_spawn")
            .map(mineDown -> mineDown.toComponent().append(Component.space()))
            .orElse(Component.empty())
            .append((isViewerMember() && hasPrivilege(Privilege.SPAWN) || spawn.isPublic())
                ? plugin.getLocales().getLocale("town_button_spawn_teleport",
                    town.getName())
                .map(mineDown -> mineDown.toComponent().append(Component.space()))
                .orElse(Component.empty()) : Component.empty())
            .append((isViewerMember() && hasPrivilege(Privilege.SPAWN_PRIVACY))
                ? plugin.getLocales().getLocale("town_button_spawn_make_" + (spawn.isPublic() ? "private" : "public"),
                town.getName()).map(MineDown::toComponent).orElse(Component.empty()) : Component.empty());
    }

    private boolean isViewerMember() {
        if (viewer instanceof OnlineUser user) {
            return town.getMembers().containsKey(user.getUuid());
        }
        return false;
    }

    private boolean hasPrivilege(@NotNull Privilege privilege) {
        if (viewer instanceof OnlineUser user) {
            return plugin.getUserTown(user)
                .map(member -> member.hasPrivilege(plugin, privilege))
                .orElse(false);
        }
        return false;
    }

}
