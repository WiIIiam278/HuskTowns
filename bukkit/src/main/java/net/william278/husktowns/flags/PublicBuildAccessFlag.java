package net.william278.husktowns.flags;

import net.william278.husktowns.listener.ActionType;

public class PublicBuildAccessFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "public_build_access";

    public PublicBuildAccessFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, allowed, ActionType.BREAK_BLOCK, ActionType.PLACE_BLOCK, ActionType.BREAK_HANGING_ENTITY, ActionType.BREAK_HANGING_ENTITY_PROJECTILE, ActionType.PLACE_HANGING_ENTITY, ActionType.ARMOR_STAND_MANIPULATE, ActionType.ENTITY_INTERACTION, ActionType.EMPTY_BUCKET, ActionType.FILL_BUCKET, ActionType.USE_SPAWN_EGG, ActionType.PVE, ActionType.PVE_PROJECTILE);
    }
}