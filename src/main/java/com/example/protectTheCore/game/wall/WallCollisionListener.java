package com.example.protectTheCore.game.wall;

import com.example.protectTheCore.helper.HelperFunctions;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import static com.example.protectTheCore.ProtectTheCore.plugin;

public class WallCollisionListener implements Listener {

    private final WallManager wallManager;

    public WallCollisionListener(WallManager wallManager) {
        this.wallManager = wallManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.SPECTATOR || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        Location to = event.getTo();
        if (to == null) return;
        World world = to.getWorld();
        if (world == null || !wallManager.isWorldEnabled(world.getName())) return;
        WallManager.WallMode mode = wallManager.getCurrentMode();
        if (mode == WallManager.WallMode.NONE) return;
        int bufferSize = wallManager.getBufferSize();
        boolean violate = false;
        if (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) {
            if (to.getBlockX() >= -bufferSize && to.getBlockX() < bufferSize) violate = true;
        }
        if (!violate && (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH)) {
            if (to.getBlockZ() >= -bufferSize && to.getBlockZ() < bufferSize) violate = true;
        }
        if (violate) {
            Location from = event.getFrom();
            Location fallback = from.clone();
            if (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) {
                if (to.getBlockX() >= -bufferSize && to.getBlockX() < bufferSize) {
                    fallback.setX(from.getX() >= bufferSize ? from.getX() + 0.1 : from.getX() - 0.1);
                }
            }
            if (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH) {
                if (to.getBlockZ() >= -bufferSize && to.getBlockZ() < bufferSize) {
                    fallback.setZ(from.getZ() >= bufferSize ? from.getZ() + 0.1 : from.getZ() - 0.1);
                }
            }
            event.setTo(fallback);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpectatorMove(PlayerMoveEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.SPECTATOR) return;
        if (HelperFunctions.getConfigEntryFromWorld(event.getTo().getWorld()).equals(event.getTo().getWorld().getName())) return;
        if (
                Math.abs(event.getTo().getX()) > plugin.getConfig().getInt("config." + HelperFunctions.getConfigEntryFromWorld(event.getTo().getWorld()) + ".border") + 16 ||
                Math.abs(event.getTo().getZ()) > plugin.getConfig().getInt("config." + HelperFunctions.getConfigEntryFromWorld(event.getTo().getWorld()) + ".border") + 16
        ) {
            event.getPlayer().teleport(event.getFrom());
            event.getPlayer().sendMessage(Component.text("You cannot leave the confines of the world!", NamedTextColor.RED));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMoveBounds(PlayerMoveEvent event) {
        if (isWallActive(event.getPlayer().getLocation().getWorld())) return;
        WallManager.WallMode mode = wallManager.getCurrentMode();
        if (mode == WallManager.WallMode.NONE) return;
        Player player = event.getPlayer();
        if (event.getPlayer().isOp() || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        Location to = event.getTo();
        if (to == null) return;
        if (isInsideWall(to.getBlockX(), to.getBlockZ(), mode)) {
            event.setTo(getSafeEjectLocation(event.getFrom(), to, mode));
            return;
        }
        int maxHeight = to.getWorld().getMaxHeight();
        if (to.getBlockY() >= maxHeight - 3) {
            int cx = to.getBlockX() >> 4;
            int cz = to.getBlockZ() >> 4;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    int targetCX = cx + dx;
                    int targetCZ = cz + dz;
                    if (targetCX >= -2 && targetCX <= 1 || targetCZ >= -2 && targetCZ <= 1) {
                        wallManager.updateChunkSectionVisual(player, targetCX, targetCZ, (maxHeight >> 4) - 1, mode);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.SPECTATOR || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        Location to = event.getTo();
        if (to == null) return;
        World world = to.getWorld();
        if (world == null || !wallManager.isWorldEnabled(world.getName())) return;
        WallManager.WallMode mode = wallManager.getCurrentMode();
        if (mode == WallManager.WallMode.NONE) return;
        boolean violate = false;
        if (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) {
            if (to.getBlockX() == 0) violate = true;
        }
        if (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH) {
            if (to.getBlockZ() == 0) violate = true;
        }
        if (violate) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobMove(EntityMoveEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player || entity instanceof Projectile || entity instanceof Item) return;
        Location from = event.getFrom();
        Location to = event.getTo();
        World world = to.getWorld();
        if (world == null || !wallManager.isWorldEnabled(world.getName())) return;
        WallManager.WallMode mode = wallManager.getCurrentMode();
        if (mode == WallManager.WallMode.NONE) return;
        int bufferSize = wallManager.getBufferSize();
        BoundingBox box = entity.getBoundingBox();
        double halfWidthX = (box.getMaxX() - box.getMinX()) / 2.0;
        double halfWidthZ = (box.getMaxZ() - box.getMinZ()) / 2.0;
        double fromX = from.getX();
        double fromZ = from.getZ();
        double newX = to.getX();
        double newZ = to.getZ();
        boolean modifiedX = false;
        boolean modifiedZ = false;
        if (mode == WallManager.WallMode.X || mode == WallManager.WallMode.BOTH) {
            double negWallX = -bufferSize;
            double posWallX = bufferSize + 1;
            boolean wasInsideOfNeg = fromX > negWallX;
            boolean wasInsideOfPos = fromX < posWallX;
            if (wasInsideOfNeg && (newX - halfWidthX) <= negWallX) {
                newX = negWallX + halfWidthX + 0.03;
                modifiedX = true;
            } else if (!wasInsideOfNeg && (newX + halfWidthX) >= negWallX) {
                newX = negWallX - halfWidthX - 0.03;
                modifiedX = true;
            }
            if (!modifiedX) {
                if (wasInsideOfPos && (newX + halfWidthX) >= posWallX) {
                    newX = posWallX - halfWidthX - 0.03;
                    modifiedX = true;
                } else if (!wasInsideOfPos && (newX - halfWidthX) <= posWallX) {
                    newX = posWallX + halfWidthX + 0.03;
                    modifiedX = true;
                }
            }
        }
        if (mode == WallManager.WallMode.Z || mode == WallManager.WallMode.BOTH) {
            double negWallZ = -bufferSize;
            boolean wasInsideOfNeg = fromZ > negWallZ;
            boolean wasInsideOfPos = fromZ < (double) bufferSize;
            if (wasInsideOfNeg && (newZ - halfWidthZ) <= negWallZ) {
                newZ = negWallZ + halfWidthZ + 0.03;
                modifiedZ = true;
            } else if (!wasInsideOfNeg && (newZ + halfWidthZ) >= negWallZ) {
                newZ = negWallZ - halfWidthZ - 0.03;
                modifiedZ = true;
            }
            if (!modifiedZ) {
                if (wasInsideOfPos && (newZ + halfWidthZ) >= (double) bufferSize) {
                    newZ = (double) bufferSize - halfWidthZ - 0.03;
                    modifiedZ = true;
                } else if (!wasInsideOfPos && (newZ - halfWidthZ) <= (double) bufferSize) {
                    newZ = (double) bufferSize + halfWidthZ + 0.03;
                    modifiedZ = true;
                }
            }
        }
        if (modifiedX || modifiedZ) {
            Location clampedTarget = new Location(world, newX, to.getY(), newZ, to.getYaw(), to.getPitch());
            event.setTo(clampedTarget);
            Vector velocity = entity.getVelocity();
            double velX = modifiedX ? 0 : velocity.getX();
            double velZ = modifiedZ ? 0 : velocity.getZ();
            entity.setVelocity(new Vector(velX, velocity.getY(), velZ));
        }
    }

    private boolean isWallActive(World world) {
        return !wallManager.isWorldEnabled(world.getName());
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
}