package com.example.protectTheCore.game;


import net.kyori.adventure.text.Component;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

import static com.example.protectTheCore.ProtectTheCore.plugin;

// Just decorative for the scoreboard
public class Events {
    private static ArrayList<JSONObject> events = new ArrayList<>();

    public static void parseEvents() throws IOException {
        events.clear();
        try {
            JSONArray teamsData = new JSONArray(Files.readString(Path.of("./plugins/ProtectTheCore/events.json")));
            teamsData.forEach(obj -> events.add((JSONObject) obj));
        } catch (Exception e) {
            events = new ArrayList<>();
        }
    }

    public static ArrayList<JSONObject> getEvents() {
        return events;
    }

    public static void setEvents(ArrayList<JSONObject> config) {
        events = config;
    }

    public static void saveEvents() throws Exception {
        JSONArray combinedData = new JSONArray();
        for (JSONObject jsonObject : events) {
            combinedData.put(jsonObject);
        }
        JSONObject combineData = new JSONObject();
        combineData.put("events", combinedData);
        Files.writeString(Path.of("./plugins/ProtectTheCore/events.json"), combinedData.toString());
    }

    public static void putEvent(String eventName, String duration) {
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

    public static String getName(int eventIdx) {
        return events.get(eventIdx).getString("name");
    }

    public static String getDuration(int eventIdx) {
        return events.get(eventIdx).getString("duration");
    }

    public static int getEventFromName(String eventName) {
        for (int i = 0; i < events.size(); i++) {
            if (Objects.equals(getName(i), eventName)) {
                return i;
            }
        }
        return -1;
    }

    public static LocalDateTime getEventStart(String eventName) {
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

    public static void populateDefaultEvents() {
        putEvent("The End opens", LocalDateTime.now().plusDays(1).toString());
        putEvent("The Awakening", LocalDateTime.now().plusDays(5).toString());
        putEvent("Base building event", LocalDateTime.parse(Objects.requireNonNull(plugin.getConfig().getString("game.time_end"))).minusHours(4).toString());
    }
}
