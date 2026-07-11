package com.example.protectTheCore.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

import static com.example.protectTheCore.ProtectTheCore.plugin;

public class PortalLinkingListener implements Listener {
    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) return;
        if (plugin.getConfig().getBoolean("game.restrict_players_to_overworld")) {
            event.setCancelled(true);
            return;
        }
        World from = event.getFrom().getWorld();
        if (from.getName().equals("ptcoverworld")) {
            World target = Bukkit.getWorld("ptcnether");
            Location targetLoc = event.getTo().clone();
            targetLoc.setWorld(target);
            targetLoc.setX(targetLoc.getX() / 8.0);
            targetLoc.setZ(targetLoc.getZ() / 8.0);
            event.setTo(targetLoc);
        }
        else if (from.getName().equals("ptcnether")) {
            World target = Bukkit.getWorld("ptcoverworld");
            Location targetLoc = event.getTo().clone();
            targetLoc.setWorld(target);
            targetLoc.setX(targetLoc.getX() * 8.0);
            targetLoc.setZ(targetLoc.getZ() * 8.0);
            event.setTo(targetLoc);
        }
    }

    @EventHandler
    public void onEndPortal(PlayerPortalEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) return;
        if (plugin.getConfig().getBoolean("game.restrict_players_to_overworld")) {
            event.setCancelled(true);
            return;
        }
        World from = event.getFrom().getWorld();
        if (from.getName().equals("ptcoverworld") || from.getName().equals("ptcnether")) {
            event.setTo(Objects.requireNonNull(Bukkit.getWorld("ptctheend")).getSpawnLocation());
        }
        else if (from.getName().equals("ptctheend")) {
            event.setTo(Objects.requireNonNull(Bukkit.getWorld("ptcoverworld")).getSpawnLocation());
        }
    }
}
