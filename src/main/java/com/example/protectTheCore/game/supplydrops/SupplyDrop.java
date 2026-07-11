package com.example.protectTheCore.game.supplydrops;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.helper.HelperFunctions;
import net.kyori.adventure.text.Component;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;

import static com.example.protectTheCore.ProtectTheCore.*;
import static com.example.protectTheCore.helper.HelperFunctions.JSONArrayToInventory;

public class SupplyDrop {

    private static ArrayList<JSONObject> supplyDropConfig = new ArrayList<>();

    private JSONArray readSupplyDropData() throws IOException {
        Path path = Path.of("./plugins/ProtectTheCore/supply_drops.json");
        try {
            return new JSONArray(Files.readString(path));
        }
        catch (IOException e) {
            Files.writeString(path,"[]");
            return new JSONArray();
        } catch (ClassCastException e) {
            ProtectTheCore.logger.error(Component.text("An unexpected error occurred while parsing the supply_drops.json file.\n" + e, NamedTextColor.RED));
            return new JSONArray();
        }
    }

    public static void parseSupplyDropConfig() {
        supplyDropConfig.clear();
        try {
            JSONArray teamsData = new JSONArray(Files.readString(Path.of("./plugins/ProtectTheCore/supply_drops.json")));
            teamsData.forEach(obj -> {
                supplyDropConfig.add((JSONObject) obj);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveSupplyDropConfig() {
        try {
            JSONArray combinedData = new JSONArray();
            for (JSONObject jsonObject : supplyDropConfig) {
                combinedData.put(jsonObject);
            }
            JSONObject combineData = new JSONObject();
            combineData.put("teams", combinedData);
            Files.writeString(Path.of("./plugins/ProtectTheCore/supply_drops.json"), combinedData.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void createSupplyDrop(Inventory inventory, Location location, Material container, String time) {
        JSONObject supplyDrop = new JSONObject();
        supplyDrop.put("contents", HelperFunctions.inventoryToJSONArray(inventory));
        supplyDrop.put("location", location.getWorld().getName() + " " + ((int) location.getX()) + " " + ((int) location.getY()) + " " + ((int) location.getZ()));
        supplyDrop.put("container", container);
        supplyDrop.put("time", time);
        supplyDrop.put("enabled", true);
        supplyDropConfig.add(supplyDrop);
        saveSupplyDropConfig();
    }

    public static void removeSupplyDrop(int idx) {
        if (idx > supplyDropConfig.size() || idx < 0) {
            ProtectTheCore.logger.error(Component.text("Supply drop index " + idx + " is out of bounds! Max: " + (supplyDropConfig.size() - 1), NamedTextColor.RED));
        }
        supplyDropConfig.remove(idx);
        saveSupplyDropConfig();
    }

    public static int getSupplyDropConfigSize() {
        return supplyDropConfig.size();
    }

    public static Inventory getInventory(int supplyDropID) {
        parseSupplyDropConfig();
        if (supplyDropID > supplyDropConfig.size() || supplyDropID < 0) {
            ProtectTheCore.logger.error(Component.text("Supply drop index " + supplyDropID + " is out of bounds! Max: " + (supplyDropConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject supplyDrop = supplyDropConfig.get(supplyDropID);
        return HelperFunctions.JSONArrayToInventory((JSONArray) supplyDrop.get("contents"));
    }

    public static void setInventory(Inventory inventory, int supplyDropID) {
        if (supplyDropID > supplyDropConfig.size() || supplyDropID < 0) {
            ProtectTheCore.logger.error(Component.text("Supply drop index " + supplyDropID + " is out of bounds! Max: " + (supplyDropConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject supplyDrop = supplyDropConfig.get(supplyDropID);
        JSONArray inventoryContents = HelperFunctions.inventoryToJSONArray(inventory);
        supplyDrop.put("contents", inventoryContents);
        supplyDropConfig.set(supplyDropID, supplyDrop);
        saveSupplyDropConfig();
    }

    public static Location getLocation(int supplyDropID) {
        parseSupplyDropConfig();
        if (supplyDropID > supplyDropConfig.size() || supplyDropID < 0) {
            ProtectTheCore.logger.error(Component.text("Supply drop index " + supplyDropID + " is out of bounds! Max: " + (supplyDropConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject supplyDrop = supplyDropConfig.get(supplyDropID);
        return new Location(plugin.getServer().getWorld(supplyDrop.getString("location").split(" ")[0]), Integer.parseInt(supplyDrop.getString("location").split(" ")[1]), Integer.parseInt(supplyDrop.getString("location").split(" ")[2]), Integer.parseInt(supplyDrop.getString("location").split(" ")[3]));
    }

    public static void setLocation(Location location, int supplyDropID) {
        if (supplyDropID > supplyDropConfig.size() || supplyDropID < 0) {
            ProtectTheCore.logger.error(Component.text("Supply drop index " + supplyDropID + " is out of bounds! Max: " + (supplyDropConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject supplyDrop = supplyDropConfig.get(supplyDropID);
        supplyDrop.put("location", location.getWorld().getName() + " " + (int) location.getX() + " " + (int) location.getY() + " " + (int) location.getZ());
        supplyDropConfig.set(supplyDropID, supplyDrop);
        saveSupplyDropConfig();
    }

    public static String getTime(int supplyDropID) {
        parseSupplyDropConfig();
        if (supplyDropID > supplyDropConfig.size() || supplyDropID < 0) {
            ProtectTheCore.logger.error(Component.text("Supply drop index " + supplyDropID + " is out of bounds! Max: " + (supplyDropConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject supplyDrop = supplyDropConfig.get(supplyDropID);
        return supplyDrop.getString("time");
    }

    public static void setTime(String time, int supplyDropID) {
        if (supplyDropID > supplyDropConfig.size() || supplyDropID < 0) {
            ProtectTheCore.logger.error(Component.text("Supply drop index " + supplyDropID + " is out of bounds! Max: " + (supplyDropConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject supplyDrop = supplyDropConfig.get(supplyDropID);
        supplyDrop.put("time", time);
        supplyDropConfig.set(supplyDropID, supplyDrop);
        saveSupplyDropConfig();
    }

    public static Material getContainer(int supplyDropID) {
        parseSupplyDropConfig();
        if (supplyDropID > supplyDropConfig.size() || supplyDropID < 0) {
            ProtectTheCore.logger.error(Component.text("Supply drop index " + supplyDropID + " is out of bounds! Max: " + (supplyDropConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject supplyDrop = supplyDropConfig.get(supplyDropID);
        return SupplyDrop.getContainersList().get(SupplyDrop.getContainersListString().indexOf(supplyDrop.get("container")));
    }

    public static void setContainer(Material container, int supplyDropID) {
        if (supplyDropID > supplyDropConfig.size() || supplyDropID < 0) {
            ProtectTheCore.logger.error(Component.text("Supply drop index " + supplyDropID + " is out of bounds! Max: " + (supplyDropConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject supplyDrop = supplyDropConfig.get(supplyDropID);
        supplyDrop.put("container", container);
        supplyDropConfig.set(supplyDropID, supplyDrop);
        saveSupplyDropConfig();
    }

    public static void spawnSupplyDrop(JSONArray inventory, Location location, Material container) {
        BlockDisplay display = location.getWorld().spawn(new Location(location.getWorld(), location.getX(), 256, location.getZ()), BlockDisplay.class, entity -> {
            entity.getPersistentDataContainer().set(new NamespacedKey(plugin, "supply_drop_contents"), PersistentDataType.STRING, inventory.toString());
            entity.getPersistentDataContainer().set(new NamespacedKey(plugin, "supply_drop_final_y"), PersistentDataType.INTEGER, ((int) location.getY()));
            entity.setBlock(Bukkit.createBlockData(container));
            Transformation transformation = entity.getTransformation();
            transformation.getScale().set(1.0f, 1.0f, 1.0f);
            entity.setTransformation(transformation);
        });
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<white>A <yellow>Supply Drop</yellow> has spawned at <yellow>" + location.getX() + " " + location.getY() + " " + location.getZ() + "</yellow>"));
    }

    public static void supplyDropLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
                for (int i = 0; i < supplyDropConfig.size(); i++) {
                    JSONObject obj = supplyDropConfig.get(i);
                    if (LocalDateTime.now().isAfter(LocalDateTime.parse(obj.getString("time"), formatter)) && obj.getBoolean("enabled")) {
                        spawnSupplyDrop(obj.getJSONArray("contents"), SupplyDrop.getLocation(i), SupplyDrop.getContainer(i));
                        obj.put("enabled", false);
                        supplyDropConfig.set(i, obj);
                        saveSupplyDropConfig();
                    }
                }
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (!entity.getPersistentDataContainer().has(new NamespacedKey(plugin, "supply_drop_final_y"), PersistentDataType.INTEGER)) continue;
                        if (!(entity instanceof BlockDisplay)) continue;
                        if (entity.getWorld().getBlockAt(entity.getLocation().add(0, -1, 0)).getState().getType() == Material.AIR) {
                            entity.teleport(entity.getLocation().add(0, -0.4, 0));
                            if (Math.floor(Math.random() * 2) < 1) {
                                entity.getWorld().spawnParticle(Particle.SMOKE, entity.getLocation().getX() + 0.5, entity.getLocation().getY() + 0.5, entity.getLocation().getZ() + 0.5, 1);
                            }
                        }
                        else {
                            if (entity.getPersistentDataContainer().has(new NamespacedKey(plugin, "supply_drop_contents"), PersistentDataType.STRING)) {
                                ItemStack[] deserializedItems = HelperFunctions.JSONArrayToInventory(new JSONArray(entity.getPersistentDataContainer().get(new NamespacedKey(plugin, "supply_drop_contents"), PersistentDataType.STRING))).getContents();
                                Block supplyDropBlock = entity.getLocation().getWorld().getBlockAt(entity.getLocation().getBlockX(), entity.getLocation().getBlockY(), entity.getLocation().getBlockZ());
                                supplyDropBlock.setType(((BlockDisplay) entity).getBlock().getMaterial());
                                if (supplyDropBlock.getState() instanceof Container container) {
                                    if (container instanceof Chest || container instanceof Barrel || container instanceof ShulkerBox || container instanceof EnderChest) {
                                        container.customName(Component.text("Supply Drop"));
                                        container.update();
                                    }
                                    Inventory supplyDropInventory = container.getSnapshotInventory();
                                    supplyDropInventory.clear();
                                    int i = 0;
                                    for (ItemStack item : deserializedItems) {
                                        supplyDropInventory.setItem(i, item);
                                        i++;
                                    }
                                    container.update(true, true);
                                }
                                else {
                                    ProtectTheCore.logger.warn("Not an instance of Container!");
                                }
                            }
                            else {
                                ProtectTheCore.logger.warn("No items found!");
                            }
                            entity.remove();
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public static ArrayList<Material> getContainersList() {
        ArrayList<Material> materials = new ArrayList<>();
        materials.add(Material.CHEST);
        materials.add(Material.BARREL);
        materials.add(Material.ENDER_CHEST);
        materials.add(Material.WAXED_COPPER_CHEST);
        materials.add(Material.WAXED_EXPOSED_COPPER_CHEST);
        materials.add(Material.WAXED_WEATHERED_COPPER_CHEST);
        materials.add(Material.WAXED_OXIDIZED_COPPER_CHEST);
        materials.add(Material.RED_SHULKER_BOX);
        materials.add(Material.ORANGE_SHULKER_BOX);
        materials.add(Material.YELLOW_SHULKER_BOX);
        materials.add(Material.LIME_SHULKER_BOX);
        materials.add(Material.GREEN_SHULKER_BOX);
        materials.add(Material.CYAN_SHULKER_BOX);
        materials.add(Material.LIGHT_BLUE_SHULKER_BOX);
        materials.add(Material.BLUE_SHULKER_BOX);
        materials.add(Material.PURPLE_SHULKER_BOX);
        materials.add(Material.MAGENTA_SHULKER_BOX);
        materials.add(Material.PINK_SHULKER_BOX);
        materials.add(Material.WHITE_SHULKER_BOX);
        materials.add(Material.LIGHT_GRAY_SHULKER_BOX);
        materials.add(Material.GRAY_SHULKER_BOX);
        materials.add(Material.BLACK_SHULKER_BOX);
        materials.add(Material.BROWN_SHULKER_BOX);
        return materials;
    }

    public static ArrayList<String> getContainersListString() {
        ArrayList<String> materials = new ArrayList<>();
        materials.add("CHEST");
        materials.add("BARREL");
        materials.add("ENDER_CHEST");
        materials.add("WAXED_COPPER_CHEST");
        materials.add("WAXED_EXPOSED_COPPER_CHEST");
        materials.add("WAXED_WEATHERED_COPPER_CHEST");
        materials.add("WAXED_OXIDIZED_COPPER_CHEST");
        materials.add("RED_SHULKER_BOX");
        materials.add("ORANGE_SHULKER_BOX");
        materials.add("YELLOW_SHULKER_BOX");
        materials.add("LIME_SHULKER_BOX");
        materials.add("GREEN_SHULKER_BOX");
        materials.add("CYAN_SHULKER_BOX");
        materials.add("LIGHT_BLUE_SHULKER_BOX");
        materials.add("BLUE_SHULKER_BOX");
        materials.add("PURPLE_SHULKER_BOX");
        materials.add("MAGENTA_SHULKER_BOX");
        materials.add("PINK_SHULKER_BOX");
        materials.add("WHITE_SHULKER_BOX");
        materials.add("LIGHT_GRAY_SHULKER_BOX");
        materials.add("GRAY_SHULKER_BOX");
        materials.add("BLACK_SHULKER_BOX");
        materials.add("BROWN_SHULKER_BOX");
        return materials;
    }
}
