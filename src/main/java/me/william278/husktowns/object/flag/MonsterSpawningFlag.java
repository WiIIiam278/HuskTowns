package me.william278.husktowns.object.flag;

import me.william278.husktowns.listener.EventListener;

public class MonsterSpawningFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "monster_spawning";

    public MonsterSpawningFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, "Monster Spawning","Allows monsters to spawn within claims", allowed);
    }

    @Override
    public boolean isActionAllowed(EventListener.ActionType actionType) {
        if (actionType == EventListener.ActionType.MONSTER_SPAWN) {
            return isFlagSet();
        }
        return true;
    }
}