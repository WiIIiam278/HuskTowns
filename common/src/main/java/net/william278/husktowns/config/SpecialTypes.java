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

package net.william278.husktowns.config;

import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
@YamlFile(header = "Internal resource config containing lists of special block and entity types")
public class SpecialTypes {

    @YamlKey("farm_blocks")
    private List<String> farmBlocks;

    @YamlKey("pressure_sensitive_blocks")
    private List<String> pressureSensitiveBlocks;

    @YamlKey("griefing_mobs")
    private List<String> griefingMobs;

    @SuppressWarnings("unused")
    private SpecialTypes() {
    }

    public boolean isFarmBlock(@NotNull String block) {
        return farmBlocks.contains(formatKey(block));
    }

    public boolean isPressureSensitiveBlock(@NotNull String block) {
        return pressureSensitiveBlocks.contains(formatKey(block));
    }

    public boolean isGriefingMob(@NotNull String mob) {
        return griefingMobs.contains(formatKey(mob));
    }

    @NotNull
    private static String formatKey(@NotNull String key) {
        return key.trim().toLowerCase().replace("minecraft:", "");
    }

}
