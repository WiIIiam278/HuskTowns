package me.william278.husktowns.flags;

import me.william278.husktowns.listener.ActionType;

public class ExplosionDamageFlag extends Flag {

    public static final String FLAG_IDENTIFIER = "explosion_damage";

    public ExplosionDamageFlag(boolean allowed) {
        super(FLAG_IDENTIFIER, allowed, ActionType.BLOCK_EXPLOSION_DAMAGE, ActionType.MOB_EXPLOSION_DAMAGE);
    }
}