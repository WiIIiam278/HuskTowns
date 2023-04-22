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
