package me.william278.husktowns.object.flag;

import me.william278.husktowns.listener.EventListener;

public class PublicInteractAccessFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "public_interact_access";

    public PublicInteractAccessFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, "Public Interact Access","Allows members of the public to interact with doors, levers, redstone, etc", allowed, EventListener.ActionType.INTERACT_BLOCKS, EventListener.ActionType.INTERACT_WORLD, EventListener.ActionType.INTERACT_REDSTONE);
    }
}