package com.example.protectTheCore.helper;

import com.example.protectTheCore.ProtectTheCore;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PluginData {
    public JSONObject data;
    private final ProtectTheCore plugin;

    public PluginData(@NotNull ProtectTheCore plugin) {
        this.plugin = plugin;
    }

    public void getData() {
        String dataStr;
        try {
            dataStr = Files.readString(new File(plugin.getDataPath().toFile(), "data.json").toPath());
            data = new JSONObject(dataStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getEntry(String key) {
        getData();
        return data.get(key);
    }

    public void putEntry(String key, Object value) {
        getData();
        data.put(key,value);
    }
}
