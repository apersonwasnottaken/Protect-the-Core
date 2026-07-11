package com.example.protectTheCore.helper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.json.JSONArray;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;

public class HelperFunctions {
    public static Player getNearestPlayer(Player sourcePlayer) {
        Location loc = sourcePlayer.getLocation();
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        if (Bukkit.getOnlinePlayers().size() < 2) {
            return sourcePlayer;
        }

        for (Player onlinePlayer : loc.getWorld().getPlayers()) {
            if (onlinePlayer.equals(sourcePlayer)) continue;

            double distance = onlinePlayer.getLocation().distanceSquared(loc);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = onlinePlayer;
            }
        }
        return nearest;
    }

    public static JSONArray inventoryToJSONArray(Inventory inventory) {
        JSONArray inventoryContents = new JSONArray();
        for (ItemStack itemStack : inventory.getContents()) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

                dataOutput.writeObject(itemStack);
                dataOutput.close();

                inventoryContents.put(Base64Coder.encodeLines(outputStream.toByteArray()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return inventoryContents;
    }

    public static Inventory JSONArrayToInventory(JSONArray contents) {
        ArrayList<ItemStack> items = new ArrayList<>();
        contents.forEach(obj -> {
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getMimeDecoder().decode(obj.toString()));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

                ItemStack item = (ItemStack) dataInput.readObject();
                dataInput.close();
                items.add(item);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to load item stack.", e);
            }
        });
        Inventory inventory = Bukkit.createInventory(null, 27);
        for (int i = 0; i < Math.min(items.size(), 27); i++) {
            if (items.get(i) == null) continue;
            inventory.setItem(i, items.get(i));
        }
        return inventory;
    }

    public static String getConfigEntryFromWorld(World world) {
        return switch (world.getName()) {
            case "ptcoverworld" -> "overworld";
            case "ptcnether" -> "nether";
            case "ptctheend" -> "the_end";
            default -> world.getName();
        };
    }
}
