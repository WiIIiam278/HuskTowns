package net.william278.husktowns.events;

import org.jetbrains.annotations.NotNull;

/**
 * Event that is fired when a member leaves a town.
 * <p>
 * Note that {@link ITownDisbandEvent} is fired when a town is disbanded instead.
 */
public interface IMemberLeaveEvent extends MemberEvent {

    /**
     * Get the {@link LeaveReason reason} the member left the town
     *
     * @return the reason why the member left the town
     */
    @NotNull
    @SuppressWarnings("unused")
    LeaveReason getLeaveReason();

    /**
     * Identifies the reason a member left a town
     */
    enum LeaveReason {
        /**
         * The member left the town of their own accord through the leave command
         */
        LEAVE,
        /**
         * The member was evicted (kicked) from the town
         */
        EVICTED
    }
}
