package me.william278.husktowns.listener;

/**
 * Different actions that are executed, by players, entites and the world.
 * In HuskTowns, flags check against these actions and the thing that caused them to determine if they can be carried out.
 */
public enum ActionType {
    /**
     * When a player attacks another player
     */
    PVP,
    /**
     * When a player shoots another player with a projectile
     */
    PVP_PROJECTILE,
    /**
     * When a player takes damage from an explosion caused by a mob
     */
    MOB_EXPLOSION_HURT,
    /**
     * When a player attacks a mob
     */
    PVE,
    /**
     * When a player attacks a mob with a projectile
     */
    PVE_PROJECTILE,
    /**
     * When a monster spawns naturally in the world
     */
    MONSTER_SPAWN,
    /**
     * When a mob griefs the world (excluding explosions; e.g {@link org.bukkit.entity.Enderman})
     */
    MOB_GRIEF_WORLD,
    /**
     * When a monster explosion griefs the world (e.g {@link org.bukkit.entity.Creeper})
     */
    MOB_EXPLOSION_DAMAGE,
    /**
     * When a block exploding griefs the world (e.g {@link org.bukkit.entity.TNTPrimed})
     */
    BLOCK_EXPLOSION_DAMAGE,
    /**
     * When fire destroys a block in the world
     */
    FIRE_DAMAGE,
    /**
     * When fire spreads
     */
    FIRE_SPREAD,
    /**
     * When a player interacts with an entity in the world
     */
    ENTITY_INTERACTION,
    /**
     * When a player interacts with blocks in the world
     */
    INTERACT_BLOCKS,
    /**
     * When a player interacts with redstone components in the world
     */
    INTERACT_REDSTONE,
    /**
     * When a player interacts with the world in some other way
     * Note: Not in use
     */
    INTERACT_WORLD,
    /**
     * When a player opens a container (e.g {@link org.bukkit.block.Chest}, {@link org.bukkit.block.Hopper}, etc.)
     */
    OPEN_CONTAINER,
    /**
     * When a player uses a spawn egg item
     */
    USE_SPAWN_EGG,
    /**
     * When a player places a hanging entity (i.e. Paintings, Item Frames)
     */
    PLACE_HANGING_ENTITY,
    /**
     * When a player breaks a hanging entity
     */
    BREAK_HANGING_ENTITY,
    /**
     * When a player shoots a hanging entity with a projectile
     */
    BREAK_HANGING_ENTITY_PROJECTILE,
    /**
     * When a player manipulates (changing equipment of) an armour stand
     */
    ARMOR_STAND_MANIPULATE,
    /**
     * When a player fills a bucket
     */
    FILL_BUCKET,
    /**
     * When a player empties a bucket
     */
    EMPTY_BUCKET,
    /**
     * When a player places a block
     */
    PLACE_BLOCK,
    /**
     * When the player breaks a block
     */
    BREAK_BLOCK,
    /**
     * When the player places crop blocks
     */
    PLACE_CROPS,
    /**
     * When the player breaks crop blocks
     */
    BREAK_CROPS
}
