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

package net.william278.husktowns.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.network.Message;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.Preferences;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface GsonProvider {

    @NotNull
    default GsonBuilder getGsonBuilder() {
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Map.class, (JsonDeserializer<Map<String, Object>>)
                        (json, type, context) -> new Gson().fromJson(json, type));
    }

    @NotNull
    default Gson getGson() {
        return getGsonBuilder().create();
    }

    @NotNull
    default Town getTownFromJson(@NotNull String json) throws JsonSyntaxException {
        return getGson().fromJson(json, Town.class).upgradeSchema(json, getGson());
    }

    @NotNull
    default ClaimWorld getClaimWorldFromJson(@NotNull String json) throws JsonSyntaxException {
        final ClaimWorld world = getGson().fromJson(json, ClaimWorld.class);
        world.cacheClaims();
        return world;
    }

    @NotNull
    default Message getMessageFromJson(@NotNull String json) throws JsonSyntaxException {
        return getGson().fromJson(json, Message.class);
    }

    @NotNull
    default Preferences getPreferencesFromJson(@NotNull String json) throws JsonSyntaxException {
        return getGson().fromJson(json, Preferences.class);
    }

}
