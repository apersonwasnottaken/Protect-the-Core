package com.example.protectTheCore.listeners;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.core.EndShop;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.EnderDragon;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;

import java.io.InputStream;
import java.util.Random;

import static com.example.protectTheCore.ProtectTheCore.plugin;

public class DisableDragonFightListener implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (plugin.getConfig().getBoolean("config.the_end.dragon_fight")) return;
        disableDragonFight(event.getWorld());
    }

    public static void disableDragonFight(World world) {
        if (!world.getName().equalsIgnoreCase("ptctheend")) return;
        EndShop.spawnEndShop(new Location(world, -5, world.getHighestBlockYAt(-5, -20) - 1, -20));
        DragonBattle battle = world.getEnderDragonBattle();
        if (battle == null) return;
        EnderDragon dragon = battle.getEnderDragon();
        if (dragon != null) {
            dragon.remove();
        }
        battle.generateEndPortal(true);
    }
}