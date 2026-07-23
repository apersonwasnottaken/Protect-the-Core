package com.example.protectTheCore.menu.teams;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.menu.CustomMenuHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class TeamsMenu implements CustomMenuHolder {

    private Inventory inventory;
    private final ProtectTheCore plugin;
    private final ComponentLogger logger;
    private final TeamCreationMenu teamCreationMenu;

    private JSONArray readTeamData() throws IOException {
        Path path = Path.of("./plugins/ProtectTheCore/teams.json");
        try {
            return new JSONArray(Files.readString(path));
        }
        catch (IOException e) {
            Files.writeString(path,"[]");
            return new JSONArray();
        } catch (ClassCastException e) {
            logger.error(Component.text("An unexpected error occurred while parsing the teams.json file.\n" + e, NamedTextColor.RED));
            return new JSONArray();
        }
    }

    public TeamsMenu(@NotNull ProtectTheCore plugin, @NotNull ComponentLogger logger, @NotNull TeamCreationMenu teamCreationMenu) {
        this.plugin = plugin;
        this.logger = logger;
        this.teamCreationMenu = teamCreationMenu;
        this.inventory = this.getInventory();
    }
    @Override
    public @NotNull Inventory getInventory() {
        this.inventory = plugin.getServer().createInventory(this, 36, Component.text("ᴛᴇᴀᴍꜱ"));
        try {
            JSONArray teams = readTeamData();
            final int[] i = {0};
            teams.forEach(obj -> {
                try {
                    JSONObject teamInfo = (JSONObject) obj;
                    ArrayList<Material> rainbowConcrete = new ArrayList<>();
                    rainbowConcrete.add(Material.RED_CONCRETE);
                    rainbowConcrete.add(Material.ORANGE_CONCRETE);
                    rainbowConcrete.add(Material.YELLOW_CONCRETE);
                    rainbowConcrete.add(Material.LIME_CONCRETE);
                    rainbowConcrete.add(Material.GREEN_CONCRETE);
                    rainbowConcrete.add(Material.CYAN_CONCRETE);
                    rainbowConcrete.add(Material.LIGHT_BLUE_CONCRETE);
                    rainbowConcrete.add(Material.BLUE_CONCRETE);
                    rainbowConcrete.add(Material.PURPLE_CONCRETE);
                    rainbowConcrete.add(Material.MAGENTA_CONCRETE);
                    rainbowConcrete.add(Material.PINK_CONCRETE);
                    rainbowConcrete.add(Material.WHITE_CONCRETE);
                    rainbowConcrete.add(Material.LIGHT_GRAY_CONCRETE);
                    rainbowConcrete.add(Material.GRAY_CONCRETE);
                    rainbowConcrete.add(Material.BLACK_CONCRETE);
                    rainbowConcrete.add(Material.BROWN_CONCRETE);

                    ItemStack teamNameDialog = ItemStack.of(rainbowConcrete.get(teamCreationMenu.getTeamColorsInt().indexOf(teamInfo.getInt("color"))));
                    teamNameDialog.editMeta(meta -> {
                        meta.displayName(Component.text(teamInfo.getString("name"),TextColor.color(teamInfo.getInt("color"))).decorationIfAbsent(TextDecoration.ITALIC,TextDecoration.State.FALSE));
                        ArrayList<Component> lore = new ArrayList<>();
                        lore.add(Component.text("PvP: ", NamedTextColor.WHITE).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append( teamInfo.getBoolean("pvp") ? Component.text("ON", NamedTextColor.GREEN).decorate(TextDecoration.BOLD) : Component.text("OFF", NamedTextColor.RED).decorate(TextDecoration.BOLD)));
                        lore.add(Component.text(""));
                        lore.add(Component.text("Members (" + ((JSONArray) teamInfo.get("members")).length() + ")", TextColor.color(0, 255, 0)).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        JSONArray members = (JSONArray) teamInfo.get("members");
                        if (members.isEmpty()) {
                            lore.add(Component.text("None!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC,TextDecoration.State.FALSE));
                        }
                        else {
                            members.forEach(member -> {
                                JSONObject memberInfo = (JSONObject) member;
                                lore.add(Component.text(memberInfo.get("username").toString(), NamedTextColor.WHITE).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                            });
                        }
                        lore.add(Component.text(""));
                        lore.add(Component.text("Left-click to manage team.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        lore.add(Component.text("Right-click to delete team.", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        meta.lore(lore);
                    });
                    this.inventory.setItem(i[0], teamNameDialog);
                    i[0]++;
                }
                catch (ClassCastException e) {
                    logger.error(Component.text("An error occurred while trying to parse the teams list! This could potentially be due to malformed data. Please check the teams.json file for corruption.", NamedTextColor.RED));
                    logger.error(Component.text("The error is below: \n" + e, NamedTextColor.RED));
                }
            });
            // Items
            ItemStack addTeam = ItemStack.of(Material.LIME_DYE);
            addTeam.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><green>ᴀᴅᴅ ᴛᴇᴀᴍ</green>")));
            ItemStack blankGrayStainedGlassPane = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
            blankGrayStainedGlassPane.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("")));
            this.inventory.setItem(27, blankGrayStainedGlassPane);
            this.inventory.setItem(28, blankGrayStainedGlassPane);
            this.inventory.setItem(29, blankGrayStainedGlassPane);
            this.inventory.setItem(30, blankGrayStainedGlassPane);
            this.inventory.setItem(31, addTeam);
            this.inventory.setItem(32, blankGrayStainedGlassPane);
            this.inventory.setItem(33, blankGrayStainedGlassPane);
            this.inventory.setItem(34, blankGrayStainedGlassPane);
            this.inventory.setItem(35, blankGrayStainedGlassPane);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this.inventory;
    }
}