package net.william278.husktowns.flags;

import net.william278.husktowns.listener.ActionType;

public class MobGriefingFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "mob_griefing";

    public MobGriefingFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, allowed, ActionType.MOB_GRIEF_WORLD);
    }
}