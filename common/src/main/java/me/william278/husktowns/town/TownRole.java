package me.william278.husktowns.town;

/**
 * The roles members of a town can hold
 */
public enum TownRole {
    /**
     * A regular member of the town - players joining a town are residents by default
     */
    RESIDENT,
    /**
     * A trusted citizen, promoted from a resident by the mayor, who has additional privileges in the town
     */
    TRUSTED,
    /**
     * The mayor of the town. There is only one per town, being the person who created the town or transferred their ownership to another citizen.
     */
    MAYOR
}