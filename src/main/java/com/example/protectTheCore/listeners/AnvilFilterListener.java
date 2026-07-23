package com.example.protectTheCore.listeners;

import com.example.protectTheCore.ProtectTheCore;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class AnvilFilterListener implements Listener {

    private final ProtectTheCore plugin;

    public AnvilFilterListener(@NotNull ProtectTheCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        ItemStack firstItem = event.getInventory().getFirstItem();
        ItemStack secondItem = event.getInventory().getSecondItem();
        if (firstItem == null || secondItem == null) return;
        if (isCrown(firstItem)) {
            if (hasVanishingCurse(secondItem)) {
                event.setResult(null);
            }
        }
    }

    private boolean isCrown(ItemStack item) {
        if (item.getItemMeta() == null) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
                new NamespacedKey(plugin, "ptc_crown"),
                PersistentDataType.BOOLEAN
        );
    }

    private boolean hasVanishingCurse(ItemStack item) {
        if (item.getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) {
            return true;
        }
        if (item.getItemMeta() instanceof EnchantmentStorageMeta bookMeta) {
            return bookMeta.hasStoredEnchant(Enchantment.VANISHING_CURSE);
        }
        return false;
    }
}