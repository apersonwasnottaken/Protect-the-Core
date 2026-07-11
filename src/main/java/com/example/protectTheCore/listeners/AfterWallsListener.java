package com.example.protectTheCore.listeners;

import com.example.protectTheCore.core.Teams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.example.protectTheCore.ProtectTheCore.plugin;

public class AfterWallsListener implements Listener {
    private ArrayList<Integer> destroyedCores = new ArrayList<>();

    public void addToDestroyedCores(int team) {
        destroyedCores.add(team);
    }

    public ArrayList<Integer> getDestroyedCores() {
        return destroyedCores;
    }

    public void saveDestroyedCores() {
        try {
            Files.writeString(Path.of("./plugins/ProtectTheCore/destroyed_cores.txt"), destroyedCores.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void parseDestroyedCores() {
        destroyedCores.clear();
        try {
            Files.createFile(Path.of("./plugins/ProtectTheCore/destroyed_cores.json"));
            destroyedCores = Arrays.stream(Files.readString(Path.of("./plugins/ProtectTheCore/destroyed_cores.json")).substring(1, Files.readString(Path.of("./plugins/ProtectTheCore/destroyed_cores.json")).length() - 2).split(",")).map(Integer::parseInt).collect(Collectors.toCollection(ArrayList::new));
        } catch (Exception e) {
            // throw new RuntimeException(e);
        }
    }

    public boolean isCoreDestroyed(int team) {
        if (destroyedCores == null) return false;
        if (destroyedCores.isEmpty()) return false;
        return destroyedCores.contains(team);
    }

    public void teamCoreDestroyed(int team) {
        this.addToDestroyedCores(team);
        saveDestroyedCores();
    }

    @EventHandler
    public void onFatalDamage(EntityDeathEvent event) throws IOException {
        if (event.getEntity() instanceof Player player) {
            if (player.getWorld().getName().equals("ptcoverworld") && isCoreDestroyed(Teams.getTeamIndexFromPlayer(player.getName()))) {
                event.setCancelled(true);
                Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<italic:false><yellow>" + player.getName() + " has died!"));
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(Component.text("Your core has been destroyed, so you will no longer respawn."));
                player.showTitle(Title.title(Component.text("YOU DIED!", NamedTextColor.RED),Component.text("You will no longer respawn.", NamedTextColor.GRAY)));
            }
        }
    }
}
