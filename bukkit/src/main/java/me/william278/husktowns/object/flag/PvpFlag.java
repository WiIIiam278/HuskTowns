package me.william278.husktowns.object.flag;

import me.william278.husktowns.listener.EventListener;

public class PvpFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "pvp";

    public PvpFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, allowed, EventListener.ActionType.PVP, EventListener.ActionType.PVP_PROJECTILE);
    }
}