package me.william278.husktowns.object.flag;

import me.william278.husktowns.listener.EventListener;

public class FireDamageFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "fire_damage";

    public FireDamageFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, "Fire Damage","Allows fire to spread and damage blocks, if fire tick is enabled", allowed);
    }

    @Override
    public boolean isActionAllowed(EventListener.ActionType actionType) {
        return (actionType == EventListener.ActionType.FIRE_DAMAGE || actionType == EventListener.ActionType.FIRE_SPREAD) && isFlagSet();
    }
}