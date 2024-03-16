package net.william278.husktowns.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class BukkitWorldGuardHook extends WorldGuardHook {
    public BukkitWorldGuardHook(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @Override
    public boolean isChunkInRestrictedRegion(@NotNull Chunk chunk, @NotNull String worldName) {
        if (isDisabled()) {
            return false;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return false;
        }
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regionManager == null) {
            return false;
        }
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        int chunkMinX = chunkX << 4;
        int chunkMinZ = chunkZ << 4;
        int chunkMaxZ = chunkMinZ + 15;
        int chunkMaxX = chunkMinX + 15;
        BlockVector3 min = BlockVector3.at(chunkMinX, -50, chunkMinZ);
        BlockVector3 max = BlockVector3.at(chunkMaxX, 256, chunkMaxZ);
        ProtectedRegion chunkRegion = new ProtectedCuboidRegion("dummy", min, max);
        ApplicableRegionSet set = regionManager.getApplicableRegions(chunkRegion);
        for (ProtectedRegion region : set.getRegions()) {
            return region.getFlag(CLAIMING) == StateFlag.State.DENY;
        }
        return false;
    }
}
