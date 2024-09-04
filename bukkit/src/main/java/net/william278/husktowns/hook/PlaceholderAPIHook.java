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

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.william278.cloplib.operation.Operation;
import net.william278.cloplib.operation.OperationType;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PlaceholderAPIHook extends Hook {
    @PluginHook(id = "PlaceholderAPI", register = PluginHook.Register.ON_ENABLE, platform = "bukkit")
    public PlaceholderAPIHook(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @Override
    protected void onEnable() {
        plugin.runSync(() -> new HuskTownsExpansion(plugin).register());
    }

    public static class HuskTownsExpansion extends PlaceholderExpansion {

        private static final String NOT_IN_TOWN_COLOR = "#aaaaaa";
        private static final String WILDERNESS_COLOR = "#2e2e2e";
        private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);
        private static final String DECIMAL_FORMAT = "#.#";
        private static final TreeMap<BigDecimal, String> NUMBER_FORMAT = new TreeMap<>();

        static {
            NUMBER_FORMAT.put(THOUSAND, "k");
            NUMBER_FORMAT.put(THOUSAND.pow(2), "M");
            NUMBER_FORMAT.put(THOUSAND.pow(3), "G");
            NUMBER_FORMAT.put(THOUSAND.pow(4), "T");
            NUMBER_FORMAT.put(THOUSAND.pow(5), "Q");
        }

        @NotNull
        private final HuskTowns plugin;

        private HuskTownsExpansion(@NotNull HuskTowns plugin) {
            this.plugin = plugin;
        }

        @Override
        public String onRequest(@Nullable OfflinePlayer offlinePlayer, @NotNull String params) {
            if (!plugin.isLoaded()) {
                return plugin.getLocales().getNotApplicable();
            }

            if (params.startsWith("town_leaderboard_")) {
                if (params.length() == 17) {
                    return null;
                }
                return getTownLeaderboard(params.substring(17));
            }

            // Ensure the player is online
            if (offlinePlayer == null || !offlinePlayer.isOnline() || offlinePlayer.getPlayer() == null) {
                return plugin.getLocales().getRawLocale("placeholder_player_offline")
                    .orElse("Player offline");
            }

            // Return the requested placeholder
            final OnlineUser player = BukkitUser.adapt(offlinePlayer.getPlayer(), plugin);

            if (params.startsWith("town_")) {
                if (params.length() == 5) {
                    return null;
                }
                return getTown(player, params.substring(5));
            }

            if (params.startsWith("current_location_")) {
                if (params.length() == 17) {
                    return null;
                }
                return getCurrentLocation(player, params.substring(17));
            }

            return null;
        }

        @Nullable
        private String getTown(@NotNull OnlineUser player, @NotNull String identifier) {
            return switch (identifier) {
                case "name" -> plugin.getUserTown(player)
                    .map(Member::town)
                    .map(Town::getName)
                    .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                        .orElse("Not in town"));

                case "role" -> plugin.getUserTown(player)
                    .map(Member::role)
                    .map(Role::getName)
                    .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                        .orElse("Not in town"));

                case "color" -> plugin.getUserTown(player)
                    .map(Member::town)
                    .map(Town::getColorRgb)
                    .orElse(NOT_IN_TOWN_COLOR);

                default -> plugin.getUserTown(player)
                    .map(Member::town)
                    .map(town -> resolveTownData(town, identifier))
                    .map(String::valueOf)
                    .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                        .orElse("Not in town"));
            };
        }

        @Nullable
        public String getCurrentLocation(@NotNull OnlineUser player, @NotNull String identifier) {
            return switch (identifier) {
                case "town" -> plugin.getClaimAt(player.getPosition())
                    .map(TownClaim::town)
                    .map(Town::getName)
                    .orElse(plugin.getLocales().getRawLocale("placeholder_wilderness")
                        .orElse("Wilderness"));

                case "can_build" -> getBooleanValue(!plugin
                    .cancelOperation(Operation.of(player, OperationType.BLOCK_PLACE, player.getPosition(), true)));

                case "can_interact" -> getBooleanValue(!plugin.cancelOperation(
                    Operation.of(player, OperationType.BLOCK_INTERACT, player.getPosition(), true)
                ));

                case "can_open_containers" -> getBooleanValue(!plugin.cancelOperation(
                    Operation.of(player, OperationType.CONTAINER_OPEN, player.getPosition(), true)
                ));

                case "claim_type" -> plugin.getClaimAt(player.getPosition())
                    .map(TownClaim::claim)
                    .map(Claim::getType)
                    .map(Claim.Type::name)
                    .map(String::toLowerCase)
                    .orElse(plugin.getLocales().getRawLocale("placeholder_wilderness")
                        .orElse("Wilderness"));

                case "plot_members" -> plugin.getClaimAt(player.getPosition())
                    .map(townClaim -> {
                        final Claim claim = townClaim.claim();
                        if (claim.getType() != Claim.Type.PLOT) {
                            return plugin.getLocales().getRawLocale("placeholder_not_a_plot")
                                .orElse("Not a plot");
                        }

                        return claim.getPlotMembers().stream()
                            .map(user -> resolveTownMemberName(townClaim.town(), user).orElse("?"))
                            .collect(Collectors.joining(", "));
                    })
                    .orElse(plugin.getLocales().getRawLocale("placeholder_not_claimed")
                        .orElse("Not claimed"));

                case "plot_managers" -> plugin.getClaimAt(player.getPosition())
                    .map(townClaim -> {
                        final Claim claim = townClaim.claim();
                        if (claim.getType() != Claim.Type.PLOT) {
                            return plugin.getLocales().getRawLocale("placeholder_not_a_plot")
                                .orElse("Not a plot");
                        }

                        return claim.getPlotMembers().stream()
                            .filter(claim::isPlotManager)
                            .map(user -> resolveTownMemberName(townClaim.town(), user).orElse("?"))
                            .collect(Collectors.joining(", "));
                    })
                    .orElse(plugin.getLocales().getRawLocale("placeholder_not_claimed")
                        .orElse("Not claimed"));

                case "town_color" -> plugin.getClaimAt(player.getPosition())
                    .map(TownClaim::town)
                    .map(Town::getColorRgb)
                    .orElse(WILDERNESS_COLOR);

                default -> identifier.startsWith("town_") ? plugin.getClaimAt(player.getPosition())
                    .map(TownClaim::town)
                    .map(town -> resolveTownData(town, identifier.substring(5)))
                    .map(String::valueOf)
                    .orElse(plugin.getLocales().getRawLocale("placeholder_not_claimed")
                        .orElse("Not claimed")) : null;
            };
        }

        // Get a town leaderboard
        @Nullable
        private String getTownLeaderboard(@NotNull String identifier) {
            final String[] split = identifier.split("_", 3);
            if (split.length < 2) {
                return null;
            }

            // Get the leaderboard index and return the sorted list element at the index
            try {
                final int leaderboardIndex = Math.max(1, Integer.parseInt(split[1]));
                final List<Town> towns = getSortedTownList(split[0]);
                if (towns == null) {
                    return null;
                }

                if (towns.size() >= leaderboardIndex) {
                    final Town town = towns.get(leaderboardIndex - 1);
                    if (split.length > 2) {
                        return String.valueOf(resolveTownData(town, split[2]));
                    } else {
                        return town.getName();
                    }
                } else {
                    return plugin.getLocales().getNotApplicable();
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Nullable
        private List<Town> getSortedTownList(@NotNull String sortingKey) {
            return switch (sortingKey.toLowerCase(Locale.ENGLISH)) {
                case "money" -> plugin.getTowns().stream()
                    .sorted(Comparator.comparing(Town::getMoney).reversed())
                    .collect(Collectors.toList());
                case "level" -> plugin.getTowns().stream()
                    .sorted(Comparator.comparingInt(Town::getLevel).reversed())
                    .collect(Collectors.toList());
                case "claims" -> plugin.getTowns().stream()
                    .sorted(Comparator.comparingInt(Town::getClaimCount).reversed())
                    .collect(Collectors.toList());
                case "members" -> plugin.getTowns().stream()
                    .sorted(Comparator.comparingInt(town -> ((Town) town).getMembers().size()).reversed())
                    .collect(Collectors.toList());
                default -> null;
            };
        }

        @NotNull
        public static String formatNumber(@NotNull BigDecimal number) {
            final Map.Entry<BigDecimal, String> format = NUMBER_FORMAT.floorEntry(number);
            if (format != null) {
                return new DecimalFormat(DECIMAL_FORMAT).format(number.divide(format.getKey().divide(THOUSAND, RoundingMode.DOWN), RoundingMode.DOWN).doubleValue() / 1000.0) + format.getValue();
            } else {
                return number.toString();
            }
        }

        // Resolve a cached town member name from a UUID
        private Optional<String> resolveTownMemberName(@NotNull Town town, @NotNull UUID uuid) {
            return town.getLog().getActions().values().stream()
                .map(Action::getUser).filter(Optional::isPresent).map(Optional::get)
                .filter(user -> user.getUuid().equals(uuid))
                .map(User::getUsername)
                .findFirst();
        }

        @Nullable
        public Object resolveTownData(@NotNull Town town, @NotNull String identifier) {
            return switch (identifier) {
                case "name" -> town.getName();

                case "mayor" -> resolveTownMemberName(town, town.getMayor()).orElse("?");

                case "members" -> town.getMembers().keySet().stream()
                    .map(uuid -> resolveTownMemberName(town, uuid).orElse("?"))
                    .collect(Collectors.joining(", "));

                case "member_count" -> town.getMembers().size();

                case "claim_count" -> town.getClaimCount();

                case "max_claims" -> town.getMaxClaims(plugin);

                case "max_members" -> town.getMaxMembers(plugin);

                case "crop_growth_rate" -> town.getCropGrowthRate(plugin);

                case "mob_spawner_rate" -> town.getMobSpawnerRate(plugin);

                case "money" -> plugin.formatMoney(town.getMoney());

                case "money_formatted" -> formatNumber(town.getMoney());

                case "level_up_cost" -> town.getLevel() >= plugin.getLevels().getMaxLevel()
                    ? plugin.getLocales().getNotApplicable()
                    : plugin.formatMoney(plugin.getLevels().getLevelUpCost(town.getLevel()));

                case "level" -> town.getLevel();

                case "max_level" -> plugin.getLevels().getMaxLevel();

                default -> null;
            };
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        @NotNull
        public String getIdentifier() {
            return "husktowns";
        }

        @Override
        @NotNull
        public String getAuthor() {
            return "William278";
        }

        @Override
        @NotNull
        public String getVersion() {
            return plugin.getVersion().toStringWithoutMetadata();
        }

        @NotNull
        private String getBooleanValue(final boolean bool) {
            return bool ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        }

    }

}
