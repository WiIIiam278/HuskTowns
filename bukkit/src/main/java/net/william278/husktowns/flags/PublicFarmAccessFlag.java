package net.william278.husktowns.flags;

import net.william278.husktowns.listener.ActionType;

public class PublicFarmAccessFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "public_farm_access";

    public PublicFarmAccessFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, allowed, ActionType.BREAK_CROPS, ActionType.PLACE_CROPS, ActionType.ENTITY_INTERACTION, ActionType.PVE, ActionType.PVE_PROJECTILE);
    }
}