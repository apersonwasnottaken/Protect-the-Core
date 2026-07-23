package com.example.protectTheCore.core;

import com.example.protectTheCore.ProtectTheCore;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class EndToggle {

    private final ProtectTheCore plugin;
    private final ComponentLogger logger;
    private final File bukkitConfigFile;

    public EndToggle(@NotNull ProtectTheCore plugin, @NotNull ComponentLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.bukkitConfigFile = new File(plugin.getServer().getWorldContainer().getParentFile(), "bukkit.yml");
    }

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
            logger.error(e.toString());
            return false;
        }
    }
}
