package com.example.protectTheCore.core;

import com.example.protectTheCore.ProtectTheCore;
import com.example.protectTheCore.helper.WorldGuardHook;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;

import java.io.InputStream;
import java.util.*;

import static com.example.protectTheCore.ProtectTheCore.plugin;

public class EndShop {
    public static void spawnEndShop(Player player, Location loc) {
        StructureManager structureManager = Bukkit.getStructureManager();
        try (InputStream is = plugin.getResource("purpul.nbt")) {
            if (is == null) {
                player.sendMessage("Structure file not found.");
                return;
            }

            Structure structure = structureManager.loadStructure(is);

            structure.place(
                    loc,
                    true,
                    StructureRotation.NONE,
                    Mirror.NONE,
                    0,
                    1.0F,
                    new Random()
            );

            spawnShopNPC(loc.clone().add(5.5, 2, 3.5));

            if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
                Location minBound = new Location(loc.getWorld(), -5, (Objects.requireNonNull(Bukkit.getWorld(new NamespacedKey(plugin, "ptctheend"))).getHighestBlockYAt(-5, -20) - 3), -20);
                Location maxBound = new Location(loc.getWorld(), 5, (Objects.requireNonNull(Bukkit.getWorld(new NamespacedKey(plugin, "ptctheend"))).getHighestBlockYAt(-5, -20) + 2), -8);

                WorldGuardHook.createShopRegion(loc.getWorld(), minBound, maxBound, "end_shop");
            } else {
                ProtectTheCore.logger.warn("WorldGuard was not detected! Unable to protect the end shop.");
            }

            player.sendMessage("Structure spawned!");

        } catch (Exception e) {
            ProtectTheCore.logger.error(e.getMessage());
            player.sendMessage("Failed to load or place the structure.");
        }
    }

    public static void spawnEndShop(Location loc) {
        loc.getWorld().getEntities().stream().filter(entity -> entity.getPersistentDataContainer().has(new NamespacedKey(plugin, "end_trader"))).forEach(Entity::remove);
        StructureManager structureManager = Bukkit.getStructureManager();
        try (InputStream is = plugin.getResource("purpul.nbt")) {
            if (is == null) {
                ProtectTheCore.logger.warn("Structure file not found.");
                return;
            }

            Structure structure = structureManager.loadStructure(is);

            structure.place(
                    loc,
                    true,
                    StructureRotation.NONE,
                    Mirror.NONE,
                    0,
                    1.0F,
                    new Random()
            );

            spawnShopNPC(loc.clone().add(5.5, 2, 3.5));

            if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
                Location minBound = loc.clone().add(-5, (Objects.requireNonNull(Bukkit.getWorld(new NamespacedKey(plugin, "ptctheend"))).getHighestBlockYAt(-5, -20) - 3), -20);
                Location maxBound = loc.clone().add(5, (Objects.requireNonNull(Bukkit.getWorld(new NamespacedKey(plugin, "ptctheend"))).getHighestBlockYAt(-5, -20) + 2), -8);
                WorldGuardHook.createShopRegion(loc.getWorld(), minBound, maxBound, "end_shop");
            } else {
                ProtectTheCore.logger.warn("WorldGuard was not detected! Unable to protect the end shop.");
            }

        } catch (Exception e) {
            ProtectTheCore.logger.error(e.toString());
        }
    }

    public static void spawnShopNPC(Location location) {
        WanderingTrader trader = (WanderingTrader) location.getWorld().spawnEntity(location, EntityType.WANDERING_TRADER);
        trader.setAI(false);
        trader.setDespawnDelay(-1);
        trader.setInvulnerable(true);
        Objects.requireNonNull(trader.getAttribute(Attribute.ARMOR)).setBaseValue(2048);
        Objects.requireNonNull(trader.getAttribute(Attribute.ARMOR_TOUGHNESS)).setBaseValue(2048);
        Objects.requireNonNull(trader.getAttribute(Attribute.GRAVITY)).setBaseValue(0);
        Objects.requireNonNull(trader.getAttribute(Attribute.KNOCKBACK_RESISTANCE)).setBaseValue(1);
        Objects.requireNonNull(trader.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(1024);
        trader.setHealth(1024);
        trader.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 255, false, false, false));
        trader.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 255, false, false, false));
        trader.customName(Component.text("End Trader"));
        trader.getPersistentDataContainer().set(new NamespacedKey(plugin, "end_trader"), PersistentDataType.BOOLEAN, true);
        trader.setRecipes(new ArrayList<>());

        List<MerchantRecipe> trades = new ArrayList<>();
        MerchantRecipe netheriteUpgradeSmithingTemplate = new MerchantRecipe(ItemStack.of(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        netheriteUpgradeSmithingTemplate.addIngredient(ItemStack.of(Material.DIAMOND_BLOCK, 4));
        netheriteUpgradeSmithingTemplate.addIngredient(ItemStack.of(Material.NETHERRACK, 16));

        MerchantRecipe boltArmorTrim = new MerchantRecipe(ItemStack.of(Material.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        boltArmorTrim.addIngredient(ItemStack.of(Material.DIAMOND_BLOCK, 2));
        boltArmorTrim.addIngredient(ItemStack.of(Material.COPPER_BLOCK, 16));

        MerchantRecipe coastArmorTrim = new MerchantRecipe(ItemStack.of(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        coastArmorTrim.addIngredient(ItemStack.of(Material.DIAMOND_BLOCK, 2));
        coastArmorTrim.addIngredient(ItemStack.of(Material.COBBLESTONE, 16));

        MerchantRecipe duneArmorTrim = new MerchantRecipe(ItemStack.of(Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        duneArmorTrim.addIngredient(ItemStack.of(Material.DIAMOND_BLOCK, 2));
        duneArmorTrim.addIngredient(ItemStack.of(Material.SANDSTONE, 16));

        MerchantRecipe eyeArmorTrim = new MerchantRecipe(ItemStack.of(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        eyeArmorTrim.addIngredient(ItemStack.of(Material.ANCIENT_DEBRIS, 2));
        eyeArmorTrim.addIngredient(ItemStack.of(Material.END_STONE, 32));

        MerchantRecipe flowArmorTrim = new MerchantRecipe(ItemStack.of(Material.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        flowArmorTrim.addIngredient(ItemStack.of(Material.DIAMOND_BLOCK, 2));
        flowArmorTrim.addIngredient(ItemStack.of(Material.BREEZE_ROD, 16));

        MerchantRecipe hostArmorTrim = new MerchantRecipe(ItemStack.of(Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        hostArmorTrim.addIngredient(ItemStack.of(Material.DIAMOND_BLOCK, 2));
        hostArmorTrim.addIngredient(ItemStack.of(Material.TERRACOTTA, 16));

        MerchantRecipe raiserArmorTrim = new MerchantRecipe(ItemStack.of(Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        raiserArmorTrim.addIngredient(ItemStack.of(Material.DIAMOND_BLOCK, 2));
        raiserArmorTrim.addIngredient(ItemStack.of(Material.TERRACOTTA, 16));

        MerchantRecipe ribArmorTrim = new MerchantRecipe(ItemStack.of(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        ribArmorTrim.addIngredient(ItemStack.of(Material.DIAMOND_BLOCK, 2));
        ribArmorTrim.addIngredient(ItemStack.of(Material.NETHERRACK, 16));

        MerchantRecipe sentryArmorTrim = new MerchantRecipe(ItemStack.of(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        sentryArmorTrim.addIngredient(ItemStack.of(Material.ANCIENT_DEBRIS, 2));
        sentryArmorTrim.addIngredient(ItemStack.of(Material.COBBLESTONE, 32));

        MerchantRecipe shaperArmorTrim = new MerchantRecipe(ItemStack.of(Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        shaperArmorTrim.addIngredient(ItemStack.of(Material.DIAMOND_BLOCK, 2));
        shaperArmorTrim.addIngredient(ItemStack.of(Material.TERRACOTTA, 16));

        MerchantRecipe silenceArmorTrim = new MerchantRecipe(ItemStack.of(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        silenceArmorTrim.addIngredient(ItemStack.of(Material.NETHERITE_INGOT, 1));
        silenceArmorTrim.addIngredient(ItemStack.of(Material.COBBLED_DEEPSLATE, 64));

        MerchantRecipe snoutArmorTrim = new MerchantRecipe(ItemStack.of(Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        snoutArmorTrim.addIngredient(ItemStack.of(Material.DIAMOND_BLOCK, 2));
        snoutArmorTrim.addIngredient(ItemStack.of(Material.BLACKSTONE, 16));

        MerchantRecipe spireArmorTrim = new MerchantRecipe(ItemStack.of(Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        spireArmorTrim.addIngredient(ItemStack.of(Material.ANCIENT_DEBRIS, 2));
        spireArmorTrim.addIngredient(ItemStack.of(Material.PURPUR_BLOCK, 32));

        MerchantRecipe tideArmorTrim = new MerchantRecipe(ItemStack.of(Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        tideArmorTrim.addIngredient(ItemStack.of(Material.DIAMOND_BLOCK, 2));
        tideArmorTrim.addIngredient(ItemStack.of(Material.PRISMARINE, 16));

        MerchantRecipe vexArmorTrim = new MerchantRecipe(ItemStack.of(Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        vexArmorTrim.addIngredient(ItemStack.of(Material.ANCIENT_DEBRIS, 2));
        vexArmorTrim.addIngredient(ItemStack.of(Material.COBBLESTONE, 32));

        MerchantRecipe wardArmorTrim = new MerchantRecipe(ItemStack.of(Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        wardArmorTrim.addIngredient(ItemStack.of(Material.ANCIENT_DEBRIS, 2));
        wardArmorTrim.addIngredient(ItemStack.of(Material.COBBLED_DEEPSLATE, 32));

        MerchantRecipe wayfinderArmorTrim = new MerchantRecipe(ItemStack.of(Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        wayfinderArmorTrim.addIngredient(ItemStack.of(Material.DIAMOND_BLOCK, 2));
        wayfinderArmorTrim.addIngredient(ItemStack.of(Material.NETHERRACK, 16));

        MerchantRecipe wildArmorTrim = new MerchantRecipe(ItemStack.of(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, 1), Integer.MAX_VALUE);
        wildArmorTrim.addIngredient(ItemStack.of(Material.DIAMOND_BLOCK, 2));
        wildArmorTrim.addIngredient(ItemStack.of(Material.MOSSY_COBBLESTONE, 16));

        MerchantRecipe chorusFruit = new MerchantRecipe(ItemStack.of(Material.CHORUS_FRUIT, 2), Integer.MAX_VALUE);
        chorusFruit.addIngredient(ItemStack.of(Material.AMETHYST_SHARD, 1));

        MerchantRecipe elytra = new MerchantRecipe(ItemStack.of(Material.ELYTRA, 1), Integer.MAX_VALUE);
        elytra.addIngredient(ItemStack.of(Material.PHANTOM_MEMBRANE, 16));
        elytra.addIngredient(ItemStack.of(Material.NETHERITE_SCRAP, 2));

        MerchantRecipe netherWart = new MerchantRecipe(ItemStack.of(Material.NETHER_WART, 1), Integer.MAX_VALUE);
        netherWart.addIngredient(ItemStack.of(Material.NETHER_BRICK, 8));
        netherWart.addIngredient(ItemStack.of(Material.BLAZE_ROD, 4));

        MerchantRecipe shulkerShell = new MerchantRecipe(ItemStack.of(Material.SHULKER_SHELL, 2), Integer.MAX_VALUE);
        shulkerShell.addIngredient(ItemStack.of(Material.CHORUS_FRUIT, 3));
        shulkerShell.addIngredient(ItemStack.of(Material.END_STONE, 1));

        trades.add(netheriteUpgradeSmithingTemplate);
        trades.add(boltArmorTrim);
        trades.add(coastArmorTrim);
        trades.add(duneArmorTrim);
        trades.add(eyeArmorTrim);
        trades.add(flowArmorTrim);
        trades.add(hostArmorTrim);
        trades.add(raiserArmorTrim);
        trades.add(ribArmorTrim);
        trades.add(sentryArmorTrim);
        trades.add(shaperArmorTrim);
        trades.add(silenceArmorTrim);
        trades.add(snoutArmorTrim);
        trades.add(spireArmorTrim);
        trades.add(tideArmorTrim);
        trades.add(vexArmorTrim);
        trades.add(wardArmorTrim);
        trades.add(wayfinderArmorTrim);
        trades.add(wildArmorTrim);
        trades.add(chorusFruit);
        trades.add(elytra);
        trades.add(netherWart);
        trades.add(shulkerShell);

        trader.setRecipes(trades);
    }
}
