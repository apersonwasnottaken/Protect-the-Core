package com.example.protectTheCore.game.events;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.helper.HelperFunctions;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class AwakeningEvent {

    private ProtectTheCore plugin;

    public AwakeningEvent(@NotNull ProtectTheCore plugin) {
        this.plugin = plugin;
    }

    @Nullable
    public Player getAssassin() {
        return Bukkit.getPlayer("SummersCute");
    }

    @Nullable
    public Player getAssassin(String playerName) {
        return Bukkit.getPlayer(playerName);
    }

    public void fillAssassinGear(Player player, boolean includeTotems) {
        ItemStack[] items = new ItemStack[41];

        // Manually defining all the items
        ItemStack shield = ItemStack.of(Material.SHIELD);
        shield.editMeta(meta -> {
            meta.addEnchant(Enchantment.UNBREAKING, 5, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        });
        ItemStack elytra = ItemStack.of(Material.ELYTRA);
        elytra.editMeta(meta -> {
            meta.addEnchant(Enchantment.UNBREAKING, 5, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        });
        ItemStack netheriteHelmet = ItemStack.of(Material.NETHERITE_HELMET);
        netheriteHelmet.editMeta(meta -> {
            meta.addEnchant(Enchantment.AQUA_AFFINITY, 1, true);
            meta.addEnchant(Enchantment.RESPIRATION, 3, true);
            meta.addEnchant(Enchantment.PROTECTION, 4, true);
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        });
        ItemStack netheriteChestplate = ItemStack.of(Material.NETHERITE_CHESTPLATE);
        netheriteChestplate.editMeta(meta -> {
            meta.addEnchant(Enchantment.PROTECTION, 5, true);
            meta.addEnchant(Enchantment.UNBREAKING, 5, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        });
        ItemStack netheriteLeggings = ItemStack.of(Material.NETHERITE_LEGGINGS);
        netheriteLeggings.editMeta(meta -> {
            meta.addEnchant(Enchantment.PROTECTION, 4, true);
            meta.addEnchant(Enchantment.SWIFT_SNEAK, 3, true);
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        });
        ItemStack netheriteBoots = ItemStack.of(Material.NETHERITE_BOOTS);
        netheriteBoots.editMeta(meta -> {
            meta.addEnchant(Enchantment.PROTECTION, 4, true);
            meta.addEnchant(Enchantment.FEATHER_FALLING, 4, true);
            meta.addEnchant(Enchantment.SOUL_SPEED, 3, true);
            meta.addEnchant(Enchantment.DEPTH_STRIDER, 3, true);
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        });
        ItemStack netheriteSword = ItemStack.of(Material.NETHERITE_SWORD);
        netheriteSword.editMeta(meta -> {
            meta.addEnchant(Enchantment.SHARPNESS, 8, true);
            meta.addEnchant(Enchantment.LOOTING, 3, true);
            meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
            meta.addEnchant(Enchantment.SWEEPING_EDGE, 3, true);
            meta.addEnchant(Enchantment.UNBREAKING, 5, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        });
        ItemStack netheriteSwordKB = ItemStack.of(Material.NETHERITE_SWORD);
        netheriteSwordKB.editMeta(meta -> {
            meta.addEnchant(Enchantment.SHARPNESS, 8, true);
            meta.addEnchant(Enchantment.LOOTING, 3, true);
            meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
            meta.addEnchant(Enchantment.SWEEPING_EDGE, 3, true);
            meta.addEnchant(Enchantment.UNBREAKING, 5, true);
            meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        });
        ItemStack netheriteAxe = ItemStack.of(Material.NETHERITE_AXE);
        netheriteAxe.editMeta(meta -> {
            meta.addEnchant(Enchantment.SHARPNESS, 8, true);
            meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
            meta.addEnchant(Enchantment.SILK_TOUCH, 1, true);
            meta.addEnchant(Enchantment.UNBREAKING, 5, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        });
        ItemStack netheritePickaxe = ItemStack.of(Material.NETHERITE_PICKAXE);
        netheritePickaxe.editMeta(meta -> {
            meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
            meta.addEnchant(Enchantment.SILK_TOUCH, 1, true);
            meta.addEnchant(Enchantment.UNBREAKING, 5, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        });
        ItemStack netheriteShovel = ItemStack.of(Material.NETHERITE_SHOVEL);
        netheriteShovel.editMeta(meta -> {
            meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
            meta.addEnchant(Enchantment.SILK_TOUCH, 1, true);
            meta.addEnchant(Enchantment.UNBREAKING, 5, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        });
        ItemStack netheriteSpear = ItemStack.of(Material.NETHERITE_SPEAR);
        netheriteSpear.editMeta(meta -> {
            meta.addEnchant(Enchantment.SHARPNESS, 8, true);
            meta.addEnchant(Enchantment.LOOTING, 3, true);
            meta.addEnchant(Enchantment.LUNGE, 4, true);
            meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
            meta.addEnchant(Enchantment.KNOCKBACK, 2, true);
            meta.addEnchant(Enchantment.UNBREAKING, 5, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        });
        ItemStack fireworkRocket = ItemStack.of(Material.FIREWORK_ROCKET, 64);
        fireworkRocket.editMeta(meta -> {
            ((FireworkMeta) meta).setPower(3);
            ((FireworkMeta) meta).addEffect(FireworkEffect.builder().trail(true).withColor(Color.RED, Color.BLACK).build());
        });
        ItemStack bundle = ItemStack.of(Material.LIGHT_BLUE_BUNDLE, 1);
        bundle.editMeta(meta -> {
            ((BundleMeta) meta).setItems(List.of(ItemStack.of(Material.ENDER_CHEST, 12)));
        });
        items[40] = shield;

        items[36] = netheriteBoots;
        items[37] = netheriteLeggings;
        items[38] = netheriteChestplate;
        items[39] = netheriteHelmet;

        items[0] = netheriteSwordKB;
        items[1] = getPlayerTracker(player);
        items[2] = ItemStack.of(Material.GOLDEN_APPLE, 64);
        items[3] = netheriteSword;
        items[4] = ItemStack.of(Material.WIND_CHARGE, 64);
        items[5] = ItemStack.of(Material.WATER_BUCKET);
        items[6] = ItemStack.of(Material.COBWEB, 64);
        items[7] = netheriteAxe;
        items[8] = ItemStack.of(Material.ENDER_PEARL, 16);

        items[9] = ItemStack.of(Material.EXPERIENCE_BOTTLE, 64);
        items[10] = ItemStack.of(Material.EXPERIENCE_BOTTLE, 64);
        items[11] = ItemStack.of(Material.COOKED_PORKCHOP, 64);
        items[12] = ItemStack.of(Material.CHERRY_LOG, 64);
        items[13] = includeTotems ? ItemStack.of(Material.TOTEM_OF_UNDYING, 64) : ItemStack.of(Material.AIR);
        items[14] = ItemStack.of(Material.BUCKET, 16);
        items[15] = elytra;
        items[16] = ItemStack.of(Material.BREEZE_ROD, 64);
        items[17] = ItemStack.of(Material.ENDER_PEARL, 16);

        items[18] = ItemStack.of(Material.EXPERIENCE_BOTTLE, 64);
        items[19] = ItemStack.of(Material.EXPERIENCE_BOTTLE, 64);
        items[20] = netheriteSpear;
        items[21] = netheritePickaxe;
        items[22] = includeTotems ? ItemStack.of(Material.TOTEM_OF_UNDYING, 1) : ItemStack.of(Material.AIR);
        items[23] = ItemStack.of(Material.WATER_BUCKET, 1);
        items[24] = fireworkRocket;
        items[25] = bundle;
        items[26] = ItemStack.of(Material.ENDER_PEARL, 16);

        items[27] = ItemStack.of(Material.EXPERIENCE_BOTTLE, 64);
        items[28] = ItemStack.of(Material.EXPERIENCE_BOTTLE, 64);
        items[29] = ItemStack.of(Material.GOLDEN_APPLE, 64);
        items[30] = netheriteShovel;
        items[31] = includeTotems ? ItemStack.of(Material.TOTEM_OF_UNDYING, 1) : ItemStack.of(Material.AIR);
        items[32] = ItemStack.of(Material.WATER_BUCKET, 1);
        items[33] = ItemStack.of(Material.COBWEB, 64);
        items[34] = ItemStack.of(Material.COBWEB, 64);
        items[35] = ItemStack.of(Material.ENDER_PEARL, 16);

        player.getInventory().setContents(items);
    }

    public void initEvent() {
        if (getAssassin() == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(""));
        }
    }

    public ItemStack getPlayerTracker(Player source) {
        ItemStack playerTracker = ItemStack.of(Material.COMPASS);
        playerTracker.editMeta(meta -> {
            meta.setEnchantmentGlintOverride(true);
            meta.displayName(MiniMessage.miniMessage().deserialize("<italic:false><yellow>Player Tracker"));
            meta.lore(List.of(MiniMessage.miniMessage().deserialize("<italic:false><gray>Points towards the nearest player."), MiniMessage.miniMessage().deserialize("<italic:false><dark_gray>Note: Only works for the assassin!")));
            CompassMeta compassMeta = (CompassMeta) meta;
            compassMeta.setLodestone(HelperFunctions.getNearestPlayer(source).getLocation());
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "player_tracker"), PersistentDataType.BOOLEAN, true);
        });
        return playerTracker;
    }

    public int getPlayerTrackerSlotFromPlayer(Player player) {
        for(int i = 0; i < player.getInventory().getContents().length; i++) {
            if (player.getInventory().getContents()[i].getPersistentDataContainer().has(new NamespacedKey(plugin, "player_tracker"))) return i;
        }
        return -1;
    }

    public void awakeningEventLoop(boolean alreadyStarted) {
        if (!alreadyStarted) initEvent();
        new BukkitRunnable() {
            int timer = 0;
            @Override
            public void run() {
                if (getAssassin() == null) return;
                getAssassin().displayName(MiniMessage.miniMessage().deserialize("<obfuscated><bold><dark_red>Assassin<reset>"));
                getAssassin().playerListName(MiniMessage.miniMessage().deserialize("<obfuscated><bold><dark_red>Assassin<reset>"));
                ItemStack playerTracker = getAssassin().getInventory().getItem(getPlayerTrackerSlotFromPlayer(getAssassin()));
                if (playerTracker != null) {
                    playerTracker.editMeta(meta -> {
                        CompassMeta compassMeta = (CompassMeta) meta;
                        compassMeta.setLodestone(HelperFunctions.getNearestPlayer(getAssassin()).getLocation());
                    });
                    if (getPlayerTrackerSlotFromPlayer(Objects.requireNonNull(getAssassin())) >= 0) {
                        Objects.requireNonNull(getAssassin()).getInventory().setItem(getPlayerTrackerSlotFromPlayer(getAssassin()), playerTracker);
                    }
                }
                timer++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
