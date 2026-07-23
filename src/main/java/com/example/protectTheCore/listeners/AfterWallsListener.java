package com.example.protectTheCore.listeners;

import com.example.protectTheCore.core.Teams;
import com.example.protectTheCore.helper.PluginData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AfterWallsListener implements Listener {
    private JSONArray destroyedCores = new JSONArray();
    private Teams teams;
    private PluginData pluginData;

    public AfterWallsListener(@NotNull Teams teams, @NotNull PluginData pluginData) {
        this.teams = teams;
        this.pluginData = pluginData;
    }

    public void addToDestroyedCores(int team) {
        destroyedCores.put(team);
    }

    public JSONArray getDestroyedCores() {
        return destroyedCores;
    }

    public void saveDestroyedCores() {
        pluginData.putEntry("destroyed_cores", destroyedCores);
    }

    public void parseDestroyedCores() {
        destroyedCores.clear();
        try {
            destroyedCores = (JSONArray) pluginData.getEntry("destroyed_cores");
        } catch (Exception e) {
            // throw new RuntimeException(e);
        }
    }

    public boolean isCoreDestroyed(int team) {
        if (destroyedCores == null) return false;
        if (destroyedCores.isEmpty()) return false;
        return destroyedCores.toList().contains(team);
    }

    public void teamCoreDestroyed(int team) {
        this.addToDestroyedCores(team);
        saveDestroyedCores();
    }

    @EventHandler
    public void onFatalDamage(EntityDeathEvent event) throws IOException {
        if (event.getEntity() instanceof Player player) {
            if (player.getWorld().getName().equals("ptcoverworld") && isCoreDestroyed(teams.getTeamIndexFromPlayer(player.getName()))) {
                event.setCancelled(true);
                Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<italic:false><yellow>" + player.getName() + " has died!"));
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(Component.text("Your core has been destroyed, so you will no longer respawn."));
                player.showTitle(Title.title(Component.text("YOU DIED!", NamedTextColor.RED),Component.text("You will no longer respawn.", NamedTextColor.GRAY)));
            }
        }
    }
}
