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

package net.william278.husktowns.town;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a privilege which can be granted to a {@link Role} in a {@link Town}, letting users perform certain actions
 */
public enum Privilege {

    /**
     * Ability to kick town members (cannot evict members with an equal or higher rank than you)
     */
    EVICT,
    /**
     * Ability to promote town members (cannot promote players to your rank or to a rank higher than you)
     */
    PROMOTE,
    /**
     * Ability to demote town members (cannot demote players with an equal or higher rank than you)
     */
    DEMOTE,
    /**
     * Ability to modify town flags
     */
    SET_RULES,
    /**
     * Ability to rename the town
     */
    RENAME,
    /**
     * Ability to convert claimed chunks into farms and vice versa
     */
    SET_FARM,
    /**
     * Ability to convert claimed chunks into plots and vice versa
     */
    SET_PLOT,
    /**
     * Ability to add members to a plot
     */
    MANAGE_PLOT_MEMBERS,
    /**
     * Ability to build outside your assigned plot chunk(s), including in regular claimed chunks.
     */
    TRUSTED_ACCESS,
    /**
     * Ability to claim chunks for your town
     **/
    CLAIM,
    /**
     * Ability to unclaim chunks from your town
     **/
    UNCLAIM,
    /**
     * Ability to view town audit logs
     */
    VIEW_LOGS,
    /**
     * Ability to change the town bio
     */
    SET_BIO,
    /**
     * Ability to change the town greeting message
     **/
    SET_GREETING,
    /**
     * Ability to change the town farewell message
     **/
    SET_FAREWELL,
    /**
     * Ability to change the town color
     */
    SET_COLOR,
    /**
     * Ability to invite new members to your town
     **/
    INVITE,
    /**
     * Ability to teleport to your town's spawn
     **/
    SPAWN,
    /**
     * Ability to update the town spawn
     **/
    SET_SPAWN,
    /**
     * Ability to toggle the privacy of your town's spawn
     **/
    SPAWN_PRIVACY,
    /**
     * Ability to deposit money into your town coffers
     **/
    DEPOSIT,
    /**
     * Ability to use the town chat
     **/
    CHAT,
    /**
     * Ability to withdraw money from your town coffers
     */
    WITHDRAW,
    /**
     * Ability to spend money from your town coffers to level up your town
     */
    LEVEL_UP;

    /**
     * Get the ID of the privilege
     *
     * @return the ID of the privilege
     */
    @NotNull
    public String id() {
        return name().toLowerCase();
    }

    /**
     * Get the privilege from its ID
     *
     * @param id the ID of the privilege
     * @return the privilege
     */
    @NotNull
    public static Privilege fromId(@NotNull String id) {
        return valueOf(id.toUpperCase());
    }

}
