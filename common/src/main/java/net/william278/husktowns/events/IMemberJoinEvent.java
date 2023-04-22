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
