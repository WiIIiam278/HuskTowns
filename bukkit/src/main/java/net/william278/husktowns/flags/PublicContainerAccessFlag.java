package net.william278.husktowns.flags;

import net.william278.husktowns.listener.ActionType;

public class PublicContainerAccessFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "public_container_access";

    public PublicContainerAccessFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, allowed, ActionType.OPEN_CONTAINER);
    }
}