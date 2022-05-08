package net.william278.husktowns.flags;

import net.william278.husktowns.listener.ActionType;

public class MonsterSpawningFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "monster_spawning";

    public MonsterSpawningFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, allowed, ActionType.MONSTER_SPAWN);
    }
}