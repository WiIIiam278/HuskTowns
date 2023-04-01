package net.william278.husktowns.hook;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.listener.Operation;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class PlaceholderAPIHook extends Hook {
    public PlaceholderAPIHook(@NotNull HuskTowns plugin) {
        super(plugin, "PlaceholderAPI");
    }

    @Override
    protected void onEnable() {
        new HuskTownsExpansion(plugin).register();
    }

    public static class HuskTownsExpansion extends PlaceholderExpansion {

        private static final String NOT_IN_TOWN_COLOR = "#aaaaaa";
        private static final String WILDERNESS_COLOR = "#2e2e2e";

        @NotNull
        private final HuskTowns plugin;

        private HuskTownsExpansion(@NotNull HuskTowns plugin) {
            this.plugin = plugin;
        }

        @Override
        public String onRequest(@Nullable OfflinePlayer offlinePlayer, @NotNull String params) {
            if (!plugin.isLoaded()) {
                return plugin.getLocales().getRawLocale("not_applicable").orElse("N/A");
            }

            // Ensure the player is online
            if (offlinePlayer == null || !offlinePlayer.isOnline() || offlinePlayer.getPlayer() == null) {
                return plugin.getLocales().getRawLocale("placeholder_player_offline")
                        .orElse("Player offline");
            }

            // Return the requested placeholder
            final OnlineUser player = BukkitUser.adapt(offlinePlayer.getPlayer());
            return switch (params) {
                case "town_name" -> plugin.getUserTown(player)
                        .map(Member::town)
                        .map(Town::getName)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                                .orElse("Not in town"));

                case "town_role" -> plugin.getUserTown(player)
                        .map(Member::role)
                        .map(Role::getName)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                                .orElse("Not in town"));

                case "town_mayor" -> plugin.getUserTown(player)
                        .map(Member::town)
                        .map(town -> resolveTownMemberName(town, town.getMayor()).orElse("?"))
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                                .orElse("Not in town"));

                case "town_color" -> plugin.getUserTown(player)
                        .map(Member::town)
                        .map(Town::getColorRgb)
                        .orElse(NOT_IN_TOWN_COLOR);

                case "town_members" -> plugin.getUserTown(player)
                        .map(Member::town)
                        .map(town -> town.getMembers().keySet().stream()
                                .map(uuid -> resolveTownMemberName(town, uuid).orElse("?"))
                                .collect(Collectors.joining(", ")))
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                                .orElse("Not in town"));

                case "town_member_count" -> plugin.getUserTown(player)
                        .map(Member::town)
                        .map(Town::getMembers)
                        .map(members -> String.valueOf(members.size()))
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                                .orElse("Not in town"));

                case "town_claim_count" -> plugin.getUserTown(player)
                        .map(Member::town)
                        .map(Town::getClaimCount)
                        .map(String::valueOf)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                                .orElse("Not in town"));

                case "town_max_claims" -> plugin.getUserTown(player)
                        .map(Member::town)
                        .map(town -> town.getMaxClaims(plugin))
                        .map(String::valueOf)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                                .orElse("Not in town"));

                case "town_max_members" -> plugin.getUserTown(player)
                        .map(Member::town)
                        .map(town -> town.getMaxMembers(plugin))
                        .map(String::valueOf)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                                .orElse("Not in town"));

                case "town_money" -> plugin.getUserTown(player)
                        .map(Member::town)
                        .map(Town::getMoney)
                        .map(String::valueOf)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                                .orElse("Not in town"));

                case "town_level_up_cost" -> plugin.getUserTown(player)
                        .map(Member::town)
                        .map(town -> plugin.getLevels().getLevelUpCost(town.getLevel()))
                        .map(String::valueOf)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                                .orElse("Not in town"));

                case "town_level" -> plugin.getUserTown(player)
                        .map(Member::town)
                        .map(Town::getLevel)
                        .map(String::valueOf)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                                .orElse("Not in town"));

                case "town_max_level" -> plugin.getUserTown(player)
                        .map(Member::town)
                        .map(town -> plugin.getLevels().getMaxLevel())
                        .map(String::valueOf)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                                .orElse("Not in town"));

                case "current_location_town" -> plugin.getClaimAt(player.getPosition())
                        .map(TownClaim::town)
                        .map(Town::getName)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_wilderness")
                                .orElse("Wilderness"));

                case "current_location_can_build" -> getBooleanValue(!plugin.getOperationHandler()
                        .cancelOperation(Operation.of(player, Operation.Type.BLOCK_PLACE, player.getPosition())));

                case "current_location_can_interact" -> getBooleanValue(!plugin.getOperationHandler()
                        .cancelOperation(Operation.of(player, Operation.Type.BLOCK_INTERACT, player.getPosition())));

                case "current_location_can_open_containers" -> getBooleanValue(!plugin.getOperationHandler()
                        .cancelOperation(Operation.of(player, Operation.Type.CONTAINER_OPEN, player.getPosition())));

                case "current_location_claim_type" -> plugin.getClaimAt(player.getPosition())
                        .map(TownClaim::claim)
                        .map(Claim::getType)
                        .map(Claim.Type::name)
                        .map(String::toLowerCase)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_wilderness")
                                .orElse("Wilderness"));

                case "current_location_plot_members" -> plugin.getClaimAt(player.getPosition())
                        .map(townClaim -> {
                            final Claim claim = townClaim.claim();
                            if (claim.getType() == Claim.Type.PLOT) {
                                return plugin.getLocales().getRawLocale("placeholder_not_a_plot")
                                        .orElse("Not a plot");
                            }

                            return claim.getPlotMembers().stream()
                                    .map(user -> resolveTownMemberName(townClaim.town(), user).orElse("?"))
                                    .collect(Collectors.joining(", "));
                        })
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_claimed")
                                .orElse("Not claimed"));

                case "current_location_plot_managers" -> plugin.getClaimAt(player.getPosition())
                        .map(townClaim -> {
                            final Claim claim = townClaim.claim();
                            if (claim.getType() == Claim.Type.PLOT) {
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

                case "current_location_town_color" -> plugin.getClaimAt(player.getPosition())
                        .map(TownClaim::town)
                        .map(Town::getColorRgb)
                        .orElse(WILDERNESS_COLOR);

                case "current_location_town_money" -> plugin.getClaimAt(player.getPosition())
                        .map(TownClaim::town)
                        .map(Town::getMoney)
                        .map(String::valueOf)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_claimed")
                                .orElse("Not claimed"));

                case "current_location_town_level" -> plugin.getClaimAt(player.getPosition())
                        .map(TownClaim::town)
                        .map(Town::getLevel)
                        .map(String::valueOf)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_claimed")
                                .orElse("Not claimed"));

                case "current_location_town_level_up_cost" -> plugin.getClaimAt(player.getPosition())
                        .map(TownClaim::town)
                        .map(town -> plugin.getLevels().getLevelUpCost(town.getLevel()))
                        .map(String::valueOf)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_in_town")
                                .orElse("Not in town"));

                case "current_location_town_max_claims" -> plugin.getClaimAt(player.getPosition())
                        .map(TownClaim::town)
                        .map(town -> town.getMaxClaims(plugin))
                        .map(String::valueOf)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_claimed")
                                .orElse("Not claimed"));

                case "current_location_town_max_members" -> plugin.getClaimAt(player.getPosition())
                        .map(TownClaim::town)
                        .map(town -> town.getMaxMembers(plugin))
                        .map(String::valueOf)
                        .orElse(plugin.getLocales().getRawLocale("placeholder_not_claimed")
                                .orElse("Not claimed"));

                default -> params.startsWith("town_leaderboard_") ? getTownLeaderboard(params.substring(17)) : null;
            };
        }

        // Get a town leaderboard
        @Nullable
        private String getTownLeaderboard(@NotNull String identifier) {
            // Get the identifier up to the last underscore
            final int lastUnderscore = identifier.lastIndexOf('_');
            if (lastUnderscore == -1) {
                return null;
            }

            // Get the leaderboard index and return the sorted list element at the index
            try {
                final int leaderboardIndex = Math.max(1, Integer.parseInt(identifier.substring(lastUnderscore + 1)));
                final List<Town> towns = getSortedTownList(identifier.substring(0, lastUnderscore));
                if (towns == null) {
                    return null;
                }

                return towns.size() >= leaderboardIndex ? towns.get(leaderboardIndex - 1).getName()
                        : plugin.getLocales().getRawLocale("not_applicable").orElse("N/A");
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

        // Resolve a cached town member name from a UUID
        private Optional<String> resolveTownMemberName(@NotNull Town town, @NotNull UUID uuid) {
            return town.getLog().getActions().values().stream()
                    .map(Action::getUser).filter(Optional::isPresent).map(Optional::get)
                    .filter(user -> user.getUuid().equals(uuid))
                    .map(User::getUsername)
                    .findFirst();
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
