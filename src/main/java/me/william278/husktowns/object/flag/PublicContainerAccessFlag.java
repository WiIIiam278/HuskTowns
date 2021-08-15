package me.william278.husktowns.object.flag;

import me.william278.husktowns.listener.EventListener;

public class PublicContainerAccessFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "public_container_access";

    public PublicContainerAccessFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, allowed, EventListener.ActionType.OPEN_CONTAINER);
    }
}