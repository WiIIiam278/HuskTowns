package me.william278.husktowns.object.flag;

import me.william278.husktowns.listener.EventListener;

public class PublicBuildAccessFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "public_build_access";

    public PublicBuildAccessFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, allowed, EventListener.ActionType.BREAK_BLOCK, EventListener.ActionType.PLACE_BLOCK, EventListener.ActionType.BREAK_HANGING_ENTITY, EventListener.ActionType.PLACE_HANGING_ENTITY, EventListener.ActionType.ARMOR_STAND_MANIPULATE, EventListener.ActionType.ENTITY_INTERACTION, EventListener.ActionType.EMPTY_BUCKET, EventListener.ActionType.FILL_BUCKET, EventListener.ActionType.USE_SPAWN_EGG, EventListener.ActionType.PVE, EventListener.ActionType.PVP_PROJECTILE);
    }
}