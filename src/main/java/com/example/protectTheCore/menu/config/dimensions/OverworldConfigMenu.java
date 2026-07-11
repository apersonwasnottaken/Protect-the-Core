package com.example.protectTheCore.menu.config.dimensions;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.menu.CustomMenuHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.example.protectTheCore.ProtectTheCore.config;

public class OverworldConfigMenu implements CustomMenuHolder {

    private final Inventory inventory;
    private int seedInt, borderSizeInt = 10000;

    public void setSeed(int val) {
        seedInt = val;
    }

    public void setBorderSize(int val) {
        borderSizeInt = val;
    }

    public OverworldConfigMenu(ProtectTheCore plugin) {
        this.inventory = plugin.getServer().createInventory(this, 36, Component.text("ᴏᴠᴇʀᴡᴏʀʟᴅ ᴄᴏɴꜰɪɢ"));
        // Items
        ItemStack close = ItemStack.of(Material.BARRIER);
        close.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><red>ᴄʟᴏꜱᴇ</red>")));
        ItemStack back = ItemStack.of(Material.ARROW);
        back.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false>ʙᴀᴄᴋ")));
        // Putting the items in the menu
        this.inventory.setItem(31, back);
        this.inventory.setItem(35, close);
    }

    public ItemStack getEnabledStateItem() {
        ItemStack enabled = ItemStack.of(Material.LIME_CONCRETE);
        enabled.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><green>ᴇɴᴀʙʟᴇᴅ</green>")));
        ItemStack disabled = ItemStack.of(Material.RED_CONCRETE);
        disabled.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><red>ᴅɪꜱᴀʙʟᴇᴅ</red>")));
        if (config.getBoolean("config.overworld.enabled")) {
            return enabled;
        }
        else {
            return disabled;
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        ItemStack borderSize = ItemStack.of(Material.ACACIA_SIGN);
        borderSize.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><aqua>ʙᴏʀᴅᴇʀ ꜱɪᴢᴇ</aqua><white>:</white> <yellow>" + borderSizeInt + "</yellow>")));
        ItemStack seed = ItemStack.of(Material.OAK_SIGN);
        seed.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><green>ꜱᴇᴇᴅ</green><white>:</white> <yellow>" + seedInt + "</yellow>")));
        this.inventory.setItem(11, seed);
        this.inventory.setItem(13, getEnabledStateItem());
        this.inventory.setItem(15, borderSize);
        return this.inventory;
    }
    }



