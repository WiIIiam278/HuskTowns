package net.william278.husktowns.events;

import net.william278.husktowns.town.Role;
import org.jetbrains.annotations.NotNull;

/**
 * Event when a member's role is changed (promoted or demoted)
 */
public interface IMemberRoleChangeEvent extends MemberEvent {

    /**
     * Get the member's old role
     *
     * @return the member's old role
     */
    @NotNull
    Role getNewRole();

    /**
     * Returns true if the user was promoted to a higher role
     *
     * @return true if the user was promoted to a higher role
     */
    @SuppressWarnings("unused")
    default boolean isPromotion() {
        return getMemberRole().getWeight() < getNewRole().getWeight();
    }

}
