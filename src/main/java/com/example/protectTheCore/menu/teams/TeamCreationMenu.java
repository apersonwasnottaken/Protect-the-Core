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
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeamCreationMenu implements CustomMenuHolder {

    private final Inventory inventory;
    private final ProtectTheCore plugin;
    private final ComponentLogger logger;

    public JSONArray readTeamData() throws IOException {
        try {
            Object obj = new JSONParser().parse(new FileReader("./plugins/ProtectTheCore/teams.json"));
            return (JSONArray) obj;
        } catch (IOException e) {
            Files.writeString(Path.of("./plugins/ProtectTheCore/teams.json"), "{}");
            return new JSONArray();
        } catch (ParseException e) {
            Files.writeString(Path.of("./plugins/ProtectTheCore/teams.json"), "{}");
            logger.error(Component.text("An unexpected error occurred while parsing the teams.json file.\n" + e, TextColor.color(255, 0, 0)));
        }
        return new JSONArray();
    }

    public TeamCreationMenu(@NotNull ProtectTheCore plugin, @NotNull ComponentLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.inventory = plugin.getServer().createInventory(this, 9, Component.text("ᴄʀᴇᴀᴛᴇ ᴀ ᴛᴇᴀᴍ"));
    }

    private int previousMaterial = 0;
    private boolean pvpMode = true;
    private String teamName = "";

    public void setTeamName(String name) {
        teamName = name;
    }

    public String getTeamName() {
        return teamName;
    }

    public void togglePvPStatus() {
        pvpMode = !pvpMode;
    }

    public void increasePreviousMaterial() {
        previousMaterial++;
        previousMaterial = previousMaterial % 16;
    }

    public void decreasePreviousMaterial() {
        previousMaterial--;
        if (previousMaterial < 0) {
            previousMaterial = 15;
        }
        previousMaterial = previousMaterial % 16;
    }

    public void resetPreviousMaterial() {
        previousMaterial = 0;
    }

    public int getPreviousMaterial() {
        return previousMaterial;
    }

    public ItemStack getTeamColorItem() {
        ArrayList<Component> lore = new ArrayList<>();
        lore.add(Component.text("Left-click to cycle forwards!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC,TextDecoration.State.FALSE));
        lore.add(Component.text("Right-click to cycle backwards!", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC,TextDecoration.State.FALSE));
        ItemStack teamColorRed = ItemStack.of(Material.RED_DYE);
        teamColorRed.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><red>ʀᴇᴅ</red></bold>"));
            meta.lore(lore);
        });
        ItemStack teamColorOrange = ItemStack.of(Material.ORANGE_DYE);
        teamColorOrange.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><#eb7114>ᴏʀᴀɴɢᴇ</#eb7114></bold>"));
            meta.lore(lore);
        });
        ItemStack teamColorYellow = ItemStack.of(Material.YELLOW_DYE);
        teamColorYellow.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><yellow>ʏᴇʟʟᴏᴡ</yellow></bold>"));
            meta.lore(lore);
        });
        ItemStack teamColorLime = ItemStack.of(Material.LIME_DYE);
        teamColorLime.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><green>ʟɪᴍᴇ</green></bold>"));
            meta.lore(lore);
        });
        ItemStack teamColorGreen = ItemStack.of(Material.GREEN_DYE);
        teamColorGreen.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><dark_green>ɢʀᴇᴇɴ</dark_green></bold>"));
            meta.lore(lore);
        });
        ItemStack teamColorCyan = ItemStack.of(Material.CYAN_DYE);
        teamColorCyan.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><dark_aqua>ᴄʏᴀɴ</dark_aqua></bold>"));
            meta.lore(lore);
        });
        ItemStack teamColorLightBlue = ItemStack.of(Material.LIGHT_BLUE_DYE);
        teamColorLightBlue.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><aqua>ʟɪɢʜᴛ ʙʟᴜᴇ</aqua></bold>"));
            meta.lore(lore);
        });
        ItemStack teamColorBlue = ItemStack.of(Material.BLUE_DYE);
        teamColorBlue.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><blue>ʙʟᴜᴇ</blue></bold>"));
            meta.lore(lore);
        });
        ItemStack teamColorPurple = ItemStack.of(Material.PURPLE_DYE);
        teamColorPurple.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><light_purple>ᴘᴜʀᴘʟᴇ</light_purple></bold>"));
            meta.lore(lore);
        });
        ItemStack teamColorMagenta = ItemStack.of(Material.MAGENTA_DYE);
        teamColorMagenta.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><light_purple>ᴍᴀɢᴇɴᴛᴀ</light_purple></bold>"));
            meta.lore(lore);
        });
        ItemStack teamColorPink = ItemStack.of(Material.PINK_DYE);
        teamColorPink.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><#f38baa>ᴘɪɴᴋ</#f38baa></bold>"));
            meta.lore(lore);
        });
        ItemStack teamColorWhite = ItemStack.of(Material.WHITE_DYE);
        teamColorWhite.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><white>ᴡʜɪᴛᴇ</white></bold>"));
            meta.lore(lore);
        });
        ItemStack teamColorLightGray = ItemStack.of(Material.LIGHT_GRAY_DYE);
        teamColorLightGray.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><gray>ʟɪɢʜᴛ ɢʀᴀʏ</gray></bold>"));
            meta.lore(lore);
        });
        ItemStack teamColorGray = ItemStack.of(Material.GRAY_DYE);
        teamColorGray.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><dark_gray>ɢʀᴀʏ</dark_gray></bold>"));
            meta.lore(lore);
        });
        ItemStack teamColorBlack = ItemStack.of(Material.BLACK_DYE);
        teamColorBlack.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><black>ʙʟᴀᴄᴋ</black></bold>"));
            meta.lore(lore);
        });
        ItemStack teamColorBrown = ItemStack.of(Material.BROWN_DYE);
        teamColorBrown.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴛᴇᴀᴍ ᴄᴏʟᴏʀ: </white><bold><#724829>ʙʀᴏᴡɴ</#724829></bold>"));
            meta.lore(lore);
        });
        ArrayList<ItemStack> teamColors = new ArrayList<>();
        teamColors.add(teamColorRed);
        teamColors.add(teamColorOrange);
        teamColors.add(teamColorYellow);
        teamColors.add(teamColorLime);
        teamColors.add(teamColorGreen);
        teamColors.add(teamColorCyan);
        teamColors.add(teamColorLightBlue);
        teamColors.add(teamColorBlue);
        teamColors.add(teamColorPurple);
        teamColors.add(teamColorMagenta);
        teamColors.add(teamColorPink);
        teamColors.add(teamColorWhite);
        teamColors.add(teamColorLightGray);
        teamColors.add(teamColorGray);
        teamColors.add(teamColorBlack);
        teamColors.add(teamColorBrown);
        return teamColors.get(previousMaterial);
    }

    public boolean getPvPStatus() {
        return pvpMode;
    }

    public ArrayList<Integer> getTeamColorsInt() {
        ArrayList<Integer> teamColors = new ArrayList<>();
        teamColors.add(16733525);
        teamColors.add(15429908);
        teamColors.add(16777045);
        teamColors.add(5635925);
        teamColors.add(43520);
        teamColors.add(43690);
        teamColors.add(5636095);
        teamColors.add(5592575);
        teamColors.add(11141290);
        teamColors.add(16733695);
        teamColors.add(15961002);
        teamColors.add(16777215);
        teamColors.add(11184810);
        teamColors.add(5592405);
        teamColors.add(0);
        teamColors.add(7489577);
        return teamColors;
    }

    public static ArrayList<String> getTeamColorsName() {
        ArrayList<String> teamColorsName = new ArrayList<>();
        teamColorsName.add("red");
        teamColorsName.add("orange");
        teamColorsName.add("yellow");
        teamColorsName.add("lime");
        teamColorsName.add("green");
        teamColorsName.add("cyan");
        teamColorsName.add("light_blue");
        teamColorsName.add("blue");
        teamColorsName.add("purple");
        teamColorsName.add("magenta");
        teamColorsName.add("pink");
        teamColorsName.add("white");
        teamColorsName.add("light_gray");
        teamColorsName.add("gray");
        teamColorsName.add("black");
        teamColorsName.add("brown");
        return teamColorsName;
    }

    public int getTeamColorInt() {
        return getTeamColorsInt().get(previousMaterial);
    }

    public String getTeamColorName() {
        return getTeamColorsName().get(previousMaterial);
    }

    public ItemStack getPvPStatusItem() {
        ItemStack pvp = ItemStack.of(Material.IRON_SWORD);
        pvp.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴘᴠᴘ:</white> " + (pvpMode ? "<bold><green>ᴏɴ</green></bold>" : "<bold><red>ᴏꜰꜰ</red></bold>")));
        });
        return pvp;
    }

    @Override
    public @NotNull Inventory getInventory() {
        ItemStack teamNameDialog = ItemStack.of(Material.OAK_SIGN);
        teamNameDialog.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><yellow>ᴛᴇᴀᴍ ɴᴀᴍᴇ</yellow><white>: " + (teamName.isEmpty() ? "<italic>None!</italic>" : teamName) + "</white>"));
            List<Component> lore = Arrays.asList(
                    Component.text("Left-click to change.", TextColor.color(255, 255, 0)).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                    Component.text("Right-click to clear.", TextColor.color(0, 255, 255)).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            );
            meta.lore(lore);
        });
        ItemStack cancel = ItemStack.of(Material.RED_CONCRETE);
        cancel.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><red>ᴄᴀɴᴄᴇʟ</red>"));
        });
        ItemStack confirm = ItemStack.of(Material.LIME_CONCRETE);
        confirm.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><green>ᴄᴏɴꜰɪʀᴍ</green>"));
        });
        this.inventory.setItem(0, cancel);
        this.inventory.setItem(8, confirm);
        this.inventory.setItem(3, getTeamColorItem());
        this.inventory.setItem(4, teamNameDialog);
        this.inventory.setItem(5, getPvPStatusItem());
        return this.inventory;
    }
}