package com.example.protectTheCore.game;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.core.Teams;
import com.example.protectTheCore.helper.HelperFunctions;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class GameScoreboard {

    private final FileConfiguration config;
    private final Teams teams;
    private final Events events;
    private BossBar bossBar;

    public GameScoreboard(@NotNull FileConfiguration config, @NotNull Teams teams, @NotNull Events events) {
        this.config = config;
        this.teams = teams;
        this.events = events;
        this.bossBar = BossBar.bossBar(
                MiniMessage.miniMessage().deserialize("<white>Time left: </white><red>" + HelperFunctions.parseTimeLeft(config.getInt("game.duration") - config.getInt("game.time_left")) + "</red>"),
                Math.min(1, Math.max(0, ((float) config.getInt("game.time_left")) / ((float) config.getInt("game.duration")))),
                BossBar.Color.RED,
                BossBar.Overlay.NOTCHED_20
        );
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public int getTimer() {
        if (LocalDateTime.now().isAfter(LocalDateTime.parse(Objects.requireNonNull(config.getString("game.time_end"))))) {
            return -1;
        }
        return Math.toIntExact(Duration.between(LocalDateTime.now(), LocalDateTime.parse(Objects.requireNonNull(config.getString("game.time_end")))).toSeconds());
    }

    public void sendPtcBossbar(Player player, int timer, BossBar bossBar) {
        if (timer > 0) {
            Duration duration = Duration.between(LocalDateTime.now(), LocalDateTime.parse(config.getString("game.time_end")));
            Component event = MiniMessage.miniMessage().deserialize(String.format("<yellow>Walls collapse<white>:%02d:%02d:%02d", duration.toHours(),
                    duration.toMinutesPart(),
                    duration.toSecondsPart()));
            JSONObject nextEvent = events.getNextEvent();
            if (nextEvent == null) {
                bossBar.name(MiniMessage.miniMessage().deserialize("<white>The walls are down! Go fight!"));
                bossBar.progress(1);
                return;
            }
            duration = Duration.between(LocalDateTime.now(), LocalDateTime.parse(nextEvent.getString("duration")));
            String eventString = String.format("<yellow>%s<white>: %02d:%02d:%02d",
                    nextEvent.get("name"),
                    duration.toHours(),
                    duration.toMinutesPart(),
                    duration.toSecondsPart());
            event = MiniMessage.miniMessage().deserialize(eventString);
            bossBar.name(event);
            bossBar.progress(Math.max(0, Math.min(1, ((float) timer) / ((float) config.getInt("game.duration")))));
        }
        else {
            bossBar.name(MiniMessage.miniMessage().deserialize("<white>The walls are down! Go fight!"));
            bossBar.progress(1);
        }
        bossBar.color(BossBar.Color.RED);
        player.showBossBar(bossBar);
    }

    public void sendPtcScoreboard(Player player, int timer) throws IOException {
        if (!config.getList("enabled-worlds").contains(player.getWorld().getName())) return;

        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective("ptcscoreboard");

        if (objective == null) {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            scoreboard = manager.getNewScoreboard();
            objective = scoreboard.registerNewObjective("ptcscoreboard", Criteria.DUMMY, Component.text("PROTECT THE CORE"));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            for (int i = 0; i < 15; i++) {
                String entryKey = "§" + Integer.toHexString(i) + "§r";
                Team team = scoreboard.registerNewTeam("line_" + i);
                team.addEntry(entryKey);
            }
            player.setScoreboard(scoreboard);
        }

        String color1 = "#" + Integer.toHexString((int) Math.floor(Math.sin(timer * 0.001) * 100) + 16777045);
        String color2 = "#" + Integer.toHexString((int) Math.floor(Math.sin(-timer * 0.001) * 100) + 15132245);
        objective.displayName(MiniMessage.miniMessage().deserialize("<bold><italic:false><gradient:" + color1 + ":" + color2 + ">PROTECT THE CORE</gradient>"));

        ArrayList<Component> scoreboardLines = new ArrayList<>();
        scoreboardLines.add(Component.text(""));
        Component event = MiniMessage.miniMessage().deserialize("<white>Event soon!");
        try {
            events.saveEvents();
        } catch (Exception e) {
            HelperFunctions.sendErrorMessage(player, e);
            throw new RuntimeException(e);
        }
        /*
        for (JSONObject obj : getEvents()) {
            if (!LocalDateTime.parse(obj.getString("duration")).isAfter(LocalDateTime.now())) {
                continue;
            }
            Duration duration = Duration.between(LocalDateTime.now(), LocalDateTime.parse(obj.getString("duration")));
            String eventString = String.format("<yellow>%s<white>: %02d:%02d:%02d",
                    obj.get("name"),
                    duration.toHours(),
                    duration.toMinutesPart(),
                    duration.toSecondsPart());
            event = MiniMessage.miniMessage().deserialize(eventString);
            break;
        }
        scoreboardLines.add(event);
        scoreboardLines.add(Component.text(""));

        // Rule 1 in coding: If it works, don't touch it.
        scoreboardLines.add(Component.text(Teams.getTeamName(0) + "'s Core", TextColor.color(getTeamColor(0))).decorationIfAbsent(TextDecoration.BOLD, (Teams.getTeamIndexFromPlayer(player.getName()) == 0) ? TextDecoration.State.TRUE : TextDecoration.State.FALSE).append(Component.text(": ", NamedTextColor.WHITE)).append(coreHealthMap.containsKey(0) ? getHealthbarComponent(coreHealthMap.get(0)).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.FALSE) : afterWallsListener.isCoreDestroyed(0) ? Component.text("Destroyed!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.FALSE) : Component.text("Not found!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.FALSE)));
        scoreboardLines.add(Component.text(Teams.getTeamName(1) + "'s Core", TextColor.color(getTeamColor(1))).decorationIfAbsent(TextDecoration.BOLD, (Teams.getTeamIndexFromPlayer(player.getName()) == 1) ? TextDecoration.State.TRUE : TextDecoration.State.FALSE).append(Component.text(": ", NamedTextColor.WHITE)).append(coreHealthMap.containsKey(1) ? getHealthbarComponent(coreHealthMap.get(1)).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.FALSE) : afterWallsListener.isCoreDestroyed(1) ? Component.text("Destroyed!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.FALSE) : Component.text("Not found!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.FALSE)));
        scoreboardLines.add(Teams.getTeamsConfig().size() == 4 ? (Component.text(Teams.getTeamName(2) + "'s Core", TextColor.color(getTeamColor(2))).decorationIfAbsent(TextDecoration.BOLD, (Teams.getTeamIndexFromPlayer(player.getName()) == 2) ? TextDecoration.State.TRUE : TextDecoration.State.FALSE).append(Component.text(": ", NamedTextColor.WHITE)).append(coreHealthMap.containsKey(0) ? getHealthbarComponent(coreHealthMap.get(2)).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.FALSE) : afterWallsListener.isCoreDestroyed(2) ? Component.text("Destroyed!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.FALSE) : Component.text("Not found!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.FALSE))) : Component.text("ignore"));
        scoreboardLines.add(Teams.getTeamsConfig().size() == 4 ? (Component.text(Teams.getTeamName(3) + "'s Core", TextColor.color(getTeamColor(3))).decorationIfAbsent(TextDecoration.BOLD, (Teams.getTeamIndexFromPlayer(player.getName()) == 3) ? TextDecoration.State.TRUE : TextDecoration.State.FALSE).append(Component.text(": ", NamedTextColor.WHITE)).append(coreHealthMap.containsKey(0) ? getHealthbarComponent(coreHealthMap.get(3)).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.FALSE) : afterWallsListener.isCoreDestroyed(3) ? Component.text("Destroyed!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.FALSE) : Component.text("Not found!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.FALSE))) : Component.text("ignore"));

        scoreboardLines.add(Component.text(""));
         */
        scoreboardLines.add(Component.text("Teammates:", NamedTextColor.AQUA));

        teams.getTeamMembers(teams.getTeamIndexFromPlayer(player.getName())).forEach(obj -> {
            try {
                scoreboardLines.add(Component.text("    " + ((JSONObject) obj).getString("username") + (!Bukkit.getOfflinePlayer(((JSONObject) obj).getString("username")).isOnline() ? " (Offline)" : "") + (teams.getTeamLeader(teams.getTeamIndexFromPlayer(player.getName())).equals(((JSONObject) obj).getString("username")) ? " [LEADER]" : ""), Bukkit.getOfflinePlayer(((JSONObject) obj).getString("username")).isOnline() ? NamedTextColor.GREEN : NamedTextColor.GRAY));
            } catch (IOException e) {
                HelperFunctions.sendErrorMessage(player, e);
                throw new RuntimeException(e);
            }
        });
        scoreboardLines.add(Component.text("", NamedTextColor.WHITE));

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
        String formattedTime = now.format(formatter);
        scoreboardLines.add(Component.text(formattedTime, NamedTextColor.DARK_GRAY));

        scoreboardLines.removeIf(component -> Objects.equals(component, Component.text("ignore")));

        for (int i = 0; i < 15; i++) {
            Team team = scoreboard.getTeam("line_" + i);
            if (team == null) continue;

            String entryKey = "§" + Integer.toHexString(i) + "§r";

            if (i < scoreboardLines.size()) {
                team.prefix(scoreboardLines.get(i));

                Score score = objective.getScore(entryKey);
                score.setScore(scoreboardLines.size() - i);

                score.numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat.fixed(Component.empty()));
            } else {
                team.prefix(Component.empty());
                scoreboard.resetScores(entryKey);
            }
        }
    }
}
