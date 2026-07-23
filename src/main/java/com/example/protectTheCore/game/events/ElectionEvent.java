package com.example.protectTheCore.game.events;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.core.Teams;
import com.example.protectTheCore.game.ProtectTheCoreGame;
import com.example.protectTheCore.helper.HelperFunctions;
import com.example.protectTheCore.helper.PluginData;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ElectionEvent {

    // Format: {votes:[{"candidate1":1},{"candidate2":2}],voted:["voter1","voter2","voter3"]}

    private ArrayList<String> voted = new ArrayList<>();
    private JSONArray sortedVoteResults = new JSONArray();
    private final ProtectTheCore plugin;
    private final Teams teams;
    private final ProtectTheCoreGame protectTheCoreGame;
    private final PluginData pluginData;

    public ElectionEvent(@NotNull ProtectTheCore plugin, @NotNull Teams teams, @NotNull ProtectTheCoreGame protectTheCoreGame, @NotNull PluginData pluginData) {
        this.plugin = plugin;
        this.teams = teams;
        this.protectTheCoreGame = protectTheCoreGame;
        this.pluginData = pluginData;
    }

    public void parseVotes() {
        voted.clear();
        try {
            JSONObject teamsData = new JSONObject(pluginData.getEntry("votes"));
            for (int i = 0; i < teamsData.getJSONArray("voted").length(); i++) {
                voted.add(i, teamsData.getJSONArray("voted").getString(i));
            }
        } catch (Exception e) {
            voted = new ArrayList<>();
        }
    }

    public JSONArray getVotes() {
        return sortedVoteResults;
    }

    public void setVotes(JSONArray config) {
        sortedVoteResults = config;
    }

    public void saveVotes() throws Exception {
        JSONArray combinedVotes = new JSONArray();
        for (String voter : voted) {
            combinedVotes.put(voter);
        }
        JSONObject combineData = new JSONObject();
        combineData.put("voted", combinedVotes);
        combineData.put("votes", sortedVoteResults);
        pluginData.putEntry("votes", combineData);
    }

    public void putVote(String voter, String candidate) {
        if (voted.contains(voter)) {
            Objects.requireNonNull(plugin.getServer().getPlayer(voter)).sendMessage(MiniMessage.miniMessage().deserialize("<red>You already voted for someone!"));
            return;
        }
        try {
            if (teams.getTeamIndexFromPlayer(voter) != teams.getTeamIndexFromPlayer(candidate)) {
                Objects.requireNonNull(plugin.getServer().getPlayer(voter)).sendMessage(MiniMessage.miniMessage().deserialize("<red>You can only vote for members of your team!"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (Objects.equals(voter, candidate)) {
            Objects.requireNonNull(plugin.getServer().getPlayer(voter)).sendMessage(MiniMessage.miniMessage().deserialize("<red>You cannot vote for yourself!"));
        }
        voted.add(voter);
        if (sortedVoteResults.isEmpty()) {
            sortedVoteResults.put(new JSONObject("{\"" + candidate + "\",1}"));
            return;
        }
        sortedVoteResults.forEach(obj -> {
            JSONObject jsonObject = (JSONObject) obj;
            if (!jsonObject.has(candidate)) {
                sortedVoteResults.put(new JSONObject("{\"" + candidate + "\",1}"));
            }
            else {
                if (Objects.equals(jsonObject.keys().next(), candidate)) {
                    sortedVoteResults.put(new JSONObject().put(candidate, jsonObject.getInt(candidate) + 1));
                }
            }
        });
        sortedVoteResults = HelperFunctions.sortJSONArrayByValue(sortedVoteResults, true);
        try {
            if (checkIfAllTeamMembersVoted(teams.getTeamIndexFromPlayer(voter))) {
                String teamLeader = "";
                for (Object candidates : sortedVoteResults) {
                    if (teams.getTeamIndexFromPlayer(((JSONObject) candidates).keys().next()) == teams.getTeamIndexFromPlayer(voter)) {
                        teamLeader = ((JSONObject) candidates).keys().next();
                    }
                }
                String finalTeamLeader = teamLeader;
                if (plugin.getServer().getPlayer(teamLeader).isOnline()) {
                    Objects.requireNonNull(plugin.getServer().getPlayer(teamLeader)).give(protectTheCoreGame.getCrown(teams.getTeamIndexFromPlayer(teamLeader), teamLeader));
                }
                else {
                    Objects.requireNonNull(plugin.getServer().getOfflinePlayer(teamLeader).getPlayer()).give(protectTheCoreGame.getCrown(teams.getTeamIndexFromPlayer(teamLeader), teamLeader));
                }
                teams.getTeamMembers(teams.getTeamIndexFromPlayer(voter)).forEach(member -> {
                    if (plugin.getServer().getPlayer(((JSONObject) member).getString("username")).isOnline()) {
                        plugin.getServer().getPlayer(((JSONObject) member).getString("username")).sendMessage(MiniMessage.miniMessage().deserialize("<green>All members of your team have decided to elect " + finalTeamLeader + " as the team leader.\n<red>The Nether</red> is now opened for your team."));
                    }
                });
            }
            saveVotes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkIfAllTeamMembersVoted(int teamIdx) {
        AtomicInteger count = new AtomicInteger();
        voted.forEach(member -> {
            try {
                if (teams.getTeamIndexFromPlayer(member) == teamIdx) {
                    count.getAndIncrement();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return teams.getTeamMembers(teamIdx).length() == count.get();
    }

    public void startElection(boolean alreadyStarted) {
        if (alreadyStarted) return;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            player.sendMessage(MiniMessage.miniMessage().deserialize("""
                                  <bold><color:yellow>ELECTION<reset>
Each member of your team must vote for one person to be their team leader.
The leader will get buffed, but will weaken the entire team if killed.
All votes are final! There is no turning back once you vote for someone.
The nether will be unlocked once all members of your team has voted.
"""));
        }
    }

    public JSONArray getSortedVoteResults() {
        return sortedVoteResults;
    }

    public void setSortedVoteResults(JSONArray voteResults) {
        sortedVoteResults = voteResults;
    }

    public void collectVoteResults() {
        sortedVoteResults.forEach(obj -> {
            JSONObject jsonObject = (JSONObject) obj;
            String candidateName = jsonObject.keys().next();
        });
        sortedVoteResults = HelperFunctions.sortJSONArrayByValue(sortedVoteResults, true);
    }
}
