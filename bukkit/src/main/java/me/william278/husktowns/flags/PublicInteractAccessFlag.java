package me.william278.husktowns.flags;

import me.william278.husktowns.listener.EventListener;

public class PublicInteractAccessFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "public_interact_access";

    public PublicInteractAccessFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, allowed, EventListener.ActionType.INTERACT_BLOCKS, EventListener.ActionType.INTERACT_WORLD, EventListener.ActionType.INTERACT_REDSTONE);
    }
}