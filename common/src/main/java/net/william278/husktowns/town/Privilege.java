package net.william278.husktowns.town;

import org.jetbrains.annotations.NotNull;

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
     * Note that {@link #UNASSIGN_PLOT} is needed to convert back claimed plots.
     */
    SET_PLOT,
    /**
     * Ability to assign town members to a plot
     */
    ASSIGN_PLOT,
    /**
     * Ability to unclaim a plot assigned to someone
     */
    UNASSIGN_PLOT,
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
    CHAT;

    @NotNull
    public String id() {
        return name().toLowerCase();
    }

    @NotNull
    public static Privilege fromId(@NotNull String id) {
        return valueOf(id.toUpperCase());
    }

}
