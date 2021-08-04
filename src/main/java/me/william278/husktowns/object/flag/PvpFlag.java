package me.william278.husktowns.object.flag;

import me.william278.husktowns.listener.EventListener;

public class PvpFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "pvp";

    public PvpFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, "PVP","Allows players to engage in combat with each other", allowed);
    }

    @Override
    public boolean isActionAllowed(EventListener.ActionType actionType) {
        return ((actionType == EventListener.ActionType.PVP) || (actionType == EventListener.ActionType.PVP_PROJECTILE)) && isFlagSet();
    }
}