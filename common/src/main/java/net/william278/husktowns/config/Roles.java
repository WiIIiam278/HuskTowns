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

import com.google.common.collect.Maps;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Role;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("FieldMayBeFinal")
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Roles {

    protected static final String CONFIG_HEADER = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃  HuskTowns town role config  ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ This file is for configuring town roles and associated privileges.
        ┣╸ Each role is mapped to a weight, identifying its hierarchical position. Each weight is also mapped to the role name.
        ┣╸ Config Help: https://william278.net/docs/husktowns/config-files
        ┗╸ Documentation: https://william278.net/docs/husktowns/roles""";

    @Comment("Map of role weight IDs to display names")
    private LinkedHashMap<String, String> names = Maps.newLinkedHashMap(Map.of(
        "3", "Mayor",
        "2", "Trustee",
        "1", "Resident"
    ));

    @Comment("Map of role weight IDs to privileges")
    private LinkedHashMap<String, List<String>> roles = Maps.newLinkedHashMap(Map.of(
        "3", List.of(
            Privilege.SET_BIO.id(),
            Privilege.EVICT.id(),
            Privilege.PROMOTE.id(),
            Privilege.DEMOTE.id(),
            Privilege.WITHDRAW.id(),
            Privilege.LEVEL_UP.id(),
            Privilege.SET_RULES.id(),
            Privilege.RENAME.id(),
            Privilege.SET_COLOR.id(),
            Privilege.DECLARE_WAR.id()),
        "2", List.of(
            Privilege.SET_FARM.id(),
            Privilege.SET_PLOT.id(),
            Privilege.MANAGE_PLOT_MEMBERS.id(),
            Privilege.TRUSTED_ACCESS.id(),
            Privilege.UNCLAIM.id(),
            Privilege.CLAIM.id(),
            Privilege.SET_GREETING.id(),
            Privilege.SET_FAREWELL.id(),
            Privilege.INVITE.id(),
            Privilege.SET_SPAWN.id(),
            Privilege.MANAGE_RELATIONS.id(),
            Privilege.SPAWN_PRIVACY.id(),
            Privilege.VIEW_LOGS.id()),
        "1", List.of(
            Privilege.DEPOSIT.id(),
            Privilege.CHAT.id(),
            Privilege.CLAIM_PLOT.id(),
            Privilege.SPAWN.id())
    ));

    /**
     * Get the town roles map
     *
     * @return the town roles map
     * @throws IllegalStateException if the role map is invalid
     */
    @NotNull
    public List<Role> getRoles() throws IllegalStateException {
        final ArrayList<Role> roleList = new ArrayList<>();
        for (final Map.Entry<String, List<String>> roleMapping : roles.entrySet()) {
            final int weight = Integer.parseInt(roleMapping.getKey());
            final List<Privilege> privileges = roleMapping.getValue().stream().map(Privilege::fromId).toList();
            roleList.add(Role.of(weight, getName(weight), privileges));
        }
        return roleList;
    }

    @NotNull
    private String getName(int weight) throws IllegalStateException {
        if (!names.containsKey(Integer.toString(weight))) {
            throw new IllegalStateException("Invalid roles.yml file: Weight " + weight + " does not have a name assigned");
        }
        return names.get(Integer.toString(weight));
    }

    @NotNull
    public Role getMayorRole() {
        return getRoles().stream()
            .max(Comparator.comparingInt(Role::getWeight))
            .orElseThrow();
    }

    @NotNull
    public Role getDefaultRole() {
        return getRoles().stream()
            .min(Comparator.comparingInt(Role::getWeight))
            .orElseThrow();
    }

    public Optional<Role> fromWeight(int weight) {
        return getRoles().stream()
            .filter(role -> role.getWeight() == weight)
            .findFirst();
    }

}
