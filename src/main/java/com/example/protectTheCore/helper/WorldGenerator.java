package com.example.protectTheCore.helper;

import com.example.protectTheCore.ProtectTheCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WorldGenerator {

    private final ProtectTheCore plugin;

    public WorldGenerator(@NotNull ProtectTheCore plugin) {
        this.plugin = plugin;
    }

    public World createOverworld(Integer seed, Integer worldBorder) {
        NamespacedKey overworldKey = new NamespacedKey(plugin, "ptcoverworld");
        WorldCreator creator = new WorldCreator(overworldKey).seed(seed).environment(World.Environment.NORMAL);
        World ptcOverworld = creator.createWorld();
        if (ptcOverworld != null) {
            WorldBorder overworldBorder = ptcOverworld.getWorldBorder();
            overworldBorder.setCenter(0.0, 0.0);
            overworldBorder.setSize(worldBorder * 2);
        }
        return ptcOverworld;
    }
    public World createNether(Integer seed, Integer worldBorder) {
        NamespacedKey netherKey = new NamespacedKey(plugin, "ptcnether");
        WorldCreator creator = new WorldCreator(netherKey).seed(seed).environment(World.Environment.NETHER);
        World ptcNether = creator.createWorld();
        if (ptcNether != null) {
            WorldBorder netherBorder = ptcNether.getWorldBorder();
            netherBorder.setSize(worldBorder * 2);
            netherBorder.setCenter(0.0, 0.0);
        }
        return ptcNether;
    }
    public World createTheEnd(Integer seed, Integer worldBorder) {
        NamespacedKey theEndKey = new NamespacedKey(plugin, "ptctheend");

        WorldCreator creator = new WorldCreator(theEndKey)
                .seed(seed)
                .environment(World.Environment.THE_END);

        World ptcTheEnd = creator.createWorld();
        ptcTheEnd.setSpawnLocation(0, ptcTheEnd.getHighestBlockYAt(0, -5) + 1, -5, 180);

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
