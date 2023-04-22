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
