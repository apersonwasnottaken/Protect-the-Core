package com.example.protectTheCore.listeners;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.core.Teams;
import com.example.protectTheCore.helper.WorldGenerator;
import com.example.protectTheCore.game.supplydrops.SupplyDrop;
import com.example.protectTheCore.menu.*;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.protectTheCore.ProtectTheCore.*;
import static com.example.protectTheCore.core.Teams.getTeamIndexFromPlayer;
import static org.bukkit.Bukkit.getOfflinePlayer;
import static org.bukkit.Bukkit.getWorld;

public class MenusEventListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) throws Exception {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null) {
            return;
        }

        if (!(event.getView().getTopInventory().getHolder() instanceof CustomMenuHolder || event.getView().getTopInventory().getHolder() instanceof ManageSupplyDropMenu)) {
            return;
        }

        if (event.getClick().isShiftClick()) {
            int topInventorySize = event.getView().getTopInventory().getSize();

            if (event.getRawSlot() >= topInventorySize) {
                event.setCancelled(true);
                return;
            }
        }

        if (!(event.getView().getTopInventory().getHolder() instanceof ManageSupplyDropMenu && event.getSlot() < 27)) {
            event.setCancelled(true);
        }
        Player player = (Player) event.getView().getPlayer();

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) {
            return;
        }
        if (inventory.getHolder() instanceof ConfigMenu menu) {
            handleConfigMenu(event, menu, player, clicked);
        }
        if (inventory.getHolder() instanceof OverworldConfigMenu overworldConfigMenu || inventory.getHolder() instanceof NetherConfigMenu netherConfigMenu || inventory.getHolder() instanceof TheEndConfigMenu theEndConfigMenu) { // Menus are basically identical
            handleDimensionsConfigMenu(event, new OverworldConfigMenu(plugin), new NetherConfigMenu(plugin), new TheEndConfigMenu(plugin), player, clicked, inventory);
        }
        if (inventory.getHolder() instanceof TeamsMenu teamsMenu) {
            handleTeamsMenu(event, teamsMenu, player, clicked);
        }
        if (inventory.getHolder() instanceof TeamCreationMenu teamCreationMenu) {
            handleTeamCreationMenu(event, teamCreationMenu, player, clicked);
        }
        if (inventory.getHolder() instanceof ManageTeamMenu manageTeamMenu) {
            handleManageTeamsMenu(event, manageTeamMenu, player, clicked);
        }
        if (inventory.getHolder() instanceof GameConfigMenu gameConfigMenu) {
            handleGameConfigMenu(event, gameConfigMenu, player, clicked);
        }
        if (inventory.getHolder() instanceof SupplyDropMenu supplyDropMenu) {
            handleSupplyDropMenu(event, supplyDropMenu, player, clicked);
        }
        if (inventory.getHolder() instanceof SupplyDropCreationMenu supplyDropCreationMenu) {
            handleSupplyDropCreationMenu(event, supplyDropCreationMenu, player, clicked);
        }
        if (inventory.getHolder() instanceof ManageSupplyDropMenu manageSupplyDropMenu) {
            handleManageSupplyDropMenu(event, manageSupplyDropMenu, player, clicked);
        }
    }

    private void handleConfigMenu(InventoryClickEvent event, ConfigMenu menu, Player player, ItemStack clicked) {
        if (clicked.getType() == Material.GRASS_BLOCK) {
            // Overworld Config
            OverworldConfigMenu overworldConfigMenu = new OverworldConfigMenu(plugin);
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(overworldConfigMenu.getInventory()));
        } else if (clicked.getType() == Material.NETHERRACK) {
            // Nether Config
            NetherConfigMenu netherConfigMenu = new NetherConfigMenu(plugin);
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(netherConfigMenu.getInventory()));
        } else if (clicked.getType() == Material.END_STONE) {
            // The end Config
            TheEndConfigMenu theEndConfigMenu = new TheEndConfigMenu(plugin);
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(theEndConfigMenu.getInventory()));
        } else if (clicked.getType() == Material.LIME_CONCRETE) {
            WorldGenerator generator = new WorldGenerator();
            if (config.getBoolean("config.overworld.enabled")) {
                generator.createOverworld(config.getInt("config.overworld.seed"), config.getInt("config.overworld.border"));
                player.sendMessage(Component.text("Generating Overworld...", TextColor.color(255, 255, 0)));
            }
            if (config.getBoolean("config.nether.enabled")) {
                generator.createNether(config.getInt("config.nether.seed"), config.getInt("config.nether.border"));
                player.sendMessage(Component.text("Generating Nether...", TextColor.color(255, 255, 0)));
            }
            if (config.getBoolean("config.the_end.enabled")) {
                generator.createTheEnd(config.getInt("config.the_end.seed"), config.getInt("config.the_end.border"));
                player.sendMessage(Component.text("Generating The End...", NamedTextColor.YELLOW));
            }
        } else if (clicked.getType() == Material.RED_CONCRETE) {
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            World world = getWorld("ptcoverworld");
            assert world != null;
            for (Player player1 : world.getPlayers()) {
                player1.teleport(Objects.requireNonNull(Objects.requireNonNull(getWorld("overworld")).getSpawnLocation()));
                player1.sendMessage(Component.text("The world is being deleted, so you were teleported back to the overworld.", NamedTextColor.GREEN));
            }
            boolean isUnloaded = Bukkit.getServer().unloadWorld(world, false);
            if (!isUnloaded) {
                event.getView().getPlayer().sendMessage(Component.text("Failed to unload the world: " + world.getName(), NamedTextColor.RED));
                return;
            }
            File worldFolder = world.getWorldFolder();
            try {
                FileUtils.deleteDirectory(worldFolder);
                event.getView().getPlayer().sendMessage(Component.text("Successfully deleted world: " + world.getName(), NamedTextColor.GREEN));
            } catch (IOException e) {
                event.getView().getPlayer().sendMessage(Component.text("Could not delete the world folder for " + world.getName(), NamedTextColor.RED));
                ProtectTheCore.logger.error(Component.text("An error occurred while deleting the world '" + world.getName() + "'.\n" + e, NamedTextColor.RED));
            }
            world = getWorld("ptcnether");
            assert world != null;
            for (Player player1 : world.getPlayers()) {
                player1.teleport(Objects.requireNonNull(Objects.requireNonNull(getWorld("overworld")).getSpawnLocation()));
                player1.sendMessage(Component.text("The world is being deleted, so you were teleported back to the overworld.", NamedTextColor.GREEN));
            }
            isUnloaded = Bukkit.getServer().unloadWorld(world, false);
            if (!isUnloaded) {
                event.getView().getPlayer().sendMessage(Component.text("Failed to unload the world: " + world.getName(), NamedTextColor.RED));
                return;
            }
            worldFolder = world.getWorldFolder();
            try {
                FileUtils.deleteDirectory(worldFolder);
                event.getView().getPlayer().sendMessage(Component.text("Successfully deleted world: " + world.getName(), NamedTextColor.GREEN));
            } catch (IOException e) {
                event.getView().getPlayer().sendMessage(Component.text("Could not delete the world folder for " + world.getName(), TextColor.color(255, 0, 0)));
                ProtectTheCore.logger.error(Component.text("An error occurred while deleting the world '" + world.getName() + "'.\n" + e, NamedTextColor.RED));
            }
            world = getWorld("ptctheend");
            assert world != null;
            for (Player player1 : world.getPlayers()) {
                player1.teleport(Objects.requireNonNull(Objects.requireNonNull(getWorld("overworld")).getSpawnLocation()));
                player1.sendMessage(Component.text("The world is being deleted, so you were teleported back to the overworld.", TextColor.color(0, 255, 0)));
            }
            isUnloaded = Bukkit.getServer().unloadWorld(world, false);
            if (!isUnloaded) {
                event.getView().getPlayer().sendMessage(Component.text("Failed to unload the world: " + world.getName(), TextColor.color(255, 0, 0)));
                return;
            }
            worldFolder = world.getWorldFolder();
            try {
                FileUtils.deleteDirectory(worldFolder);
                event.getView().getPlayer().sendMessage(Component.text("Successfully deleted world: " + world.getName(), TextColor.color(0, 255, 0)));
            } catch (IOException e) {
                event.getView().getPlayer().sendMessage(Component.text("Could not delete the world folder for " + world.getName(), TextColor.color(255, 0, 0)));
                ProtectTheCore.logger.error(Component.text("An error occurred while deleting the world '" + world.getName() + "'.\n" + e, NamedTextColor.RED));
            }
        } else if (clicked.getType() == Material.CRAFTING_TABLE) {
            GameConfigMenu gameConfigMenu = new GameConfigMenu(plugin);
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(gameConfigMenu.getInventory()));
        }
    }

    private void handleDimensionsConfigMenu(InventoryClickEvent event, OverworldConfigMenu overworldConfigMenu, NetherConfigMenu netherConfigMenu, TheEndConfigMenu theEndConfigMenu, Player player, ItemStack clicked, Inventory inventory) {
        if (clicked.getType() == Material.ARROW) {
            ConfigMenu configMenu = new ConfigMenu(plugin);
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(configMenu.getInventory()));
        } else if (clicked.getType() == Material.BARRIER) {
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
        } else if (clicked.getType() == Material.OAK_SIGN) {
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            signInputManager.open(player, (p, lines) -> {
                String input = String.join(" ", Arrays.stream(lines).filter(line -> !Objects.equals(line, "")).toList());
                try {
                    int seed;
                    if (input.isEmpty()) {
                        seed = ThreadLocalRandom.current().nextInt(-2147483648, 2147483647);
                    } else {
                        seed = Integer.parseInt(input);
                    }
                    if (inventory.getHolder() instanceof OverworldConfigMenu) {
                        overworldConfigMenu.setSeed(seed);
                        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(overworldConfigMenu.getInventory()));
                    }
                    if (inventory.getHolder() instanceof NetherConfigMenu) {
                        netherConfigMenu.setSeed(seed);
                        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(netherConfigMenu.getInventory()));
                    }
                    if (inventory.getHolder() instanceof TheEndConfigMenu) {
                        theEndConfigMenu.setSeed(seed);
                        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(theEndConfigMenu.getInventory()));
                    }
                } catch (ClassCastException | NumberFormatException e) {
                    p.sendMessage(Component.text("Invalid number!", NamedTextColor.RED));
                    logger.error(Component.text(e.toString()));
                }
            });
        } else if (clicked.getType() == Material.ACACIA_SIGN) {
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            signInputManager.open(player, (p, lines) -> {
                String input = String.join(" ", Arrays.stream(lines).filter(line -> !Objects.equals(line, "")).toList());
                try {
                    int borderSize = Integer.parseInt(input);
                    if (borderSize < 1) {
                        p.sendMessage(Component.text("Border Size cannot be less than 1!", NamedTextColor.RED));
                    } else {
                        if (inventory.getHolder() instanceof OverworldConfigMenu) {
                            overworldConfigMenu.setBorderSize(borderSize);
                            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(overworldConfigMenu.getInventory()));
                        }
                        if (inventory.getHolder() instanceof NetherConfigMenu) {
                            netherConfigMenu.setBorderSize(borderSize);
                            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(netherConfigMenu.getInventory()));
                        }
                        if (inventory.getHolder() instanceof TheEndConfigMenu) {
                            theEndConfigMenu.setBorderSize(borderSize);
                            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(theEndConfigMenu.getInventory()));
                        }
                    }
                } catch (ClassCastException e) {
                    p.sendMessage(Component.text("Invalid number!", NamedTextColor.RED));
                    logger.error(Component.text(e.toString()));
                }
            });
        }
        if (inventory.getHolder() instanceof OverworldConfigMenu) {
            handleOverworldConfigMenu(event, overworldConfigMenu, player, clicked);
        }
        if (inventory.getHolder() instanceof NetherConfigMenu) {
            handleNetherConfigMenu(event, netherConfigMenu, player, clicked);
        }
        if (inventory.getHolder() instanceof TheEndConfigMenu) {
            handleTheEndConfigMenu(event, theEndConfigMenu, player, clicked);
        }
    }

    private void handleOverworldConfigMenu(InventoryClickEvent event, OverworldConfigMenu overworldConfigMenu, Player player, ItemStack clicked) {
        if (clicked.getType() == Material.LIME_CONCRETE) {
            config.set("config.overworld.enabled", false);
            plugin.saveConfig();
            Bukkit.getScheduler().runTask(plugin, () -> player.getOpenInventory().getTopInventory().setItem(13, overworldConfigMenu.getEnabledStateItem()));
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        } else if (clicked.getType() == Material.RED_CONCRETE) {
            config.set("config.overworld.enabled", true);
            plugin.saveConfig();
            Bukkit.getScheduler().runTask(plugin, () -> player.getOpenInventory().getTopInventory().setItem(13, overworldConfigMenu.getEnabledStateItem()));
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        }
        plugin.saveConfig();
        try {
            config.save("./plugins/ProtectTheCore/config.yml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleNetherConfigMenu(InventoryClickEvent event, NetherConfigMenu netherConfigMenu, Player player, ItemStack clicked) {
        if (clicked.getType() == Material.LIME_CONCRETE) {
            config.set("config.nether.enabled", false);
            plugin.saveConfig();
            Bukkit.getScheduler().runTask(plugin, () -> player.getOpenInventory().getTopInventory().setItem(13, netherConfigMenu.getEnabledStateItem()));
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        } else if (clicked.getType() == Material.RED_CONCRETE) {
            config.set("config.nether.enabled", true);
            plugin.saveConfig();
            Bukkit.getScheduler().runTask(plugin, () -> player.getOpenInventory().getTopInventory().setItem(13, netherConfigMenu.getEnabledStateItem()));
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
            try {
                config.save("./plugins/ProtectTheCore/config.yml");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleTheEndConfigMenu(InventoryClickEvent event, TheEndConfigMenu theEndConfigMenu, Player player, ItemStack clicked) {
        if (clicked.getType() == Material.LIME_CONCRETE) {
            config.set("config.the_end.enabled", false);
            plugin.saveConfig();
            Bukkit.getScheduler().runTask(plugin, () -> player.getOpenInventory().getTopInventory().setItem(13, theEndConfigMenu.getEnabledStateItem()));
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        } else if (clicked.getType() == Material.RED_CONCRETE) {
            config.set("config.the_end.enabled", true);
            plugin.saveConfig();
            Bukkit.getScheduler().runTask(plugin, () -> player.getOpenInventory().getTopInventory().setItem(13, theEndConfigMenu.getEnabledStateItem()));
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        }
        try {
            config.save("./plugins/ProtectTheCore/config.yml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleTeamsMenu(InventoryClickEvent event, TeamsMenu teamsMenu, Player player, ItemStack clicked) throws Exception {
        if (clicked.getType() == Material.LIME_DYE) {
            TeamCreationMenu teamCreationMenu = new TeamCreationMenu();
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(teamCreationMenu.getInventory()));
        }
        if (
                clicked.getType() == Material.RED_CONCRETE ||
                        clicked.getType() == Material.ORANGE_CONCRETE ||
                        clicked.getType() == Material.YELLOW_CONCRETE ||
                        clicked.getType() == Material.LIME_CONCRETE ||
                        clicked.getType() == Material.GREEN_CONCRETE ||
                        clicked.getType() == Material.CYAN_CONCRETE ||
                        clicked.getType() == Material.LIGHT_BLUE_CONCRETE ||
                        clicked.getType() == Material.BLUE_CONCRETE ||
                        clicked.getType() == Material.PURPLE_CONCRETE ||
                        clicked.getType() == Material.MAGENTA_CONCRETE ||
                        clicked.getType() == Material.PINK_CONCRETE ||
                        clicked.getType() == Material.WHITE_CONCRETE ||
                        clicked.getType() == Material.LIGHT_GRAY_CONCRETE ||
                        clicked.getType() == Material.GRAY_CONCRETE ||
                        clicked.getType() == Material.BLACK_CONCRETE ||
                        clicked.getType() == Material.BROWN_CONCRETE

        ) {
            // A team is clicked
            if (event.isLeftClick()) {
                Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
                ManageTeamMenu manageTeamMenu = new ManageTeamMenu();
                manageTeamMenu.setTeamIdx(event.getSlot());
                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(manageTeamMenu.getInventory()));
            } else if (event.isRightClick()) {
                Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
                Teams.removeTeam(event.getSlot());
                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(teamsMenu.getInventory()));
            }
        }
    }

    private void handleTeamCreationMenu(InventoryClickEvent event, TeamCreationMenu teamCreationMenu, Player player, ItemStack clicked) {
        if (
                clicked.getType() == Material.RED_DYE ||
                        clicked.getType() == Material.ORANGE_DYE ||
                        clicked.getType() == Material.YELLOW_DYE ||
                        clicked.getType() == Material.LIME_DYE ||
                        clicked.getType() == Material.GREEN_DYE ||
                        clicked.getType() == Material.CYAN_DYE ||
                        clicked.getType() == Material.LIGHT_BLUE_DYE ||
                        clicked.getType() == Material.BLUE_DYE ||
                        clicked.getType() == Material.PURPLE_DYE ||
                        clicked.getType() == Material.MAGENTA_DYE ||
                        clicked.getType() == Material.PINK_DYE ||
                        clicked.getType() == Material.WHITE_DYE ||
                        clicked.getType() == Material.LIGHT_GRAY_DYE ||
                        clicked.getType() == Material.GRAY_DYE ||
                        clicked.getType() == Material.BLACK_DYE ||
                        clicked.getType() == Material.BROWN_DYE
        ) {
            if (event.isLeftClick()) {
                teamCreationMenu.increasePreviousMaterial();
            } else if (event.isRightClick()) {
                teamCreationMenu.decreasePreviousMaterial();
            }
            Bukkit.getScheduler().runTask(plugin, () -> player.getOpenInventory().getTopInventory().setItem(3, teamCreationMenu.getTeamColorItem()));
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        }
        if (clicked.getType() == Material.IRON_SWORD) {
            teamCreationMenu.togglePvPStatus();
            Bukkit.getScheduler().runTask(plugin, () -> player.getOpenInventory().getTopInventory().setItem(5, teamCreationMenu.getPvPStatusItem()));
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        }
        if (clicked.getType() == Material.RED_CONCRETE) {
            TeamsMenu teamsMenu = new TeamsMenu();
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(teamsMenu.getInventory()));
        }
        if (clicked.getType() == Material.OAK_SIGN) {
            signInputManager.open(player, (p, lines) -> {
                String input = String.join(" ", Arrays.stream(lines).filter(line -> !Objects.equals(line, "")).toList());
                teamCreationMenu.setTeamName(input);
                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(teamCreationMenu.getInventory()));
            });
        }
        if (clicked.getType() == Material.LIME_CONCRETE) {
            try {
                Teams.createNewTeam(teamCreationMenu.getTeamName().isBlank() ? UUID.randomUUID().toString() : teamCreationMenu.getTeamName(), teamCreationMenu.getTeamColorInt(), teamCreationMenu.getPvPStatus());
                Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
                player.sendMessage(Component.text("Team successfully created!", NamedTextColor.GREEN));
                TeamsMenu teamsMenu = new TeamsMenu();
                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(teamsMenu.getInventory()));
            } catch (Exception e) {
                player.sendMessage(Component.text("An error occurred while creating the team. Please check the latest log for more info.", NamedTextColor.RED));
                ProtectTheCore.logger.error(Component.text("An error occurred while making the team. The stacktrace is below: \n" + e, NamedTextColor.RED));
            }
        }
    }

    private void handleManageTeamsMenu(InventoryClickEvent event, ManageTeamMenu manageTeamMenu, Player player, ItemStack clicked) throws Exception {
        if (clicked.getType() == Material.LIME_DYE) {
            signInputManager.open(player, (p, lines) -> {
                String input = String.join(" ", Arrays.stream(lines).filter(line -> !Objects.equals(line, "")).toList());
                if (getTeamIndexFromPlayer(input) >= 0) {
                    p.sendMessage(Component.text("This player is already on another team!", NamedTextColor.RED));
                    return;
                }
                Teams.addTeamMember(manageTeamMenu.getTeamIdx(), input, getOfflinePlayer(input).getUniqueId());
                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(manageTeamMenu.getInventory()));
            });
        }
        if (clicked.getType() == Material.PLAYER_HEAD) {
            UUID playerUUID = UUID.fromString(Teams.getTeamsConfig().get(manageTeamMenu.getTeamIdx()).getJSONArray("members").getJSONObject(event.getSlot()).getString("uuid"));
            if (event.isLeftClick()) {
                if (!Teams.getTeamLeader(manageTeamMenu.getTeamIdx()).isEmpty()) {
                    if (!Teams.getTeamLeader(manageTeamMenu.getTeamIdx()).equals(Bukkit.getOfflinePlayer(playerUUID).getName())) {
                        player.sendMessage(Component.text("There is already a team leader defined!"));
                        return;
                    }
                    else {
                        Teams.removeTeamLeader(manageTeamMenu.getTeamIdx());
                    }
                }
                else {
                    Teams.setTeamLeader(manageTeamMenu.getTeamIdx(), Bukkit.getOfflinePlayer(playerUUID).getName());
                }
                ItemStack playerHead = ItemStack.of(Material.PLAYER_HEAD);
                playerHead.editMeta(SkullMeta.class, meta -> {
                    PlayerProfile profile = Bukkit.createProfile(playerUUID, Bukkit.getOfflinePlayer(playerUUID).getName());
                    profile.complete(true);
                    meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>" + Bukkit.getOfflinePlayer(playerUUID).getName() + "</white>" + (Objects.equals(Teams.getTeamLeader(manageTeamMenu.getTeamIdx()), Bukkit.getOfflinePlayer(playerUUID).getName()) ? " <yellow><Leader></yellow>" : "")));
                    meta.setPlayerProfile(profile);
                    ArrayList<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Left-click to toggle team leader!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Right-click to remove!", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    meta.lore(lore);
                });
                Bukkit.getScheduler().runTask(plugin, () -> player.getOpenInventory().getTopInventory().setItem(event.getSlot(), playerHead));
                Bukkit.getScheduler().runTask(plugin, player::updateInventory);
            }
            else if (event.isRightClick()) {
                Teams.removeTeamMember(manageTeamMenu.getTeamIdx(), playerUUID);
                Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(manageTeamMenu.getInventory()));
            }
        }
        if (clicked.getType() == Material.SPECTRAL_ARROW) {
            TeamsMenu teamsMenu = new TeamsMenu();
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(teamsMenu.getInventory()));
        }
        if (clicked.getType() == Material.IRON_SWORD) {
            manageTeamMenu.togglePvPStatus();
            Teams.setTeamPvPStatus(manageTeamMenu.getTeamIdx(), manageTeamMenu.getPvPStatus());
            Bukkit.getScheduler().runTask(plugin, () -> player.getOpenInventory().getTopInventory().setItem(31, manageTeamMenu.getPvPStatusItem()));
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        }
        if (clicked.getType() == Material.OAK_SIGN) {
            signInputManager.open(player, (p, lines) -> {
                String input = String.join(" ", Arrays.stream(lines).filter(line -> !Objects.equals(line, "")).toList());
                Teams.setTeamName(manageTeamMenu.getTeamIdx(), input);
                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(manageTeamMenu.getInventory()));
            });
        }
        if (
                clicked.getType() == Material.RED_CONCRETE_POWDER ||
                        clicked.getType() == Material.ORANGE_CONCRETE_POWDER ||
                        clicked.getType() == Material.YELLOW_CONCRETE_POWDER ||
                        clicked.getType() == Material.LIME_CONCRETE_POWDER ||
                        clicked.getType() == Material.GREEN_CONCRETE_POWDER ||
                        clicked.getType() == Material.CYAN_CONCRETE_POWDER ||
                        clicked.getType() == Material.LIGHT_BLUE_CONCRETE_POWDER ||
                        clicked.getType() == Material.BLUE_CONCRETE_POWDER ||
                        clicked.getType() == Material.PURPLE_CONCRETE_POWDER ||
                        clicked.getType() == Material.MAGENTA_CONCRETE_POWDER ||
                        clicked.getType() == Material.PINK_CONCRETE_POWDER ||
                        clicked.getType() == Material.WHITE_CONCRETE_POWDER ||
                        clicked.getType() == Material.LIGHT_GRAY_CONCRETE_POWDER ||
                        clicked.getType() == Material.GRAY_CONCRETE_POWDER ||
                        clicked.getType() == Material.BLACK_CONCRETE_POWDER ||
                        clicked.getType() == Material.BROWN_CONCRETE_POWDER
        ) {
            if (event.isLeftClick()) {
                manageTeamMenu.increasePreviousMaterial();
            } else if (event.isRightClick()) {
                manageTeamMenu.decreasePreviousMaterial();
            }
            new TeamCreationMenu();
            Teams.setTeamColor(manageTeamMenu.getTeamIdx(), TeamCreationMenu.getTeamColorsInt().get(manageTeamMenu.getPreviousMaterial()));
            Bukkit.getScheduler().runTask(plugin, () -> player.getOpenInventory().getTopInventory().setItem(30, manageTeamMenu.getTeamColorItemConcretePowder(manageTeamMenu.getPreviousMaterial())));
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        }
    }

    private void handleGameConfigMenu(InventoryClickEvent event, GameConfigMenu gameConfigMenu, Player player, ItemStack clicked) {
        signInputManager.open(player, (p, lines) -> {
            String input = String.join(" ", Arrays.stream(lines).filter(line -> !Objects.equals(line, "")).toList());
            gameConfigMenu.setDurationDisp(input);
            gameConfigMenu.setDuration(GameConfigMenu.parseDuration(input.replaceAll(" ", "")));
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(gameConfigMenu.getInventory()));
        });
    }

    private void handleSupplyDropMenu(InventoryClickEvent event, SupplyDropMenu supplyDropMenu, Player player, ItemStack clicked) {
        if (clicked.getType() == Material.LIME_DYE) {
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new SupplyDropCreationMenu().getInventory()));
        }
        if (
                clicked.getType() == Material.CHEST ||
                clicked.getType() == Material.BARREL ||
                clicked.getType() == Material.ENDER_CHEST ||
                clicked.getType() == Material.WAXED_COPPER_CHEST ||
                clicked.getType() == Material.WAXED_EXPOSED_COPPER_CHEST ||
                clicked.getType() == Material.WAXED_OXIDIZED_COPPER_CHEST ||
                clicked.getType() == Material.WAXED_WEATHERED_COPPER_CHEST ||
                clicked.getType() == Material.RED_SHULKER_BOX ||
                clicked.getType() == Material.ORANGE_SHULKER_BOX ||
                clicked.getType() == Material.YELLOW_SHULKER_BOX ||
                clicked.getType() == Material.LIME_SHULKER_BOX ||
                clicked.getType() == Material.GREEN_SHULKER_BOX ||
                clicked.getType() == Material.CYAN_SHULKER_BOX ||
                clicked.getType() == Material.LIGHT_BLUE_SHULKER_BOX ||
                clicked.getType() == Material.BLUE_SHULKER_BOX ||
                clicked.getType() == Material.PURPLE_SHULKER_BOX ||
                clicked.getType() == Material.MAGENTA_SHULKER_BOX ||
                clicked.getType() == Material.PINK_SHULKER_BOX ||
                clicked.getType() == Material.WHITE_SHULKER_BOX ||
                clicked.getType() == Material.LIGHT_GRAY_SHULKER_BOX ||
                clicked.getType() == Material.GRAY_SHULKER_BOX ||
                clicked.getType() == Material.BLACK_SHULKER_BOX ||
                clicked.getType() == Material.BROWN_SHULKER_BOX
        ) {
            if (event.isLeftClick()) {
                Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
                ManageSupplyDropMenu manageSupplyDropMenu = new ManageSupplyDropMenu();
                manageSupplyDropMenu.setSupplyDropIdx(event.getSlot());
                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(manageSupplyDropMenu.getInventory()));
            } else if (event.isRightClick()) {
                Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
                SupplyDrop.removeSupplyDrop(event.getSlot());
                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(supplyDropMenu.getInventory()));
            }
        }
    }

    private void handleSupplyDropCreationMenu(InventoryClickEvent event, SupplyDropCreationMenu supplyDropCreationMenu, Player player, ItemStack clicked) {
        if (clicked.getType() == Material.LIME_CONCRETE) {
            SupplyDrop.createSupplyDrop(Bukkit.createInventory(null, 27), supplyDropCreationMenu.getLocation(), supplyDropCreationMenu.getContainer(), supplyDropCreationMenu.getTime());
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new SupplyDropMenu().getInventory()));
        }
        if (clicked.getType() == Material.RED_CONCRETE) {
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new SupplyDropCreationMenu().getInventory()));
        }
        if (clicked.getType() == Material.OAK_SIGN) {
            if (event.isLeftClick()) {
                signInputManager.open(player, (p, lines) -> {
                    String input = String.join(" ", Arrays.stream(lines).filter(line -> !Objects.equals(line, "")).toList());
                    if (input.matches("\\w+ -?\\d* -?\\d* -?\\d* *")) {
                        supplyDropCreationMenu.setLocation(new Location(Bukkit.getWorld(input.split(" ")[0]), Integer.parseInt(input.split(" ")[1]), Integer.parseInt(input.split(" ")[2]), Integer.parseInt(input.split(" ")[3])));
                    } else {
                        player.sendMessage(Component.text("Unable to parse input!", NamedTextColor.RED));
                    }
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(supplyDropCreationMenu.getInventory()));
                });
            }
            if (event.isRightClick()) {
                supplyDropCreationMenu.setLocation(new Location(Bukkit.getWorld("ptcoverworld"), 0, 64, 0));
                Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(supplyDropCreationMenu.getInventory()));
            }
        }
        if (clicked.getType() == Material.PALE_OAK_SIGN) {
            if (event.isLeftClick()) {
                signInputManager.open(player, (p, lines) -> {
                    String input = String.join(" ", Arrays.stream(lines).filter(line -> !Objects.equals(line, "")).toList());
                    if (input.matches("\\d\\d/\\d\\d/\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d *")) {
                        supplyDropCreationMenu.setTime(input);
                    } else {
                        player.sendMessage(Component.text("Unable to parse input!", NamedTextColor.RED));
                    }
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(supplyDropCreationMenu.getInventory()));
                });
            }
            if (event.isRightClick()) {
                supplyDropCreationMenu.setTime("01/01/1970 00:00:00");
                Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(supplyDropCreationMenu.getInventory()));
            }
        }
        if (
                clicked.getType() == Material.CHEST ||
                        clicked.getType() == Material.BARREL ||
                        clicked.getType() == Material.ENDER_CHEST ||
                        clicked.getType() == Material.WAXED_COPPER_CHEST ||
                        clicked.getType() == Material.WAXED_EXPOSED_COPPER_CHEST ||
                        clicked.getType() == Material.WAXED_OXIDIZED_COPPER_CHEST ||
                        clicked.getType() == Material.WAXED_WEATHERED_COPPER_CHEST ||
                        clicked.getType() == Material.RED_SHULKER_BOX ||
                        clicked.getType() == Material.ORANGE_SHULKER_BOX ||
                        clicked.getType() == Material.YELLOW_SHULKER_BOX ||
                        clicked.getType() == Material.LIME_SHULKER_BOX ||
                        clicked.getType() == Material.GREEN_SHULKER_BOX ||
                        clicked.getType() == Material.CYAN_SHULKER_BOX ||
                        clicked.getType() == Material.LIGHT_BLUE_SHULKER_BOX ||
                        clicked.getType() == Material.BLUE_SHULKER_BOX ||
                        clicked.getType() == Material.PURPLE_SHULKER_BOX ||
                        clicked.getType() == Material.MAGENTA_SHULKER_BOX ||
                        clicked.getType() == Material.PINK_SHULKER_BOX ||
                        clicked.getType() == Material.WHITE_SHULKER_BOX ||
                        clicked.getType() == Material.LIGHT_GRAY_SHULKER_BOX ||
                        clicked.getType() == Material.GRAY_SHULKER_BOX ||
                        clicked.getType() == Material.BLACK_SHULKER_BOX ||
                        clicked.getType() == Material.BROWN_SHULKER_BOX
        ) {
            if (event.isLeftClick()) {
                supplyDropCreationMenu.increasePreviousMaterial();
            } else if (event.isRightClick()) {
                supplyDropCreationMenu.decreasePreviousMaterial();
            }
            Bukkit.getScheduler().runTask(plugin, () -> player.getOpenInventory().getTopInventory().setItem(4, supplyDropCreationMenu.getContainerItem(supplyDropCreationMenu.getPreviousMaterial())));
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        }
    }

    private void handleManageSupplyDropMenu(InventoryClickEvent event, ManageSupplyDropMenu manageSupplyDropMenu, Player player, ItemStack clicked) {
        // Top three rows are already ignored
        if (clicked.getType() == Material.SPECTRAL_ARROW) {
            SupplyDropMenu supplyDropMenu = new SupplyDropMenu();
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(supplyDropMenu.getInventory()));
        }
        if (clicked.getType() == Material.LIME_CONCRETE) {
            SupplyDrop.setContainer(manageSupplyDropMenu.getContainerItem(manageSupplyDropMenu.getPreviousMaterial()).getType(), manageSupplyDropMenu.getSupplyDropIdx());
            SupplyDrop.setInventory(manageSupplyDropMenu.getContents(), manageSupplyDropMenu.getSupplyDropIdx());
            SupplyDrop.setLocation(manageSupplyDropMenu.getLocation(), manageSupplyDropMenu.getSupplyDropIdx());
            SupplyDrop.saveSupplyDropConfig();
            SupplyDropMenu supplyDropMenu = new SupplyDropMenu();
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(supplyDropMenu.getInventory()));
            player.sendMessage(Component.text("Supply drop saved!", NamedTextColor.GREEN));
        }
        if (clicked.getType() == Material.OAK_SIGN) {
            if (event.isLeftClick()) {
                signInputManager.open(player, (p, lines) -> {
                    String input = String.join(" ", Arrays.stream(lines).filter(line -> !Objects.equals(line, "")).toList());
                    if (input.matches("\\w+ -?\\d* -?\\d* -?\\d* *")) {
                        manageSupplyDropMenu.setLocation(new Location(Bukkit.getWorld(input.split(" ")[0]), Integer.parseInt(input.split(" ")[1]), Integer.parseInt(input.split(" ")[2]), Integer.parseInt(input.split(" ")[3])));
                    } else {
                        player.sendMessage(Component.text("Unable to parse input!", NamedTextColor.RED));
                    }
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(manageSupplyDropMenu.getInventory()));
                });
            }
            if (event.isRightClick()) {
                manageSupplyDropMenu.setLocation(new Location(Bukkit.getWorld("ptcoverworld"), 0, 64, 0));
                Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(manageSupplyDropMenu.getInventory()));
            }
        }
        if (clicked.getType() == Material.PALE_OAK_SIGN) {
            if (event.isLeftClick()) {
                signInputManager.open(player, (p, lines) -> {
                    String input = String.join(" ", Arrays.stream(lines).filter(line -> !Objects.equals(line, "")).toList());
                    if (input.matches("\\d\\d/\\d\\d/\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d *")) {
                        manageSupplyDropMenu.setTime(input);
                    } else {
                        player.sendMessage(Component.text("Unable to parse input!", NamedTextColor.RED));
                    }
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(manageSupplyDropMenu.getInventory()));
                });
            }
            if (event.isRightClick()) {
                manageSupplyDropMenu.setTime("01/01/1970 00:00:00");
                Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(manageSupplyDropMenu.getInventory()));
            }
        }
        if (
                clicked.getType() == Material.CHEST ||
                        clicked.getType() == Material.BARREL ||
                        clicked.getType() == Material.ENDER_CHEST ||
                        clicked.getType() == Material.WAXED_COPPER_CHEST ||
                        clicked.getType() == Material.WAXED_EXPOSED_COPPER_CHEST ||
                        clicked.getType() == Material.WAXED_OXIDIZED_COPPER_CHEST ||
                        clicked.getType() == Material.WAXED_WEATHERED_COPPER_CHEST ||
                        clicked.getType() == Material.RED_SHULKER_BOX ||
                        clicked.getType() == Material.ORANGE_SHULKER_BOX ||
                        clicked.getType() == Material.YELLOW_SHULKER_BOX ||
                        clicked.getType() == Material.LIME_SHULKER_BOX ||
                        clicked.getType() == Material.GREEN_SHULKER_BOX ||
                        clicked.getType() == Material.CYAN_SHULKER_BOX ||
                        clicked.getType() == Material.LIGHT_BLUE_SHULKER_BOX ||
                        clicked.getType() == Material.BLUE_SHULKER_BOX ||
                        clicked.getType() == Material.PURPLE_SHULKER_BOX ||
                        clicked.getType() == Material.MAGENTA_SHULKER_BOX ||
                        clicked.getType() == Material.PINK_SHULKER_BOX ||
                        clicked.getType() == Material.WHITE_SHULKER_BOX ||
                        clicked.getType() == Material.LIGHT_GRAY_SHULKER_BOX ||
                        clicked.getType() == Material.GRAY_SHULKER_BOX ||
                        clicked.getType() == Material.BLACK_SHULKER_BOX ||
                        clicked.getType() == Material.BROWN_SHULKER_BOX
        ) {
            if (event.isLeftClick()) {
                manageSupplyDropMenu.increasePreviousMaterial();
            } else if (event.isRightClick()) {
                manageSupplyDropMenu.decreasePreviousMaterial();
            }
            Bukkit.getScheduler().runTask(plugin, () -> player.getOpenInventory().getTopInventory().setItem(31, manageSupplyDropMenu.getContainerItem(manageSupplyDropMenu.getPreviousMaterial())));
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        }
    }
}