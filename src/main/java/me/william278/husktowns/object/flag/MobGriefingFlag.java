package me.william278.husktowns.object.flag;

import me.william278.husktowns.listener.EventListener;

public class MobGriefingFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "mob_griefing";

    public MobGriefingFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, "Mob Griefing","Allows mobs to cause world damage to stuff", allowed);
    }

    @Override
    public boolean isActionAllowed(EventListener.ActionType actionType) {
        return (actionType == EventListener.ActionType.MOB_GRIEF_WORLD) && isFlagSet();
    }
}