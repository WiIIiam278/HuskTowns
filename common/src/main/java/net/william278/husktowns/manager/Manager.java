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

import net.kyori.adventure.text.Component;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.network.Message;
import net.william278.husktowns.network.Payload;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Manager, for interfacing and editing town, claim and user data
 */
public class Manager {

    private final HuskTowns plugin;

    private final TownsManager towns;
    private final ClaimsManager claims;
    private final AdminManager admin;
    private final WarManager wars;

    public Manager(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
        this.towns = new TownsManager(plugin);
        this.claims = new ClaimsManager(plugin);
        this.admin = new AdminManager(plugin);
        final Settings.TownSettings.RelationsSettings settings = plugin.getSettings().getTowns().getRelations();
        this.wars = settings.isEnabled() && settings.getWars().isEnabled() ? new WarManager(plugin) : null;
    }

    @NotNull
    public TownsManager towns() {
        return towns;
    }

    @NotNull
    public ClaimsManager claims() {
        return claims;
    }

    @NotNull
    public AdminManager admin() {
        return admin;
    }

    @NotNull
    public Optional<WarManager> wars() {
        return Optional.ofNullable(wars);
    }

    public void editTown(@NotNull OnlineUser user, @NotNull Town town, @NotNull Consumer<Town> editor) {
        editTown(user, town, editor, null);
    }

    public void editTown(@NotNull OnlineUser user, @NotNull Town town, @NotNull Consumer<Town> editor,
                         @Nullable Consumer<Town> callback) {
        plugin.runAsync(() -> {
            editor.accept(town);
            updateTownData(user, town);
            if (callback != null) {
                callback.accept(town);
            }
            plugin.checkAdvancements(town, user);
        });
    }

    public void memberEditTown(@NotNull OnlineUser user, @Nullable Privilege privilege,
                               @NotNull Function<Member, Boolean> editor, @Nullable Consumer<Member> callback) {
        this.ifMember(user, privilege, (member -> plugin.runAsync(() -> {
            if (editor.apply(member)) {
                updateTownData(user, member.town());
                if (callback != null) {
                    callback.accept(member);
                }
                plugin.checkAdvancements(member.town(), user);
            }
        })));
    }

    public void memberEditTown(@NotNull OnlineUser user, @NotNull Privilege privilege, @NotNull Function<Member, Boolean> editor) {
        this.memberEditTown(user, privilege, editor, null);
    }

    /**
     * If the user is the mayor of a town, edit it with the given editor, update the town, then run the callback
     *
     * @param user     the user to check
     * @param editor   the editor to run
     * @param callback the callback to run
     */
    public void mayorEditTown(@NotNull OnlineUser user, @NotNull Function<Member, Boolean> editor, @Nullable Consumer<Member> callback) {
        this.ifMayor(user, (mayor -> plugin.runSync(() -> {
            if (editor.apply(mayor)) {
                plugin.runAsync(() -> {
                    updateTownData(user, mayor.town());
                    if (callback != null) {
                        callback.accept(mayor);
                    }
                    plugin.checkAdvancements(mayor.town(), user);
                });
            }
        }, user)));
    }

    /**
     * If the user is a member of a town, and has privileges, run the callback
     *
     * @param user      the user
     * @param privilege the privilege to check for
     * @param callback  the callback to run
     */
    protected void ifMember(@NotNull OnlineUser user, @Nullable Privilege privilege, @NotNull Consumer<Member> callback) {
        final Optional<Member> member = plugin.getUserTown(user);
        if (member.isEmpty()) {
            plugin.getLocales().getLocale("error_not_in_town")
                .ifPresent(user::sendMessage);
            return;
        }

        if (privilege != null && !member.get().hasPrivilege(plugin, privilege)) {
            plugin.getLocales().getLocale("error_insufficient_privileges", member.get().town().getName())
                .ifPresent(user::sendMessage);
            return;
        }

        callback.accept(member.get());
    }

    /**
     * If the user is the member of a town, run the callback with the member
     *
     * @param user     The user
     * @param callback The callback
     */
    protected void ifMember(@NotNull OnlineUser user, @NotNull Consumer<Member> callback) {
        this.ifMember(user, null, callback);
    }

    /**
     * If the user is a town mayor, run the callback
     *
     * @param user     The user
     * @param callback The callback
     */
    protected void ifMayor(@NotNull OnlineUser user, @NotNull Consumer<Member> callback) {
        final Optional<Member> member = plugin.getUserTown(user);
        if (member.isEmpty()) {
            plugin.getLocales().getLocale("error_not_in_town")
                .ifPresent(user::sendMessage);
            return;
        }

        if (!member.get().role().equals(plugin.getRoles().getMayorRole())) {
            plugin.getLocales().getLocale("error_not_town_mayor", member.get().town().getName())
                .ifPresent(user::sendMessage);
            return;
        }

        callback.accept(member.get());
    }

    /**
     * Check if a member is the owner of the claim at a chunk in a world, and if so, run a callback
     *
     * @param member   The member to check
     * @param user     The user to send messages to
     * @param chunk    The chunk to check
     * @param world    The world to check
     * @param callback The callback to run if the member is the owner
     */
    protected void ifClaimOwner(@NotNull Member member, @NotNull OnlineUser user, @NotNull Chunk chunk,
                                @NotNull World world, @NotNull Consumer<TownClaim> callback) {
        final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
        if (existingClaim.isEmpty()) {
            plugin.getLocales().getLocale("error_chunk_not_claimed")
                .ifPresent(user::sendMessage);
            return;
        }

        final TownClaim claim = existingClaim.get();
        final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
        if (claimWorld.isEmpty()) {
            plugin.getLocales().getLocale("error_world_not_claimable")
                .ifPresent(user::sendMessage);
            return;
        }

        final Town town = member.town();
        if (!claim.town().equals(town)) {
            plugin.getLocales().getLocale("error_chunk_claimed_by", claim.town().getName())
                .ifPresent(user::sendMessage);
            return;
        }

        callback.accept(claim);
    }

    /**
     * Update a town's data to the database and propagate cross-server
     *
     * @param actor The user who is updating the town's data
     * @param town  The town to update
     */
    public void updateTownData(@NotNull OnlineUser actor, @NotNull Town town) {
        // Update in the cache
        plugin.updateTown(town);

        // Update in the database
        plugin.getDatabase().updateTown(town);

        // Propagate to other servers
        plugin.getMessageBroker().ifPresent(broker -> Message.builder()
            .type(Message.Type.TOWN_UPDATE)
            .payload(Payload.integer(town.getId()))
            .target(Message.TARGET_ALL, Message.TargetType.SERVER)
            .build()
            .send(broker, actor));
    }

    /**
     * Send a message to all online users in a {@link Town}
     *
     * @param town    The town to send the notification for
     * @param message The message to send
     */
    public void sendTownMessage(@NotNull Town town, @NotNull Component message) {
        plugin.getOnlineUsers().stream()
            .filter(user -> town.getMembers().containsKey(user.getUuid()))
            .filter(user -> plugin.getUserPreferences(user.getUuid())
                .map(Preferences::sendTownMessages).orElse(true))
            .forEach(user -> user.sendMessage(message));
    }

}
