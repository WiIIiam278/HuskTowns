package net.william278.husktowns.events;

import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Role;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link TownEvent} that involves a {@link Member}
 */
public interface MemberEvent extends UserEvent, TownEvent {

    /**
     * Get the member involved in the event
     *
     * @return the member involved in the event
     */
    @NotNull
    default Member getMember() {
        return new Member(getUser(), getTown(), getMemberRole());
    }

    /**
     * Get the {@link Role} of the member involved in the event
     *
     * @return the role of the member involved in the event
     */
    @NotNull
    Role getMemberRole();

}
