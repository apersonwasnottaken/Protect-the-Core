package com.example.protectTheCore.helper;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.World;

public class WorldGuardHook {
    public static void createShopRegion(World world, Location minLoc, Location maxLoc, String regionId) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));

        if (regions == null) return;

        BlockVector3 min = BlockVector3.at(minLoc.getX(), minLoc.getY(), minLoc.getZ());
        BlockVector3 max = BlockVector3.at(maxLoc.getX(), maxLoc.getY(), maxLoc.getZ());
        ProtectedRegion region = new ProtectedCuboidRegion(regionId, min, max);

        regions.addRegion(region);
    }
}