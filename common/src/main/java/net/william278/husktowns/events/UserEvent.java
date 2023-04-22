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

import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * An event that involves a {@link User}
 */
public interface UserEvent extends Event {

    /**
     * Get the user involved in the event
     *
     * @return the {@link User} involved in the event
     */
    @NotNull
    User getUser();

}
