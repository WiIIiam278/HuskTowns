package net.william278.husktowns.events;

import org.jetbrains.annotations.NotNull;

/**
 * Event that is fired when a member joins a town
 */
public interface IMemberJoinEvent extends MemberEvent {

    /**
     * Get the {@link JoinReason reason} the member joined the town
     *
     * @return the reason the member joined the town
     */
    @NotNull
    @SuppressWarnings("unused")
    JoinReason getJoinReason();

    /**
     * Identifies the reason a member joined a town
     */
    enum JoinReason {
        /**
         * The member joined the town by invitation
         */
        ACCEPT_INVITE,
        /**
         * The member joined the town forcefully through an admin takeover
         */
        ADMIN_TAKE_OVER
    }

}
