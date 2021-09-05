package me.william278.husktowns.flags;

import me.william278.husktowns.listener.ActionType;

public class PvpFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "pvp";

    public PvpFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, allowed, ActionType.PVP, ActionType.PVP_PROJECTILE);
    }
}