package com.example.protectTheCore.core;

import com.example.protectTheCore.game.zone.ZoneManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.N;

import java.util.Objects;

import static com.example.protectTheCore.ProtectTheCore.*;

public class DimensionKicker {
    public static void DimensionKickerListenerLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (plugin.getConfig().getBoolean("game.restrict_players_to_overworld")) {
                        if (!player.getWorld().getName().equals("ptcoverworld")) {
                            try {
                                player.teleport(zoneManager.getTeamSpawn(zoneManager.getHomeZone(player.getName()).teamIndex(), Bukkit.getWorld(new NamespacedKey(plugin, "ptcoverworld")), wallManager));
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>You were sent back to the Overworld."));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
