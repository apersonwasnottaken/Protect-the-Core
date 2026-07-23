package com.example.protectTheCore.menu.config;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.menu.CustomMenuHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GameConfigMenu implements CustomMenuHolder {
    private final Inventory inventory;
    private String durationDisp = "0d 0h0m0s";
    private final ProtectTheCore plugin;

    public GameConfigMenu(@NotNull ProtectTheCore plugin) {
        this.plugin = plugin;
        this.inventory = plugin.getServer().createInventory(this, 36, Component.text("ɢᴀᴍᴇ ᴄᴏɴꜰɪɢ"));
    }

    public void setDuration(int input) {
        plugin.getConfig().set("game.duration", input);
        plugin.saveConfig();
    }

    public void setDurationDisp(String input) {
        durationDisp = input;
    }

    @Override
    public @NotNull Inventory getInventory() {
        ItemStack gameDuration = ItemStack.of(Material.OAK_SIGN);
        gameDuration.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><green>ɢᴀᴍᴇ ᴅᴜʀᴀᴛɪᴏɴ</green><white>:</white> <yellow>" + durationDisp + "</yellow>")));
        this.inventory.setItem(13, gameDuration);
        return this.inventory;
    }
}
