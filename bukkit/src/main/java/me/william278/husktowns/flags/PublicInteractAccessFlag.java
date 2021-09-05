package me.william278.husktowns.flags;

import me.william278.husktowns.listener.ActionType;

public class PublicInteractAccessFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "public_interact_access";

    public PublicInteractAccessFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, allowed, ActionType.INTERACT_BLOCKS, ActionType.INTERACT_WORLD, ActionType.INTERACT_REDSTONE);
    }
}