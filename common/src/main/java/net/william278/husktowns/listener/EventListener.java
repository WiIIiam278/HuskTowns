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

package net.william278.husktowns.listener;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.advancement.Advancement;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.network.Broker;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import net.william278.husktowns.user.SavedUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class EventListener {

    protected final HuskTowns plugin;

    public EventListener(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    @NotNull
    protected OperationHandler handler() {
        return plugin.getOperationHandler();
    }

    protected void onPlayerJoin(@NotNull OnlineUser user) {
        plugin.runAsync(() -> {
            final Optional<SavedUser> userData = plugin.getDatabase().getUser(user.getUuid());
            if (userData.isEmpty()) {
                plugin.getDatabase().createUser(user, Preferences.getDefaults());
                plugin.setUserPreferences(user.getUuid(), Preferences.getDefaults());
                return;
            }

            // Update the user preferences if necessary
            final SavedUser savedUser = userData.get();
            final Preferences preferences = savedUser.preferences();
            boolean updateNeeded = !savedUser.user().getUsername().equals(user.getUsername());
            final Optional<Town> userTown = plugin.getUserTown(user).map(Member::town);

            // Notify if the user is in town chat, remove them if they are not in a town
            if (preferences.isTownChatTalking()) {
                if (userTown.isEmpty()) {
                    preferences.setTownChatTalking(false);
                    updateNeeded = true;
                } else {
                    plugin.getLocales().getLocale("town_chat_reminder")
                            .ifPresent(user::sendMessage);
                }
            }

            // Handle cross-server teleports
            if (plugin.getSettings().doCrossServer()) {
                if (plugin.getSettings().getBrokerType() == Broker.Type.PLUGIN_MESSAGE && plugin.getOnlineUsers().size() == 1) {
                    plugin.setLoaded(false);
                    plugin.loadData();
                }

                if (preferences.getTeleportTarget().isPresent()) {
                    final Position position = preferences.getTeleportTarget().get();
                    plugin.runSync(() -> {
                        user.teleportTo(position);
                        plugin.getLocales().getLocale("teleportation_complete").ifPresent(locale -> user
                                .sendMessage(plugin.getSettings().getNotificationSlot(), locale));
                    });

                    preferences.clearTeleportTarget();
                    updateNeeded = true;
                }
            }

            // Setup advancements
            final Optional<Advancement> advancements = plugin.getAdvancements();
            if (advancements.isPresent()) {
                final Advancement rootAdvancement = advancements.get();
                if (preferences.isCompletedAdvancement(rootAdvancement.getKey())) {
                    preferences.addCompletedAdvancement(rootAdvancement.getKey());
                    updateNeeded = true;
                }
                plugin.awardAdvancement(rootAdvancement, user);
            }

            // Save the user preferences
            plugin.setUserPreferences(user.getUuid(), preferences);
            if (updateNeeded) {
                plugin.getDatabase().updateUser(user, preferences);
            }

            // Check advancements
            userTown.ifPresent(town -> plugin.checkAdvancements(town, user));
        });
    }

    // When a player quits
    public void onPlayerQuit(@NotNull OnlineUser user) {
        plugin.getManager().wars().ifPresent(manager -> manager.handlePlayerQuit(user));
    }

    // When a player right clicks to inspect a claim
    protected void onPlayerInspect(@NotNull OnlineUser user, @NotNull Position position) {
        final Optional<TownClaim> claim = plugin.getClaimAt(position);
        if (claim.isPresent()) {
            final TownClaim townClaim = claim.get();
            final Claim claimData = townClaim.claim();
            plugin.highlightClaim(user, townClaim);
            if (townClaim.isAdminClaim(plugin)) {
                plugin.getLocales().getLocale("inspect_chunk_admin_claim",
                                Integer.toString(claimData.getChunk().getX()), Integer.toString(claimData.getChunk().getZ()))
                        .ifPresent(user::sendMessage);
                return;
            }
            plugin.getLocales().getLocale("inspect_chunk_claimed_" + claimData.getType().name().toLowerCase(),
                            Integer.toString(claimData.getChunk().getX()), Integer.toString(claimData.getChunk().getZ()),
                            townClaim.town().getName())
                    .ifPresent(user::sendMessage);
            return;
        }
        if (plugin.getClaimWorld(user.getWorld()).isEmpty()) {
            plugin.getLocales().getLocale("inspect_chunk_not_claimable")
                    .ifPresent(user::sendMessage);
            return;
        }
        plugin.getLocales().getLocale("inspect_chunk_not_claimed")
                .ifPresent(user::sendMessage);
    }

    // When a player uses SHIFT+RIGHT CLICK to inspect nearby claims
    protected void onPlayerInspectNearby(@NotNull OnlineUser user, @NotNull Chunk center, @NotNull World world) {
        final Optional<ClaimWorld> optionalClaimWorld = plugin.getClaimWorld(world);
        if (optionalClaimWorld.isEmpty()) {
            plugin.getLocales().getLocale("inspect_chunk_not_claimable")
                    .ifPresent(user::sendMessage);
            return;
        }
        final ClaimWorld claimWorld = optionalClaimWorld.get();

        final int radius = 1 + ((plugin.getSettings().getClaimMapWidth() + plugin.getSettings().getClaimMapHeight()) / 4);
        final List<TownClaim> nearbyClaims = claimWorld.getClaimsNear(center, radius, plugin.getPlugin());
        if (nearbyClaims.isEmpty()) {
            plugin.getLocales().getLocale("inspect_nearby_no_claims", Integer.toString(radius),
                            Integer.toString(center.getX()), Integer.toString(center.getZ()))
                    .ifPresent(user::sendMessage);
            return;
        }
        plugin.highlightClaims(user, nearbyClaims);
        plugin.getLocales().getLocale("inspect_nearby_claims", Integer.toString(nearbyClaims.size()),
                        Long.toString(nearbyClaims.stream().map(TownClaim::town).distinct().count()),
                        Integer.toString(radius), Integer.toString(center.getX()), Integer.toString(center.getZ()))
                .ifPresent(user::sendMessage);
    }

    public boolean handlePlayerChat(@NotNull OnlineUser user, @NotNull String message) {
        final Optional<Preferences> preferences = plugin.getUserPreferences(user.getUuid());
        if (preferences.isPresent() && preferences.get().isTownChatTalking()) {
            plugin.getManager().towns().sendChatMessage(user, message);
            return true;
        }
        return false;
    }

}
