package com.example.protectTheCore.menu.teams;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.example.protectTheCore.core.Teams;
import com.example.protectTheCore.menu.CustomMenuHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static com.example.protectTheCore.ProtectTheCore.plugin;

public class ManageTeamMenu implements CustomMenuHolder {

    private int teamIdx = 0;

    @Override
    public @NotNull Inventory getInventory() {
        Inventory inventory = plugin.getServer().createInventory(this, 36, Component.text("ᴍᴀɴᴀɢᴇ ᴛᴇᴀᴍ"));
        try {
            JSONArray teamData = TeamCreationMenu.readTeamData();
            JSONObject selectedTeam = (JSONObject) teamData.get(teamIdx);
            setPreviousMaterial(TeamCreationMenu.getTeamColorsInt().indexOf(Integer.parseInt(selectedTeam.get("color").toString())));
            setPvpStatus((Boolean) selectedTeam.get("pvp"));
            final int[] idx = {0};
            JSONArray members = (JSONArray) selectedTeam.get("members");
            if (members != null) {
                for (Object obj : members) {
                    ItemStack playerHead = ItemStack.of(Material.PLAYER_HEAD);
                    playerHead.editMeta(SkullMeta.class, meta -> {
                        PlayerProfile profile = Bukkit.createProfile(UUID.fromString(((JSONObject) obj).get("uuid").toString()), ((JSONObject) obj).get("username").toString());
                        profile.complete(true);
                        meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>" + ((JSONObject) obj).get("username").toString() + "</white>" + (Objects.equals(Teams.getTeamLeader(teamIdx), ((JSONObject) obj).get("username").toString()) ? " <yellow><Leader></yellow>" : "")));
                        meta.setPlayerProfile(profile);
                        ArrayList<Component> lore = new ArrayList<>();
                        lore.add(Component.text("Left-click to toggle team leader!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        lore.add(Component.text("Right-click to remove!", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        meta.lore(lore);
                    });
                    inventory.setItem(idx[0], playerHead);
                    idx[0] = idx[0] + 1;
                }
            }
            ItemStack addTeamMember = ItemStack.of(Material.LIME_DYE);
            addTeamMember.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><green>ᴀᴅᴅ ᴛᴇᴀᴍ ᴍᴇᴍʙᴇʀ</green>")));
            inventory.setItem(idx[0], addTeamMember);

            TeamCreationMenu teamCreationMenu = new TeamCreationMenu();
            teamCreationMenu.resetPreviousMaterial();
            ItemStack blankGrayStainedGlassPane = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
            blankGrayStainedGlassPane.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false>")));
            ItemStack back = ItemStack.of(Material.SPECTRAL_ARROW);
            back.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false>ʙᴀᴄᴋ")));
            ItemStack editTeamName = ItemStack.of(Material.OAK_SIGN);
            editTeamName.editMeta(meta -> {
                meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><aqua>ᴄʜᴀɴɢᴇ ᴛᴇᴀᴍ ɴᴀᴍᴇ"));
            });
            inventory.setItem(27, back);
            inventory.setItem(28, blankGrayStainedGlassPane);
            inventory.setItem(29, blankGrayStainedGlassPane);
            inventory.setItem(30, getTeamColorItemConcretePowder(getPreviousMaterial()));
            inventory.setItem(31, getPvPStatusItem());
            inventory.setItem(32, editTeamName);
            inventory.setItem(33, blankGrayStainedGlassPane);
            inventory.setItem(34, blankGrayStainedGlassPane);
            inventory.setItem(35, blankGrayStainedGlassPane);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return inventory;
    }

    private int previousMaterial = 0;
    private boolean pvpMode = true;

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

    public int getPreviousMaterial() {
        return previousMaterial;
    }

    public void setPreviousMaterial(int input) {
        previousMaterial = input < 0 ? 0 : input % 16;
    }

    public ItemStack getTeamColorItemConcretePowder(int previousMaterial) {
        ArrayList<Material> rainbowConcretePowder = new ArrayList<>();
        rainbowConcretePowder.add(Material.RED_CONCRETE_POWDER);
        rainbowConcretePowder.add(Material.ORANGE_CONCRETE_POWDER);
        rainbowConcretePowder.add(Material.YELLOW_CONCRETE_POWDER);
        rainbowConcretePowder.add(Material.LIME_CONCRETE_POWDER);
        rainbowConcretePowder.add(Material.GREEN_CONCRETE_POWDER);
        rainbowConcretePowder.add(Material.CYAN_CONCRETE_POWDER);
        rainbowConcretePowder.add(Material.LIGHT_BLUE_CONCRETE_POWDER);
        rainbowConcretePowder.add(Material.BLUE_CONCRETE_POWDER);
        rainbowConcretePowder.add(Material.PURPLE_CONCRETE_POWDER);
        rainbowConcretePowder.add(Material.MAGENTA_CONCRETE_POWDER);
        rainbowConcretePowder.add(Material.PINK_CONCRETE_POWDER);
        rainbowConcretePowder.add(Material.WHITE_CONCRETE_POWDER);
        rainbowConcretePowder.add(Material.GRAY_CONCRETE_POWDER);
        rainbowConcretePowder.add(Material.LIGHT_GRAY_CONCRETE_POWDER);
        rainbowConcretePowder.add(Material.BLACK_CONCRETE_POWDER);
        rainbowConcretePowder.add(Material.BROWN_CONCRETE_POWDER);
        ItemStack teamColorItem = ItemStack.of(rainbowConcretePowder.get(previousMaterial));
        teamColorItem.setItemMeta(getTeamColorItem().getItemMeta());
        return teamColorItem;
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

    public int getTeamIdx() {
        return teamIdx;
    }

    public void setTeamIdx(int teamIdx) {
        this.teamIdx = teamIdx;
    }

    public ItemStack getPvPStatusItem() {
        ItemStack pvp = ItemStack.of(Material.IRON_SWORD);
        pvp.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><white>ᴘᴠᴘ:</white> " + (pvpMode ? "<bold><green>ᴏɴ</green></bold>" : "<bold><red>ᴏꜰꜰ</red></bold>"))));
        return pvp;
    }

    public boolean getPvPStatus() {
        return pvpMode;
    }

    public void setPvpStatus(boolean input) {
        pvpMode = input;
    }
}

