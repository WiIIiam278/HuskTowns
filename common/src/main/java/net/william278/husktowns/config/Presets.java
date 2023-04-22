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
        ┗╸ Documentation: https://william278.net/docs/husktowns/claim-rules""")
public class Presets {

    @YamlKey("wilderness_rules")
    private Map<String, Boolean> wildernessRules = Map.of(
            Flag.EXPLOSION_DAMAGE.name().toLowerCase(), true,
            Flag.FIRE_DAMAGE.name().toLowerCase(), true,
            Flag.MOB_GRIEFING.name().toLowerCase(), true,
            Flag.MONSTER_SPAWNING.name().toLowerCase(), true,
            Flag.PUBLIC_BUILD_ACCESS.name().toLowerCase(), true,
            Flag.PUBLIC_CONTAINER_ACCESS.name().toLowerCase(), true,
            Flag.PUBLIC_FARM_ACCESS.name().toLowerCase(), true,
            Flag.PUBLIC_INTERACT_ACCESS.name().toLowerCase(), true,
            Flag.PVP.name().toLowerCase(), true
    );

    @YamlKey("admin_claim_rules")
    private Map<String, Boolean> adminClaimRules = Map.of(
            Flag.EXPLOSION_DAMAGE.name().toLowerCase(), false,
            Flag.FIRE_DAMAGE.name().toLowerCase(), false,
            Flag.MOB_GRIEFING.name().toLowerCase(), false,
            Flag.MONSTER_SPAWNING.name().toLowerCase(), false,
            Flag.PUBLIC_BUILD_ACCESS.name().toLowerCase(), false,
            Flag.PUBLIC_CONTAINER_ACCESS.name().toLowerCase(), false,
            Flag.PUBLIC_FARM_ACCESS.name().toLowerCase(), true,
            Flag.PUBLIC_INTERACT_ACCESS.name().toLowerCase(), true,
            Flag.PVP.name().toLowerCase(), false
    );

    @YamlKey("unclaimable_world_rules")
    private Map<String, Boolean> unclaimableWorldRules = Map.of(
            Flag.EXPLOSION_DAMAGE.name().toLowerCase(), true,
            Flag.FIRE_DAMAGE.name().toLowerCase(), true,
            Flag.MOB_GRIEFING.name().toLowerCase(), true,
            Flag.MONSTER_SPAWNING.name().toLowerCase(), true,
            Flag.PUBLIC_BUILD_ACCESS.name().toLowerCase(), true,
            Flag.PUBLIC_CONTAINER_ACCESS.name().toLowerCase(), true,
            Flag.PUBLIC_FARM_ACCESS.name().toLowerCase(), true,
            Flag.PUBLIC_INTERACT_ACCESS.name().toLowerCase(), true,
            Flag.PVP.name().toLowerCase(), true
    );

    @YamlKey("default_rules.claims")
    private Map<String, Boolean> claimRules = Map.of(
            Flag.EXPLOSION_DAMAGE.name().toLowerCase(), false,
            Flag.FIRE_DAMAGE.name().toLowerCase(), false,
            Flag.MOB_GRIEFING.name().toLowerCase(), false,
            Flag.MONSTER_SPAWNING.name().toLowerCase(), true,
            Flag.PUBLIC_BUILD_ACCESS.name().toLowerCase(), false,
            Flag.PUBLIC_CONTAINER_ACCESS.name().toLowerCase(), false,
            Flag.PUBLIC_FARM_ACCESS.name().toLowerCase(), false,
            Flag.PUBLIC_INTERACT_ACCESS.name().toLowerCase(), false,
            Flag.PVP.name().toLowerCase(), false
    );
    @YamlKey("default_rules.farms")
    private Map<String, Boolean> farmRules = Map.of(
            Flag.EXPLOSION_DAMAGE.name().toLowerCase(), false,
            Flag.FIRE_DAMAGE.name().toLowerCase(), false,
            Flag.MOB_GRIEFING.name().toLowerCase(), false,
            Flag.MONSTER_SPAWNING.name().toLowerCase(), true,
            Flag.PUBLIC_BUILD_ACCESS.name().toLowerCase(), false,
            Flag.PUBLIC_CONTAINER_ACCESS.name().toLowerCase(), false,
            Flag.PUBLIC_FARM_ACCESS.name().toLowerCase(), true,
            Flag.PUBLIC_INTERACT_ACCESS.name().toLowerCase(), false,
            Flag.PVP.name().toLowerCase(), false
    );

    @YamlKey("default_rules.plots")
    private Map<String, Boolean> plotRules = Map.of(
            Flag.EXPLOSION_DAMAGE.name().toLowerCase(), false,
            Flag.FIRE_DAMAGE.name().toLowerCase(), false,
            Flag.MOB_GRIEFING.name().toLowerCase(), false,
            Flag.MONSTER_SPAWNING.name().toLowerCase(), false,
            Flag.PUBLIC_BUILD_ACCESS.name().toLowerCase(), false,
            Flag.PUBLIC_CONTAINER_ACCESS.name().toLowerCase(), false,
            Flag.PUBLIC_FARM_ACCESS.name().toLowerCase(), false,
            Flag.PUBLIC_INTERACT_ACCESS.name().toLowerCase(), false,
            Flag.PVP.name().toLowerCase(), false
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
        return defaultRules.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, entry -> Rules.of(entry.getValue().entrySet().stream()
                                .collect(Collectors.toMap(
                                        flagEntry -> Flag.fromId(flagEntry.getKey()).orElseThrow(),
                                        Map.Entry::getValue))
                        )
                ));
    }

    @NotNull
    public Rules getUnclaimableWorldRules() {
        return Rules.of(unclaimableWorldRules.entrySet().stream()
                .collect(Collectors.toMap(e -> Flag.fromId(e.getKey()).orElseThrow(), Map.Entry::getValue)));
    }

    @NotNull
    public Rules getWildernessRules() {
        return Rules.of(wildernessRules.entrySet().stream()
                .collect(Collectors.toMap(e -> Flag.fromId(e.getKey()).orElseThrow(), Map.Entry::getValue)));
    }

    @NotNull
    public Rules getAdminClaimRules() {
        return Rules.of(adminClaimRules.entrySet().stream()
                .collect(Collectors.toMap(e -> Flag.fromId(e.getKey()).orElseThrow(), Map.Entry::getValue)));
    }

}
