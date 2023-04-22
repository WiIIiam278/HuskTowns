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

package net.william278.husktowns.town;

import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a role in a town
 */
public class Role {

    private int weight;
    private String name;
    private List<Privilege> privileges;

    private Role(int weight, @NotNull String name, @NotNull List<Privilege> privileges) {
        this.weight = weight;
        this.name = name;
        this.privileges = privileges;
    }

    /**
     * Create a role from a weight, name and list of privileges
     *
     * @param weight     The weight of the role, determining its position in the role hierarchy
     * @param name       The name of the role
     * @param privileges The privileges of the role
     * @return The role
     */
    public static Role of(int weight, @NotNull String name, @NotNull List<Privilege> privileges) {
        return new Role(weight, name, privileges);
    }

    @SuppressWarnings("unused")
    private Role() {
    }

    /**
     * Get the weight of the role, determining its position in the role hierarchy.
     * <p>
     * A higher weight means a higher position in the hierarchy.
     *
     * @return the weight of the role
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Get the name of the role
     *
     * @return the name of the role
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Get the list of privileges this role has.
     * <p>
     * Use {@link #hasPrivilege(HuskTowns, Privilege)} to check if a role has a privilege, including inherited privileges
     *
     * @return the list of privileges
     * @apiNote Note this does not include inherited privileges
     * @see #hasPrivilege(HuskTowns, Privilege)
     */
    @NotNull
    public List<Privilege> getPrivileges() {
        return privileges;
    }

    /**
     * Returns if the role has the specified privilege, including inherited privileges from parent roles
     *
     * @param plugin    the HuskTowns plugin instance
     * @param privilege the privilege to check
     * @return {@code true} if the role has the specified privilege; {@code false} otherwise
     */
    public boolean hasPrivilege(@NotNull HuskTowns plugin, @NotNull Privilege privilege) {
        return getPrivileges().contains(privilege) || plugin.getRoles().fromWeight(getWeight() - 1)
                .map(role -> role.hasPrivilege(plugin, privilege))
                .orElse(false);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Role role)) {
            return false;
        }
        return role.getWeight() == getWeight() && role.getName().equals(getName());
    }
}
