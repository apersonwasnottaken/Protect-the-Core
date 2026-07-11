package com.example.protectTheCore.game.zone;

import com.example.protectTheCore.core.Teams;
import com.example.protectTheCore.game.wall.WallManager;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZoneManager {

    public record Zone(int teamIndex, double minX, double minZ, double maxX, double maxZ) {
        public boolean contains(double x, double z) {
            return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        }
    }

    private final List<Zone> zones = new ArrayList<>();
    private boolean enabled = false;
    private World activeWorld = null;

    public void setZones(List<Zone> newZones, World world) {
        zones.clear();
        zones.addAll(newZones);
        activeWorld = world;
        enabled = !newZones.isEmpty();
    }

    public void clear() {
        zones.clear();
        enabled = false;
        activeWorld = null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Zone getZoneAt(Location loc) {
        if (!enabled) return null;
        if (activeWorld == null || !activeWorld.equals(loc.getWorld())) return null;
        for (Zone z : zones) {
            if (z.contains(loc.getX(), loc.getZ())) return z;
        }
        return null;
    }

    public Zone getHomeZone(String playerName) throws IOException {
        if (!enabled) return null;
        int teamIdx = Teams.getTeamIndexFromPlayer(playerName);
        if (teamIdx < 0) return null;
        for (Zone z : zones) {
            if (z.teamIndex() == teamIdx) return z;
        }
        return null;
    }

    public Location getTeamCenter(int teamIndex, World world) {
        for (Zone z : zones) {
            if (z.teamIndex() == teamIndex) {
                double cx = (z.minX() + z.maxX()) / 2.0;
                double cz = (z.minZ() + z.maxZ()) / 2.0;
                if (cx <= -15000000) cx = z.maxX() - 150.0;
                if (cx >=  15000000) cx = z.minX() + 150.0;
                if (cz <= -15000000) cz = z.maxZ() - 150.0;
                if (cz >=  15000000) cz = z.minZ() + 150.0;
                cx = Math.max(z.minX() + 2.0, Math.min(cx, z.maxX() - 2.0));
                cz = Math.max(z.minZ() + 2.0, Math.min(cz, z.maxZ() - 2.0));
                double cy = world.getHighestBlockYAt((int) cx, (int) cz) + 1.0;
                return new Location(world, Math.floor(cx) + 0.5, cy, Math.floor(cz) + 0.5);
            }
        }
        return null;
    }

    public Location getTeamSpawn(int teamIndex, World world, WallManager wallManager) {
        for (Zone z : zones) {
            if (z.teamIndex() == teamIndex) {
                int buffer = wallManager.getBufferSize();
                WallManager.WallMode mode = wallManager.getCurrentMode();
                Location centerLoc = new Location(world, 0, world.getHighestBlockYAt(0, 0), 0);
                double spawnX = centerLoc.getX();
                double spawnZ = centerLoc.getZ();
                if (mode == WallManager.WallMode.NONE) {
                    return centerLoc;
                }
                if (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) {
                    if (z.maxX() <= 0) {
                        spawnX = -buffer - 16.0;
                    } else if (z.minX() >= 0) {
                        spawnX = buffer + 16.0;
                    }
                }
                if (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH) {
                    if (z.maxZ() <= 0) {
                        spawnZ = -buffer - 16.0;
                    } else if (z.minZ() >= 0) {
                        spawnZ = buffer + 16.0;
                    }
                }
                if (!z.contains(spawnX, spawnZ)) {
                    return centerLoc;
                }
                double spawnY = world.getHighestBlockYAt((int) spawnX, (int) spawnZ) + 1.0;
                return new Location(world, Math.floor(spawnX) + 0.5, spawnY, Math.floor(spawnZ) + 0.5);
            }
        }
        return null;
    }

    public List<Zone> getZones() {
        return List.copyOf(zones);
    }
}