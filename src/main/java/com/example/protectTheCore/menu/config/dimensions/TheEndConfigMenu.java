package com.example.protectTheCore.menu.config.dimensions;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.menu.CustomMenuHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TheEndConfigMenu implements CustomMenuHolder {

    private final Inventory inventory;
    private final FileConfiguration config;
    private int seedInt, borderSizeInt = 10000;

    public void setSeed(int val) {
        seedInt = val;
    }

    public void setBorderSize(int val) {
        borderSizeInt = val;
    }

    public TheEndConfigMenu(@NotNull ProtectTheCore plugin, @NotNull FileConfiguration config) {
        this.config = config;
        this.inventory = plugin.getServer().createInventory(this, 36, Component.text("ᴛʜᴇ ᴇɴᴅ ᴄᴏɴꜰɪɢ"));
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
        if (config.getBoolean("config.the_end.enabled")) {
            return enabled;
        }
        else {
            return disabled;
        }
    }

    public ItemStack getEnderDragonFightStateItem() {
        ItemStack enabled = ItemStack.of(Material.DRAGON_HEAD);
        enabled.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><light_purple>ᴇɴᴅᴇʀ ᴅʀᴀɢᴏɴ ꜰɪɢʜᴛ</light_purple><white>:</white> <green>ᴇɴᴀʙʟᴇᴅ</green>")));
        ItemStack disabled = ItemStack.of(Material.DRAGON_HEAD);
        disabled.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><light_purple>ᴇɴᴅᴇʀ ᴅʀᴀɢᴏɴ ꜰɪɢʜᴛ</light_purple><white>:</white> <red>ᴅɪꜱᴀʙʟᴇᴅ</red>")));
        if (config.getBoolean("config.the_end.dragon_fight")) {
            return enabled;
        }
        else {
            return disabled;
        }
    }
    public ItemStack getEndShopStateItem() {
        ItemStack enabled = ItemStack.of(Material.DRAGON_HEAD);
        enabled.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><green>ꜱʜᴏᴘ ᴍᴏᴅᴇ</green><white>:</white> <green>ᴇɴᴀʙʟᴇᴅ</green>")));
        ItemStack disabled = ItemStack.of(Material.DRAGON_HEAD);
        disabled.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><green>ꜱʜᴏᴘ ᴍᴏᴅᴇ</green><white>:</white> <red>ᴅɪꜱᴀʙʟᴇᴅ</red>")));
        if (config.getBoolean("config.the_end.end_shop")) {
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
        this.inventory.setItem(12, getEndShopStateItem());
        this.inventory.setItem(13, getEnabledStateItem());
        this.inventory.setItem(14, getEnderDragonFightStateItem());
        this.inventory.setItem(15, borderSize);
        return this.inventory;
    }
}





