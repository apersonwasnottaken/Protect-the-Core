package com.example.protectTheCore.listeners;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.game.wall.WallManager;
import com.example.protectTheCore.game.zone.ZoneManager;
import com.example.protectTheCore.helper.HelperFunctions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

public class DeathInterceptorListener implements Listener {

    private volatile World defaultWorld;
    private final ProtectTheCore plugin;
    private final ZoneManager zoneManager;
    private final WallManager wallManager;

    public DeathInterceptorListener(@NotNull ProtectTheCore plugin, @NotNull WallManager wallManager, @NotNull ZoneManager zoneManager) {
        this.plugin = plugin;
        this.zoneManager = zoneManager;
        this.wallManager = wallManager;
    }

    public void setDefaultWorld(World world) {
        this.defaultWorld = world;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!plugin.getConfig().getBoolean("game.started")) return;
        if (event.isBedSpawn() || event.isAnchorSpawn()) {
            Location bedOrAnchorLoc = event.getRespawnLocation();
            if (bedOrAnchorLoc.getWorld() != null) {
                String worldName = bedOrAnchorLoc.getWorld().getName();
                if (worldName.equalsIgnoreCase("ptcoverworld") || worldName.equalsIgnoreCase("ptcnether")) {
                    return;
                }
            }
        }
        if (defaultWorld == null) return;
        try {
            ZoneManager.Zone homeZone = zoneManager.getHomeZone(event.getPlayer().getName());
            if (homeZone != null) {
                Location spawnLoc = zoneManager.getTeamSpawn(homeZone.teamIndex(), defaultWorld, wallManager);
                if (spawnLoc != null) {
                    event.setRespawnLocation(spawnLoc);
                    return;
                }
            }
        } catch (Exception e) {
            if (HelperFunctions.getDebugMode(event.getPlayer())) {
                HelperFunctions.sendErrorMessage(event.getPlayer(), e);
            }
            throw new RuntimeException(e);
        }
        event.setRespawnLocation(defaultWorld.getSpawnLocation());
    }
}