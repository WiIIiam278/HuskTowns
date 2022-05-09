package net.william278.husktowns.town;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The roles members of a town can hold
 *
 * @param weight             Weight of this town role - higher is a higher rank.
 * @param id                 Internal ID of this town role
 * @param displayName        Display name of this town role
 * @param allowedSubCommands Identifiers of sub commands this town role can perform
 */
public record TownRole(int weight, String id, String displayName,
                       List<String> allowedSubCommands) implements Comparable<TownRole> {

    public static ArrayList<TownRole> townRoles;

    /**
     * Returns true if this role has permission to use the sub command
     *
     * @param subCommand Identifier of the sub command
     * @return {@code true} if this role can use it
     */
    public boolean canPerform(String subCommand) {
        return allowedSubCommands.contains(subCommand) ||
                (getRoleBeneath().isPresent() && getRoleBeneath().get().canPerform(subCommand));
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
        for (int i = weight-1; i >= getDefaultRole().weight(); i--) {
            final Optional<TownRole> role = getRoleByWeight(i);
            if (role.isPresent()) {
                return role;
            }
        }
        return Optional.empty();
    }

    public Optional<TownRole> getRoleAbove() {
        for (int i = weight+1; i <= getMayorRole().weight(); i++) {
            final Optional<TownRole> role = getRoleByWeight(i);
            if (role.isPresent()) {
                return role;
            }
        }
        return Optional.empty();
    }

    public static TownRole getLowestRoleWithPermission(String subCommand) {
        for (int i = getDefaultRole().weight; i < getMayorRole().weight; i++) {
            final Optional<TownRole> role = getRoleByWeight(i);
            if (role.isPresent()) {
                if (role.get().allowedSubCommands.contains(subCommand)) {
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
}