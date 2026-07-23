package com.example.protectTheCore;

import com.example.protectTheCore.game.Cores;
import com.example.protectTheCore.core.EndShop;
import com.example.protectTheCore.game.Events;
import com.example.protectTheCore.game.events.ElectionEvent;
import com.example.protectTheCore.helper.HelperFunctions;
import com.example.protectTheCore.helper.WorldGenerator;
import com.example.protectTheCore.game.ProtectTheCoreGame;
import com.example.protectTheCore.game.events.AwakeningEvent;
import com.example.protectTheCore.game.supplydrops.SupplyDrop;
import com.example.protectTheCore.menu.config.ConfigMenu;
import com.example.protectTheCore.menu.supplydrops.SupplyDropMenu;
import com.example.protectTheCore.menu.teams.TeamsMenu;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class CommandRegistration {

    private final ProtectTheCore plugin;
    private final FileConfiguration config;
    private final ComponentLogger logger;
    private final EndShop endShop;
    private final SupplyDrop supplyDrop;
    private final SupplyDropMenu supplyDropMenu;
    private final WorldGenerator worldGenerator;
    private final ProtectTheCoreGame protectTheCoreGame;
    private final Cores cores;
    private final AwakeningEvent awakeningEvent;
    private final TeamsMenu teamsMenu;
    private final ConfigMenu configMenu;
    private final ElectionEvent electionEvent;
    private final Events events;

    public CommandRegistration(@NotNull ProtectTheCore plugin, @NotNull FileConfiguration config, @NotNull ComponentLogger logger, @NotNull EndShop endShop, @NotNull SupplyDrop supplyDrop, @NotNull WorldGenerator worldGenerator, @NotNull SupplyDropMenu supplyDropMenu, @NotNull ProtectTheCoreGame protectTheCoreGame, @NotNull Cores cores, @NotNull AwakeningEvent awakeningEvent, @NotNull TeamsMenu teamsMenu, @NotNull ConfigMenu configMenu, @NotNull ElectionEvent electionEvent, @NotNull Events events) {
        this.plugin = plugin;
        this.config = config;
        this.logger = logger;
        this.endShop = endShop;
        this.supplyDrop = supplyDrop;
        this.supplyDropMenu = supplyDropMenu;
        this.worldGenerator = worldGenerator;
        this.protectTheCoreGame = protectTheCoreGame;
        this.cores = cores;
        this.awakeningEvent = awakeningEvent;
        this.teamsMenu = teamsMenu;
        this.configMenu = configMenu;
        this.electionEvent = electionEvent;
        this.events = events;
    }

    public void registerCommand(LifecycleEventManager<@NotNull Plugin> lifecycleEventManager) {
        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            LiteralCommandNode<CommandSourceStack> buildCommand = Commands.literal("protectthecore")
                    .then(opBranch("spawn_end_shop")
                            .executes(ctx -> {
                                if (ctx.getSource().getExecutor() instanceof Player player) {
                                    endShop.spawnEndShop(player, new Location(plugin.getServer().getWorld(new NamespacedKey(plugin, "ptctheend")), -5, Objects.requireNonNull(plugin.getServer().getWorld(new NamespacedKey(plugin, "ptctheend"))).getHighestBlockYAt(-5, -20) - 1, -20));
                                }
                                else {
                                    endShop.spawnEndShop(new Location(plugin.getServer().getWorld(new NamespacedKey(plugin, "ptctheend")), -5, Objects.requireNonNull(plugin.getServer().getWorld(new NamespacedKey(plugin, "ptctheend"))).getHighestBlockYAt(-5, -20) - 1, -20));
                                }
                                return 1;
                            })
                    )
                    .then(opBranch("supply_drops")
                            .then(opBranch("spawn_supply_drop")
                                    .then(Commands.argument("supply_drop_id", IntegerArgumentType.integer(0))
                                            .executes(ctx -> {
                                                if (IntegerArgumentType.getInteger(ctx, "supply_drop_id") >= supplyDrop.getSupplyDropConfigSize()) {
                                                    Objects.requireNonNull(ctx.getSource().getExecutor()).sendMessage(Component.text("Index out of range!", NamedTextColor.RED));
                                                    return -1;
                                                }
                                                supplyDrop.spawnSupplyDrop(HelperFunctions.inventoryToJSONArray(supplyDrop.getInventory(IntegerArgumentType.getInteger(ctx, "supply_drop_id"))), supplyDrop.getLocation(IntegerArgumentType.getInteger(ctx, "supply_drop_id")), supplyDrop.getContainer(IntegerArgumentType.getInteger(ctx, "supply_drop_id")));
                                                return 1;
                                            })
                                    )
                            )
                            .executes(ctx -> {
                                if (!(ctx.getSource().getExecutor() instanceof Player player)) {
                                    logger.warn(Component.text("Command can only be executed by a player!", NamedTextColor.RED));
                                    return 1;
                                }
                                player.openInventory(supplyDropMenu.getInventory());
                                return 1;
                            })
                    )
                    .then(opBranch("event_time")
                            .then(Commands.literal("set")
                                    .then(Commands.argument("event_time", ArgumentTypes.time())
                                            .executes(ctx -> {
                                                config.set("game.time_end", LocalDateTime.now().plusSeconds(IntegerArgumentType.getInteger(ctx, "event_time") / 20).toString());
                                                plugin.saveConfig();
                                                return 1;
                                            })
                                    )
                            )
                            .then(Commands.literal("add")
                                    .then(Commands.argument("event_time", ArgumentTypes.time())
                                            .executes(ctx -> {
                                                config.set("game.time_end", LocalDateTime.parse(Objects.requireNonNull(config.getString("game.time_end")), DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")).plusSeconds(IntegerArgumentType.getInteger(ctx, "event_time") / 20).toString());
                                                plugin.saveConfig();
                                                return 1;
                                            })
                                    )
                            )
                    )
                    .then(Commands.literal("rules")
                            .executes(ctx -> {
                                Objects.requireNonNull(ctx.getSource().getExecutor()).sendMessage(Component.text("""
                                        Rules:\s
                                        1. Follow all rules of the Rift SMP.
                                        2. No entering the territory of other teams while the wall is up.
                                        3. No blocking the end portal.
                                        4. No monopolies.
                                        """, NamedTextColor.GOLD));
                                return 1;
                            })
                    )
                    .then(opBranch("give_crown")
                            .then(Commands.argument("team_id", IntegerArgumentType.integer(0))
                                    .then(Commands.argument("player_name", StringArgumentType.word())
                                            .executes(ctx -> {
                                                int teamIdx = IntegerArgumentType.getInteger(ctx, "team_id");
                                                String playerName = StringArgumentType.getString(ctx, "player_name");
                                                if (ctx.getSource().getSender() instanceof Player player) {
                                                    player.give(protectTheCoreGame.getCrown(teamIdx, playerName));
                                                } else {
                                                    logger.warn(Component.text("Command can only be executed by a player!", NamedTextColor.RED));
                                                }
                                                return 1;
                                            })
                                    )
                            )
                    )
                    .then(opBranch("give_core")
                            .then(Commands.argument("team_id", IntegerArgumentType.integer(0))
                                    .executes(ctx -> {
                                        int teamIdx = IntegerArgumentType.getInteger(ctx, "team_id");
                                        if (ctx.getSource().getSender() instanceof Player player) {
                                            cores.giveCore(teamIdx, player);
                                        } else {
                                            logger.warn(Component.text("Command can only be executed by a player!", NamedTextColor.RED));
                                        }
                                        return 1;
                                    })
                            )
                    )
                    .then(opBranch("teams")
                            .executes(ctx -> {
                                if (!(ctx.getSource().getExecutor() instanceof Player player)) {
                                    logger.warn(Component.text("Command can only be executed by a player!", NamedTextColor.RED));
                                    return 1;
                                }
                                player.openInventory(teamsMenu.getInventory());
                                return Command.SINGLE_SUCCESS;
                            }))
                    .then(opBranch("start")
                            .executes(ctx -> {
                                protectTheCoreGame.startGame();
                                return 1;
                            })
                    )
                    .then(opBranch("stop")
                            .executes(ctx -> {
                                protectTheCoreGame.stopGame();
                                ctx.getSource().getSender().sendMessage(
                                        Component.text("Event stopped.", NamedTextColor.GREEN));
                                return Command.SINGLE_SUCCESS;
                            })
                    )
                    .then(opBranch("lower_wall")
                            .executes(ctx -> {
                                protectTheCoreGame.lowerWall();

                                return Command.SINGLE_SUCCESS;
                            })
                    )
                    .then(opBranch("config")
                            .executes(ctx -> {
                                if (!(ctx.getSource().getExecutor() instanceof Player player)) {
                                    logger.warn(Component.text("Command can only be executed by a player!", NamedTextColor.RED));
                                    return 1;
                                }
                                player.openInventory(configMenu.getInventory());
                                return Command.SINGLE_SUCCESS;
                            })
                    )
                    .then(opBranch("teleport_to")
                            .then(Commands.literal("world")
                                    .executes(ctx -> {
                                        if (ctx.getSource().getExecutor() == null) {
                                            logger.info(Component.text("Command must be executed by a player!"));
                                            return -1;
                                        }
                                        if (plugin.getServer().getWorld(new NamespacedKey("minecraft", "overworld")) == null) {
                                            ctx.getSource().getExecutor().sendMessage(Component.text("World 'overworld' does not exist!", NamedTextColor.YELLOW));
                                            return -1;
                                        }
                                        WorldGenerator.teleportToWorld((Player) ctx.getSource().getExecutor(), Objects.requireNonNull(plugin.getServer().getWorld(new NamespacedKey("minecraft", "overworld"))));
                                        return 1;
                                    })
                            )
                            .then(Commands.literal("world_nether")
                                    .executes(ctx -> {
                                        if (ctx.getSource().getExecutor() == null) {
                                            logger.info(Component.text("Command must be executed by a player!"));
                                            return -1;
                                        }
                                        if (plugin.getServer().getWorld(new NamespacedKey("minecraft", "world_nether")) == null) {
                                            ctx.getSource().getExecutor().sendMessage(Component.text("World 'world_nether' does not exist!", NamedTextColor.YELLOW));
                                            return -1;
                                        }
                                        WorldGenerator.teleportToWorld((Player) ctx.getSource().getExecutor(), Objects.requireNonNull(plugin.getServer().getWorld(new NamespacedKey("minecraft", "world_nether"))));
                                        return 1;
                                    })
                            )
                            .then(Commands.literal("world_the_end")
                                    .executes(ctx -> {
                                        if (ctx.getSource().getExecutor() == null) {
                                            logger.info(Component.text("Command must be executed by a player!"));
                                            return -1;
                                        }
                                        if (plugin.getServer().getWorld(new NamespacedKey("minecraft", "world_the_end")) == null) {
                                            ctx.getSource().getExecutor().sendMessage(Component.text("World 'world_the_end' does not exist!", NamedTextColor.YELLOW));
                                            return -1;
                                        }
                                        WorldGenerator.teleportToWorld((Player) ctx.getSource().getExecutor(), Objects.requireNonNull(plugin.getServer().getWorld(new NamespacedKey("minecraft", "world_the_end"))));
                                        return 1;
                                    })
                            )
                            .then(Commands.literal("ptcoverworld")
                                    .executes(ctx -> {
                                        if (ctx.getSource().getExecutor() == null) {
                                            logger.info(Component.text("Command must be executed by a player!"));
                                            return -1;
                                        }
                                        if (plugin.getServer().getWorld(new NamespacedKey(plugin, "ptcoverworld")) == null) {
                                            ctx.getSource().getExecutor().sendMessage(Component.text("World 'ptcoverworld' does not exist! Generating it now."));
                                            worldGenerator.createOverworld(plugin.getConfig().getInt("config.overworld.seed"), plugin.getConfig().getInt("config.overworld.border"));
                                            return -1;
                                        }
                                        WorldGenerator.teleportToWorld((Player) ctx.getSource().getExecutor(), Objects.requireNonNull(plugin.getServer().getWorld(new NamespacedKey(plugin, "ptcoverworld"))));
                                        return 1;
                                    })
                            )
                            .then(Commands.literal("ptcnether")
                                    .executes(ctx -> {
                                        if (ctx.getSource().getExecutor() == null) {
                                            logger.info(Component.text("Command must be executed by a player!"));
                                            return -1;
                                        }
                                        if (plugin.getServer().getWorld(new NamespacedKey(plugin, "ptcnether")) == null) {
                                            ctx.getSource().getExecutor().sendMessage(Component.text("World 'ptcnether' does not exist! Generating it now."));
                                            worldGenerator.createNether(plugin.getConfig().getInt("config.nether.seed"), plugin.getConfig().getInt("config.nether.border"));
                                            return -1;
                                        }
                                        WorldGenerator.teleportToWorld((Player) ctx.getSource().getExecutor(), Objects.requireNonNull(plugin.getServer().getWorld(new NamespacedKey(plugin, "ptcnether"))));
                                        return 1;
                                    })
                            )
                            .then(Commands.literal("ptctheend")
                                    .executes(ctx -> {
                                        if (ctx.getSource().getExecutor() == null) {
                                            logger.info(Component.text("Command must be executed by a player!"));
                                            return -1;
                                        }
                                        if (plugin.getServer().getWorld(new NamespacedKey(plugin, "ptctheend")) == null) {
                                            ctx.getSource().getExecutor().sendMessage(Component.text("World 'ptctheend' does not exist! Generating it now."));
                                            worldGenerator.createTheEnd(plugin.getConfig().getInt("config.the_end.seed"), plugin.getConfig().getInt("config.the_end.border"));
                                            return -1;
                                        }
                                        WorldGenerator.teleportToWorld((Player) ctx.getSource().getExecutor(), Objects.requireNonNull(plugin.getServer().getWorld(new NamespacedKey(plugin, "ptctheend"))));
                                        return 1;
                                    })
                            )
                    )
                    .then(opBranch("refill")
                            .executes(ctx -> {
                                awakeningEvent.fillAssassinGear(Objects.requireNonNull(awakeningEvent.getAssassin()), false);
                                return 1;
                            })
                            .then(Commands.argument("include_totems", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    awakeningEvent.fillAssassinGear(Objects.requireNonNull(awakeningEvent.getAssassin()), BoolArgumentType.getBool(ctx, "include_totems"));
                                    return 1;
                                })
                                .then(Commands.argument("target", ArgumentTypes.player())
                                    .executes(ctx -> {
                                        final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                                        final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                        awakeningEvent.fillAssassinGear(target, BoolArgumentType.getBool(ctx, "include_totems"));
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            )
                    )
                    .then(Commands.literal("vote")
                            .then(Commands.argument("candidate", ArgumentTypes.player())).executes(ctx -> {
                                if (ctx.getSource().getExecutor() == null) {
                                    logger.info(Component.text("Command must be executed by a player!"));
                                    return -1;
                                }
                                if (events.getNextEvent().getString("name") == "Election") {
                                    ctx.getSource().getExecutor().sendMessage(MiniMessage.miniMessage().deserialize("<red>You do not have permission to execute this command!"));
                                    return -1;
                                }
                                electionEvent.putVote(ctx.getSource().getExecutor().getName(), ctx.getArgument("candidate", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst().getName());
                                return Command.SINGLE_SUCCESS;
                            }))
                    .then(opBranch("debug")
                            .executes(ctx -> {
                                if (ctx.getSource().getExecutor() == null) {
                                    logger.info(Component.text("Command must be executed by a player!"));
                                    return -1;
                                }
                                Player player = (Player) ctx.getSource().getExecutor();
                                if (player.getPersistentDataContainer().has(new NamespacedKey(plugin, "debug_mode"))) {
                                    player.getPersistentDataContainer().set(new NamespacedKey(plugin, "debug_mode"), PersistentDataType.BOOLEAN, !player.getPersistentDataContainer().get(new NamespacedKey(plugin, "debug_mode"), PersistentDataType.BOOLEAN));
                                }
                                else {
                                    player.getPersistentDataContainer().set(new NamespacedKey(plugin, "debug_mode"), PersistentDataType.BOOLEAN, true);
                                }
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<white>Debug mode has been set to <bold>" + (player.getPersistentDataContainer().get(new NamespacedKey(plugin, "debug_mode"), PersistentDataType.BOOLEAN) ? "<green>TRUE" : "<red>FALSE") + "<bold:false><white>."));
                                return Command.SINGLE_SUCCESS;
                            })
                    )
                    .build();
            commands.registrar().register(buildCommand, "Commands for the Protect The Core event.", List.of("ptc"));
        });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> opBranch(String name) {
        return Commands.literal(name).requires(source -> source.getSender().isOp());
    }
}
