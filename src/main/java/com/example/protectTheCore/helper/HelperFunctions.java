package com.example.protectTheCore.helper;

import com.example.protectTheCore.ProtectTheCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public static boolean getDebugMode(Entity entity) {
        if (entity.getPersistentDataContainer().has(new NamespacedKey("protectthecore", "debug_mode"))) {
            return Boolean.TRUE.equals(entity.getPersistentDataContainer().get(new NamespacedKey("protectthecore", "debug_mode"), PersistentDataType.BOOLEAN));
        }
        return false;
    }

    public static void sendErrorMessage(Player player, Exception e) {
        if (getDebugMode(player)) player.sendMessage(MiniMessage.miniMessage().deserialize("<red>" + e));
    }

    public static JSONArray sortJSONArray(JSONArray jsonArray, Object key, boolean reverse) {
        if (jsonArray == null || jsonArray.isEmpty()) {
            return jsonArray;
        }

        Comparator<JSONObject> comparator = (obj1, obj2) -> {
            Object val1 = getValueByCriteria(obj1, key);
            Object val2 = getValueByCriteria(obj2, key);

            if (val1 == null && val2 == null) return 0;
            if (val1 == null) return 1;
            if (val2 == null) return -1;

            if (val1 instanceof Number && val2 instanceof Number) {
                return Double.compare(((Number) val1).doubleValue(), ((Number) val2).doubleValue());
            }

            if (val1 instanceof String && val2 instanceof String) {
                return ((String) val1).compareTo((String) val2);
            }

            if (val1 instanceof Boolean && val2 instanceof Boolean) {
                return Boolean.compare((Boolean) val1, (Boolean) val2);
            }

            return val1.toString().compareTo(val2.toString());
        };

        if (reverse) {
            comparator = comparator.reversed();
        }

        return IntStream.range(0, jsonArray.length())
                .mapToObj(jsonArray::getJSONObject)
                .sorted(comparator)
                .collect(JSONArray::new, JSONArray::put, JSONArray::putAll);
    }

    public static JSONArray sortJSONArrayByValue(JSONArray jsonArray, boolean reverse) {
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getJSONObject(i));
        }

        list.sort((jsonObject1, jsonObject2) -> {
            int val1 = jsonObject1.getInt(jsonObject1.keys().next());
            int val2 = jsonObject2.getInt(jsonObject2.keys().next());
            if (reverse) {
                return Integer.compare(val1, val2);
            }
            else {
                return Integer.compare(val2, val1);
            }
        });
        return new JSONArray(list);
    }

    private static Object getValueByCriteria(JSONObject jsonObject, Object criteria) {
        return jsonObject.opt((String) criteria);
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
            Bukkit.getLogger().severe("[ProtectTheCore] There was an error while parsing the duration.");
            Bukkit.getLogger().severe(e.toString());
            return -1;
        }
    }

    public static List<Entity> findEntitiesWithKey(World world, NamespacedKey key, PersistentDataType persistentDataType) {
        return world.getEntities().stream()
                .filter(entity -> entity.getPersistentDataContainer().has(key, persistentDataType))
                .collect(Collectors.toList());
    }

    public static List<Entity> findEntitiesWithKeyValue(List<Entity> entities, NamespacedKey key, PersistentDataType persistentDataType, Object value) {
        return entities.stream()
                .filter(entity -> {
                    if (!entity.getPersistentDataContainer().has(key, persistentDataType)) {
                        return false;
                    }
                    Object containerValue = entity.getPersistentDataContainer().get(key, persistentDataType);
                    return Objects.equals(containerValue, value);
                })
                .collect(Collectors.toList());
    }

    public static String parseTimeLeft(int timeLeft) {
        int days = 60 * 60 * 24, hours = 60 * 60, minutes = 60, seconds = 1;
        int totalDays = 0, totalHours = 0, totalMinutes = 0, totalSeconds = 0;
        int i = timeLeft;
        while (i > days) {
            totalDays++;
            i = i - days;
        }
        while (i > hours) {
            totalHours++;
            i = i - hours;
        }
        while (i > minutes) {
            totalMinutes++;
            i = i - minutes;
        }
        while (i > seconds) {
            totalSeconds++;
            i = i - seconds;
        }
        return totalDays + "d " + totalHours + "h " + totalMinutes + "m " + totalSeconds + "s";
    }
}
