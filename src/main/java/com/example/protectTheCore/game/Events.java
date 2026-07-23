package com.example.protectTheCore.game;

import com.example.protectTheCore.helper.PluginData;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

// Just decorative for the scoreboard
public class Events {
    private ArrayList<JSONObject> events = new ArrayList<>();
    private PluginData pluginData;

    public Events (@NotNull PluginData pluginData) {
        this.pluginData = pluginData;
    }

    public void parseEvents() throws IOException {
        events.clear();
        try {
            JSONArray teamsData = new JSONArray(pluginData.getEntry("events"));
            teamsData.forEach(obj -> events.add((JSONObject) obj));
        } catch (Exception e) {
            events = new ArrayList<>();
        }
    }

    public ArrayList<JSONObject> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<JSONObject> config) {
        events = config;
    }

    public void saveEvents() throws Exception {
        JSONArray combinedData = new JSONArray();
        for (JSONObject jsonObject : events) {
            combinedData.put(jsonObject);
        }
        pluginData.putEntry("events", combinedData);
    }

    public void putEvent(String eventName, String duration) {
        JSONObject event = new JSONObject();
        event.put("name", eventName);
        event.put("duration", duration);
        events.add(event);
        try {
            saveEvents();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getName(int eventIdx) {
        return events.get(eventIdx).getString("name");
    }

    public String getDuration(int eventIdx) {
        return events.get(eventIdx).getString("duration");
    }

    public int getEventFromName(String eventName) {
        for (int i = 0; i < events.size(); i++) {
            if (Objects.equals(getName(i), eventName)) {
                return i;
            }
        }
        return -1;
    }

    public LocalDateTime getEventStart(String eventName) {
        try {
            parseEvents();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (getEventFromName(eventName) == -1) {
            return LocalDateTime.now();
        }
        return LocalDateTime.parse(events.get(getEventFromName(eventName)).getString("duration"));
    }

    public void populateDefaultEvents() {
        events.clear();
        putEvent("Election", LocalDateTime.now().plusDays(1).toString());
        putEvent("The Awakening", LocalDateTime.now().plusDays(4).toString());
    }

    public JSONObject getNextEvent() {
        for (JSONObject obj : events) {
            if (!LocalDateTime.parse(obj.getString("duration")).isAfter(LocalDateTime.now())) {
                continue;
            }
            return obj;
        }
        return null;
    }
}
