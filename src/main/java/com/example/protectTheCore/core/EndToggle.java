package com.example.protectTheCore.core;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

public class EndToggle {

    private final File bukkitConfigFile = new File(Bukkit.getServer().getWorldContainer().getParentFile(), "bukkit.yml");

    public boolean isEndEnabled() {
        if (!bukkitConfigFile.exists()) return true; // Default fallback

        YamlConfiguration config = YamlConfiguration.loadConfiguration(bukkitConfigFile);
        return config.getBoolean("settings.allow-end", true);
    }

    public boolean setEndState(boolean enable) {
        if (!bukkitConfigFile.exists()) {
            return false;
        }

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(bukkitConfigFile);
            config.set("settings.allow-end", enable);
            config.save(bukkitConfigFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
