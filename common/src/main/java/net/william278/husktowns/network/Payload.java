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

package net.william278.husktowns.network;

import com.google.gson.annotations.Expose;
import net.william278.husktowns.town.Invite;
import net.william278.husktowns.war.Declaration;
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
    private Declaration declaration;

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
    public static Payload declaration(@NotNull Declaration declaration) {
        final Payload payload = new Payload();
        payload.declaration = declaration;
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

    public Optional<Declaration> getDeclaration() {
        return Optional.ofNullable(declaration);
    }

    public Optional<Boolean> getBool() {
        return Optional.ofNullable(bool);
    }

    public Optional<String> getString() {
        return Optional.ofNullable(string);
    }
}
