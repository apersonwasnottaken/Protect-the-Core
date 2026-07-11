package com.example.protectTheCore.game.wall;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class WallProtectionListener implements Listener {

    private final WallManager wallManager;
    private final Plugin plugin;

    public WallProtectionListener(WallManager wallManager, Plugin plugin) {
        this.wallManager = wallManager;
        this.plugin = plugin;
        startEntityTickLoop();
    }

    private void startEntityTickLoop() {
        new BukkitRunnable() {
            private int tickCount = 0;
            @Override
            public void run() {
                tickCount++;
                WallManager.WallMode mode = wallManager.getCurrentMode();
                if (mode == WallManager.WallMode.NONE) return;
                for (World world : Bukkit.getWorlds()) {
                    if (isWallActive(world)) continue;
                    for (Entity entity : world.getEntitiesByClasses(
                            Item.class,
                            TNTPrimed.class,
                            Projectile.class,
                            LivingEntity.class,
                            Vehicle.class,
                            EyeOfEnder.class,
                            Firework.class)) {
                        if (entity instanceof Player player) {
                            if (player.isOp() || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) continue;
                        }
                        if (entity instanceof Vehicle vehicle) {
                            boolean bypass = vehicle.getPassengers().stream().anyMatch(entity1 ->
                                    entity1 instanceof Player player2 &&
                                            (player2.isOp() || player2.getGameMode() == GameMode.CREATIVE || player2.getGameMode() == GameMode.SPECTATOR));
                            if (bypass) continue;
                        }
                        Location loc = entity.getLocation();
                        Vector velocity = entity.getVelocity();
                        Location prevLoc = loc.clone().subtract(velocity);
                        boolean collided = false;
                        if (isInsideWall(entity, mode)) {
                            collided = true;
                        } else {
                            if (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) {
                                if ((prevLoc.getX() >= 1.0 && loc.getX() <= -1.0) || (prevLoc.getX() <= -1.0 && loc.getX() >= 1.0)) {
                                    collided = true;
                                }
                            }
                            if (!collided && (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH)) {
                                if ((prevLoc.getZ() >= 1.0 && loc.getZ() <= -1.0) || (prevLoc.getZ() <= -1.0 && loc.getZ() >= 1.0)) {
                                    collided = true;
                                }
                            }
                        }
                        if (!collided) continue;
                        if (entity instanceof Player player && tickCount % 10 == 0) {
                            player.damage(1.0, DamageSource.builder(DamageType.IN_WALL).build());
                        }
                        handleEntityWallCollision(entity, mode);
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void handleEntityWallCollision(Entity entity, WallManager.WallMode mode) {
        Location loc = entity.getLocation();
        if (isWallActive(loc.getWorld())) return;
        Vector velocity = entity.getVelocity();
        Location prevLoc = loc.clone().subtract(velocity);
        BoundingBox box = entity.getBoundingBox();
        double halfWidth = Math.max(
                (box.getMaxX() - box.getMinX()) / 2.0,
                (box.getMaxZ() - box.getMinZ()) / 2.0
        );
        Location ejectLoc = getSafeEjectLocation(prevLoc, loc, mode, halfWidth);
        if (entity instanceof EyeOfEnder eye) {
            eye.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        } else if (entity instanceof WitherSkull skull) {
            skull.remove();
            loc.getWorld().spawnParticle(Particle.BLOCK, loc, 8, Material.SOUL_SAND.createBlockData());
            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_HURT, 1.0f, 1.2f);
        } else if (entity instanceof Firework fw) {
            Vector vel = velocity.clone();
            if ((mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH)
                    && (loc.getBlockX() == -1 || loc.getBlockX() == 0)) vel.setX(-vel.getX());
            if ((mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH)
                    && (loc.getBlockZ() == -1 || loc.getBlockZ() == 0)) vel.setZ(-vel.getZ());
            fw.teleport(ejectLoc);
            fw.setVelocity(vel);
        } else if (entity.getType() == EntityType.WIND_CHARGE || entity.getType() == EntityType.BREEZE_WIND_CHARGE) {
            if (entity instanceof Projectile proj) {
                breakEffectProjectile(proj, ejectLoc);
            } else {
                entity.remove();
            }
        } else if (entity instanceof Fireball fireball) {
            fireball.remove();
            World world = ejectLoc.getWorld();
            if (world != null) {
                world.spawnParticle(Particle.EXPLOSION, ejectLoc, 1);
                world.playSound(ejectLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                Entity shooter = (fireball.getShooter() instanceof Entity) ? (Entity) fireball.getShooter() : null;
                float yield = fireball.getYield();
                world.createExplosion(ejectLoc, yield, false, false, shooter);
            }
        } else if (entity instanceof Trident trident) {
            ItemStack item = trident.getItemStack().clone();
            trident.remove();
            if (trident.getShooter() instanceof Player shooter) {
                Map<Integer, ItemStack> leftover = shooter.getInventory().addItem(item);
                leftover.values().forEach(dropped -> shooter.getWorld().dropItemNaturally(ejectLoc, dropped));
                shooter.sendMessage(Component.text("Your ").append(Component.text("Trident", NamedTextColor.YELLOW)).append(Component.text(" was blocked by the wall.", NamedTextColor.RED)));
            } else {
                loc.getWorld().dropItemNaturally(ejectLoc, item);
            }
            loc.getWorld().spawnParticle(Particle.BLOCK, ejectLoc, 6, Material.STONE.createBlockData());
            loc.getWorld().playSound(ejectLoc, Sound.BLOCK_STONE_HIT, 1.0f, 1.0f);
        } else if (entity instanceof AbstractArrow arrow) {
            arrow.remove();
            if (arrow.getShooter() instanceof Player shooter) {
                boolean infinity = arrow instanceof Arrow a && !(arrow instanceof SpectralArrow)
                        && a.getWeapon() != null && a.getWeapon().getType() == Material.BOW
                        && a.getWeapon().containsEnchantment(Enchantment.INFINITY);
                if (!infinity) {
                    Material mat = (arrow instanceof SpectralArrow) ? Material.SPECTRAL_ARROW : Material.ARROW;
                    Map<Integer, ItemStack> leftover = shooter.getInventory().addItem(ItemStack.of(mat, 1));
                    leftover.values().forEach(dropped -> shooter.getWorld().dropItemNaturally(ejectLoc, dropped));
                    shooter.sendMessage(Component.text("Your ").append(Component.text(formatItemName(mat), NamedTextColor.YELLOW)).append(Component.text(" was blocked by the wall.", NamedTextColor.RED)));
                } else {
                    shooter.sendMessage(Component.text("Your shot was blocked by the wall.", NamedTextColor.RED));
                }
            } else if (arrow.getShooter() instanceof BlockProjectileSource source) {
                Material mat = (arrow instanceof SpectralArrow) ? Material.SPECTRAL_ARROW : Material.ARROW;
                Block dispenserBlock = source.getBlock();
                if (dispenserBlock.getState() instanceof Dispenser dispenser) {
                    Map<Integer, ItemStack> leftover = dispenser.getInventory().addItem(ItemStack.of(mat, 1));
                    if (!leftover.isEmpty()) {
                        BlockFace facing = (dispenserBlock.getBlockData() instanceof Directional direction) ? direction.getFacing() : BlockFace.NORTH;
                        Location dropLoc = dispenserBlock.getRelative(facing).getLocation().add(0.5, 0.5, 0.5);
                        leftover.values().forEach(item -> dispenserBlock.getWorld().dropItemNaturally(dropLoc, item));
                    }
                }
            }
            loc.getWorld().spawnParticle(Particle.BLOCK, ejectLoc, 6, Material.BEDROCK.createBlockData());
            loc.getWorld().playSound(ejectLoc, Sound.BLOCK_STONE_HIT, 1.0f, 1.0f);
        } else if (entity instanceof Projectile proj && !isEffectProjectile(proj)) {
            proj.remove();
            loc.getWorld().spawnParticle(Particle.BLOCK, ejectLoc, 6, Material.BEDROCK.createBlockData());
            loc.getWorld().playSound(ejectLoc, Sound.BLOCK_STONE_HIT, 1.0f, 1.0f);
        } else if (entity instanceof TNTPrimed) {
            handleTntCollision(entity, loc, velocity, ejectLoc, mode);
        } else if (entity instanceof Vehicle vehicle) {
            handleVehicleCollision(vehicle, loc, prevLoc, mode);
        } else if (entity instanceof Item item) {
            Vector reflected = velocity.clone();
            if ((mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH)
                    && (loc.getBlockX() == -1 || loc.getBlockX() == 0)) {
                reflected.setX(-Math.abs(reflected.getX()) * (ejectLoc.getX() < 0 ? 1 : -1));
            }
            if ((mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH)
                    && (loc.getBlockZ() == -1 || loc.getBlockZ() == 0)) {
                reflected.setZ(-Math.abs(reflected.getZ()) * (ejectLoc.getZ() < 0 ? 1 : -1));
            }
            item.teleport(new Location(ejectLoc.getWorld(), ejectLoc.getX(), loc.getY(), ejectLoc.getZ(), loc.getYaw(), loc.getPitch()));
            item.setVelocity(reflected);
        } else if (entity instanceof Mob mob) {
            mob.getPathfinder().stopPathfinding();
            Vector push = velocity.clone();
            if ((mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) && (loc.getBlockX() == -1 || loc.getBlockX() == 0)) {
                push.setX(loc.getX() >= 0.0 ? 0.25 : -0.25);
            }
            if ((mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH) && (loc.getBlockZ() == -1 || loc.getBlockZ() == 0)) {
                push.setZ(loc.getZ() >= 0.0 ? 0.25 : -0.25);
            }
            mob.setVelocity(push);
            mob.teleport(new Location(ejectLoc.getWorld(), ejectLoc.getX(), loc.getY(), ejectLoc.getZ(), loc.getYaw(), loc.getPitch()));
        } else if (entity instanceof LivingEntity) {
            Vector push = velocity.clone();
            if ((mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) && (loc.getBlockX() == -1 || loc.getBlockX() == 0)) {
                push.setX(loc.getX() >= 0.0 ? 0.25 : -0.25);
            }
            if ((mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH) && (loc.getBlockZ() == -1 || loc.getBlockZ() == 0)) {
                push.setZ(loc.getZ() >= 0.0 ? 0.25 : -0.25);
            }
            entity.setVelocity(push);
            entity.teleport(new Location(ejectLoc.getWorld(), ejectLoc.getX(), loc.getY(), ejectLoc.getZ(), loc.getYaw(), loc.getPitch()));
        }
    }

    private void handleTntCollision(Entity tnt, Location loc, Vector velocity, Location ejectLoc, WallManager.WallMode mode) {
        if (isWallActive(loc.getWorld())) return;
        if (velocity.lengthSquared() <= 0.01) return;
        Vector bounceVel = velocity.clone();
        if ((mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH)
                && (loc.getBlockX() == -1 || loc.getBlockX() == 0)) {
            bounceVel.setX(-bounceVel.getX() * 0.4);
        }
        if ((mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH)
                && (loc.getBlockZ() == -1 || loc.getBlockZ() == 0)) {
            bounceVel.setZ(-bounceVel.getZ() * 0.4);
        }
        tnt.setVelocity(bounceVel);
        tnt.teleport(ejectLoc);
    }

    private void handleVehicleCollision(Vehicle vehicle, Location loc, Location prevLoc, WallManager.WallMode mode) {
        if (isWallActive(loc.getWorld())) return;
        vehicle.setVelocity(new Vector(0, vehicle.getVelocity().getY(), 0));
        double cushionX = loc.getX();
        double cushionZ = loc.getZ();
        if (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) {
            if (loc.getBlockX() == -1 || loc.getBlockX() == 0) {
                cushionX = (prevLoc.getX() > 0) ? 0.8 : -1.8;
            }
        }
        if (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH) {
            if (loc.getBlockZ() == -1 || loc.getBlockZ() == 0) {
                cushionZ = (prevLoc.getZ() > 0) ? 0.8 : -1.8;
            }
        }
        Location finalEject = new Location(loc.getWorld(), cushionX, loc.getY(), cushionZ, loc.getYaw(), loc.getPitch());
        vehicle.teleport(finalEject);
        for (Entity passenger : vehicle.getPassengers()) {
            passenger.teleport(finalEject);
            if (passenger instanceof Player) {
                passenger.sendMessage(Component.text("Nice try, but I already thought of that.", NamedTextColor.AQUA));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChunkReceive(PlayerChunkLoadEvent event) {
        Player player = event.getPlayer();
        int cx = event.getChunk().getX();
        int cz = event.getChunk().getZ();
        String worldName = event.getChunk().getWorld().getName();
        boolean worldEnabled = wallManager.isWorldEnabled(worldName);
        WallManager.WallMode mode = wallManager.getCurrentMode();
        if (!wallManager.isWallChunk(cx, cz, mode)) return;
        if (!worldEnabled || mode == WallManager.WallMode.NONE) return;

        sendWallChunkToPlayer(player, event.getChunk().getWorld(), cx, cz, 3L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        sendWallChunksToPlayer(event.getPlayer(), event.getPlayer().getWorld(), 20L);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        sendWallChunksToPlayer(event.getPlayer(), event.getPlayer().getWorld(), 10L);
    }

    private void sendWallChunksToPlayer(Player player, World world, long delayTicks) {
        WallManager.WallMode mode = wallManager.getCurrentMode();
        if (mode == WallManager.WallMode.NONE) return;
        for (Chunk chunk : world.getLoadedChunks()) {
            int cx = chunk.getX();
            int cz = chunk.getZ();
            if (wallManager.isWallChunk(cx, cz, mode)) {
                sendWallChunkToPlayer(player, world, cx, cz, delayTicks);
            }
        }
    }

    private void sendWallChunkToPlayer(Player player, World world, int cx, int cz, long delayTicks) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !player.getWorld().equals(world)) return;
                try {
                    ServerPlayer nmsPlayer =
                            ((CraftPlayer) player).getHandle();
                    ClientboundLevelChunkWithLightPacket pkt = getClientboundLevelChunkWithLightPacket();
                    nmsPlayer.connection.send(pkt);
                } catch (Exception e) {
                    plugin.getLogger().warning("[WallManager] sendWallChunkToPlayer failed for "
                            + player.getName() + " at " + cx + "," + cz + ": " + e.getMessage());
                }
                boolean worldEnabled = wallManager.isWorldEnabled(world.getName());
                WallManager.WallMode mode = wallManager.getCurrentMode();
                if (!worldEnabled || mode == WallManager.WallMode.NONE) return;
                int minSection = world.getMinHeight() >> 4;
                int maxSection = world.getMaxHeight() >> 4;
                for (int sy = minSection; sy < maxSection; sy++) {
                    wallManager.updateChunkSectionVisual(player, cx, cz, sy, mode);
                }
            }

            private @NotNull ClientboundLevelChunkWithLightPacket getClientboundLevelChunkWithLightPacket() {
                ServerLevel nmsLevel =
                        ((org.bukkit.craftbukkit.CraftWorld) world).getHandle();
                LevelChunk chunk = nmsLevel.getChunk(cx, cz);
                return new ClientboundLevelChunkWithLightPacket(
                        chunk, nmsLevel.getLightEngine(), null, null);
            }
        }.runTaskLater(plugin, delayTicks);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleClipPrevent(VehicleMoveEvent event) {
        if (isWallActive(event.getVehicle().getLocation().getWorld())) return;
        WallManager.WallMode mode = wallManager.getCurrentMode();
        if (mode == WallManager.WallMode.NONE) return;
        Vehicle vehicle = event.getVehicle();
        if (vehicle.getPassengers().stream().anyMatch(entity -> entity instanceof Player)) return;
        Location to = event.getTo();
        Location from = event.getFrom();
        Vector velocity = vehicle.getVelocity();
        BoundingBox predictedBox = vehicle.getBoundingBox().clone().expand(velocity.getX(), 0, velocity.getZ());
        boolean overlapX = (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) && (predictedBox.getMinX() < 1.0 && predictedBox.getMaxX() > -1.0);
        boolean overlapZ = (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH) && (predictedBox.getMinZ() < 1.0 && predictedBox.getMaxZ() > -1.0);
        if (!overlapX && !overlapZ) return;
        vehicle.setVelocity(new Vector(0, velocity.getY(), 0));
        double cushionX = (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) ? ((from.getX() > 0) ? 0.8 : -1.8) : to.getX();
        double cushionZ = (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH) ? ((from.getZ() > 0) ? 0.8 : -1.8) : to.getZ();
        vehicle.teleport(new Location(to.getWorld(), cushionX, to.getY(), cushionZ, to.getYaw(), to.getPitch()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProjectileFly(ProjectileLaunchEvent event) {
        if (isWallActive(event.getEntity().getLocation().getWorld())) return;
        WallManager.WallMode mode = wallManager.getCurrentMode();
        if (mode == WallManager.WallMode.NONE) return;
        if (event.getEntity() instanceof EnderPearl) return;
        Projectile proj = event.getEntity();
        if (!isEffectProjectile(proj)) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!proj.isValid() || proj.isOnGround() || proj.isDead()) {
                    this.cancel();
                    return;
                }
                if (!isInsideWall(proj, mode)) return;
                this.cancel();
                Location loc = proj.getLocation();
                Vector velocity = proj.getVelocity();
                Location prevLoc = loc.clone().subtract(velocity);
                Location wallFace = getSafeEjectLocation(prevLoc, loc, mode);
                breakEffectProjectile(proj, wallFace);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private boolean isEffectProjectile(Projectile proj) {
        return proj instanceof Snowball
                || proj instanceof Egg
                || proj instanceof ThrownExpBottle
                || proj instanceof ThrownPotion
                || proj.getType() == EntityType.WIND_CHARGE
                || proj.getType() == EntityType.BREEZE_WIND_CHARGE;
    }

    private void breakEffectProjectile(Projectile proj, Location breakLoc) {
        proj.remove();
        World world = breakLoc.getWorld();
        if (world == null) return;
        if (proj instanceof Snowball) {
            world.spawnParticle(Particle.ITEM, breakLoc, 8, 0.1, 0.1, 0.1, 0.05, new ItemStack(Material.SNOWBALL));
            world.playSound(breakLoc, Sound.BLOCK_SNOW_BREAK, 1.0f, 1.0f);
        } else if (proj instanceof Egg) {
            world.spawnParticle(Particle.ITEM, breakLoc, 8, 0.1, 0.1, 0.1, 0.05, new ItemStack(Material.EGG));
            if (ThreadLocalRandom.current().nextInt(8) == 0) {
                int count = (ThreadLocalRandom.current().nextInt(32) == 0) ? 4 : 1;
                for (int i = 0; i < count; i++) {
                    world.spawn(breakLoc, Chicken.class, Ageable::setBaby);
                }
            }

        } else if (proj instanceof ThrownExpBottle) {
            world.spawnParticle(Particle.SPLASH, breakLoc, 8, 0.2, 0.2, 0.2, 0.1);
            world.playSound(breakLoc, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
            int xp = ThreadLocalRandom.current().nextInt(3, 12);
            world.spawn(breakLoc, ExperienceOrb.class, orb -> orb.setExperience(xp));
        } else if (proj instanceof ThrownPotion) {
            world.spawnParticle(Particle.SPLASH, breakLoc, 12, 0.3, 0.3, 0.3, 0.1);
            world.playSound(breakLoc, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
        } else if (proj.getType() == EntityType.WIND_CHARGE || proj.getType() == EntityType.BREEZE_WIND_CHARGE) {
            world.spawnParticle(Particle.GUST_EMITTER_SMALL, breakLoc, 1);
            world.playSound(breakLoc, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0f, 1.0f);
            double radius = 4.0;
            for (Entity entity : world.getNearbyEntities(breakLoc, radius, radius, radius)) {
                if (!(entity instanceof LivingEntity) && !(entity instanceof TNTPrimed)) continue;
                Location entityLoc = entity.getLocation();
                Vector direction = entityLoc.toVector().subtract(breakLoc.toVector());
                double distance = direction.length();
                if (distance == 0) {
                    direction = new Vector(0, 1, 0);
                    distance = 0.1;
                }
                if (distance <= radius) {
                    direction.normalize();
                    double force = (1.0 - (distance / radius)) * 1.5;
                    Vector launch = direction.multiply(force);
                    launch.setY(Math.max(0.3, force * 0.5));
                    entity.setVelocity(entity.getVelocity().add(launch));
                }
            }
        }
    }

    private String formatItemName(Material material) {
        String raw = material.name().replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        for (String word : raw.split(" ")) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEnderPearlHitGlobal(ProjectileHitEvent event) {
        if (isWallActive(event.getEntity().getLocation().getWorld())) return;
        if (wallManager.getCurrentMode() == WallManager.WallMode.NONE) return;
        if (!(event.getEntity() instanceof EnderPearl pearl)) return;
        if (!(pearl.getShooter() instanceof Player shooter)) return;
        Location launchLoc = pearl.getOrigin() != null ? pearl.getOrigin() : shooter.getLocation();
        Location impactLoc = pearl.getLocation();
        WallManager.WallMode mode = wallManager.getCurrentMode();
        boolean intersected = false;
        double intersectX = impactLoc.getX();
        double intersectZ = impactLoc.getZ();
        if (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) {
            if ((launchLoc.getX() > 0.5 && impactLoc.getX() < -1.5) || (launchLoc.getX() < -1.5 && impactLoc.getX() > 0.5)) {
                intersected = true;
                intersectX = launchLoc.getX() > 0.5 ? 0.9 : -1.9;
            }
        }
        if (!intersected && (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH)) {
            if ((launchLoc.getZ() > 0.5 && impactLoc.getZ() < -1.5) || (launchLoc.getZ() < -1.5 && impactLoc.getZ() > 0.5)) {
                intersected = true;
                intersectZ = launchLoc.getZ() > 0.5 ? 0.9 : -1.9;
            }
        }
        if (!intersected) return;
        event.setCancelled(true);
        pearl.remove();
        Location wallImpactFace = new Location(impactLoc.getWorld(), intersectX, impactLoc.getY(), intersectZ,
                shooter.getLocation().getYaw(), shooter.getLocation().getPitch());
        shooter.teleport(wallImpactFace);
        wallImpactFace.getWorld().playSound(wallImpactFace, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        shooter.damage(5.0, pearl);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnderPearlLaunch(ProjectileLaunchEvent event) {
        if (isWallActive(event.getEntity().getLocation().getWorld())) return;
        if (wallManager.getCurrentMode() == WallManager.WallMode.NONE) return;
        if (!(event.getEntity() instanceof EnderPearl pearl)) return;
        if (!(pearl.getShooter() instanceof Player shooter)) return;
        WallManager.WallMode mode = wallManager.getCurrentMode();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!pearl.isValid() || pearl.isDead()) {
                    this.cancel();
                    return;
                }
                Location loc = pearl.getLocation();
                boolean collided = false;
                double tpX = loc.getX();
                double tpZ = loc.getZ();
                if (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) {
                    if (loc.getBlockX() == -1 || loc.getBlockX() == 0) {
                        collided = true;
                        tpX = (pearl.getVelocity().getX() > 0) ? -1.2 : 0.2;
                    }
                }
                if (!collided && (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH)) {
                    if (loc.getBlockZ() == -1 || loc.getBlockZ() == 0) {
                        collided = true;
                        tpZ = (pearl.getVelocity().getZ() > 0) ? -1.2 : 0.2;
                    }
                }
                if (collided && !loc.getBlock().getType().isAir()) {
                    this.cancel();
                    return;
                }
                if (!collided) return;
                this.cancel();
                pearl.remove();
                Location wallImpactFace = new Location(loc.getWorld(), tpX, loc.getY(), tpZ,
                        shooter.getLocation().getYaw(), shooter.getLocation().getPitch());
                shooter.teleport(wallImpactFace);
                wallImpactFace.getWorld().playSound(wallImpactFace, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                shooter.damage(5.0, pearl);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWallExplosion(EntityExplodeEvent event) {
        World world = event.getLocation().getWorld();
        if (world == null || !wallManager.isWorldEnabled(world.getName())) return;
        WallManager.WallMode mode = wallManager.getCurrentMode();
        if (mode == WallManager.WallMode.NONE) return;
        int bufferSize = wallManager.getBufferSize();
        event.blockList().removeIf(block -> {
            int bx = block.getX();
            int bz = block.getZ();
            boolean isPastX = (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) && (bx <= -bufferSize || bx >= bufferSize);
            boolean isPastZ = (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH) && (bz <= -bufferSize || bz >= bufferSize);
            return isPastX || isPastZ;
        });
    }

    private void recalculateWallLight(Player player, Block targetBlock, int bufferSize) {
        for (BlockFace face : BlockFace.values()) {
            if (!face.isCartesian()) continue;
            Block relative = targetBlock.getRelative(face);
            int rx = relative.getX();
            int rz = relative.getZ();
            WallManager.WallMode mode = wallManager.getCurrentMode();
            boolean isBehindX = (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH)
                    && (rx < -bufferSize || rx > bufferSize);
            boolean isBehindZ = (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH)
                    && (rz < -bufferSize || rz > bufferSize);
            if (isBehindX || isBehindZ) {
                player.sendBlockChange(relative.getLocation(), Material.BARRIER.createBlockData());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWallInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getPlayer().isOp()) return;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        World world = clickedBlock.getWorld();
        if (!wallManager.isWorldEnabled(world.getName())) return;
        WallManager.WallMode mode = wallManager.getCurrentMode();
        if (mode == WallManager.WallMode.NONE) return;
        int bufferSize = wallManager.getBufferSize();
        int cx = clickedBlock.getX();
        int cz = clickedBlock.getZ();
        boolean isXWall = (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) && (cx == -bufferSize || cx == bufferSize);
        boolean isZWall = (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH) && (cz == -bufferSize || cz == bufferSize);
        if (isXWall || isZWall) {
            Player player = event.getPlayer();
            ItemStack itemInHand = event.getItem();
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            event.setCancelled(true);
            if (itemInHand != null && itemInHand.getType().isBlock()) {
                Block relativeBlock = clickedBlock.getRelative(event.getBlockFace());
                if (relativeBlock.getType() == Material.AIR || relativeBlock.isLiquid()) {
                    BoundingBox blockBox = BoundingBox.of(relativeBlock);
                    if (!player.getBoundingBox().overlaps(blockBox)) {
                        relativeBlock.setType(itemInHand.getType(), true);
                        recalculateWallLight(player, relativeBlock, bufferSize);
                        world.playSound(relativeBlock.getLocation(),
                                relativeBlock.getBlockData().getSoundGroup().getPlaceSound(), 1.0F, 1.0F);
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            itemInHand.setAmount(itemInHand.getAmount() - 1);
                        }
                    }
                }
            }
            Location blockLoc = clickedBlock.getLocation();
            BlockData bedrockData = Material.BEDROCK.createBlockData();
            player.sendBlockChange(blockLoc, bedrockData);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.sendBlockChange(blockLoc, bedrockData);
                    player.updateInventory();
                }
            }, 1L);
        }
    }

    private boolean playerOccupies(Player player, Block target) {
        BoundingBox blockBox = BoundingBox.of(target.getLocation().toCenterLocation(), 0.5, 0.5, 0.5);
        return player.getBoundingBox().overlaps(blockBox);
    }

    private BlockFace getHorizontalFaceFromYaw(float yaw) {
        yaw = ((yaw % 360) + 360) % 360;
        if (yaw < 45 || yaw >= 315) return BlockFace.SOUTH;
        if (yaw < 135) return BlockFace.WEST;
        if (yaw < 225) return BlockFace.NORTH;
        return BlockFace.EAST;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isWallActive(event.getPlayer().getWorld())) return;
        if (wallManager.getCurrentMode() == WallManager.WallMode.NONE) return;
        WallManager.WallMode mode = wallManager.getCurrentMode();
        Block placed = event.getBlockPlaced();
        if (!isInsideWall(placed.getX(), placed.getZ(), mode)) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        player.updateInventory();
        int cx = placed.getChunk().getX();
        int cz = placed.getChunk().getZ();
        int sectionY = placed.getY() >> 4;
        wallManager.updateChunkSectionVisual(player, cx, cz, sectionY, mode);
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (isWallActive(event.getBlock().getLocation().getWorld())) return;
        if (wallManager.getCurrentMode() == WallManager.WallMode.NONE) return;
        WallManager.WallMode mode = wallManager.getCurrentMode();
        for (Block pushedBlock : event.getBlocks()) {
            int targetX = pushedBlock.getX() + event.getDirection().getModX();
            int targetZ = pushedBlock.getZ() + event.getDirection().getModZ();
            if (isInsideWall(targetX, targetZ, mode)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (isWallActive(event.getBlock().getLocation().getWorld())) return;
        if (wallManager.getCurrentMode() == WallManager.WallMode.NONE) return;
        WallManager.WallMode mode = wallManager.getCurrentMode();
        for (Block pulledBlock : event.getBlocks()) {
            if (isInsideWall(pulledBlock.getX(), pulledBlock.getZ(), mode)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWallExplode(EntityExplodeEvent event) {
        if (isWallActive(event.getLocation().getWorld())) return;
        WallManager.WallMode mode = wallManager.getCurrentMode();
        if (mode == WallManager.WallMode.NONE) return;
        event.blockList().removeIf(block -> isInsideWall(block.getX(), block.getZ(), mode));
        Location loc = event.getLocation();
        int cx = loc.getBlockX() >> 4;
        int cz = loc.getBlockZ() >> 4;
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distanceSquared(loc) >= 2500) continue;
                    for (int dx = -2; dx <= 2; dx++) {
                        for (int dz = -2; dz <= 2; dz++) {
                            int targetCX = cx + dx;
                            int targetCZ = cz + dz;
                            if (targetCX >= -2 && targetCX <= 1 || targetCZ >= -2 && targetCZ <= 1) {
                                int minSection = player.getWorld().getMinHeight() >> 4;
                                int maxSection = player.getWorld().getMaxHeight() >> 4;
                                for (int sy = minSection; sy < maxSection; sy++) {
                                    wallManager.updateChunkSectionVisual(player, targetCX, targetCZ, sy, mode);
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    private boolean isInsideWall(Entity entity, WallManager.WallMode mode) {
        if (isWallActive(entity.getLocation().getWorld())) return false;
        int bufferSize = wallManager.getBufferSize();
        if (entity instanceof Player) {
            Location loc = entity.getLocation();
            boolean inX = (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) && (loc.getX() >= -bufferSize && loc.getX() < bufferSize);
            boolean inZ = (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH) && (loc.getZ() >= -bufferSize && loc.getZ() < bufferSize);
            return inX || inZ;
        }
        BoundingBox box = entity.getBoundingBox();
        boolean inX = (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) && (box.getMinX() < bufferSize && box.getMaxX() > -bufferSize);
        boolean inZ = (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH) && (box.getMinZ() < bufferSize && box.getMaxZ() > -bufferSize);
        return inX || inZ;
    }

    private boolean isInsideWall(int blockX, int blockZ, WallManager.WallMode mode) {
        int bufferSize = wallManager.getBufferSize();
        return switch (mode) {
            case X -> blockX == -bufferSize || blockX == bufferSize;
            case Z -> blockZ == -bufferSize || blockZ == bufferSize;
            case BOTH -> blockX == -bufferSize || blockX == bufferSize || blockZ == -bufferSize || blockZ == bufferSize;
            default -> false;
        };
    }

    private Location getSafeEjectLocation(Location from, Location to, WallManager.WallMode mode) {
        return getSafeEjectLocation(from, to, mode, 0.3);
    }

    private Location getSafeEjectLocation(Location from, Location to, WallManager.WallMode mode, double halfWidth) {
        if (isWallActive(from.getWorld()) || isWallActive(to.getWorld())) return from;
        double safeX = to.getX();
        double safeZ = to.getZ();
        if ((mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH)
                && (to.getBlockX() == -1 || to.getBlockX() == 0)) {
            safeX = (to.getX() >= 0.0) ? (1.0 + halfWidth + 0.1) : (-1.0 - halfWidth - 0.1);
        }
        if ((mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH)
                && (to.getBlockZ() == -1 || to.getBlockZ() == 0)) {
            safeZ = (to.getZ() >= 0.0) ? (1.0 + halfWidth + 0.1) : (-1.0 - halfWidth - 0.1);
        }
        return new Location(to.getWorld(), safeX, to.getY(), safeZ, to.getYaw(), to.getPitch());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (isWallActive(event.getLocation().getWorld())) return;
        WallManager.WallMode mode = wallManager.getCurrentMode();
        if (mode == WallManager.WallMode.NONE) return;
        if (!wallManager.isWorldEnabled(event.getLocation().getWorld().getName())) {
            return;
        }
        Location loc = event.getLocation();
        if (isInsideWall(loc.getBlockX(), loc.getBlockZ(), mode)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (isWallActive(event.getLocation().getWorld())) return;
        WallManager.WallMode mode = wallManager.getCurrentMode();
        if (mode == WallManager.WallMode.NONE) return;
        if (!wallManager.isWorldEnabled(event.getLocation().getWorld().getName())) {
            return;
        }
        Location loc = event.getLocation();
        if (isInsideWall(loc.getBlockX(), loc.getBlockZ(), mode)) {
            event.setCancelled(true);
        }
    }

    private boolean isWallActive(World world) {
        return !wallManager.isWorldEnabled(world.getName());
    }
}