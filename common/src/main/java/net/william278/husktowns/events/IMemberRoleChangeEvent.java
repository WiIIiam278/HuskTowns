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
