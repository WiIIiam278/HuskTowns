package me.william278.husktowns.object.flag;

import me.william278.husktowns.listener.EventListener;

public class ExplosionDamageFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "explosion_damage";

    public ExplosionDamageFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, allowed, EventListener.ActionType.BLOCK_EXPLOSION_DAMAGE, EventListener.ActionType.MOB_EXPLOSION_DAMAGE);
    }
}