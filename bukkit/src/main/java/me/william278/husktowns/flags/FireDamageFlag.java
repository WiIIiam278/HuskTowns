package me.william278.husktowns.flags;

import me.william278.husktowns.listener.ActionType;

public class FireDamageFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "fire_damage";

    public FireDamageFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, allowed, ActionType.FIRE_DAMAGE, ActionType.FIRE_SPREAD);
    }
}