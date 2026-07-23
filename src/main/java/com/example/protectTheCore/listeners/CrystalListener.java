package com.example.protectTheCore.listeners;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.game.Cores;
import com.example.protectTheCore.core.Teams;
import com.example.protectTheCore.helper.HelperFunctions;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.protectTheCore.game.Cores.coreHealthMap;

public class CrystalListener implements Listener {

    private final ProtectTheCore plugin;
    private final ComponentLogger logger;
    private final Teams teams;
    private final AfterWallsListener afterWallsListener;
    private final Cores cores;

    public CrystalListener(@NotNull ProtectTheCore plugin, @NotNull ComponentLogger logger, @NotNull Teams teams, @NotNull AfterWallsListener afterWallsListener, @NotNull Cores cores) {
        this.plugin = plugin;
        this.logger = logger;
        this.teams = teams;
        this.afterWallsListener = afterWallsListener;
        this.cores = cores;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            try {
                if (event.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "team_core_id"))) {
                    assert event.getClickedBlock() != null;
                    try {
                        cores.placeCore(event.getPlayer(), event.getClickedBlock(), event.getBlockFace(), event.getItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER));
                    } catch (Exception e) {
                        logger.error(Component.text("An error occurred while spawning a core. \n" + e));
                    }
                }
            } catch (Exception ignored) {

            }
        }
    }


    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity clickedEntity = event.getRightClicked();
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (player.isSneaking() && clickedEntity.getType() == EntityType.END_CRYSTAL && clickedEntity.getPersistentDataContainer().has(new NamespacedKey(plugin, "team_core_id")) && clickedEntity.getPersistentDataContainer().has(new NamespacedKey(plugin, "placed_by_hand"))) {
            event.setCancelled(true);
            clickedEntity.remove();
            for (Entity entity : clickedEntity.getNearbyEntities(0, 4, 0)) {
                if (entity.getPersistentDataContainer().has(new NamespacedKey(plugin, "custom_core_nametag_bottom"), PersistentDataType.INTEGER) || entity.getPersistentDataContainer().has(new NamespacedKey(plugin, "custom_core_nametag_top"), PersistentDataType.INTEGER)) {
                    entity.remove();
                }
            }
            cores.giveCore(clickedEntity.getPersistentDataContainer().get(new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER), player);
        }
    }

    @EventHandler
    public void onCrystalDamage(EntityDamageByEntityEvent event) throws IOException {
        if (event.getEntity() instanceof EnderCrystal crystal) {
            NamespacedKey healthKey = new NamespacedKey(plugin, "crystal_health");

            if (crystal.getPersistentDataContainer().has(healthKey, PersistentDataType.INTEGER)) {
                double currentHealth = crystal.getPersistentDataContainer().get(healthKey, PersistentDataType.INTEGER);

                double damage = event.getFinalDamage();
                double newHealth = currentHealth - damage;

                if (HelperFunctions.getDebugMode(event.getDamager())) {
                    event.getDamager().sendMessage(MiniMessage.miniMessage().deserialize("Old core health: " + currentHealth));
                    event.getDamager().sendMessage(MiniMessage.miniMessage().deserialize("New core health: " + newHealth));
                }

                coreHealthMap.put(crystal.getPersistentDataContainer().get(new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER), newHealth);

                if (event.getDamager() instanceof Player player) {
                    if (teams.getTeamIndexFromPlayer(player.getName()) == crystal.getPersistentDataContainer().get(new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER)) {
                        event.getDamager().sendMessage(Component.text("You cannot damage your own core!", NamedTextColor.RED));
                        event.setCancelled(true);
                        return;
                    }
                }

                if (newHealth > 0) {
                    event.getDamager().playSound(net.kyori.adventure.sound.Sound.sound(Key.key("entity.ender_dragon.hurt"), Sound.Source.HOSTILE, 1, 1));
                    crystal.getPersistentDataContainer().set(healthKey, PersistentDataType.INTEGER, (int) newHealth);
                    for (Entity entity : crystal.getNearbyEntities(0, 4, 0)) {
                        if (entity.getPersistentDataContainer().has(new NamespacedKey(plugin, "custom_core_nametag_bottom"), PersistentDataType.INTEGER)) {
                            ((TextDisplay) entity).text(cores.getHealthbarComponent((double) Math.round(crystal.getPersistentDataContainer().get(healthKey, PersistentDataType.INTEGER) / Cores.getMaxCoreHealth() * 1000) / 10));
                        }
                    }
                    event.setCancelled(true);
                } else {
                    for (Entity entity : crystal.getNearbyEntities(0, 4, 0)) {
                        if (entity.getPersistentDataContainer().has(new NamespacedKey(plugin, "custom_core_nametag_top"), PersistentDataType.INTEGER) || entity.getPersistentDataContainer().has(new NamespacedKey(plugin, "custom_core_nametag_bottom"), PersistentDataType.INTEGER)) {
                            entity.remove();
                        }
                    }
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(net.kyori.adventure.sound.Sound.sound(Key.key("entity.ender_dragon.death"), Sound.Source.HOSTILE, 1, 1));
                    }
                    if (event.getDamager() instanceof Player player) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>For breaking a core, you have been awarded with <gold>1 random piece of netherite gear<green>."));
                        List<Material> items = List.of(
                            Material.NETHERITE_HELMET,
                            Material.NETHERITE_CHESTPLATE,
                            Material.NETHERITE_LEGGINGS,
                            Material.NETHERITE_BOOTS,
                            Material.NETHERITE_SWORD,
                            Material.NETHERITE_AXE
                        );
                        player.give(ItemStack.of(items.get(ThreadLocalRandom.current().nextInt(0, items.size() - 1))));
                    }
                    plugin.getServer().broadcast(Component.text(teams.getTeamName(crystal.getPersistentDataContainer().get(new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER)), TextColor.color(teams.getTeamColor(crystal.getPersistentDataContainer().get(new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER)))).append(Component.text("'s core is destroyed!")));
                    afterWallsListener.teamCoreDestroyed(crystal.getPersistentDataContainer().get(new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER));
                    teams.getTeamMembers(crystal.getPersistentDataContainer().get(new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER)).forEach(member -> {
                        if (Bukkit.getPlayer(((JSONObject) member).getString("username")) != null) {
                            Bukkit.getPlayer(((JSONObject) member).getString("username")).sendMessage(MiniMessage.miniMessage().deserialize("<italic:false><gray>You will no longer respawn."));
                        }
                    });
                }
            }
        }
    }
}

