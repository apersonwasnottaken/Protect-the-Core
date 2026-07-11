package com.example.protectTheCore;

import com.example.protectTheCore.core.*;
import com.example.protectTheCore.game.ProtectTheCoreGame;
import com.example.protectTheCore.game.supplydrops.SupplyDrop;
import com.example.protectTheCore.game.wall.WallCollisionListener;
import com.example.protectTheCore.game.wall.WallManager;
import com.example.protectTheCore.game.wall.WallProtectionListener;
import com.example.protectTheCore.game.zone.ZoneEnforcementListener;
import com.example.protectTheCore.game.zone.ZoneManager;
import com.example.protectTheCore.helper.WorldGenerator;
import com.example.protectTheCore.signgui.SignInputManager;
import com.example.protectTheCore.listeners.*;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import static com.example.protectTheCore.core.Teams.*;

public final class ProtectTheCore extends JavaPlugin {

    public static ProtectTheCore plugin;
    public static ComponentLogger logger;
    public static FileConfiguration config;
    public static SignInputManager signInputManager;
    public static GlowManager glowManager;
    public static WallManager wallManager;
    public static ZoneManager zoneManager;       // NEW
    public static DeathInterceptorListener deathInterceptor;
    public static AfterWallsListener afterWallsListener;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();

        try {
            glowManager = new GlowManager(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        signInputManager = new SignInputManager(this);
        PacketEvents.getAPI().getEventManager().registerListener(signInputManager);

        Teams.parseTeamsConfig();

        this.saveDefaultConfig();
        plugin = this;
        config = this.getConfig();
        logger = plugin.getComponentLogger();

        wallManager = new WallManager(plugin);
        zoneManager = new ZoneManager();
        deathInterceptor = new DeathInterceptorListener();
        afterWallsListener = new AfterWallsListener();

        Bukkit.getPluginManager().registerEvents(new MenusEventListener(), this);
        Bukkit.getPluginManager().registerEvents(afterWallsListener, this);
        Bukkit.getPluginManager().registerEvents(new AnvilFilterListener(), this);
        Bukkit.getPluginManager().registerEvents(new DisableDragonFightListener(), this);

        getServer().getPluginManager().registerEvents(new PortalLinkingListener(), this);
        getServer().getPluginManager().registerEvents(new CrystalListener(), this);
        getServer().getPluginManager().registerEvents(deathInterceptor, this);
        getServer().getPluginManager().registerEvents(new WallProtectionListener(wallManager, plugin), this);
        getServer().getPluginManager().registerEvents(new WallCollisionListener(wallManager), this);
        getServer().getPluginManager().registerEvents(new ZoneEnforcementListener(zoneManager), this); // NEW

        WorldGenerator.createOverworld(plugin.getConfig().getInt("config.overworld.seed"), plugin.getConfig().getInt("config.overworld.border"));
        WorldGenerator.createNether(plugin.getConfig().getInt("config.nether.seed"), plugin.getConfig().getInt("config.nether.border"));
        WorldGenerator.createTheEnd(plugin.getConfig().getInt("config.the_end.seed"), plugin.getConfig().getInt("config.the_end.border"));

        Bukkit.getScheduler().runTaskLater(this, () -> {
            SupplyDrop.supplyDropLoop();
            World theEnd = Bukkit.getWorld(new NamespacedKey(this, "ptctheend"));
            if (theEnd != null && !getConfig().getBoolean("config.the_end.dragon_fight")) {
                DisableDragonFightListener.disableDragonFight(theEnd);
            }
        }, 1L);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (World world : Bukkit.getWorlds()) {
                forceLoadWallChunks(world, true);
            }
        }, 20L);

        new ProtectTheCoreGame().restoreStateFromConfig();

        CommandRegistration.registerCommand(this.getLifecycleManager());
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        for (World world : Bukkit.getWorlds()) {
            forceLoadWallChunks(world, false);
        }
        try {
            glowManager.shutdown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.saveConfig();
        try {
            saveTeamsConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void forceLoadWallChunks(World world, boolean forceLoad) {
        for (int cx = -1; cx <= 0; cx++) {
            for (int cz = -1; cz <= 0; cz++) {
                world.setChunkForceLoaded(cx, cz, forceLoad);
            }
        }
    }

}