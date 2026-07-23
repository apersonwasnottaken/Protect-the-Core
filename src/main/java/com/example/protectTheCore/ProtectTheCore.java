package com.example.protectTheCore;

import com.example.protectTheCore.core.*;
import com.example.protectTheCore.game.Cores;
import com.example.protectTheCore.game.Events;
import com.example.protectTheCore.game.GameScoreboard;
import com.example.protectTheCore.game.ProtectTheCoreGame;
import com.example.protectTheCore.game.events.AwakeningEvent;
import com.example.protectTheCore.game.events.ElectionEvent;
import com.example.protectTheCore.game.supplydrops.SupplyDrop;
import com.example.protectTheCore.game.wall.WallCollisionListener;
import com.example.protectTheCore.game.wall.WallManager;
import com.example.protectTheCore.game.wall.WallProtectionListener;
import com.example.protectTheCore.game.zone.ZoneEnforcementListener;
import com.example.protectTheCore.game.zone.ZoneManager;
import com.example.protectTheCore.helper.PluginData;
import com.example.protectTheCore.helper.WorldGenerator;
import com.example.protectTheCore.menu.config.ConfigMenu;
import com.example.protectTheCore.menu.config.GameConfigMenu;
import com.example.protectTheCore.menu.config.dimensions.NetherConfigMenu;
import com.example.protectTheCore.menu.config.dimensions.OverworldConfigMenu;
import com.example.protectTheCore.menu.config.dimensions.TheEndConfigMenu;
import com.example.protectTheCore.menu.supplydrops.ManageSupplyDropMenu;
import com.example.protectTheCore.menu.supplydrops.SupplyDropCreationMenu;
import com.example.protectTheCore.menu.supplydrops.SupplyDropMenu;
import com.example.protectTheCore.menu.teams.ManageTeamMenu;
import com.example.protectTheCore.menu.teams.TeamCreationMenu;
import com.example.protectTheCore.menu.teams.TeamsMenu;
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

    private SignInputManager signInputManager;
    private GlowManager glowManager;
    private WallManager wallManager;
    private ZoneManager zoneManager;
    private DeathInterceptorListener deathInterceptor;
    private AfterWallsListener afterWallsListener;
    private ElectionEvent electionEvent;
    private SupplyDrop supplyDrop;
    private EndShop endShop;
    private WorldGenerator worldGenerator;
    private Teams teams;
    private Events events;
    private Cores cores;
    private GameScoreboard gameScoreboard;
    private AwakeningEvent awakeningEvent;
    private CommandRegistration commandRegistration;
    private MenusEventListener menusEventListener;
    private OverworldConfigMenu overworldConfigMenu;
    private NetherConfigMenu netherConfigMenu;
    private TheEndConfigMenu theEndConfigMenu;
    private ConfigMenu configMenu;
    private GameConfigMenu gameConfigMenu;
    private TeamsMenu teamsMenu;
    private TeamCreationMenu teamCreationMenu;
    private ManageTeamMenu manageTeamMenu;
    private SupplyDropMenu supplyDropMenu;
    private SupplyDropCreationMenu supplyDropCreationMenu;
    private ManageSupplyDropMenu manageSupplyDropMenu;
    private AnvilFilterListener anvilFilterListener;
    private DisableDragonFightListener dragonFightListener;
    private WallProtectionListener wallProtectionListener;
    private PortalLinkingListener portalLinkingListener;
    private CrystalListener crystalListener;
    private ProtectTheCoreGame protectTheCoreGame;
    private WallCollisionListener wallCollisionListener;
    private ZoneEnforcementListener zoneEnforcementListener;
    private PluginData pluginData;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    public Teams getTeams() {
        return this.teams;
    }
    public ZoneManager getZoneManager() {
        return this.zoneManager;
    }
    public GlowManager getGlowManager() {
        return this.glowManager;
    }

    @Override
    public void onEnable() {
        this.pluginData = new PluginData(this);
        this.zoneManager = new ZoneManager(this);
        this.teams = new Teams(this, getComponentLogger());
        this.events = new Events(pluginData);
        this.signInputManager = new SignInputManager(this);
        this.awakeningEvent = new AwakeningEvent(this);
        this.worldGenerator = new WorldGenerator(this);
        this.configMenu = new ConfigMenu(this);
        this.anvilFilterListener = new AnvilFilterListener(this);
        this.portalLinkingListener = new PortalLinkingListener(this);

        this.overworldConfigMenu = new OverworldConfigMenu(this, this.getConfig());
        this.netherConfigMenu = new NetherConfigMenu(this, this.getConfig());
        this.theEndConfigMenu = new TheEndConfigMenu(this, this.getConfig());
        this.wallManager = new WallManager(this, this.getComponentLogger());
        this.supplyDrop = new SupplyDrop(this, this.getComponentLogger(), pluginData);
        this.endShop = new EndShop(this, this.getComponentLogger());
        this.gameConfigMenu = new GameConfigMenu(this);
        this.teamCreationMenu = new TeamCreationMenu(this, this.getComponentLogger());
        this.supplyDropCreationMenu = new SupplyDropCreationMenu(this, this.getComponentLogger());

        this.cores = new Cores(this, teams);
        this.afterWallsListener = new AfterWallsListener(teams, pluginData);
        this.dragonFightListener = new DisableDragonFightListener(this, endShop);
        this.wallProtectionListener = new WallProtectionListener(this, wallManager);
        this.wallCollisionListener = new WallCollisionListener(this, wallManager);
        this.supplyDropMenu = new SupplyDropMenu(this, this.getComponentLogger(), supplyDrop);
        this.teamsMenu = new TeamsMenu(this, this.getComponentLogger(), teamCreationMenu);

        this.deathInterceptor = new DeathInterceptorListener(this, wallManager, zoneManager);
        this.gameScoreboard = new GameScoreboard(this.getConfig(), teams, events);
        this.manageTeamMenu = new ManageTeamMenu(this, this.getComponentLogger(), teamCreationMenu, teams);
        this.manageSupplyDropMenu = new ManageSupplyDropMenu(this, supplyDrop, supplyDropCreationMenu);
        this.zoneEnforcementListener = new ZoneEnforcementListener(zoneManager, wallManager);
        this.glowManager = new GlowManager(this, teamCreationMenu);

        this.crystalListener = new CrystalListener(this, this.getComponentLogger(), teams, afterWallsListener, cores);
        this.protectTheCoreGame = new ProtectTheCoreGame(this, this.getComponentLogger(), this.getConfig(), teams, events, cores, worldGenerator, zoneManager, deathInterceptor, wallManager, afterWallsListener, gameScoreboard, awakeningEvent, electionEvent);
        this.electionEvent = new ElectionEvent(this, teams, protectTheCoreGame, pluginData);
        this.commandRegistration = new CommandRegistration(this, getConfig(), getComponentLogger(), endShop, supplyDrop, worldGenerator, supplyDropMenu, protectTheCoreGame, cores, awakeningEvent, teamsMenu, configMenu, electionEvent, events);
        this.menusEventListener = new MenusEventListener(this, getConfig(), getComponentLogger(), signInputManager, supplyDrop, teams, overworldConfigMenu, netherConfigMenu, theEndConfigMenu, worldGenerator, configMenu, gameConfigMenu, teamCreationMenu, teamsMenu, manageTeamMenu, supplyDropMenu, supplyDropCreationMenu, manageSupplyDropMenu);

        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(signInputManager);

        teams.parseTeamsConfig();
        electionEvent.parseVotes();

        this.saveDefaultConfig();

        getServer().getPluginManager().registerEvents(menusEventListener, this);
        getServer().getPluginManager().registerEvents(afterWallsListener, this);
        getServer().getPluginManager().registerEvents(anvilFilterListener, this);
        getServer().getPluginManager().registerEvents(dragonFightListener, this);
        getServer().getPluginManager().registerEvents(portalLinkingListener, this);
        getServer().getPluginManager().registerEvents(crystalListener, this);
        getServer().getPluginManager().registerEvents(deathInterceptor, this);
        getServer().getPluginManager().registerEvents(wallProtectionListener, this);
        getServer().getPluginManager().registerEvents(wallCollisionListener, this);
        getServer().getPluginManager().registerEvents(zoneEnforcementListener, this);

        worldGenerator.createOverworld(getConfig().getInt("config.overworld.seed"), getConfig().getInt("config.overworld.border"));
        worldGenerator.createNether(getConfig().getInt("config.nether.seed"), getConfig().getInt("config.nether.border"));
        worldGenerator.createTheEnd(getConfig().getInt("config.the_end.seed"), getConfig().getInt("config.the_end.border"));

        getServer().getScheduler().runTaskLater(this, () -> {
            supplyDrop.supplyDropLoop();
            World theEnd = getServer().getWorld(new NamespacedKey(this, "ptctheend"));
            if (theEnd != null && !getConfig().getBoolean("config.the_end.dragon_fight")) {
                dragonFightListener.disableDragonFight(theEnd);
            }
        }, 1L);

        getServer().getScheduler().runTaskLater(this, () -> {
            for (World world : getServer().getWorlds()) {
                forceLoadWallChunks(world, true);
            }
        }, 20L);

        protectTheCoreGame.restoreStateFromConfig();

        commandRegistration.registerCommand(this.getLifecycleManager());
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        for (World world : getServer().getWorlds()) {
            forceLoadWallChunks(world, false);
        }
        try {
            glowManager.shutdown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.saveConfig();
        try {
            teams.saveTeamsConfig();
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