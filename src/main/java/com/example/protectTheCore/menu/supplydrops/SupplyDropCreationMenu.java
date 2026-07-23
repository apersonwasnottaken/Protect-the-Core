package com.example.protectTheCore.menu.supplydrops;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.game.supplydrops.SupplyDrop;
import com.example.protectTheCore.menu.CustomMenuHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SupplyDropCreationMenu implements CustomMenuHolder {

    private final Inventory inventory;
    private final ProtectTheCore plugin;
    private final ComponentLogger logger;
    private Location location;
    private String time;

    public JSONArray readSupplyDropData() throws IOException {
        Path path = Path.of("./plugins/ProtectTheCore/supply_drops.json");
        try {
            Object obj = new JSONParser().parse(new FileReader("./plugins/ProtectTheCore/supply_drops.json"));
            return (JSONArray) obj;
        } catch (IOException e) {
            Files.writeString(path, "{}");
            return new JSONArray();
        } catch (ParseException e) {
            Files.writeString(path, "{}");
            logger.error(Component.text("An unexpected error occurred while parsing the supply_drops.json file.\n" + e, TextColor.color(255, 0, 0)));
        }
        return new JSONArray();
    }

    public SupplyDropCreationMenu(@NotNull ProtectTheCore plugin, @NotNull ComponentLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.location = new Location(plugin.getServer().getWorld("ptcoverworld"), 0, 64, 0);
        this.time = "01/01/1970 00:00:00";
        this.inventory = plugin.getServer().createInventory(this, 9, Component.text("ᴄʀᴇᴀᴛᴇ ᴀ ꜱᴜᴘᴘʟʏ ᴅʀᴏᴘ"));
    }

    private int previousMaterial = 0;

    public void increasePreviousMaterial() {
        previousMaterial++;
        previousMaterial = previousMaterial % 22;
    }

    public void decreasePreviousMaterial() {
        previousMaterial--;
        if (previousMaterial < 0) {
            previousMaterial = 15;
        }
        previousMaterial = previousMaterial % 22;
    }

    public void resetPreviousMaterial() {
        previousMaterial = 0;
    }

    public int getPreviousMaterial() {
        return previousMaterial;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Material getContainer() {
        return SupplyDrop.getContainersList().get(previousMaterial);
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public ItemStack getContainerItem(int previousMaterial) {
        ArrayList<Component> lore = new ArrayList<>();
        lore.add(Component.text("Left-click to cycle forwards!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC,TextDecoration.State.FALSE));
        lore.add(Component.text("Right-click to cycle backwards!", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC,TextDecoration.State.FALSE));
        ItemStack chest = ItemStack.of(Material.CHEST);
        chest.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold>ᴄʜᴇꜱᴛ</bold>"));
            meta.lore(lore);
        });
        ItemStack enderChest = ItemStack.of(Material.ENDER_CHEST);
        enderChest.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold>ᴇɴᴅᴇʀ ᴄʜᴇꜱᴛ</bold>"));
            meta.lore(lore);
        });
        ItemStack barrel = ItemStack.of(Material.BARREL);
        barrel.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold>ʙᴀʀʀᴇʟ</bold>"));
            meta.lore(lore);
        });
        ItemStack copperChest = ItemStack.of(Material.WAXED_COPPER_CHEST);
        copperChest.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold>ᴄᴏᴘᴘᴇʀ ᴄʜᴇꜱᴛ</bold>"));
            meta.lore(lore);
        });
        ItemStack exposedCopperChest = ItemStack.of(Material.WAXED_EXPOSED_COPPER_CHEST);
        exposedCopperChest.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold>ᴇxᴘᴏꜱᴇᴅ ᴄᴏᴘᴘᴇʀ ᴄʜᴇꜱᴛ</bold>"));
            meta.lore(lore);
        });
        ItemStack oxidizedCopperChest = ItemStack.of(Material.WAXED_OXIDIZED_COPPER_CHEST);
        oxidizedCopperChest.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold>ᴏxɪᴅɪᴢᴇᴅ ᴄᴏᴘᴘᴇʀ ᴄʜᴇꜱᴛ</bold>"));
            meta.lore(lore);
        });
        ItemStack weatheredCopperChest = ItemStack.of(Material.WAXED_WEATHERED_COPPER_CHEST);
        weatheredCopperChest.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold>ᴡᴇᴀᴛʜᴇʀᴇᴅ ᴄᴏᴘᴘᴇʀ ᴄʜᴇꜱᴛ</bold>"));
            meta.lore(lore);
        });
        ItemStack containerRed = ItemStack.of(Material.RED_SHULKER_BOX);
        containerRed.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><red>ʀᴇᴅ ꜱʜᴜʟᴋᴇʀ ʙᴏx</red></bold>"));
            meta.lore(lore);
        });
        ItemStack containerOrange = ItemStack.of(Material.ORANGE_SHULKER_BOX);
        containerOrange.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><#eb7114>ᴏʀᴀɴɢᴇ ꜱʜᴜʟᴋᴇʀ ʙᴏx</#eb7114></bold>"));
            meta.lore(lore);
        });
        ItemStack containerYellow = ItemStack.of(Material.YELLOW_SHULKER_BOX);
        containerYellow.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><yellow>ʏᴇʟʟᴏᴡ ꜱʜᴜʟᴋᴇʀ ʙᴏx</yellow></bold>"));
            meta.lore(lore);
        });
        ItemStack containerLime = ItemStack.of(Material.LIME_SHULKER_BOX);
        containerLime.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><green>ʟɪᴍᴇ ꜱʜᴜʟᴋᴇʀ ʙᴏx</green></bold>"));
            meta.lore(lore);
        });
        ItemStack containerGreen = ItemStack.of(Material.GREEN_SHULKER_BOX);
        containerGreen.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><dark_green>ɢʀᴇᴇɴ ꜱʜᴜʟᴋᴇʀ ʙᴏx</dark_green></bold>"));
            meta.lore(lore);
        });
        ItemStack containerCyan = ItemStack.of(Material.CYAN_SHULKER_BOX);
        containerCyan.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><dark_aqua>ᴄʏᴀɴ ꜱʜᴜʟᴋᴇʀ ʙᴏx</dark_aqua></bold>"));
            meta.lore(lore);
        });
        ItemStack containerLightBlue = ItemStack.of(Material.LIGHT_BLUE_SHULKER_BOX);
        containerLightBlue.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><aqua>ʟɪɢʜᴛ ʙʟᴜᴇ ꜱʜᴜʟᴋᴇʀ ʙᴏx</aqua></bold>"));
            meta.lore(lore);
        });
        ItemStack containerBlue = ItemStack.of(Material.BLUE_SHULKER_BOX);
        containerBlue.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><blue>ʙʟᴜᴇ ꜱʜᴜʟᴋᴇʀ ʙᴏx</blue></bold>"));
            meta.lore(lore);
        });
        ItemStack containerPurple = ItemStack.of(Material.PURPLE_SHULKER_BOX);
        containerPurple.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><light_purple>ᴘᴜʀᴘʟᴇ ꜱʜᴜʟᴋᴇʀ ʙᴏx</light_purple></bold>"));
            meta.lore(lore);
        });
        ItemStack containerMagenta = ItemStack.of(Material.MAGENTA_SHULKER_BOX);
        containerMagenta.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><light_purple>ᴍᴀɢᴇɴᴛᴀ ꜱʜᴜʟᴋᴇʀ ʙᴏx</light_purple></bold>"));
            meta.lore(lore);
        });
        ItemStack containerPink = ItemStack.of(Material.PINK_SHULKER_BOX);
        containerPink.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><#f38baa>ᴘɪɴᴋ ꜱʜᴜʟᴋᴇʀ ʙᴏx</#f38baa></bold>"));
            meta.lore(lore);
        });
        ItemStack containerWhite = ItemStack.of(Material.WHITE_SHULKER_BOX);
        containerWhite.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><white>ᴡʜɪᴛᴇ ꜱʜᴜʟᴋᴇʀ ʙᴏx</white></bold>"));
            meta.lore(lore);
        });
        ItemStack containerLightGray = ItemStack.of(Material.LIGHT_GRAY_SHULKER_BOX);
        containerLightGray.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><gray>ʟɪɢʜᴛ ɢʀᴀʏ ꜱʜᴜʟᴋᴇʀ ʙᴏx</gray></bold>"));
            meta.lore(lore);
        });
        ItemStack containerGray = ItemStack.of(Material.GRAY_SHULKER_BOX);
        containerGray.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><dark_gray>ɢʀᴀʏ ꜱʜᴜʟᴋᴇʀ ʙᴏx</dark_gray></bold>"));
            meta.lore(lore);
        });
        ItemStack containerBlack = ItemStack.of(Material.BLACK_SHULKER_BOX);
        containerBlack.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><black>ʙʟᴀᴄᴋ ꜱʜᴜʟᴋᴇʀ ʙᴏx</black></bold>"));
            meta.lore(lore);
        });
        ItemStack containerBrown = ItemStack.of(Material.BROWN_SHULKER_BOX);
        containerBrown.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴄᴏɴᴛᴀɪɴᴇʀ: </white><bold><#724829>ʙʀᴏᴡɴ ꜱʜᴜʟᴋᴇʀ ʙᴏx</#724829></bold>"));
            meta.lore(lore);
        });
        ArrayList<ItemStack> containers = new ArrayList<>();
        containers.add(chest);
        containers.add(barrel);
        containers.add(enderChest);
        containers.add(copperChest);
        containers.add(exposedCopperChest);
        containers.add(weatheredCopperChest);
        containers.add(oxidizedCopperChest);
        containers.add(containerRed);
        containers.add(containerOrange);
        containers.add(containerYellow);
        containers.add(containerLime);
        containers.add(containerGreen);
        containers.add(containerCyan);
        containers.add(containerLightBlue);
        containers.add(containerBlue);
        containers.add(containerPurple);
        containers.add(containerMagenta);
        containers.add(containerPink);
        containers.add(containerWhite);
        containers.add(containerLightGray);
        containers.add(containerGray);
        containers.add(containerBlack);
        containers.add(containerBrown);
        return containers.get(previousMaterial);
    }

    @Override
    public @NotNull Inventory getInventory() {
        ItemStack supplyDropLocationDialog = getItemStack();
        ItemStack supplyDropTimeDialog = getStack();
        ItemStack cancel = ItemStack.of(Material.RED_CONCRETE);
        cancel.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><red>ᴄᴀɴᴄᴇʟ</red>")));
        ItemStack confirm = ItemStack.of(Material.LIME_CONCRETE);
        confirm.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><green>ᴄᴏɴꜰɪʀᴍ</green>")));
        this.inventory.setItem(0, cancel);
        this.inventory.setItem(3, supplyDropLocationDialog);
        this.inventory.setItem(4, getContainerItem(previousMaterial));
        this.inventory.setItem(5, supplyDropTimeDialog);
        this.inventory.setItem(8, confirm);
        return this.inventory;
    }

    private @NotNull ItemStack getStack() {
        ItemStack supplyDropTimeDialog = ItemStack.of(Material.PALE_OAK_SIGN);
        supplyDropTimeDialog.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><yellow>ꜱᴜᴘᴘʟʏ ᴅʀᴏᴘ ᴛɪᴍᴇ<white>: " + (location == null ? "<italic>None!</italic>" : time)));
            List<Component> lore = Arrays.asList(
                    Component.text("Left-click to change.", TextColor.color(255, 255, 0)).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                    Component.text("Right-click to reset.", TextColor.color(0, 255, 255)).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                    Component.text(""),
                    MiniMessage.miniMessage().deserialize("<italic:false><white>Format: <gold>mm/dd/yyyy hh:mm:ss"),
                    Component.text("Note: Time is in PST", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.TRUE)
            );
            meta.lore(lore);
        });
        return supplyDropTimeDialog;
    }

    private @NotNull ItemStack getItemStack() {
        ItemStack supplyDropLocationDialog = ItemStack.of(Material.OAK_SIGN);
        supplyDropLocationDialog.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><yellow>ꜱᴜᴘᴘʟʏ ᴅʀᴏᴘ ʟᴏᴄᴀᴛɪᴏɴ<white>: " + "(</white>" + location.getX() + "<white>, </white>" + location.getY() + "<white>, </white>" + location.getZ() + "<white>) in <yellow>" + location.getWorld().getName()));
            List<Component> lore = Arrays.asList(
                    Component.text("Left-click to change.", TextColor.color(255, 255, 0)).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                    Component.text("Right-click to reset.", TextColor.color(0, 255, 255)).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                    Component.text(""),
                    MiniMessage.miniMessage().deserialize("<italic:false><white>Format: <gold>worldName X Y Z")
                    );
            meta.lore(lore);
        });
        return supplyDropLocationDialog;
    }
}