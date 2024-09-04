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

package net.william278.husktowns.manager;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.advancement.Advancement;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.events.IMemberJoinEvent;
import net.william278.husktowns.map.ClaimMap;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import net.william278.husktowns.user.SavedUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Consumer;

public class AdminManager {
    private final HuskTowns plugin;

    protected AdminManager(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    private Optional<Town> getTownByName(@NotNull String townName) {
        return plugin.getTowns().stream()
            .filter(town -> town.getName().equalsIgnoreCase(townName))
            .findFirst();
    }

    public void createAdminClaim(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, boolean showMap) {
        final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
        if (existingClaim.isPresent()) {
            plugin.getLocales().getLocale("error_chunk_claimed_by", existingClaim.get().town().getName())
                .ifPresent(user::sendMessage);
            return;
        }

        final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
        if (claimWorld.isEmpty()) {
            plugin.getLocales().getLocale("error_world_not_claimable")
                .ifPresent(user::sendMessage);
            return;
        }

        plugin.runAsync(() -> {
            final TownClaim claim = TownClaim.admin(chunk, plugin);
            plugin.getManager().claims().createClaimData(user, claim, world);
            plugin.getLocales().getLocale("admin_claim_created",
                    Integer.toString(chunk.getX()), Integer.toString(chunk.getZ()))
                .ifPresent(user::sendMessage);
            plugin.highlightClaim(user, claim);
            if (showMap) {
                user.sendMessage(ClaimMap.builder(plugin)
                    .center(user.getChunk()).world(user.getWorld())
                    .build()
                    .toComponent(user));
            }
        });
    }

    public void deleteClaim(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, boolean showMap) {
        final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
        if (existingClaim.isEmpty()) {
            plugin.getLocales().getLocale("error_chunk_not_claimed")
                .ifPresent(user::sendMessage);
            return;
        }

        final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
        assert claimWorld.isPresent();

        plugin.runAsync(() -> {
            plugin.getManager().claims().deleteClaimData(user, existingClaim.get(), world);
            plugin.getLocales().getLocale("claim_deleted", Integer.toString(chunk.getX()),
                Integer.toString(chunk.getZ())).ifPresent(user::sendMessage);
            if (showMap) {
                user.sendMessage(ClaimMap.builder(plugin)
                    .center(user.getChunk()).world(user.getWorld())
                    .build()
                    .toComponent(user));
            }
        });
    }

    public void deleteAllClaims(@NotNull OnlineUser user, @NotNull String townName) {
        getTownByName(townName).ifPresentOrElse(
            town -> plugin.getManager().claims().deleteAllClaims(user, town),
            () -> plugin.getLocales().getLocale("error_town_not_found", townName)
                .ifPresent(user::sendMessage));
    }

    public void deleteTown(@NotNull OnlineUser user, @NotNull String townName) {
        getTownByName(townName).ifPresentOrElse(
            town -> plugin.getManager().towns().deleteTown(user, town),
            () -> plugin.getLocales().getLocale("error_town_not_found", townName)
                .ifPresent(user::sendMessage));
    }

    public void takeOverTown(@NotNull OnlineUser user, @NotNull String townName) {
        getTownByName(townName).ifPresentOrElse(namedTown -> {
            final Optional<Member> existingMembership = plugin.getUserTown(user);
            if (existingMembership.isPresent()) {
                plugin.getLocales().getLocale("error_already_in_town")
                    .ifPresent(user::sendMessage);
                return;
            }

            final Role mayorRole = plugin.getRoles().getMayorRole();
            plugin.fireEvent(plugin.getMemberJoinEvent(user, namedTown, mayorRole, IMemberJoinEvent.JoinReason.ADMIN_TAKE_OVER),
                (event -> plugin.getManager().editTown(user, namedTown, (town -> {
                    town.getMembers().put(town.getMayor(), plugin.getRoles().getDefaultRole().getWeight());
                    town.getMembers().put(user.getUuid(), mayorRole.getWeight());
                    town.getLog().log(Action.of(user, Action.Type.ADMIN_TAKE_OVER, user.getUsername()));
                    plugin.getLocales().getLocale("town_assumed_ownership", town.getName())
                        .ifPresent(user::sendMessage);
                }))));
        }, () -> plugin.getLocales().getLocale("error_town_not_found", townName)
            .ifPresent(user::sendMessage));
    }

    public void setTownLevel(@NotNull OnlineUser user, @NotNull String townName, int level) throws IllegalArgumentException {
        if (level < 1 || level > plugin.getLevels().getMaxLevel()) {
            throw new IllegalArgumentException("Level must be between 1 and " + plugin.getLevels().getMaxLevel());
        }

        final Optional<Town> optionalTown = getTownByName(townName);
        if (optionalTown.isEmpty()) {
            plugin.getLocales().getLocale("error_town_not_found", townName)
                .ifPresent(user::sendMessage);
            return;
        }

        plugin.runAsync(() -> plugin.getManager().editTown(user, optionalTown.get(), (town -> {
            town.setLevel(level);
            town.getLog().log(Action.of(user, Action.Type.ADMIN_SET_LEVEL, Integer.toString(level)));
            plugin.getLocales().getLocale("town_levelled_up", town.getName(), Integer.toString(level))
                .ifPresent(user::sendMessage);
        })));
    }

    public void setTownBonus(@NotNull CommandUser user, @NotNull String townName, @NotNull Town.Bonus bonus, int amount) {
        final int value = Math.max(amount, 0);
        final boolean clearing = value == 0;
        final Optional<Town> optionalTown = getTownByName(townName);
        if (optionalTown.isEmpty()) {
            plugin.getLocales().getLocale("error_town_not_found", townName)
                .ifPresent(user::sendMessage);
            return;
        }

        final OnlineUser onlineUser = user instanceof OnlineUser ? (OnlineUser) user : plugin.getOnlineUsers().stream()
            .findFirst()
            .orElse(null);
        if (onlineUser == null) {
            plugin.getLocales().getLocale("error_no_online_players")
                .ifPresent(user::sendMessage);
            return;
        }
        plugin.runAsync(() -> plugin.getManager().editTown(onlineUser, optionalTown.get(), (town -> {
            final String bonusLog = bonus.name().toLowerCase() + ": " + value;
            final Action.Type action = !clearing ? Action.Type.ADMIN_SET_BONUS : Action.Type.ADMIN_CLEAR_BONUS;
            if (user instanceof OnlineUser) {
                town.getLog().log(Action.of(onlineUser, action, bonusLog));
            } else {
                town.getLog().log(Action.of(action, bonusLog));
            }
            town.setBonus(bonus, value);

            if (!clearing) {
                plugin.getLocales().getLocale("town_bonus_set", bonus.name().toLowerCase(), town.getName(),
                        Integer.toString(value))
                    .ifPresent(user::sendMessage);
            } else {
                plugin.getLocales().getLocale("town_bonus_cleared", bonus.name().toLowerCase(), town.getName())
                    .ifPresent(user::sendMessage);
            }
        })));
    }

    public void addTownBonus(@NotNull CommandUser user, @NotNull String townName, @NotNull Town.Bonus bonus, int amount) {
        final Optional<Town> town = getTownByName(townName);
        if (town.isEmpty()) {
            plugin.getLocales().getLocale("error_town_not_found", townName)
                .ifPresent(user::sendMessage);
            return;
        }
        setTownBonus(user, townName, bonus, town.get().getBonus(bonus) + amount);
    }

    public void removeTownBonus(@NotNull CommandUser user, @NotNull String townName, @NotNull Town.Bonus bonus, int amount) {
        addTownBonus(user, townName, bonus, -amount);
    }

    public void clearTownBonus(@NotNull CommandUser user, @NotNull String townName, @NotNull Town.Bonus bonus) {
        setTownBonus(user, townName, bonus, 0);
    }

    public void viewTownBonus(@NotNull CommandUser user, @NotNull String townName) {
        final Optional<Town> optionalTown = getTownByName(townName);
        if (optionalTown.isEmpty()) {
            plugin.getLocales().getLocale("error_town_not_found", townName)
                .ifPresent(user::sendMessage);
            return;
        }
        final Town town = optionalTown.get();

        // Send the town bonus entries for the town
        Component component = Component.empty();
        int skipped = 0;
        for (final Town.Bonus bonus : Town.Bonus.values()) {
            final int value = town.getBonus(bonus);
            if (value == 0) {
                skipped++;
                continue;
            }
            component = component.append(plugin.getLocales().getLocale("town_bonus_entry",
                    bonus.name().toLowerCase(), Integer.toString(value))
                .map(MineDown::toComponent)
                .map(Component::appendNewline)
                .orElse(Component.empty()));
        }

        // If no entries were displayed
        if (skipped >= Town.Bonus.values().length) {
            plugin.getLocales().getLocale("error_no_town_bonuses", town.getName())
                .ifPresent(user::sendMessage);
            return;
        }

        plugin.getLocales().getLocale("town_bonus_list", town.getName())
            .map(MineDown::toComponent)
            .ifPresent(user::sendMessage);
        user.sendMessage(component);
    }

    public void sendLocalSpyMessage(@NotNull Town town, @NotNull Member sender, @NotNull String text) {
        plugin.getOnlineUsers().stream()
            .filter(user -> !town.getMembers().containsKey(user.getUuid()))
            .filter(user -> plugin.getUserPreferences(user.getUuid()).orElse(Preferences.getDefaults()).isTownChatSpying())
            .forEach(user -> plugin.getLocales().getLocale("town_chat_spy_message_format",
                    town.getName(), sender.user().getUsername(), sender.role().getName(), text)
                .ifPresent(user::sendMessage));
    }

    public void setTownBalance(@NotNull CommandUser user, @NotNull String townName, @NotNull BigDecimal amount) {
        final Optional<Town> toEdit = getTownByName(townName);
        if (toEdit.isEmpty()) {
            plugin.getLocales().getLocale("error_town_not_found", townName)
                .ifPresent(user::sendMessage);
            return;
        }

        // Operation to be applied to the town
        final Consumer<Town> moneySetter = (town) -> {
            town.setMoney(amount.max(BigDecimal.ZERO));
            town.getLog().log(Action.of(Action.Type.ADMIN_SET_BALANCE, plugin.formatMoney(amount)));
            plugin.getLocales().getLocale("town_economy_set", town.getName(), plugin.formatMoney(amount))
                .ifPresent(user::sendMessage);
        };

        // Execute the operation with an online user if applicable
        plugin.runAsync(() -> {
            final OnlineUser editor = user instanceof OnlineUser online
                ? online : plugin.getOnlineUsers().stream().findAny().orElse(null);
            if (editor != null) {
                plugin.getManager().editTown(editor, toEdit.get(), moneySetter);
                return;
            }

            final Town town = toEdit.get();
            moneySetter.accept(town);
            plugin.updateTown(town);
            plugin.getDatabase().updateTown(town);
        });
    }

    public void changeTownBalance(@NotNull CommandUser user, @NotNull String townName, @NotNull BigDecimal amount) {
        final Optional<Town> town = getTownByName(townName);
        if (town.isEmpty()) {
            plugin.getLocales().getLocale("error_town_not_found", townName)
                .ifPresent(user::sendMessage);
            return;
        }
        this.setTownBalance(user, townName, town.get().getMoney().add(amount));
    }

    public void listAdvancements(@NotNull CommandUser executor, @Nullable String username) {
        final Advancement root = plugin.getAdvancements().orElseThrow(() -> new IllegalStateException("Advancements are not enabled"));

        Preferences preferences = null;
        if (username != null) {
            final Optional<SavedUser> user = plugin.getDatabase().getUser(username);
            if (user.isEmpty()) {
                plugin.getLocales().getLocale("error_user_not_found", username)
                    .ifPresent(executor::sendMessage);
                return;
            }
            preferences = user.get().preferences();
            plugin.getLocales().getLocale("advancements_list_title_user", user.get().user().getUsername())
                .map(MineDown::toComponent)
                .ifPresent(executor::sendMessage);
        } else {
            plugin.getLocales().getLocale("advancements_list_title")
                .map(MineDown::toComponent)
                .ifPresent(executor::sendMessage);
        }

        displayAdvancementTree(executor, preferences, root, 0);
    }

    private void displayAdvancementTree(@NotNull CommandUser user, @Nullable Preferences preferences,
                                        @NotNull Advancement advancement, int indentation) {
        final String prefix = " ".repeat(Math.max(0, indentation));
        TextColor color = switch (advancement.getFrame()) {
            case TASK -> TextColor.color(132, 132, 132);
            case GOAL -> TextColor.color(45, 215, 137);
            case CHALLENGE -> TextColor.color(215, 215, 0);
        };
        final Component title = Component.text(advancement.getTitle()).color(color);
        final Component description = Component.text(advancement.getDescription()).color(color);
        user.sendMessage(Component.text(prefix).append(title).hoverEvent(HoverEvent.showText(description)));

        for (final Advancement child : advancement.getChildren()) {
            if (preferences != null && !preferences.isCompletedAdvancement(advancement.getKey())) {
                continue;
            }
            displayAdvancementTree(user, preferences, child, indentation + 2);
        }
    }

    public void resetAdvancements(@NotNull CommandUser executor, @NotNull String user) {

        final Optional<SavedUser> optionalUser = plugin.getDatabase().getUser(user);
        if (optionalUser.isEmpty()) {
            plugin.getLocales().getLocale("error_user_not_found", user)
                .ifPresent(executor::sendMessage);
            return;
        }
        final SavedUser savedUser = optionalUser.get();
        plugin.getAdvancements().ifPresent(root -> {
            savedUser.preferences().resetAdvancements();
            plugin.getDatabase().updateUser(savedUser.user(), savedUser.preferences());
            plugin.getLocales().getLocale("advancements_reset_user", savedUser.user().getUsername(), root.getKey())
                .ifPresent(executor::sendMessage);
        });
    }
}
