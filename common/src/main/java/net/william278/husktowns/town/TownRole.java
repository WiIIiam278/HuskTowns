package net.william278.husktowns.town;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The roles members of a town can hold
 *
 * @param weight            Weight of this town role - higher is a higher rank.
 * @param id                Internal ID of this town role
 * @param displayName       Display name of this town role
 * @param allowedPrivileges Identifiers of sub commands this town role can perform
 */
public record TownRole(int weight, String id, String displayName,
                       List<RolePrivilege> allowedPrivileges) implements Comparable<TownRole> {

    public static ArrayList<TownRole> townRoles;

    /**
     * Returns true if this role has the required privilege
     *
     * @param privilege Identifier of the sub command
     * @return {@code true} if this role can use it
     */
    public boolean canPerform(RolePrivilege privilege) {
        return allowedPrivileges.contains(privilege) ||
                (getRoleBeneath().isPresent() && getRoleBeneath().get().canPerform(privilege));
    }

    /**
     * Returns the highest {@link TownRole} - the owner of the town
     *
     * @return The owner {@link TownRole}
     */
    public static TownRole getDefaultRole() {
        return townRoles.get(0);
    }

    /**
     * Returns the lowest {@link TownRole} - the default role for new members
     *
     * @return the default {@link TownRole}
     */
    public static TownRole getMayorRole() {
        return townRoles.get(townRoles.size() - 1);
    }

    public static Optional<TownRole> getRoleByWeight(int weight) {
        for (TownRole role : townRoles) {
            if (role.weight == weight) {
                return Optional.of(role);
            }
        }
        return Optional.empty();
    }

    public Optional<TownRole> getRoleBeneath() {
        for (int i = weight - 1; i >= getDefaultRole().weight(); i--) {
            final Optional<TownRole> role = getRoleByWeight(i);
            if (role.isPresent()) {
                return role;
            }
        }
        return Optional.empty();
    }

    public Optional<TownRole> getRoleAbove() {
        for (int i = weight + 1; i <= getMayorRole().weight(); i++) {
            final Optional<TownRole> role = getRoleByWeight(i);
            if (role.isPresent()) {
                return role;
            }
        }
        return Optional.empty();
    }

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

    public static Optional<TownRole> getRoleByIdentifier(String identifier) {
        for (TownRole role : townRoles) {
            if (role.id.equalsIgnoreCase(identifier)) {
                return Optional.of(role);
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
     */
    public enum RolePrivilege {
        // Ability to change the town bio
        BIO,

        // Ability to kick town members (cannot evict members with an equal or higher rank thank you)
        EVICT,

        // Ability to promote town members (cannot promote players to your rank or to a rank higher than you)
        PROMOTE,

        // Ability to demote town members (cannot demote players with an equal or higher rank than you)
        DEMOTE,

        // Ability to modify town flags
        FLAG,

        // Ability to rename the town
        RENAME,

        // Ability to convert claimed chunks into farms and vice versa
        FARM,

        // Ability to convert claimed chunks into plots and vice versa (the plot_unclaim_other is required to convert claimed plots back, though)
        PLOT,

        // Ability to assign town members to a plot
        PLOT_ASSIGN,

        // Ability to unclaim a plot assigned to someone
        PLOT_UNCLAIM_OTHER,

        // Ability to build outside your assigned plot chunk(s), including in regular claimed chunks.
        TRUSTED_ACCESS,

        // Ability to claim chunks for your town
        CLAIM,

        // Ability to unclaim chunks from your town
        UNCLAIM,

        // Ability to change the town greeting message
        GREETING,

        // Ability to change the town farewell message
        FAREWELL,

        // Ability to invite new members to your town
        INVITE,

        // Ability to teleport to your town's spawn
        SPAWN,

        // Ability to update the town spawn
        SETSPAWN,

        // Ability to toggle the privacy of your town's spawn
        PUBLICSPAWN,

        // Ability to deposit money into your town coffers
        DEPOSIT,

        // Ability to use the town chat
        CHAT
    }

}