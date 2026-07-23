package com.example.protectTheCore.menu.supplydrops;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.game.supplydrops.SupplyDrop;
import com.example.protectTheCore.menu.CustomMenuHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class SupplyDropMenu implements CustomMenuHolder {

    private Inventory inventory;
    private final ProtectTheCore plugin;
    private final ComponentLogger logger;
    private final SupplyDrop supplyDrop;

    private JSONArray readSupplyDropData() throws IOException {
        Path path = Path.of("./plugins/ProtectTheCore/supply_drops.json");
        try {
            return new JSONArray(Files.readString(path));
        }
        catch (IOException e) {
            Files.writeString(path,"[]");
            return new JSONArray();
        } catch (ClassCastException e) {
            logger.error(Component.text("An unexpected error occurred while parsing the supply_drops.json file.\n" + e, NamedTextColor.RED));
            return new JSONArray();
        }
    }

    public SupplyDropMenu(@NotNull ProtectTheCore plugin, @NotNull ComponentLogger logger, @NotNull SupplyDrop supplyDrop) {
        this.plugin = plugin;
        this.logger = logger;
        this.supplyDrop = supplyDrop;
        this.inventory = this.getInventory();
    }
    @Override
    public @NotNull Inventory getInventory() {
        assert plugin != null;
        this.inventory = plugin.getServer().createInventory(this, 36, Component.text("ꜱᴜᴘᴘʟʏ ᴅʀᴏᴘꜱ"));
        try {
            JSONArray supplyDrops = readSupplyDropData();
            final int[] i = {0};
            supplyDrops.forEach(obj -> {
                try {
                    JSONObject supplyDropInfo = (JSONObject) obj;
                    ItemStack supplyDropDialog = ItemStack.of(SupplyDrop.getContainersList().get(SupplyDrop.getContainersListString().indexOf(supplyDropInfo.getString("container"))));
                    supplyDropDialog.editMeta(meta -> {
                        var loc = supplyDrop.getLocation(i[0]);
                        var world = loc != null ? loc.getWorld() : null;
                        String worldName = (world != null) ? world.getName().toLowerCase() : "unknown";
                        Component name;
                        if (worldName.contains("nether")) {
                            name = Component.text("Supply Drop (Nether)", NamedTextColor.RED);
                        } else if (worldName.contains("end")) {
                            name = Component.text("Supply Drop (End)", NamedTextColor.LIGHT_PURPLE);
                        } else {
                            name = Component.text("Supply Drop (Overworld)", NamedTextColor.GREEN);
                        }
                        meta.displayName(name.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        ArrayList<Component> lore = new ArrayList<>();
                        lore.add(MiniMessage.miniMessage().deserialize("<italic:false><yellow>Location: (<aqua>" + supplyDrop.getLocation(i[0]).getBlockX() + "</aqua>, <aqua>" + supplyDrop.getLocation(i[0]).getBlockY() + "</aqua>, <aqua>" + supplyDrop.getLocation(i[0]).getBlockZ() + "</aqua>)"));
                        lore.add(MiniMessage.miniMessage().deserialize("<italic:false><green>Drops at: <gold>" + supplyDropInfo.get("time") + " PST"));
                        lore.add(Component.text(""));
                        lore.add(Component.text("Left-click to manage supply drop.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        lore.add(Component.text("Right-click to delete supply drop.", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        meta.lore(lore);
                    });
                    this.inventory.setItem(i[0], supplyDropDialog);
                    i[0]++;
                }
                catch (ClassCastException e) {
                    logger.error(Component.text("An error occurred while trying to parse the supply drop list! This could potentially be due to malformed data. Please check the supply_drops.json file for corruption.\n" + e, NamedTextColor.RED));
                }
            });
            // Items
            ItemStack addSupplyDrop = ItemStack.of(Material.LIME_DYE);
            addSupplyDrop.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><green>ᴀᴅᴅ ꜱᴜᴘᴘʟʏ ᴅʀᴏᴘ</green>")));
            ItemStack blankGrayStainedGlassPane = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
            blankGrayStainedGlassPane.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("")));
            this.inventory.setItem(27, blankGrayStainedGlassPane);
            this.inventory.setItem(28, blankGrayStainedGlassPane);
            this.inventory.setItem(29, blankGrayStainedGlassPane);
            this.inventory.setItem(30, blankGrayStainedGlassPane);
            this.inventory.setItem(31, addSupplyDrop);
            this.inventory.setItem(32, blankGrayStainedGlassPane);
            this.inventory.setItem(33, blankGrayStainedGlassPane);
            this.inventory.setItem(34, blankGrayStainedGlassPane);
            this.inventory.setItem(35, blankGrayStainedGlassPane);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this.inventory;
    }
}