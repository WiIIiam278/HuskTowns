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

import net.william278.annotaml.YamlComment;
import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Flag;
import net.william278.husktowns.claim.Rules;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃    HuskTowns Rule Presets    ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ This file is for configuring the default flag rule presets within towns and the public rules outside of towns.
        ┗╸ Config Help: https://william278.net/docs/husktowns/config-files""")
public class Presets {

    @YamlComment("Rules for the wilderness (claimable chunks outside of towns)")
    @YamlKey("wilderness_rules")
    private Map<String, Boolean> wildernessRules = Map.of(
            Flag.Defaults.EXPLOSION_DAMAGE.getName(), true,
            Flag.Defaults.FIRE_DAMAGE.getName(), true,
            Flag.Defaults.MOB_GRIEFING.getName(), true,
            Flag.Defaults.MONSTER_SPAWNING.getName(), true,
            Flag.Defaults.PUBLIC_BUILD_ACCESS.getName(), true,
            Flag.Defaults.PUBLIC_CONTAINER_ACCESS.getName(), true,
            Flag.Defaults.PUBLIC_FARM_ACCESS.getName(), true,
            Flag.Defaults.PUBLIC_INTERACT_ACCESS.getName(), true,
            Flag.Defaults.PVP.getName(), true
    );

    @YamlComment("Rules for admin claims (created with /admintown claim)")
    @YamlKey("admin_claim_rules")
    private Map<String, Boolean> adminClaimRules = Map.of(
            Flag.Defaults.EXPLOSION_DAMAGE.getName(), false,
            Flag.Defaults.FIRE_DAMAGE.getName(), false,
            Flag.Defaults.MOB_GRIEFING.getName(), false,
            Flag.Defaults.MONSTER_SPAWNING.getName(), false,
            Flag.Defaults.PUBLIC_BUILD_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_CONTAINER_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_FARM_ACCESS.getName(), true,
            Flag.Defaults.PUBLIC_INTERACT_ACCESS.getName(), true,
            Flag.Defaults.PVP.getName(), false
    );

    @YamlComment("Rules for worlds where claims cannot be created (as defined in unclaimable_worlds)")
    @YamlKey("unclaimable_world_rules")
    private Map<String, Boolean> unclaimableWorldRules = Map.of(
            Flag.Defaults.EXPLOSION_DAMAGE.getName(), true,
            Flag.Defaults.FIRE_DAMAGE.getName(), true,
            Flag.Defaults.MOB_GRIEFING.getName(), true,
            Flag.Defaults.MONSTER_SPAWNING.getName(), true,
            Flag.Defaults.PUBLIC_BUILD_ACCESS.getName(), true,
            Flag.Defaults.PUBLIC_CONTAINER_ACCESS.getName(), true,
            Flag.Defaults.PUBLIC_FARM_ACCESS.getName(), true,
            Flag.Defaults.PUBLIC_INTERACT_ACCESS.getName(), true,
            Flag.Defaults.PVP.getName(), true
    );

    @YamlComment("Default rules for normal claims")
    @YamlKey("default_rules.claims")
    private Map<String, Boolean> claimRules = Map.of(
            Flag.Defaults.EXPLOSION_DAMAGE.getName(), false,
            Flag.Defaults.FIRE_DAMAGE.getName(), false,
            Flag.Defaults.MOB_GRIEFING.getName(), false,
            Flag.Defaults.MONSTER_SPAWNING.getName(), true,
            Flag.Defaults.PUBLIC_BUILD_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_CONTAINER_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_FARM_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_INTERACT_ACCESS.getName(), false,
            Flag.Defaults.PVP.getName(), false
    );

    @YamlComment("Default rules for farm claims")
    @YamlKey("default_rules.farms")
    private Map<String, Boolean> farmRules = Map.of(
            Flag.Defaults.EXPLOSION_DAMAGE.getName(), false,
            Flag.Defaults.FIRE_DAMAGE.getName(), false,
            Flag.Defaults.MOB_GRIEFING.getName(), false,
            Flag.Defaults.MONSTER_SPAWNING.getName(), true,
            Flag.Defaults.PUBLIC_BUILD_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_CONTAINER_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_FARM_ACCESS.getName(), true,
            Flag.Defaults.PUBLIC_INTERACT_ACCESS.getName(), false,
            Flag.Defaults.PVP.getName(), false
    );

    @YamlComment("Default rules for plot claims")
    @YamlKey("default_rules.plots")
    private Map<String, Boolean> plotRules = Map.of(
            Flag.Defaults.EXPLOSION_DAMAGE.getName(), false,
            Flag.Defaults.FIRE_DAMAGE.getName(), false,
            Flag.Defaults.MOB_GRIEFING.getName(), false,
            Flag.Defaults.MONSTER_SPAWNING.getName(), false,
            Flag.Defaults.PUBLIC_BUILD_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_CONTAINER_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_FARM_ACCESS.getName(), false,
            Flag.Defaults.PUBLIC_INTERACT_ACCESS.getName(), false,
            Flag.Defaults.PVP.getName(), false
    );

    @SuppressWarnings("unused")
    public Presets() {
    }

    @NotNull
    public Map<Claim.Type, Rules> getDefaultClaimRules() {
        final HashMap<Claim.Type, Map<String, Boolean>> defaultRules = new HashMap<>();
        defaultRules.put(Claim.Type.CLAIM, claimRules);
        defaultRules.put(Claim.Type.FARM, farmRules);
        defaultRules.put(Claim.Type.PLOT, plotRules);
        return defaultRules.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Rules.of(entry.getValue())
                ));
    }

    @NotNull
    public Rules getUnclaimableWorldRules() {
        return Rules.of(unclaimableWorldRules);
    }

    @NotNull
    public Rules getWildernessRules() {
        return Rules.of(wildernessRules);
    }

    @NotNull
    public Rules getAdminClaimRules() {
        return Rules.of(adminClaimRules);
    }

}
