package net.william278.husktowns.hook;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import org.jetbrains.annotations.NotNull;


public abstract class WorldGuardHook extends Hook {
    public final static StateFlag CLAIMING = new StateFlag("husktowns-claim", false);
    public WorldGuardHook(@NotNull HuskTowns plugin) {
        super(plugin, "WorldGuard");
        enable();
    }

    @Override
    protected void onEnable() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        registry.register(CLAIMING);
    }

    public abstract boolean isChunkInRestrictedRegion(@NotNull Chunk chunk, @NotNull String worldName);
}
