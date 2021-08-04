package me.william278.husktowns.object.flag;

import me.william278.husktowns.listener.EventListener;

public class PublicBuildAccessFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "public_build_access";

    public PublicBuildAccessFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, "Public Build Access","Allows members of the public to break and place things", allowed);
    }

    @Override
    public boolean isActionAllowed(EventListener.ActionType actionType) {
        return (actionType == EventListener.ActionType.BREAK_BLOCK || actionType == EventListener.ActionType.PLACE_BLOCK || actionType == EventListener.ActionType.BREAK_HANGING_ENTITY || actionType == EventListener.ActionType.PLACE_HANGING_ENTITY || actionType == EventListener.ActionType.ARMOR_STAND_MANIPULATE || actionType == EventListener.ActionType.ENTITY_INTERACTION || actionType == EventListener.ActionType.EMPTY_BUCKET || actionType == EventListener.ActionType.FILL_BUCKET || actionType == EventListener.ActionType.USE_SPAWN_EGG) && isFlagSet();
    }
}