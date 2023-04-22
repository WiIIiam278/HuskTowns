/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
 */

package net.william278.husktowns.network;

import com.google.gson.annotations.Expose;
import net.william278.husktowns.town.Invite;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class Payload {
    @Nullable
    @Expose
    private UUID uuid;

    @Nullable
    @Expose
    private Integer integer;

    @Nullable
    @Expose
    private Invite invite;

    @Nullable
    @Expose
    private Boolean bool;

    @Nullable
    @Expose
    private String string;

    private Payload() {
    }

    @NotNull
    public static Payload uuid(@NotNull UUID uuid) {
        final Payload payload = new Payload();
        payload.uuid = uuid;
        return payload;
    }

    @NotNull
    public static Payload integer(int integer) {
        final Payload payload = new Payload();
        payload.integer = integer;
        return payload;
    }

    @NotNull
    public static Payload invite(@NotNull Invite invite) {
        final Payload payload = new Payload();
        payload.invite = invite;
        return payload;
    }

    @NotNull
    public static Payload empty() {
        return new Payload();
    }

    public static Payload bool(boolean accepted) {
        final Payload payload = new Payload();
        payload.bool = accepted;
        return payload;
    }

    @NotNull
    public static Payload string(@NotNull String message) {
        final Payload payload = new Payload();
        payload.string = message;
        return payload;
    }

    public Optional<UUID> getUuid() {
        return Optional.ofNullable(uuid);
    }

    public Optional<Integer> getInteger() {
        return Optional.ofNullable(integer);
    }

    public Optional<Invite> getInvite() {
        return Optional.ofNullable(invite);
    }

    public Optional<Boolean> getBool() {
        return Optional.ofNullable(bool);
    }

    public Optional<String> getString() {
        return Optional.ofNullable(string);
    }
}
