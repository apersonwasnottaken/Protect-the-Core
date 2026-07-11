package com.example.protectTheCore.game;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.core.Teams;
import com.example.protectTheCore.game.events.AwakeningEvent;
import com.example.protectTheCore.helper.WorldGenerator;
import com.example.protectTheCore.game.wall.WallManager;
import com.example.protectTheCore.game.zone.ZoneManager;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.protectTheCore.ProtectTheCore.*;
import static com.example.protectTheCore.game.Events.parseEvents;
import static com.example.protectTheCore.game.Events.populateDefaultEvents;
import static com.example.protectTheCore.game.GameScoreboard.findEntitiesWithKey;
import static com.example.protectTheCore.game.GameScoreboard.findEntitiesWithKeyValue;

public class ProtectTheCoreGame {

    private World world;

    public static ItemStack getCrown(int teamIdx, String player) {
        ItemStack crown = new ItemStack(Material.GOLDEN_HELMET);
        crown.editMeta(meta -> {
            meta.displayName(Component.text(Teams.getTeamName(teamIdx) + "'s", TextColor.color(Teams.getTeamColor(teamIdx))).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(MiniMessage.miniMessage().deserialize("<italic:false><gold> Crown</gold>")));
            meta.lore(List.of(MiniMessage.miniMessage().deserialize("<italic:false><white>Awarded to <yellow>" + player + "</yellow>, the rightful ruler of the <aqua>Rift SMP.")));
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "ptc_crown"), PersistentDataType.BOOLEAN, true);
            NamespacedKey customAttributeKey = new NamespacedKey(plugin, "custom_attribute");
            AttributeModifier healthModifier = new AttributeModifier(
                    customAttributeKey,
                    4.0,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.ARMOR
            );
            AttributeModifier armorModifier = new AttributeModifier(
                    customAttributeKey,
                    3.0,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.ARMOR
            );
            AttributeModifier armorToughnessModifier = new AttributeModifier(
                    customAttributeKey,
                    3.0,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.ARMOR
            );
            AttributeModifier knockbackResistanceModifier = new AttributeModifier(
                    customAttributeKey,
                    0.1,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.ARMOR
            );
            meta.addAttributeModifier(Attribute.MAX_HEALTH, healthModifier);
            meta.addAttributeModifier(Attribute.ARMOR, armorModifier);
            meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, armorToughnessModifier);
            meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, knockbackResistanceModifier);
            meta.addEnchant(Enchantment.PROTECTION, 5, true);
        });
        crown.setData(DataComponentTypes.MAX_DAMAGE, 407);
        return crown;
    }


    public void startGame() {
        int teamSize = Teams.getTeamsConfig().size();

        if (teamSize != 2 && teamSize != 4) {
            ProtectTheCore.logger.error(Component.text(
                    "Only 2 or 4 teams supported.",
                    NamedTextColor.RED
            ));
            return;
        }

        plugin.getConfig().set("game.time_end", LocalDateTime.now().plusSeconds(plugin.getConfig().getInt("game.duration")).toString());
        populateDefaultEvents();

        world = Bukkit.getWorld("ptcoverworld");
        if (world == null) {
            world = WorldGenerator.createOverworld(plugin.getConfig().getInt("config.overworld.seed"), plugin.getConfig().getInt("config.overworld.border"));
        }

        ProtectTheCore.zoneManager.setZones(buildZones(Teams.getTeamsConfig().size(), teamSize == 2 ? WallManager.WallMode.X : WallManager.WallMode.BOTH, world), world);

        deathInterceptor.setDefaultWorld(world);

        if (world == null) {
            ProtectTheCore.logger.error("World creation failed!");
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            preloadChunks(world);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    runGame(world, teamSize);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, 0L);
        }, 0L);
    }

    public static void startGameLoop() {
        try {
            wallManager.getBufferSize();
            parseEvents();
            afterWallsListener.parseDestroyedCores();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new BukkitRunnable() {
            int timer = GameScoreboard.getTimer();
            int counter = 0;
            boolean lowered = false;
            boolean awakeningStarted = false;
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!plugin.getConfig().getBoolean("game.started")) {
                        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                        GameScoreboard.bossBar.removeViewer(player);
                        continue;
                    }
                    try {
                        GameScoreboard.sendPtcScoreboard(player, Math.toIntExact(Duration.between(LocalDateTime.now(), LocalDateTime.parse(Objects.requireNonNull(plugin.getConfig().getString("game.time_end")))).toMillis()));
                        GameScoreboard.sendPtcBossbar(player, timer, GameScoreboard.bossBar);
                        if (timer < 1) {
                            if (!lowered) {
                                ProtectTheCoreGame.lowerWall();
                                lowered = true;
                                plugin.getConfig().set("game.zones_enabled", false);
                            }
                        }
                        player.displayName(Component.text("[LEADER] ", TextColor.color(Teams.getTeamColor(Teams.getTeamIndexFromPlayer(player.getName())))).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.TRUE).append(Component.text(player.getName(), TextColor.color(Teams.getTeamColor(Teams.getTeamIndexFromPlayer(player.getName()))))));
                        if (Objects.equals(Teams.getTeamLeader(Teams.getTeamIndexFromPlayer(player.getName())), player.getName())) {
                            if (timer <= 60 * 10 && counter < 1) {
                                player.sendMessage(Component.text("You have 10m to place down your core!", NamedTextColor.RED));
                                player.showTitle(Title.title(Component.text("Time Warning!", NamedTextColor.RED), Component.text("You have 10 minutes to place down your core!", NamedTextColor.GRAY)));
                                counter++;
                            }
                            else if (timer <= 60 * 5 && counter < 2) {
                                player.sendMessage(Component.text("You have 5m to place down your core!", NamedTextColor.RED));
                                player.showTitle(Title.title(Component.text("Time Warning!", NamedTextColor.RED), Component.text("You have 5 minutes to place down your core!", NamedTextColor.GRAY)));
                                counter++;
                            }
                            else if (timer <= 60 && counter < 3) {
                                player.sendMessage(Component.text("You have 60s to place down your core!", NamedTextColor.RED));
                                player.showTitle(Title.title(Component.text("Time Warning!", NamedTextColor.RED), Component.text("You have 60 seconds to place down your core!", NamedTextColor.GRAY)));
                                counter++;
                            }
                            else if (timer <= 30 && counter < 4) {
                                player.sendMessage(Component.text("You have 30s to place down your core!", NamedTextColor.RED));
                                player.showTitle(Title.title(Component.text("Time Warning!", NamedTextColor.RED), Component.text("You have 30 seconds to place down your core!", NamedTextColor.GRAY)));
                                counter++;
                            }
                        }
                    } catch (IOException e) {
                        ProtectTheCore.logger.error(e.toString());
                    }
                }
                for (int i = 0; i < Teams.getTeamsConfig().size(); i++) {
                    World customOverworld = Bukkit.getWorld("ptcoverworld");
                    if (customOverworld == null) continue;
                    if (!Teams.teamHasCrown(i)) {
                        EnderCrystal enderCrystal = Teams.getTeamCore(i, customOverworld);
                        if (enderCrystal == null) {
                            continue;
                        }
                        if (timer % 90 == 0 && WallManager.WallMode.valueOf(plugin.getConfig().getString("game.wall_mode")) == WallManager.WallMode.NONE) {
                            enderCrystal.getPersistentDataContainer().set(new NamespacedKey(plugin, "crystal_health"), PersistentDataType.INTEGER, enderCrystal.getPersistentDataContainer().get(new NamespacedKey(plugin, "crystal_health"), PersistentDataType.INTEGER) - 1);
                        }
                        Teams.getTeamMembers(i).forEach(member -> {
                            Player player = Bukkit.getPlayer(((JSONObject) member).getString("username"));
                            if (player != null && player.getGameMode() == GameMode.SURVIVAL) {
                                player.sendActionBar(Component.text("Your team has lost their crown! Your core will start to take damage once fight mode is on.", NamedTextColor.RED));
                            }
                        });
                    }
                    else {
                        if (Bukkit.getPlayer(Teams.getTeamLeader(i)) == null) continue;
                        Objects.requireNonNull(Bukkit.getPlayer(Teams.getTeamLeader(i))).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 255, true, true));
                    }
                }
                if (LocalDateTime.now().isAfter(Events.getEventStart("The Awakening")) && !awakeningStarted) {
                    AwakeningEvent.awakeningEventLoop(config.getBoolean("events.awakening.started"));
                    awakeningStarted = true;
                    config.set("events.awakening.started", true);
                }
                timer = GameScoreboard.getTimer();
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
    public static void lowerWall() {
        ProtectTheCore.wallManager.setMode(WallManager.WallMode.NONE);
        ProtectTheCore.zoneManager.clear();
        plugin.getConfig().set("game.wall_mode", WallManager.WallMode.NONE.name());
        plugin.getConfig().set("game.zones_enabled", false);
        plugin.saveConfig();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(Component.text("The wall has come down - go fight!", NamedTextColor.GOLD));
        }
    }

    public void stopGame() {
        ProtectTheCore.wallManager.setMode(WallManager.WallMode.NONE);
        ProtectTheCore.zoneManager.clear();
        world = Bukkit.getWorld("world");
        if (world == null) {
            world = Bukkit.getWorlds().getFirst();
        }
        deathInterceptor.setDefaultWorld(world);
        plugin.getConfig().set("game.started", false);
        plugin.getConfig().set("game.wall_mode", WallManager.WallMode.NONE.name());
        plugin.getConfig().set("game.zones_enabled", false);
        plugin.saveConfig();
    }

    public void restoreStateFromConfig() {
        boolean started = plugin.getConfig().getBoolean("game.started", false);
        if (!started) return;
        String wallModeName = plugin.getConfig().getString("game.wall_mode", "NONE");
        WallManager.WallMode wallMode;
        try {
            wallMode = WallManager.WallMode.valueOf(wallModeName);
        } catch (IllegalArgumentException e) {
            wallMode = WallManager.WallMode.NONE;
        }
        if (wallMode == WallManager.WallMode.NONE) return;
        world = Bukkit.getWorld("ptcoverworld");
        if (world == null) return;
        if (plugin.getConfig().get("game.time_end") != null) {
            ProtectTheCore.zoneManager.setZones(buildZones(Teams.getTeamsConfig().size(), wallMode, world), world);
        }
        deathInterceptor.setDefaultWorld(world);
        ProtectTheCore.wallManager.setMode(wallMode);
        boolean zonesEnabled = plugin.getConfig().getBoolean("game.zones_enabled", false);
        if (zonesEnabled) {
            int teamSize = Teams.getTeamsConfig().size();
            ProtectTheCore.zoneManager.setZones(buildZones(teamSize, wallMode, world), world);
        }
        ProtectTheCore.logger.info(Component.text(
                "Game state restored from config: wall=" + wallMode + ", zones=" + zonesEnabled,
                NamedTextColor.GREEN));
        startGameLoop();
    }

    private void preloadChunks(World world) {
        int cx = world.getSpawnLocation().getBlockX() >> 4;
        int cz = world.getSpawnLocation().getBlockZ() >> 4;
        for (int x = cx - 3; x <= cx + 3; x++) {
            for (int z = cz - 3; z <= cz + 3; z++) {
                world.getChunkAtAsync(x, z);
            }
        }
    }

    private void runGame(World world, int teamSize) throws IOException {
        Events.populateDefaultEvents();
        WallManager.WallMode mode = (teamSize == 2)
                ? WallManager.WallMode.X
                : WallManager.WallMode.BOTH;
        ProtectTheCore.zoneManager.setZones(buildZones(teamSize, mode, world), world);
        Set<Player> processed = new HashSet<>();
        int index = 0;
        plugin.getConfig().set("game.time_end", LocalDateTime.now().plusSeconds(plugin.getConfig().getInt("game.duration")).toString());
        plugin.saveConfig();
        for (JSONObject obj : Teams.getTeamsConfig()) {
            JSONArray members = (JSONArray) obj.get("members");
            if (members.isEmpty()) return;
            Location base = zoneManager.getTeamSpawn(Teams.getTeamIndexFromPlayer(Bukkit.getOfflinePlayer(members.getJSONObject(0).getString("username")).getName()), world, wallManager);
            Location coreLocation = zoneManager.getTeamCenter(Teams.getTeamIndexFromPlayer(Bukkit.getOfflinePlayer(members.getJSONObject(0).getString("username")).getName()), world);
            if (findEntitiesWithKeyValue(findEntitiesWithKey(Bukkit.getWorld(new NamespacedKey(plugin, "ptcoverworld")), new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER), new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER, index).size() > 0) {
                logger.warn("Core " + index + " already detected!");
            }
            else {
                Cores.spawnCrystal(coreLocation.add(0,1,0), index, false);
            }
            for (Object memberObj : members) {
                Player player = Bukkit.getPlayer(
                        ((JSONObject) memberObj).getString("username")
                );
                if (player == null || processed.contains(player)) continue;
                if (GameScoreboard.bossBar != null) {
                    GameScoreboard.bossBar.removeViewer(player);
                }
                if (Objects.equals(Teams.getTeamLeader(Teams.getTeamIndexFromPlayer(player.getName())), player.getName())) {
                    player.sendMessage(Component.text("As the team leader, you were given a crown.", NamedTextColor.GRAY));
                    player.give(getCrown(Teams.getTeamIndexFromPlayer(player.getName()), player.getName()));
                    // Cores.giveCore(Teams.getTeamIndexFromPlayer(player.getName()), player);
                }
                processed.add(player);
                initScoreboard(player);
                player.teleportAsync(base);
                player.sendMessage(Component.text("Event starting..."));
            }
            index++;
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ProtectTheCore.wallManager.setMode(mode);
            List<ZoneManager.Zone> zones = buildZones(teamSize, mode, world);
            ProtectTheCore.zoneManager.setZones(zones, world);
            plugin.getConfig().set("game.started", true);
            plugin.getConfig().set("game.wall_mode", mode.name());
            plugin.getConfig().set("game.zones_enabled", true);
            plugin.saveConfig();
            startGameLoop();
        }, 20L);
    }

    public static void initScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("ptcscoreboard", Criteria.DUMMY, Component.empty());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (int i = 0; i < 15; i++) {
            String entryKey = "§" + Integer.toHexString(i) + "§r";
            Team team = scoreboard.registerNewTeam("line_" + i);
            team.addEntry(entryKey);
            Score score = objective.getScore(entryKey);
            score.setScore(15 - i);
            score.numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat.fixed(Component.empty()));
        }
        player.setScoreboard(scoreboard);
    }

    private List<ZoneManager.Zone> buildZones(int teamSize, WallManager.WallMode mode, World world) {
        List<ZoneManager.Zone> zones = new ArrayList<>();
        double buffer = wallManager.getBufferSize();
        String worldType = world.getName().equals("ptcoverworld") ? "overworld" :
                world.getName().equals("ptcnether") ? "nether" : "the_end";
        int borderSize = plugin.getConfig().getInt("config." + worldType + ".border", 1000);
        if (borderSize <= 0) {
            ProtectTheCore.logger.warn("Border size for world " + world.getName() + " is invalid. Defaulting to 1000.");
            borderSize = 1000;
        }
        if (teamSize == 2) {
            zones.add(new ZoneManager.Zone(0, -borderSize, -borderSize, -buffer, borderSize));
            zones.add(new ZoneManager.Zone(1, buffer, -borderSize, borderSize, borderSize));
        } else {
            zones.add(new ZoneManager.Zone(0, -borderSize, -borderSize, -buffer, -buffer));
            zones.add(new ZoneManager.Zone(1, -borderSize,  buffer, -buffer,  borderSize));
            zones.add(new ZoneManager.Zone(2,  buffer, -borderSize,  borderSize, -buffer));
            zones.add(new ZoneManager.Zone(3,  buffer,  buffer,  borderSize,  borderSize));
        }
        return zones;
    }
}