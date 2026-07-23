package com.example.protectTheCore.listeners;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.core.EndShop;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.boss.DragonBattle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;

public class DisableDragonFightListener implements Listener {

    private final ProtectTheCore plugin;
    private final EndShop endShop;

    public DisableDragonFightListener(@NotNull ProtectTheCore plugin, @NotNull EndShop endShop) {
        this.plugin = plugin;
        this.endShop = endShop;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (plugin.getConfig().getBoolean("config.the_end.dragon_fight")) return;
        disableDragonFight(event.getWorld());
    }

    public void disableDragonFight(World world) {
        if (!world.getName().equalsIgnoreCase("ptctheend")) return;
        endShop.spawnEndShop(new Location(world, -5, world.getHighestBlockYAt(-5, -20) - 1, -20));
        DragonBattle battle = world.getEnderDragonBattle();
        if (battle == null) return;
        EnderDragon dragon = battle.getEnderDragon();
        if (dragon != null) {
            dragon.remove();
        }
        battle.generateEndPortal(true);
    }
}