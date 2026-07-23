package com.example.protectTheCore.game.zone;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.game.wall.WallManager;
import com.example.protectTheCore.helper.HelperFunctions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ZoneEnforcementListener implements Listener {

    private final ZoneManager zoneManager;
    private final WallManager wallManager;

    public ZoneEnforcementListener(@NotNull ZoneManager zoneManager, @NotNull WallManager wallManager) {
        this.zoneManager = zoneManager;
        this.wallManager = wallManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!zoneManager.isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        Location to = event.getTo();
        Location from = event.getFrom();
        if (to.getBlockX() == from.getBlockX() && to.getBlockZ() == from.getBlockZ()) return;
        enforceZone(player, to);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!zoneManager.isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) return;
        enforceZone(player, event.getTo());
    }

    private void enforceZone(Player player, Location target) {
        if (target == null) return;
        ZoneManager.Zone currentZone = zoneManager.getZoneAt(target);
        if (currentZone == null) return;
        ZoneManager.Zone homeZone;
        try {
            homeZone = zoneManager.getHomeZone(player.getName());
        } catch (IOException e) {
            return;
        }
        if (homeZone == null) return;
        if (currentZone.teamIndex() == homeZone.teamIndex()) return;
        Location home = zoneManager.getTeamSpawn(homeZone.teamIndex(), target.getWorld(), wallManager);
        player.teleport(home);
        player.sendMessage(Component.text(
                "You can't enter another team's zone!", NamedTextColor.RED));
    }
}