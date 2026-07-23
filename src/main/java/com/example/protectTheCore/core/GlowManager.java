package com.example.protectTheCore.core;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.menu.teams.TeamCreationMenu;
import com.example.protectTheCore.menu.teams.TeamsMenu;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.TeamMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.ScoreBoardTeamInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.NameTagVisibility;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.CollisionRule;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.OptionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public final class GlowManager implements Listener {

    private final ProtectTheCore plugin;
    private final Teams teams;
    private final TeamCreationMenu teamCreationMenu;

    public GlowManager(@NotNull ProtectTheCore plugin, @NotNull TeamCreationMenu teamCreationMenu) {
        this.plugin = plugin;
        this.teams = plugin.getTeams();
        this.teamCreationMenu = teamCreationMenu;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        refreshAllOnlinePlayers();
    }

    public void shutdown() {
        for (Player target : plugin.getServer().getOnlinePlayers()) {
            for (Player receiver : plugin.getServer().getOnlinePlayers()) {
                sendTeamPacket(receiver, target, TeamAction.REMOVE);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        refreshViewer(event.getPlayer());
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (!onlinePlayer.equals(event.getPlayer())) {
                refreshViewer(onlinePlayer);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player quittingPlayer = event.getPlayer();
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            sendTeamPacket(onlinePlayer, quittingPlayer, TeamAction.REMOVE);
        }
    }

    @EventHandler
    public void onHandSwap(PlayerSwapHandItemsEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> refreshViewer(event.getPlayer()));
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> refreshViewer(event.getPlayer()));
    }

    public void refreshViewer(Player viewer) {
        boolean hasDebugStickInOffhand = viewer.getInventory().getItemInOffHand().getType() == Material.DEBUG_STICK;

        for (Player target : plugin.getServer().getOnlinePlayers()) {
            if (viewer.equals(target)) continue;

            if (hasDebugStickInOffhand) {
                sendTeamPacket(viewer, target, TeamAction.REMOVE);
                sendTeamPacket(viewer, target, TeamAction.CREATE);
                if (!target.isGlowing()) {
                    target.setGlowing(true);
                }
            } else {
                sendTeamPacket(viewer, target, TeamAction.REMOVE);
            }
        }
        checkAndCleanGlobalGlow();
    }

    public void refreshAllOnlinePlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            refreshViewer(player);
        }
    }

    private void checkAndCleanGlobalGlow() {
        boolean anybodyHoldingStick = false;
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.getInventory().getItemInOffHand().getType() == Material.DEBUG_STICK) {
                anybodyHoldingStick = true;
                break;
            }
        }
        if (!anybodyHoldingStick) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.isGlowing()) p.setGlowing(false);
            }
        }
    }

    private void sendTeamPacket(Player receiver, Player target, TeamAction action) {
        String teamName = "fb_" + target.getUniqueId().toString().substring(0, 12);
        WrapperPlayServerTeams packet;

        if (action == TeamAction.REMOVE) {
            packet = new WrapperPlayServerTeams(
                    teamName,
                    TeamMode.REMOVE,
                    Optional.empty(),
                    Collections.emptyList()
            );
        } else {
            NamedTextColor color = getTeamDyeColor(target);
            Collection<String> entities = Collections.singletonList(target.getName());

            ScoreBoardTeamInfo teamInfo = new ScoreBoardTeamInfo(
                    Component.text(teamName),
                    Component.empty(),
                    Component.empty(),
                    NameTagVisibility.ALWAYS,
                    CollisionRule.ALWAYS,
                    color,
                    OptionData.NONE
            );

            packet = new WrapperPlayServerTeams(
                    teamName,
                    TeamMode.CREATE,
                    Optional.of(teamInfo),
                    entities
            );
        }

        PacketEvents.getAPI().getPlayerManager().sendPacket(receiver, packet);
    }

    private enum TeamAction {
        CREATE,
        REMOVE
    }

    private NamedTextColor getTeamDyeColor(Player player) {
        ArrayList<NamedTextColor> dyeColors = new ArrayList<>();
        dyeColors.add(NamedTextColor.RED);
        dyeColors.add(NamedTextColor.GOLD);
        dyeColors.add(NamedTextColor.YELLOW);
        dyeColors.add(NamedTextColor.GREEN);
        dyeColors.add(NamedTextColor.DARK_GREEN);
        dyeColors.add(NamedTextColor.DARK_AQUA);
        dyeColors.add(NamedTextColor.AQUA);
        dyeColors.add(NamedTextColor.DARK_BLUE);
        dyeColors.add(NamedTextColor.DARK_PURPLE);
        dyeColors.add(NamedTextColor.LIGHT_PURPLE);
        dyeColors.add(NamedTextColor.WHITE);
        dyeColors.add(NamedTextColor.GRAY);
        dyeColors.add(NamedTextColor.DARK_GRAY);
        dyeColors.add(NamedTextColor.BLACK);
        dyeColors.add(NamedTextColor.nearestTo(TextColor.color(7489577)));

        int idx = 0;
        try {
            idx = teams.getTeamIndexFromPlayer(player.getName());
        } catch (IOException e) {
            // throw new RuntimeException(e);
        }

        if (idx < 0) return null;

        return dyeColors.get(teamCreationMenu.getTeamColorsInt().indexOf(teams.getTeamColor(idx))
        );
    }
}