package com.example.protectTheCore.menu.config;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.menu.CustomMenuHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ConfigMenu implements CustomMenuHolder {

    private final Inventory inventory;

    public ConfigMenu(ProtectTheCore plugin) {
        this.inventory = plugin.getServer().createInventory(this, 36, Component.text("ᴄᴏɴꜰɪɢ"));
        // Items
        ItemStack overworldConfig = ItemStack.of(Material.GRASS_BLOCK);
        overworldConfig.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><green>ᴏᴠᴇʀᴡᴏʀʟᴅ</green>")));
        ItemStack netherConfig = ItemStack.of(Material.NETHERRACK);
        netherConfig.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><red>ɴᴇᴛʜᴇʀ</red>")));
        ItemStack endConfig = ItemStack.of(Material.END_STONE);
        endConfig.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><light_purple>ᴛʜᴇ ᴇɴᴅ</light_purple>")));
        ItemStack start = ItemStack.of(Material.LIME_CONCRETE);
        start.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><green>ɢᴇɴᴇʀᴀᴛᴇ ᴡᴏʀʟᴅꜱ</green>")));
        ItemStack delete = ItemStack.of(Material.RED_CONCRETE);
        delete.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><red>ᴅᴇʟᴇᴛᴇ ᴡᴏʀʟᴅꜱ</red>")));
        ItemStack gameConfig = ItemStack.of(Material.CRAFTING_TABLE);
        gameConfig.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><yellow>ɢᴀᴍᴇ ᴄᴏɴꜰɪɢ</yellow>")));
        ItemStack blankGrayStainedGlassPane = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
        blankGrayStainedGlassPane.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false>")));
        // Putting the items in the menu
        this.inventory.setItem(13, gameConfig);
        this.inventory.setItem(20, overworldConfig);
        this.inventory.setItem(22, netherConfig);
        this.inventory.setItem(24, endConfig);
        this.inventory.setItem(27, start);
        this.inventory.setItem(28, blankGrayStainedGlassPane);
        this.inventory.setItem(29, blankGrayStainedGlassPane);
        this.inventory.setItem(30, blankGrayStainedGlassPane);
        this.inventory.setItem(31, blankGrayStainedGlassPane);
        this.inventory.setItem(32, blankGrayStainedGlassPane);
        this.inventory.setItem(33, blankGrayStainedGlassPane);
        this.inventory.setItem(34, blankGrayStainedGlassPane);
        this.inventory.setItem(35, delete);
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
