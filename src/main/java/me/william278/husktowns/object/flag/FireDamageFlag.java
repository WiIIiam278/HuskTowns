package me.william278.husktowns.object.flag;

import me.william278.husktowns.listener.EventListener;

public class FireDamageFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "fire_damage";

    public FireDamageFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, allowed, EventListener.ActionType.FIRE_DAMAGE, EventListener.ActionType.FIRE_SPREAD);
    }
}