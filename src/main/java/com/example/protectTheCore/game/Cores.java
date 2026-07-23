package com.example.protectTheCore.game;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.core.Teams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Cores {

    public static final Map<Integer, Double> coreHealthMap = new HashMap<>();
    private ProtectTheCore plugin;
    private Teams teams;

    public Cores(@NotNull ProtectTheCore plugin, @NotNull Teams teams) {
        this.plugin = plugin;
        this.teams = teams;
    }

    public static double getMaxCoreHealth() {
        return 1000.0;
    }

    public void giveCore(int team, Player player) {
        ItemStack item = ItemStack.of(Material.END_CRYSTAL);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER, team);
        meta.displayName(Component.text(teams.getTeamName(team) + "'s Core", TextColor.color(teams.getTeamColor(team))).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(Arrays.asList(
                Component.text("Used for the 'Protect The Core' event.").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                Component.text("").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                Component.text("Right-click to place down!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                Component.text("Shift right-click to pick up!", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        ));
        item.setItemMeta(meta);
        player.give(item);
    }

    public void spawnCrystal(Location location, int team, boolean placedByHand) {
        EnderCrystal crystal = (EnderCrystal) location.getWorld().spawnEntity(location, EntityType.END_CRYSTAL);

        if (location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ()).getState().getType() == Material.AIR) {
            location.getWorld().setBlockData(location.clone().add(-1, -1, -1), Material.OBSIDIAN.createBlockData());
            location.getWorld().setBlockData(location.clone().add(-1, -1, 0), Material.OBSIDIAN.createBlockData());
            location.getWorld().setBlockData(location.clone().add(-1, -1, 1), Material.OBSIDIAN.createBlockData());
            location.getWorld().setBlockData(location.clone().add(0, -1, -1), Material.OBSIDIAN.createBlockData());
            location.getWorld().setBlockData(location.clone().add(0, -1, 0), Material.OBSIDIAN.createBlockData());
            location.getWorld().setBlockData(location.clone().add(0, -1, 1), Material.OBSIDIAN.createBlockData());
            location.getWorld().setBlockData(location.clone().add(1, -1, -1), Material.OBSIDIAN.createBlockData());
            location.getWorld().setBlockData(location.clone().add(1, -1, 0), Material.OBSIDIAN.createBlockData());
            location.getWorld().setBlockData(location.clone().add(1, -1, 1), Material.OBSIDIAN.createBlockData());
        }

        NamespacedKey healthKey = new NamespacedKey(plugin, "crystal_health");
        crystal.getPersistentDataContainer().set(healthKey, PersistentDataType.INTEGER, 1000);
        crystal.getPersistentDataContainer().set(new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER, team);
        crystal.getPersistentDataContainer().set(new NamespacedKey(plugin, "placed_by_hand"), PersistentDataType.BOOLEAN, placedByHand);
        coreHealthMap.put(team, getMaxCoreHealth());

        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setMarker(true);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_core_nametag_top"), PersistentDataType.INTEGER, crystal.getPersistentDataContainer().get(new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER));
        armorStand.addPassenger(crystal);

        TextDisplay topRow = (TextDisplay) crystal.getWorld().spawnEntity(crystal.getLocation().add(0, 2.5, 0), EntityType.TEXT_DISPLAY);
        topRow.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_core_nametag_top"), PersistentDataType.INTEGER, crystal.getPersistentDataContainer().get(new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER));
        topRow.setInvulnerable(true);
        topRow.setAlignment(TextDisplay.TextAlignment.CENTER);
        topRow.setSeeThrough(true);
        topRow.setBillboard(Display.Billboard.CENTER);
        topRow.text(Component.text(teams.getTeamName(crystal.getPersistentDataContainer().get(new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER)) + "'s", TextColor.color(teams.getTeamColor(crystal.getPersistentDataContainer().get(new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER)))).append(Component.text(" Core", NamedTextColor.WHITE)));

        double healthPercentage = (double) Math.round(crystal.getPersistentDataContainer().get(healthKey, PersistentDataType.INTEGER) / Cores.getMaxCoreHealth() * 1000) / 10;
        Component healthbar = getHealthbarComponent(healthPercentage);
        TextDisplay bottomRow = (TextDisplay) crystal.getWorld().spawnEntity(crystal.getLocation().add(0, 2.3, 0), EntityType.TEXT_DISPLAY);
        bottomRow.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_core_nametag_bottom"), PersistentDataType.INTEGER, crystal.getPersistentDataContainer().get(new NamespacedKey(plugin, "team_core_id"), PersistentDataType.INTEGER));
        bottomRow.setInvulnerable(true);
        bottomRow.setAlignment(TextDisplay.TextAlignment.CENTER);
        bottomRow.setSeeThrough(true);
        bottomRow.setBillboard(Display.Billboard.CENTER);
        bottomRow.text(healthbar);
    }

    public @NotNull Component getHealthbarComponent(double healthPercentage) {
        String healthbarChars = "||||||||||||||||||";
        List<Character> healthbarList = new ArrayList<>();

        for (char c : healthbarChars.toCharArray()) {
            healthbarList.add(c);
        }

        Component healthbar = Component.text("[", NamedTextColor.WHITE);
        for (int i = 0; i < healthbarList.size(); i++) {
            double threshold = (double) i / healthbarList.size() * 100;
            if (healthPercentage < threshold) {
                healthbar = healthbar.append(Component.text(healthbarList.get(i), NamedTextColor.RED));
            }
            else {
                healthbar = healthbar.append(Component.text(healthbarList.get(i), NamedTextColor.GREEN));
            }
        }
        healthbar = healthbar.append(Component.text("]", NamedTextColor.WHITE));
        return healthbar;
    }

    public void placeCore(Player player, Block clickedBlock, BlockFace blockFace, int team) {
        Location location = clickedBlock.getLocation();
        location = location.add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
        location.add(0.5, 0, 0.5);
        if (blockFace != BlockFace.UP && blockFace != BlockFace.DOWN) {
            location.setY(location.getBlockY());
        }
        ItemStack mainItem = player.getEquipment().getItemInMainHand();
        mainItem.setAmount(player.getEquipment().getItemInMainHand().getAmount() - 1);
        player.getEquipment().setItemInMainHand(mainItem);
        spawnCrystal(location, team, true);
    }
}

