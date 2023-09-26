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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OperationHandler {

    private static final String ADMIN_CLAIM_ACCESS_PERMISSION = "husktowns.admin_claim_access";
    private final HuskTowns plugin;

    public OperationHandler(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns whether to cancel an {@link Operation}
     *
     * @param operation the operation to check
     * @return whether to cancel the operation
     */
    public boolean cancelOperation(@NotNull Operation operation) {
        if (!plugin.isLoaded()) {
            operation.getUser().ifPresent(onlineUser -> plugin.getLocales().getLocale("error_not_loaded")
                    .ifPresent(onlineUser::sendMessage));
            return true;
        }
        final Optional<TownClaim> claim = plugin.getClaimAt(operation.getPosition());
        if (claim.isPresent()) {
            return cancelOperation(operation, claim.get());
        }
        final Optional<ClaimWorld> world = plugin.getClaimWorld(operation.getPosition().getWorld());
        if (world.isEmpty()) {
            if (plugin.getRulePresets().getUnclaimableWorldRules().cancelOperation(operation.getType(), plugin.getFlags())) {
                if (operation.isVerbose() && operation.getUser().isPresent()) {
                    plugin.getLocales().getLocale("operation_cancelled")
                            .ifPresent(operation.getUser().get()::sendMessage);
                }
                return true;
            }
            return false;
        }
        if (plugin.getRulePresets().getWildernessRules().cancelOperation(operation.getType(), plugin.getFlags())) {
            if (operation.isVerbose() && operation.getUser().isPresent()) {
                plugin.getLocales().getLocale("operation_cancelled")
                        .ifPresent(operation.getUser().get()::sendMessage);
            }
            return true;
        }
        return false;
    }

    /**
     * Returns whether to cancel an operation that takes place in a claim
     *
     * @param operation the operation to check
     * @param townClaim the claim to check
     * @return whether to cancel the operation
     */
    private boolean cancelOperation(@NotNull Operation operation, @NotNull TownClaim townClaim) {
        final Optional<OnlineUser> optionalUser = operation.getUser();
        final Town town = townClaim.town();
        final Claim claim = townClaim.claim();

        // If the operation is not allowed by the claim flags
        if (town.getRules().get(claim.getType()).cancelOperation(operation.getType(), plugin.getFlags())) {
            if (optionalUser.isEmpty()) {
                return true;
            }

            // Handle admin claims
            final OnlineUser user = optionalUser.get();
            if (townClaim.isAdminClaim(plugin) && user.hasPermission(ADMIN_CLAIM_ACCESS_PERMISSION)) {
                return false;
            }

            // Handle plot memberships
            final Claim.Type claimType = claim.getType();
            if (claimType == Claim.Type.PLOT && claim.isPlotMember(user.getUuid())) {
                return false;
            }

            // Handle ignoring claims
            if (plugin.getUserPreferences(user.getUuid()).map(Preferences::isIgnoringClaims).orElse(false)) {
                return false;
            }

            final Optional<Member> optionalMember = plugin.getUserTown(user);
            if (optionalMember.isEmpty()) {
                if (operation.isVerbose()) {
                    plugin.getLocales().getLocale("operation_cancelled_claimed",
                            town.getName()).ifPresent(user::sendMessage);
                }
                return true;
            }

            final Member member = optionalMember.get();
            if (!member.town().equals(town)) {
                if (operation.isVerbose()) {
                    plugin.getLocales().getLocale("operation_cancelled_claimed",
                            town.getName()).ifPresent(user::sendMessage);
                }
                return true;
            }

            if (!member.hasPrivilege(plugin, Privilege.TRUSTED_ACCESS)) {
                if (operation.isVerbose()) {
                    plugin.getLocales().getLocale("operation_cancelled_privileges")
                            .ifPresent(user::sendMessage);
                }
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Returns whether to cancel a natural occurrence between two chunks
     *
     * @param chunk1 the first chunk
     * @param chunk2 the second chunk
     * @param world  the world the chunks are in
     * @return whether to cancel the natural occurrence
     */
    public boolean cancelNature(@NotNull Chunk chunk1, @NotNull Chunk chunk2,
                                @NotNull World world) {
        if (plugin.getClaimWorld(world).isEmpty()) {
            return false;
        }
        final Optional<TownClaim> claim1 = plugin.getClaimAt(chunk1, world);
        final Optional<TownClaim> claim2 = plugin.getClaimAt(chunk2, world);
        if (claim1.isPresent() && claim2.isPresent()) {
            return !claim1.get().town().equals(claim2.get().town());
        }
        return !(claim1.isEmpty() && claim2.isEmpty());
    }

    private void doTownFly(@NotNull OnlineUser user, TownClaim fromClaim, TownClaim toClaim) {
        if (!user.isInSurvival()) return;
        Town srcTown = fromClaim != null ? fromClaim.town() : null;
        Town dstTown = toClaim != null ? toClaim.town() : null;
        final Town memberTown = plugin.getUserTown(user).map(Member::town).orElse(null);

        if (dstTown == null) { // Town gone!!! :(
            plugin.getUserPreferences(user.getUuid()).ifPresent(preferences -> {
                user.setFlying(false); // Stop user flight only if they are using town fly.
            });
            return;
        }

        if (dstTown == srcTown) { // Same town!!! :)
            return; // No town change
        }

        // Entering a town or changing towns
        plugin.getUserPreferences(user.getUuid()).ifPresent(preferences -> {// All joined users must have preferences
            user.setFlying(preferences.isTownFly() && dstTown.equals(memberTown));
        });
    }

    /**
     * Returns whether to cancel a player moving between chunks
     *
     * @param user the user
     * @param from the chunk the user is leaving
     * @param to   the chunk the user is entering
     * @return whether to cancel the chunk change
     */
    public boolean cancelChunkChange(@NotNull OnlineUser user, @NotNull Position from, @NotNull Position to) {
        final Optional<TownClaim> fromClaim = plugin.getClaimAt(from);
        final Optional<TownClaim> toClaim = plugin.getClaimAt(to);

        // Town fly
        doTownFly(user, fromClaim.orElse(null), toClaim.orElse(null));

        // Auto-claiming
        if (toClaim.isEmpty() && plugin.getUserPreferences(user.getUuid())
                .map(Preferences::isAutoClaimingLand)
                .orElse(false)) {
            plugin.getManager().claims().createClaim(user, to.getWorld(), to.getChunk(), false);
            return false;
        }
        if (fromClaim.map(TownClaim::town).equals(toClaim.map(TownClaim::town))) {
            return false;
        }

        // Claim entry messages
        if (toClaim.isPresent()) {
            final TownClaim entering = toClaim.get();
            if (plugin.fireIsCancelled(plugin.getPlayerEnterTownEvent(user, entering, from, to))) {
                return true;
            }

            final Town town = entering.town();

            if (town.isBanned(user.getUuid())) {
                plugin.getLocales().getLocale("banned_from_town", town.getName())
                        .ifPresent(user::sendMessage);
                return true;
            }
            final TextColor color = TextColor.fromHexString(town.getColorRgb());
            user.sendMessage(plugin.getSettings().getNotificationSlot(), Component.text(town.getName()).color(color));
            if (town.getGreeting().isPresent()) {
                user.sendMessage(Component.text(town.getGreeting().get()).color(color));
            } else {
                plugin.getLocales().getLocale("entering_town", town.getName(), town.getColorRgb())
                        .ifPresent(user::sendMessage);
            }
            return false;
        }

        // Town exit messages
        if (fromClaim.isPresent()) {
            final TownClaim leaving = fromClaim.get();
            if (plugin.fireIsCancelled(plugin.getPlayerLeaveTownEvent(user, leaving, from, to))) {
                return true;
            }

            final Town town = leaving.town();
            plugin.getLocales().getLocale("wilderness")
                    .ifPresent(locale -> user.sendMessage(plugin.getSettings().getNotificationSlot(), locale));
            if (town.getFarewell().isPresent()) {
                user.sendMessage(Component.text(town.getFarewell().get()).color(TextColor.fromHexString(town.getColorRgb())));
            } else {
                plugin.getLocales().getLocale("leaving_town", town.getName(), town.getColorRgb())
                        .ifPresent(user::sendMessage);
            }
        }
        return false;
    }


}
