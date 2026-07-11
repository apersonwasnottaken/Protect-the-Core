package com.example.protectTheCore.helper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;

import static com.example.protectTheCore.ProtectTheCore.plugin;

public class WorldGenerator {
    public static World createOverworld(Integer seed, Integer worldBorder) {
        NamespacedKey overworldKey = new NamespacedKey(plugin, "ptcoverworld");
        WorldCreator creator = WorldCreator.ofKey(overworldKey).seed(seed).environment(World.Environment.NORMAL);
        World ptcOverworld = creator.createWorld();
        if (ptcOverworld != null) {
            WorldBorder overworldBorder = ptcOverworld.getWorldBorder();
            overworldBorder.setCenter(0.0, 0.0);
            overworldBorder.setSize(worldBorder * 2);
        }
        return ptcOverworld;
    }
    public static World createNether(Integer seed, Integer worldBorder) {
        NamespacedKey netherKey = new NamespacedKey(plugin, "ptcnether");
        WorldCreator creator = WorldCreator.ofKey(netherKey).seed(seed).environment(World.Environment.NETHER);
        World ptcNether = creator.createWorld();
        if (ptcNether != null) {
            WorldBorder netherBorder = ptcNether.getWorldBorder();
            netherBorder.setSize(worldBorder * 2);
            netherBorder.setCenter(0.0, 0.0);
        }
        return ptcNether;
    }
    public static World createTheEnd(Integer seed, Integer worldBorder) {
        NamespacedKey theEndKey = new NamespacedKey(plugin, "ptctheend");
        WorldCreator creator = WorldCreator.ofKey(theEndKey).seed(seed).environment(World.Environment.THE_END);
        World ptcTheEnd = creator.createWorld();
        if (ptcTheEnd != null) {
            WorldBorder theEndBorder = ptcTheEnd.getWorldBorder();
            theEndBorder.setSize(worldBorder * 2);
            theEndBorder.setCenter(0.0, 0.0);
        }
        return ptcTheEnd;
    }
    public static void teleportToWorld(Player player, World world) {
        Location spawnLocation = world.getSpawnLocation();
        spawnLocation.setY(world.getHighestBlockYAt(spawnLocation) + 1);
        player.teleportAsync(spawnLocation).thenAccept(success -> {
            if (success) {
                player.sendMessage(Component.text("Teleported to " + world.getName() + "!", TextColor.color(255, 255, 0)));
            } else {
                player.sendMessage(Component.text("Teleportation failed. The area might be unsafe.",TextColor.color(255, 0, 0)));
            }
        });
    }
}
