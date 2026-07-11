package com.example.protectTheCore.core;

import com.example.protectTheCore.ProtectTheCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.example.protectTheCore.ProtectTheCore.*;

public class Teams {
    private static ArrayList<JSONObject> teamsConfig = new ArrayList<>();

    public static void parseTeamsConfig() {
        teamsConfig.clear();
        try {
            JSONArray teamsData = new JSONArray(Files.readString(Path.of("./plugins/ProtectTheCore/teams.json")));
            teamsData.forEach(obj -> {
                teamsConfig.add((JSONObject) obj);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<JSONObject> getTeamsConfig() {
        return teamsConfig;
    }

    public static void setTeamsConfig(ArrayList<JSONObject> config) {
        teamsConfig = config;
    }

    public static void saveTeamsConfig() throws Exception {
        JSONArray combinedData = new JSONArray();
        for (JSONObject jsonObject : teamsConfig) {
            combinedData.put(jsonObject);
        }
        JSONObject combineData = new JSONObject();
        combineData.put("teams", combinedData);
        Files.writeString(Path.of("./plugins/ProtectTheCore/teams.json"), combinedData.toString());
        glowManager.refreshAllOnlinePlayers();
    }

    public static boolean teamHasCrown(int teamIdx) {
        AtomicBoolean hasCrown = new AtomicBoolean(false);
        getTeamMembers(teamIdx).forEach(member -> {
            if (Bukkit.getPlayer(((JSONObject) member).getString("username")) != null) {
                ItemStack[] contents = Bukkit.getPlayer(((JSONObject) member).getString("username")).getInventory().getContents();
                for(ItemStack item : contents) {
                    if (item != null) {
                        if (item.getPersistentDataContainer().has(new NamespacedKey(plugin, "ptc_crown"))) {
                            hasCrown.set(true);
                        }
                    }
                }
            }
        });
        return hasCrown.get();
    }

    @Nullable
    public static EnderCrystal getTeamCore(int teamIdx, World world) {
        AtomicReference<Entity> enderCrystal = new AtomicReference<>();
        world.getNearbyEntities(zoneManager.getTeamCenter(teamIdx, world).add(0,1,0), 1, 1, 1).forEach(entity -> {
            if (entity.getType() == EntityType.END_CRYSTAL && entity.getPersistentDataContainer().has(new NamespacedKey(plugin, "crystal_health"))) {
                enderCrystal.set(entity);
            }
        });
        return (EnderCrystal) enderCrystal.get();
    }

    public static void addTeamMember(int teamIdx, String playerName, UUID playerUUID) throws Exception {
        if (teamIdx > teamsConfig.size() - 1 || teamIdx < 0) {
            ProtectTheCore.logger.error(Component.text("Team index " + teamIdx + " is out of bounds! Max: " + (teamsConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject teamObject = teamsConfig.get(teamIdx);
        JSONArray members = (JSONArray) teamObject.get("members");
        JSONObject playerEntry = new JSONObject();
        playerEntry.put("username", playerName);
        playerEntry.put("uuid", playerUUID);
        members.put(playerEntry);
        teamObject.put("members", members);
        teamsConfig.set(teamIdx, teamObject);
        saveTeamsConfig();
    }

    public static void removeTeamMember(int teamIdx, UUID playerUUID) throws Exception {
        if (teamIdx > teamsConfig.size() || teamIdx < 0) {
            ProtectTheCore.logger.error("Team index {} is out of bounds! Max: {}", teamIdx, teamsConfig.size());
            throw new IndexOutOfBoundsException();
        }
        JSONObject team = teamsConfig.get(teamIdx);
        JSONArray members = (JSONArray) team.get("members");
        int idx = -1;
        for (int i = 0; i < members.length(); i++) {
            JSONObject obj = (JSONObject) members.get(i);
            if (Objects.equals(obj.get("uuid").toString(), playerUUID.toString())) {
                idx = i;
                break;
            }
        }
        if (idx == -1) {
            ProtectTheCore.logger.warn("Player with UUID {} was not found in team {}", playerUUID.toString(), team.get("name"));
        }
        else {
            members.remove(idx);
        }
        saveTeamsConfig();
    }

    public static void setTeamName(int teamIdx, String name) throws Exception {
        if (teamIdx > teamsConfig.size() || teamIdx < 0) {
            ProtectTheCore.logger.error(Component.text("Team index " + teamIdx + " is out of bounds! Max: " + (teamsConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject team = teamsConfig.get(teamIdx);
        team.put("name", name);
        teamsConfig.set(teamIdx, team);
        saveTeamsConfig();
    }

    public static String getTeamName(int teamIdx) {
        if (teamIdx > teamsConfig.size() || teamIdx < 0) {
            ProtectTheCore.logger.error(Component.text("Team index " + teamIdx + " is out of bounds! Max: " + (teamsConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject team = teamsConfig.get(teamIdx);
        return team.getString("name");
    }

    public static void setTeamColor(int teamIdx, int color) throws Exception {
        if (teamIdx > teamsConfig.size() || teamIdx < 0) {
            ProtectTheCore.logger.error(Component.text("Team index " + teamIdx + " is out of bounds! Max: " + (teamsConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject team = teamsConfig.get(teamIdx);
        team.put("color", color);
        teamsConfig.set(teamIdx, team);
        saveTeamsConfig();
    }

    public static int getTeamColor(int teamIdx) {
        if (teamIdx > teamsConfig.size() || teamIdx < 0) {
            ProtectTheCore.logger.error(Component.text("Team index " + teamIdx + " is out of bounds! Max: " + (teamsConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject team = teamsConfig.get(teamIdx);
        return (int) team.get("color");
    }

    public static void setTeamPvPStatus(int teamIdx, boolean pvp) throws Exception {
        if (teamIdx > teamsConfig.size() || teamIdx < 0) {
            ProtectTheCore.logger.error(Component.text("Team index " + teamIdx + " is out of bounds! Max: " + (teamsConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject team = teamsConfig.get(teamIdx);
        team.put("pvp", pvp);
        teamsConfig.set(teamIdx, team);
        saveTeamsConfig();
    }

    public static boolean getTeamPvPStatus(int teamIdx) throws IndexOutOfBoundsException {
        if (teamIdx > teamsConfig.size() || teamIdx < 0) {
            ProtectTheCore.logger.error(Component.text("Team index " + teamIdx + " is out of bounds! Max: " + (teamsConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject team = teamsConfig.get(teamIdx);
        return team.getBoolean("pvp");
    }

    public static void createNewTeam(String name, int color, boolean pvp) throws Exception {
        JSONObject newTeam = new JSONObject();
        newTeam.put("name", name);
        newTeam.put("leader", new JSONObject());
        newTeam.put("members", new JSONArray());
        newTeam.put("color", color);
        newTeam.put("pvp", pvp);
        teamsConfig.add(newTeam);
        saveTeamsConfig();
    }

    public static void removeTeam(int teamIdx) throws Exception {
        if (teamIdx > teamsConfig.size() || teamIdx < 0) {
            ProtectTheCore.logger.error(Component.text("Team index " + teamIdx + " is out of bounds! Max: " + (teamsConfig.size() - 1), NamedTextColor.RED));
        }
        teamsConfig.remove(teamIdx);
        saveTeamsConfig();
    }

    public static String getTeamLeader(int teamIdx) {
        if (teamIdx > teamsConfig.size() || teamIdx < 0) {
            ProtectTheCore.logger.error(Component.text("Team index " + teamIdx + " is out of bounds! Max: " + (teamsConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject teamData = teamsConfig.get(teamIdx);
        if (Objects.equals(teamData.get("leader").toString(), "{}")) {
            return "";
        }
        return ((JSONObject) teamData.get("leader")).get("username").toString();
    }

    public static boolean isTeamLeader(String player) {
        try {
            return Objects.equals(Teams.getTeamLeader(Teams.getTeamIndexFromPlayer(player)), player);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeTeamLeader(int teamIdx) throws Exception {
        if (teamIdx > teamsConfig.size() || teamIdx < 0) {
            ProtectTheCore.logger.error(Component.text("Team index " + teamIdx + " is out of bounds! Max: " + (teamsConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject teamData = teamsConfig.get(teamIdx);
        teamData.put("leader", new JSONObject());
        teamsConfig.set(teamIdx, teamData);
        saveTeamsConfig();
    }

    public static void setTeamLeader(int teamIdx, String leader) throws Exception {
        if (teamIdx > teamsConfig.size() || teamIdx < 0) {
            ProtectTheCore.logger.error(Component.text("Team index " + teamIdx + " is out of bounds! Max: " + (teamsConfig.size() - 1), NamedTextColor.RED));
        }
        JSONObject teamData = teamsConfig.get(teamIdx);
        teamData.put("leader", new JSONObject().put("username", leader).put("uuid", Objects.requireNonNull(Bukkit.getOfflinePlayer(leader)).getUniqueId()));
        teamsConfig.set(teamIdx, teamData);
        saveTeamsConfig();
    }

    public static JSONArray getTeamMembers(int teamIdx) {
        if (teamIdx > teamsConfig.size() || teamIdx < 0) {
            ProtectTheCore.logger.error(Component.text("Team index " + teamIdx + " is out of bounds! Max: " + (teamsConfig.size() - 1), NamedTextColor.RED));
        }
        return (JSONArray) teamsConfig.get(teamIdx).get("members");
    }

    public static int getTeamIndexFromPlayer(String playerName) throws IOException {
        JSONArray teamsData = new JSONArray(Files.readString(Path.of("./plugins/ProtectTheCore/teams.json")));
        AtomicInteger teamIdx = new AtomicInteger(0);
        AtomicInteger foundIdx = new AtomicInteger(-1);
        teamsData.forEach(o -> {
            ((JSONArray) ((JSONObject) o).get("members")).forEach(obj -> {
                if (Objects.equals(((JSONObject) obj).getString("username"), playerName)) {
                    foundIdx.set(teamIdx.get());
                }
            });
            teamIdx.getAndIncrement();
        });
        return foundIdx.get();
    }

    // You should probably use this over getIndexFromPlayer
    public static int getTeamIndexFromPlayer(UUID playerUUID) throws IOException {
        JSONArray teamsData = new JSONArray(Files.readString(Path.of("./plugins/ProtectTheCore/teams.json")));
        AtomicInteger teamIdx = new AtomicInteger(0);
        AtomicInteger foundIdx = new AtomicInteger(-1);
        teamsData.forEach(o -> {
            ((JSONArray) ((JSONObject) o).get("members")).forEach(obj -> {
                if (Objects.equals(((JSONObject) obj).getString("uuid"), playerUUID.toString())) {
                    foundIdx.set(teamIdx.get());
                }
            });
            teamIdx.getAndIncrement();
        });
        return foundIdx.get();
    }
}

