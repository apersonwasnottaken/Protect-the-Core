package com.example.protectTheCore.menu.config;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.menu.CustomMenuHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.example.protectTheCore.ProtectTheCore.plugin;

public class GameConfigMenu implements CustomMenuHolder {
    private final Inventory inventory;
    private String durationDisp = "0d 0h0m0s";

    public GameConfigMenu(ProtectTheCore plugin) {
        this.inventory = plugin.getServer().createInventory(this, 36, Component.text("ɢᴀᴍᴇ ᴄᴏɴꜰɪɢ"));
    }

    public void setDuration(int input) {
        plugin.getConfig().set("game.duration", input);
        plugin.saveConfig();
    }

    public void setDurationDisp(String input) {
        durationDisp = input;
    }

    public static int parseDuration(String duration) {
        try {
            int days = 0, hours = 0, minutes = 0, seconds = 0;
            if (duration.contains("d")) {
                days = Integer.parseInt(duration.substring(0, duration.indexOf('d')));
            }
            if (duration.contains("h")) {
                hours = Integer.parseInt(duration.substring(duration.indexOf('d') < 0 ? 0 : duration.indexOf('d') + 1, duration.indexOf('h')));
            }
            if (duration.contains("m")) {
                minutes = Integer.parseInt(duration.substring(duration.indexOf('h') < 0 ? 0 : duration.indexOf('h') + 1, duration.indexOf('m')));
            }
            if (duration.contains("s")) {
                seconds = Integer.parseInt(duration.substring(duration.indexOf('m') < 0 ? 0 : duration.indexOf('m') + 1, duration.indexOf('s')));
            }
            return seconds * 20 + minutes * 20 * 60 + hours * 20 * 60 * 60 + days * 20 * 60 * 60 * 24;
        }
        catch (Exception e) {
            ProtectTheCore.logger.error("There was an error while parsing the duration.");
            ProtectTheCore.logger.error(e.toString());
            return -1;
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        ItemStack gameDuration = ItemStack.of(Material.OAK_SIGN);
        gameDuration.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><green>ɢᴀᴍᴇ ᴅᴜʀᴀᴛɪᴏɴ</green><white>:</white> <yellow>" + durationDisp + "</yellow>")));
        this.inventory.setItem(13, gameDuration);
        return this.inventory;
    }
}
