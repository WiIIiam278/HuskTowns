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

import net.william278.husktowns.BukkitHuskTowns;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Legacy object for representing a town role that members of a town can hold
 *
 * @param weight            Weight of this town role - higher is a higher rank.
 * @param id                Internal ID of this town role
 * @param displayName       Display name of this town role
 * @param allowedPrivileges Identifiers of sub commands this town role can perform
 * @deprecated See {@link Role} instead
 */
@Deprecated(since = "2.0")
public record TownRole(int weight, String id, String displayName,
                       List<RolePrivilege> allowedPrivileges) implements Comparable<TownRole> {

    /**
     * @deprecated This list is always empty since v2.0
     */
    @Deprecated(since = "2.0")
    public static ArrayList<TownRole> townRoles = new ArrayList<>();

    /**
     * Returns true if this role has the required privilege
     *
     * @param privilege Identifier of the sub command
     * @return {@code true} if this role can use it
     */
    @Deprecated(since = "2.0")
    public boolean canPerform(RolePrivilege privilege) {
        return allowedPrivileges.contains(privilege) ||
               (getRoleBeneath().isPresent() && getRoleBeneath().get().canPerform(privilege));
    }

    /**
     * Returns the highest {@link TownRole} - the owner of the town
     *
     * @return The owner {@link TownRole}
     */
    @Deprecated(since = "2.0")
    public static TownRole getDefaultRole() {
        return fromRole(BukkitHuskTowns.getInstance().getRoles().getDefaultRole());
    }

    /**
     * Returns the lowest {@link TownRole} - the default role for new members
     *
     * @return the default {@link TownRole}
     */
    @Deprecated(since = "2.0")
    public static TownRole getMayorRole() {
        return fromRole(BukkitHuskTowns.getInstance().getRoles().getMayorRole());
    }

    @NotNull
    public static TownRole fromRole(@NotNull Role role) {
        return new TownRole(role.getWeight(), role.getName().toLowerCase()
                .replaceAll(" ", "_"), role.getName(),
                role.getPrivileges().stream().map(RolePrivilege::getEquivalent).toList());
    }

    @Deprecated(since = "2.0")
    public static Optional<TownRole> getRoleByWeight(int weight) {
        return BukkitHuskTowns.getInstance().getRoles().fromWeight(weight)
                .map(TownRole::fromRole);
    }

    @Deprecated(since = "2.0")
    public Optional<TownRole> getRoleBeneath() {
        for (int i = weight - 1; i >= getDefaultRole().weight(); i--) {
            final Optional<TownRole> role = getRoleByWeight(i);
            if (role.isPresent()) {
                return role;
            }
        }
        return Optional.empty();
    }

    @Deprecated(since = "2.0")
    public Optional<TownRole> getRoleAbove() {
        for (int i = weight + 1; i <= getMayorRole().weight(); i++) {
            final Optional<TownRole> role = getRoleByWeight(i);
            if (role.isPresent()) {
                return role;
            }
        }
        return Optional.empty();
    }

    @Deprecated(since = "2.0")
    public static TownRole getLowestRoleWithPermission(RolePrivilege privilege) {
        for (int i = getDefaultRole().weight; i < getMayorRole().weight; i++) {
            final Optional<TownRole> role = getRoleByWeight(i);
            if (role.isPresent()) {
                if (role.get().allowedPrivileges.contains(privilege)) {
                    return role.get();
                }
            }
        }
        return null;
    }

    @Deprecated(since = "2.0")
    public static Optional<TownRole> getRoleByIdentifier(String identifier) {
        for (Role role : BukkitHuskTowns.getInstance().getRoles().getRoles()) {
            if (role.getName().replaceAll(" ", "_").equalsIgnoreCase(identifier)) {
                return Optional.of(fromRole(role));
            }
        }
        return Optional.empty();
    }

    @Override
    public int compareTo(TownRole o) {
        if (weight > o.weight) {
            return 1;
        } else if (weight < o.weight) {
            return -1;
        }
        return 0;
    }

    /**
     * List of privileges town roles can have
     *
     * @deprecated See {@link Privilege} instead
     */
    @Deprecated(since = "2.0")
    public enum RolePrivilege {
        /**
         * Ability to change the town bio
         */
        BIO(Privilege.SET_BIO),

        /**
         * Ability to kick town members
         * (cannot evict members with an equal or higher rank thank you)
         */
        EVICT(Privilege.EVICT),

        /**
         * Ability to promote town members
         * (cannot promote players to your rank or to a rank higher than you)
         */
        PROMOTE(Privilege.PROMOTE),

        /**
         * Ability to demote town members
         * (cannot demote players with an equal or higher rank than you)
         */
        DEMOTE(Privilege.DEMOTE),

        /**
         * Ability to modify town flags
         */
        FLAG(Privilege.SET_RULES),

        /**
         * Ability to rename the town
         */
        RENAME(Privilege.RENAME),

        /**
         * Ability to convert claimed chunks into farms and vice versa
         */
        FARM(Privilege.SET_FARM),

        /**
         * Ability to convert claimed chunks into plots and vice versa
         * (the plot_unclaim_other is required to convert claimed plots back, though)
         */
        PLOT(Privilege.SET_PLOT),

        /**
         * Ability to assign town members to a plot
         */
        PLOT_ASSIGN(Privilege.MANAGE_PLOT_MEMBERS),

        /**
         * Ability to unclaim a plot assigned to someone
         */
        PLOT_UNCLAIM_OTHER(Privilege.SET_PLOT),

        /**
         * Ability to build outside your assigned plot chunk(s), including in regular claimed chunks.
         */
        TRUSTED_ACCESS(Privilege.TRUSTED_ACCESS),

        /**
         * Ability to claim chunks for your town
         */
        CLAIM(Privilege.CLAIM),

        /**
         * Ability to unclaim chunks from your town
         */
        UNCLAIM(Privilege.UNCLAIM),

        /**
         * Ability to change the town greeting message
         */
        GREETING(Privilege.SET_GREETING),

        /**
         * Ability to change the town farewell message
         */
        FAREWELL(Privilege.SET_FAREWELL),

        /**
         * Ability to invite new members to your town
         */
        INVITE(Privilege.INVITE),

        /**
         * Ability to teleport to your town's spawn
         */
        SPAWN(Privilege.SPAWN),

        /**
         * Ability to update the town spawn
         */
        SETSPAWN(Privilege.SET_SPAWN),

        /**
         * Ability to toggle the privacy of your town's spawn
         */
        PUBLICSPAWN(Privilege.SPAWN_PRIVACY),

        /**
         * Ability to deposit money into your town coffers
         */
        DEPOSIT(Privilege.DEPOSIT),

        /**
         * Ability to use the town chat
         **/
        CHAT(Privilege.CHAT);

        private final Privilege equivalentPrivilege;

        RolePrivilege(@NotNull Privilege equivalentPrivilege) {
            this.equivalentPrivilege = equivalentPrivilege;
        }

        @NotNull
        public static RolePrivilege getEquivalent(Privilege privilege) {
            for (RolePrivilege rolePrivilege : values()) {
                if (rolePrivilege.equivalentPrivilege == privilege) {
                    return rolePrivilege;
                }
            }
            return RolePrivilege.CHAT;
        }
    }

}