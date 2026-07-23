package com.example.protectTheCore.listeners;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.helper.HelperFunctions;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PortalLinkingListener implements Listener {

    private final ProtectTheCore plugin;

    public PortalLinkingListener(@NotNull ProtectTheCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            return;
        };
        if (plugin.getConfig().getBoolean("game.restrict_players_to_overworld")) {
            if (HelperFunctions.getDebugMode(event.getPlayer())) {
                event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("Teleport canceled! The config game.restrict_players_to_overworld is enabled!"));
            }
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
            if (HelperFunctions.getDebugMode(event.getPlayer())) {
                assert target != null;
                event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("""
                Teleport redirected to: %s
                Spawn Location: (%d, %d, %d)
                Environment: %s
                """.formatted(target.getName(), target.getSpawnLocation().getBlockX(), target.getSpawnLocation().getBlockY(), target.getSpawnLocation().getBlockZ(), target.getEnvironment().toString())));
            }

        }
        else if (from.getName().equals("ptcnether")) {
            World target = Bukkit.getWorld("ptcoverworld");
            Location targetLoc = event.getTo().clone();
            targetLoc.setWorld(target);
            targetLoc.setX(targetLoc.getX() * 8.0);
            targetLoc.setZ(targetLoc.getZ() * 8.0);
            event.setTo(targetLoc);
            if (HelperFunctions.getDebugMode(event.getPlayer())) {
                assert target != null;
                event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("""
                Teleport redirected to: %s
                Spawn Location: (%d, %d, %d)
                Environment: %s
                """.formatted(target.getName(), target.getSpawnLocation().getBlockX(), target.getSpawnLocation().getBlockY(), target.getSpawnLocation().getBlockZ(), target.getEnvironment().toString())));
            }
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
            World target = Bukkit.getWorld("ptctheend");
            event.setTo(Objects.requireNonNull(target).getSpawnLocation());
            if (HelperFunctions.getDebugMode(event.getPlayer())) {
                event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("""
                Teleport redirected to: %s
                Spawn Location: (%d, %d, %d)
                Environment: %s
                """.formatted(target.getName(), target.getSpawnLocation().getBlockX(), target.getSpawnLocation().getBlockY(), target.getSpawnLocation().getBlockZ(), target.getEnvironment().toString())));
            }
        }
        else if (from.getName().equals("ptctheend")) {
            World target = Bukkit.getWorld("ptcoverworld");
            event.setTo(Objects.requireNonNull(target).getSpawnLocation());
            if (HelperFunctions.getDebugMode(event.getPlayer())) {
                event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("""
                Teleport redirected to: %s
                Spawn Location: (%d, %d, %d)
                Environment: %s
                """.formatted(target.getName(), target.getSpawnLocation().getBlockX(), target.getSpawnLocation().getBlockY(), target.getSpawnLocation().getBlockZ(), target.getEnvironment().toString())));
            }
        }
    }
}
