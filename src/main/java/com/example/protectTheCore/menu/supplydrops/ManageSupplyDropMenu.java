package com.example.protectTheCore.menu.supplydrops;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.game.supplydrops.SupplyDrop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageSupplyDropMenu implements InventoryHolder {

    private Inventory inventory;
    private int supplyDropIdx = 0;
    private int previousMaterial = 0;
    private String time;
    private Location location;
    private final ProtectTheCore plugin;
    private final SupplyDrop supplyDrop;
    private final SupplyDropCreationMenu supplyDropCreationMenu;

    public ManageSupplyDropMenu(@NotNull ProtectTheCore plugin, @NotNull SupplyDrop supplyDrop, @NotNull SupplyDropCreationMenu supplyDropCreationMenu) {
        this.plugin = plugin;
        this.supplyDrop = supplyDrop;
        this.supplyDropCreationMenu = supplyDropCreationMenu;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Inventory getContents() {
        Inventory contents = plugin.getServer().createInventory(null, 27);
        for (int i = 0; i < 27; i++) {
            if (inventory == null) continue;
            contents.setItem(i, inventory.getItem(i));
        }
        return contents;
    }

    @Override
    public @NotNull Inventory getInventory() {
        location = supplyDrop.getLocation(supplyDropIdx);
        time = supplyDrop.getTime(supplyDropIdx);
        this.inventory = plugin.getServer().createInventory(this, 36, Component.text("ᴍᴀɴᴀɢᴇ ꜱᴜᴘᴘʟʏ ᴅʀᴏᴘ"));
        try {
            JSONArray supplyDropData = supplyDropCreationMenu.readSupplyDropData();
            JSONObject selectedSupplyDrop = (JSONObject) supplyDropData.get(supplyDropIdx);
            setPreviousMaterial(SupplyDrop.getContainersListString().indexOf(selectedSupplyDrop.get("container").toString()));
            final int[] idx = {0};
            for (ItemStack itemStack : supplyDrop.getInventory(supplyDropIdx)) {
                this.inventory.setItem(idx[0], itemStack);
                idx[0] = idx[0] + 1;
            }
            supplyDropCreationMenu.resetPreviousMaterial();
            ItemStack blankGrayStainedGlassPane = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
            blankGrayStainedGlassPane.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false>")));
            ItemStack back = ItemStack.of(Material.SPECTRAL_ARROW);
            back.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false>ʙᴀᴄᴋ")));
            ItemStack changeLocation = getItemStack();
            ItemStack changeTime = getStack();
            ItemStack save = ItemStack.of(Material.LIME_CONCRETE);
            save.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><green>ꜱᴀᴠᴇ")));
            this.inventory.setItem(27, back);
            this.inventory.setItem(28, blankGrayStainedGlassPane);
            this.inventory.setItem(29, blankGrayStainedGlassPane);
            this.inventory.setItem(30, changeLocation);
            this.inventory.setItem(31, getContainerItem(getPreviousMaterial()));
            this.inventory.setItem(32, changeTime);
            this.inventory.setItem(33, blankGrayStainedGlassPane);
            this.inventory.setItem(34, blankGrayStainedGlassPane);
            this.inventory.setItem(35, save);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this.inventory;
    }

    private @NotNull ItemStack getStack() {
        ItemStack changeTime = ItemStack.of(Material.PALE_OAK_SIGN);
        changeTime.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><yellow>ᴄʜᴀɴɢᴇ ᴛɪᴍᴇ"));
            List<Component> lore = Arrays.asList(
                    MiniMessage.miniMessage().deserialize("<white>Current: <gold>" + time),
                    Component.text(""),
                    Component.text("Left-click to change.", TextColor.color(255, 255, 0)).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                    Component.text("Right-click to reset.", TextColor.color(0, 255, 255)).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                    Component.text(""),
                    MiniMessage.miniMessage().deserialize("<italic:false><white>Format: <gold>mm/dd/yyyy hh:mm:ss"),
                    Component.text("Note: Time is in PST", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.TRUE)
            );
            meta.lore(lore);
        });
        return changeTime;
    }

    private @NotNull ItemStack getItemStack() {
        ItemStack changeLocation = ItemStack.of(Material.OAK_SIGN);
        changeLocation.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><yellow>ᴄʜᴀɴɢᴇ ʟᴏᴄᴀᴛɪᴏɴ"));
            List<Component> lore = Arrays.asList(
                    MiniMessage.miniMessage().deserialize("<gold><white>Current: " + "(</white>" + location.getX() + "<white>, </white>" + location.getY() + "<white>, </white>" + location.getZ() + "<white>) in </white>" + location.getWorld().getName()),
                    Component.text(""),
                    Component.text("Left-click to change.", TextColor.color(255, 255, 0)).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                    Component.text("Right-click to reset.", TextColor.color(0, 255, 255)).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                    Component.text(""),
                    MiniMessage.miniMessage().deserialize("<italic:false><white>Format: <gold>worldName X Y Z")
            );
            meta.lore(lore);
        });
        return changeLocation;
    }

    public void increasePreviousMaterial() {
        previousMaterial++;
        previousMaterial = previousMaterial % 16;
    }

    public void decreasePreviousMaterial() {
        previousMaterial--;
        if (previousMaterial < 0) {
            previousMaterial = 15;
        }
        previousMaterial = previousMaterial % 16;
    }

    public int getPreviousMaterial() {
        return previousMaterial;
    }

    public void setPreviousMaterial(int input) {
        previousMaterial = input < 0 ? 0 : input % 16;
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

    public int getSupplyDropIdx() {
        return supplyDropIdx;
    }

    public void setSupplyDropIdx(int supplyDropIdx) {
        this.supplyDropIdx = supplyDropIdx;
    }
}

