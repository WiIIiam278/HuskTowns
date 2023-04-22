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
