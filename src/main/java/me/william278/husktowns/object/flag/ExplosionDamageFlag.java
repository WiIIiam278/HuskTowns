package me.william278.husktowns.object.flag;

import me.william278.husktowns.listener.EventListener;

public class ExplosionDamageFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "explosion_damage";

    public ExplosionDamageFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, "Explosion Damage", "Allows explosions from blocks and monsters to destroy terrain", allowed);
    }

    @Override
    public boolean isActionAllowed(EventListener.ActionType actionType) {
        if (actionType == EventListener.ActionType.BLOCK_EXPLOSION_DAMAGE || actionType == EventListener.ActionType.MOB_EXPLOSION_DAMAGE) {
            return isFlagSet();
        }
        return true;
    }
}