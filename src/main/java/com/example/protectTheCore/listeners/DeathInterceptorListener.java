package com.example.protectTheCore.listeners;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.game.zone.ZoneManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.io.IOException;

import static com.example.protectTheCore.ProtectTheCore.plugin;
import static com.example.protectTheCore.ProtectTheCore.zoneManager;

public class DeathInterceptorListener implements Listener {

    private volatile World defaultWorld;

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
                Location spawnLoc = zoneManager.getTeamSpawn(homeZone.teamIndex(), defaultWorld, ProtectTheCore.wallManager);
                if (spawnLoc != null) {
                    event.setRespawnLocation(spawnLoc);
                    return;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        event.setRespawnLocation(defaultWorld.getSpawnLocation());
    }
}