package com.example.protectTheCore.core;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.game.wall.WallManager;
import com.example.protectTheCore.game.zone.ZoneManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.example.protectTheCore.ProtectTheCore.*;

public class DimensionKicker {

    private final ProtectTheCore plugin;
    private final ZoneManager zoneManager;
    private final WallManager wallManager;

    public DimensionKicker(@NotNull ProtectTheCore plugin, @NotNull ZoneManager zoneManager, @NotNull WallManager wallManager) {
        this.plugin = plugin;
        this.zoneManager = zoneManager;
        this.wallManager = wallManager;
    }

    public void DimensionKickerListenerLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (plugin.getConfig().getBoolean("game.restrict_players_to_overworld")) {
                        if (!player.getWorld().getName().equals("ptcoverworld")) {
                            try {
                                player.teleport(zoneManager.getTeamSpawn(zoneManager.getHomeZone(player.getName()).teamIndex(), plugin.getServer().getWorld(new NamespacedKey(plugin, "ptcoverworld")), wallManager));
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>You were sent back to the Overworld."));
                            } catch (Exception e) {
                                // throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
