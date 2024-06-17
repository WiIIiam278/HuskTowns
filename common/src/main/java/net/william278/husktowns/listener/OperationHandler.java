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
import net.william278.cloplib.handler.ChunkHandler;
import net.william278.cloplib.operation.Operation;
import net.william278.cloplib.operation.OperationChunk;
import net.william278.cloplib.operation.OperationUser;
import net.william278.cloplib.operation.OperationWorld;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface OperationHandler extends ChunkHandler {

    String ADMIN_CLAIM_ACCESS_PERMISSION = "husktowns.admin_claim_access";

    /**
     * Returns whether to cancel a player moving between chunks
     *
     * @param operationUser the user
     * @param chunk1        the chunk the user is leaving
     * @param chunk2        the chunk the user is entering
     * @return whether to cancel the chunk change
     */
    @Override
    default boolean cancelChunkChange(@NotNull OperationUser operationUser, @NotNull OperationChunk chunk1,
                                      @NotNull OperationChunk chunk2) {
        final OnlineUser user = (OnlineUser) operationUser;
        final Chunk from = (Chunk) chunk1;
        final Chunk to = (Chunk) chunk2;
        if (from.equals(to)) {
            return false;
        }

        // Handle wars
        final Optional<TownClaim> fromClaim = getPlugin().getClaimAt(from, user.getWorld());
        final Optional<TownClaim> toClaim = getPlugin().getClaimAt(to, user.getWorld());
        final Locales.Slot notificationSlot = getPlugin().getSettings().getGeneral().getNotificationSlot();
        getPlugin().getManager().wars().ifPresent(wars -> wars.handlePlayerFlee(user));

        // Auto-claiming
        if (toClaim.isEmpty() && getPlugin().getUserPreferences(user.getUuid())
            .map(Preferences::isAutoClaimingLand)
            .orElse(false)) {
            getPlugin().getManager().claims().createClaim(user, user.getWorld(), to, false);
            return false;
        }
        if (fromClaim.map(TownClaim::town).equals(toClaim.map(TownClaim::town))) {
            return false;
        }

        // Claim entry messages
        if (toClaim.isPresent()) {
            final TownClaim entering = toClaim.get();
            if (getPlugin().fireIsCancelled(getPlugin().getPlayerEnterTownEvent(user, entering, user.getPosition(), user.getPosition()))) {
                return true;
            }

            final Town town = entering.town();
            final TextColor color = TextColor.fromHexString(town.getColorRgb());
            user.sendMessage(notificationSlot, Component.text(town.getName()).color(color));
            if (town.getGreeting().isPresent()) {
                user.sendMessage(Component.text(town.getGreeting().get()).color(color));
            } else {
                getPlugin().getLocales().getLocale("entering_town", town.getName(), town.getColorRgb())
                    .ifPresent(user::sendMessage);
            }
            return false;
        }

        // Town exit messages
        if (fromClaim.isPresent()) {
            final TownClaim leaving = fromClaim.get();
            if (getPlugin().fireIsCancelled(getPlugin().getPlayerLeaveTownEvent(user, leaving, user.getPosition(), user.getPosition()))) {
                return true;
            }

            final Town town = leaving.town();
            getPlugin().getLocales().getLocale("wilderness")
                .ifPresent(locale -> user.sendMessage(notificationSlot, locale));
            if (town.getFarewell().isPresent()) {
                user.sendMessage(Component.text(town.getFarewell().get()).color(TextColor.fromHexString(town.getColorRgb())));
            } else {
                getPlugin().getLocales().getLocale("leaving_town", town.getName(), town.getColorRgb())
                    .ifPresent(user::sendMessage);
            }
        }
        return false;
    }

    /**
     * Returns whether to cancel an {@link Operation}
     *
     * @param operation the operation to check
     * @return whether to cancel the operation
     */
    @Override
    default boolean cancelOperation(@NotNull Operation operation) {
        final Optional<OnlineUser> optionalUser = operation.getUser().map(u -> (OnlineUser) u);

        // Handle operations while the plugin is not loaded
        if (!getPlugin().isLoaded()) {
            optionalUser.ifPresent(user -> getPlugin().getLocales().getLocale("error_not_loaded")
                .ifPresent(user::sendMessage));
            return true;
        }

        // Handle friendly fire
        final Optional<OnlineUser> optionalVictim = operation.getVictim().map(u -> (OnlineUser) u);
        if (optionalVictim.isPresent() && optionalUser.isPresent()
            && cancelFriendlyFire(optionalUser.get(), optionalVictim.get())) {
            if (operation.isVerbose()) {
                getPlugin().getLocales().getLocale("operation_cancelled_friendly",
                    optionalVictim.get().getName()).ifPresent(optionalUser.get()::sendMessage);
            }
            return true;
        }

        // Handle operations in claims
        final Optional<TownClaim> claim = getPlugin().getClaimAt((Position) operation.getOperationPosition());
        if (claim.isPresent()) {
            return cancelOperation(operation, claim.get());
        }

        // Handle operations in unclaimable worlds
        final Optional<ClaimWorld> world = getPlugin().getClaimWorld((World) operation.getOperationPosition().getWorld());
        if (world.isEmpty()) {
            if (getPlugin().getRulePresets().getUnclaimableWorldRules(getPlugin().getFlags())
                .cancelOperation(operation.getType(), getPlugin().getFlags())) {
                if (operation.isVerbose() && optionalUser.isPresent()) {
                    getPlugin().getLocales().getLocale("operation_cancelled")
                        .ifPresent(optionalUser.get()::sendMessage);
                }
                return true;
            }
            return false;
        }

        // Handle operations in claim worlds
        if (getPlugin().getRulePresets().getWildernessRules(getPlugin().getFlags())
            .cancelOperation(operation.getType(), getPlugin().getFlags())) {
            if (operation.isVerbose() && optionalUser.isPresent()) {
                getPlugin().getLocales().getLocale("operation_cancelled")
                    .ifPresent(optionalUser.get()::sendMessage);
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
        final Optional<OnlineUser> optionalUser = operation.getUser().map(u -> (OnlineUser) u);
        final Town town = townClaim.town();
        final Claim claim = townClaim.claim();

        // Apply wartime flags if the user is active in a town that is at war
        final Settings.TownSettings.RelationsSettings relations = getPlugin().getSettings().getTowns().getRelations();
        if (relations.getWars().isEnabled() && relations.isEnabled() &&
            town.getCurrentWar().map(war -> war.getDefending() == town.getId() && operation.getUser()
                    .map(online -> war.isPlayerActive(online.getUuid()))
                    .orElse(false))
                .orElse(false)) {
            return getPlugin().getRulePresets().getWartimeRules(getPlugin().getFlags())
                .cancelOperation(operation.getType(), getPlugin().getFlags());
        }

        // If the operation is not allowed by the claim flags
        if (town.getRules().get(claim.getType()).cancelOperation(operation.getType(), getPlugin().getFlags())) {
            if (optionalUser.isEmpty()) {
                return true;
            }

            // Handle admin claims
            final OnlineUser user = optionalUser.get();
            if (townClaim.isAdminClaim(getPlugin()) && user.hasPermission(ADMIN_CLAIM_ACCESS_PERMISSION)) {
                return false;
            }

            // Handle plot memberships
            final Claim.Type claimType = claim.getType();
            if (claimType == Claim.Type.PLOT && claim.isPlotMember(user.getUuid())) {
                return false;
            }

            // Handle ignoring claims
            if (getPlugin().getUserPreferences(user.getUuid()).map(Preferences::isIgnoringClaims).orElse(false)) {
                return false;
            }

            final Optional<Member> optionalMember = getPlugin().getUserTown(user);
            if (optionalMember.isEmpty()) {
                if (operation.isVerbose()) {
                    getPlugin().getLocales().getLocale("operation_cancelled_claimed",
                        town.getName()).ifPresent(user::sendMessage);
                }
                return true;
            }

            final Member member = optionalMember.get();
            if (!member.town().equals(town)) {
                if (operation.isVerbose()) {
                    getPlugin().getLocales().getLocale("operation_cancelled_claimed",
                        town.getName()).ifPresent(user::sendMessage);
                }
                return true;
            }

            if (!member.hasPrivilege(getPlugin(), Privilege.TRUSTED_ACCESS)) {
                if (operation.isVerbose()) {
                    getPlugin().getLocales().getLocale("operation_cancelled_privileges")
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
     * @param operationWorld the world the chunks are in
     * @param chunk1         the first chunk
     * @param chunk2         the second chunk
     * @return whether to cancel the natural occurrence
     */
    @Override
    default boolean cancelNature(@NotNull OperationWorld operationWorld, @NotNull OperationChunk chunk1,
                                 @NotNull OperationChunk chunk2) {
        final World world = (World) operationWorld;
        if (getPlugin().getClaimWorld(world).isEmpty()) {
            return false;
        }

        final Optional<TownClaim> claim1 = getPlugin().getClaimAt((Chunk) chunk1, world);
        final Optional<TownClaim> claim2 = getPlugin().getClaimAt((Chunk) chunk2, world);
        if (claim1.isPresent() && claim2.isPresent()) {
            return !claim1.get().town().equals(claim2.get().town());
        }
        return !(claim1.isEmpty() && claim2.isEmpty());
    }

    private boolean cancelFriendlyFire(@NotNull OnlineUser user, @NotNull OnlineUser victim) {
        if (getPlugin().getSettings().getGeneral().isAllowFriendlyFire()) {
            return false;
        }
        final Town userTown = getPlugin().getUserTown(user).map(Member::town).orElse(null);
        final Town victimTown = getPlugin().getUserTown(victim).map(Member::town).orElse(null);
        return userTown != null && victimTown != null && userTown.areRelationsBilateral(victimTown, Town.Relation.ALLY);
    }

    @NotNull
    HuskTowns getPlugin();

}
