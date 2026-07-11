package com.example.protectTheCore.game.wall;

import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.protectTheCore.ProtectTheCore.logger;
import static com.example.protectTheCore.ProtectTheCore.plugin;

public class WallManager {

    public enum WallMode { NONE, X, Z, BOTH }

    private WallMode currentMode = WallMode.NONE;
    private final Set<String> enabledWorlds = new HashSet<>();
    private int bufferSize = 1;

    private final int wallGlobalId;
    private final int airGlobalId;

    public WallManager(Plugin plugin) {
        this.wallGlobalId = StateTypes.BEDROCK.createBlockState().getGlobalId();
        this.airGlobalId = StateTypes.AIR.createBlockState().getGlobalId();
        loadWorldsFromConfig(plugin);
    }

    public void loadWorldsFromConfig(Plugin plugin) {
        enabledWorlds.clear();
        List<String> worlds = plugin.getConfig().getStringList("enabled-worlds");
        enabledWorlds.addAll(worlds);
        this.bufferSize = plugin.getConfig().getInt("buffer-size", 1);
    }

    public boolean isWorldEnabled(String worldName) {
        return enabledWorlds.contains(worldName);
    }

    public WallMode getCurrentMode() {
        return currentMode;
    }

    public int getBufferSize() {
        this.bufferSize = plugin.getConfig().getInt("config.buffer_size", 1);
        return bufferSize;
    }

    public boolean isWallChunk(int cx, int cz, WallMode mode) {
        boolean nearX = cx == ((-bufferSize) >> 4) || cx == (bufferSize >> 4);
        boolean nearZ = cz == ((-bufferSize) >> 4) || cz == (bufferSize >> 4);
        return switch (mode) {
            case X -> nearX;
            case Z -> nearZ;
            default -> nearX || nearZ;
        };
    }

    public void setMode(WallMode mode) {
        this.currentMode = mode;
        if (mode != WallMode.NONE) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location location = player.getLocation();
                int bx = location.getBlockX();
                int bz = location.getBlockZ();
                boolean needsTeleport = false;
                double targetX = location.getX();
                double targetZ = location.getZ();
                if ((mode == WallMode.X || mode == WallMode.BOTH) && (bx >= -bufferSize && bx < bufferSize)) {
                    targetX = (bx < 0) ? -bufferSize - 0.5 : bufferSize + 0.5;
                    needsTeleport = true;
                }
                if ((mode == WallMode.Z || mode == WallMode.BOTH) && (bz >= -bufferSize && bz < bufferSize)) {
                    targetZ = (bz < 0) ? -bufferSize - 0.5 : bufferSize + 0.5;
                    needsTeleport = true;
                }
                if (needsTeleport) {
                    Location safeLocation = new Location(location.getWorld(), targetX, location.getY(), targetZ, location.getYaw(), location.getPitch());
                    player.teleport(safeLocation);
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>The walls have spawned in! You were pushed back."));
                }
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshWallForPlayer(player, mode);
        }
    }

    public boolean isWallBlock(int chunkX, int chunkZ, int localX, int localZ, WallMode mode) {
        int globalX = (chunkX * 16) + localX;
        int globalZ = (chunkZ * 16) + localZ;
        return switch (mode) {
            case X -> globalX == -bufferSize || globalX == bufferSize;
            case Z -> globalZ == -bufferSize || globalZ == bufferSize;
            case BOTH ->
                    globalX == -bufferSize || globalX == bufferSize || globalZ == -bufferSize || globalZ == bufferSize;
            default -> false;
        };
    }

    private void refreshWallForPlayer(Player player, WallMode newMode) {
        World world = player.getWorld();
        if (newMode == WallMode.NONE) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;
                    try {
                        ServerPlayer nmsPlayer =
                                ((CraftPlayer) player).getHandle();
                        ServerLevel nmsLevel =
                                ((CraftWorld) world).getHandle();
                        for (Chunk loadedChunk : world.getLoadedChunks()) {
                            int cx = loadedChunk.getX();
                            int cz = loadedChunk.getZ();
                            if (!isWallChunk(cx, cz, WallMode.BOTH)) continue;
                            net.minecraft.world.level.chunk.LevelChunk chunk = nmsLevel.getChunk(cx, cz);
                            net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket pkt =
                                    new net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket(
                                            chunk, nmsLevel.getLightEngine(), null, null);
                            nmsPlayer.connection.send(pkt);
                        }
                    } catch (Exception e) {
                        logger.warn("[Wall Protector] refreshWallForPlayer NMS failed for {}: {}", player.getName(), e.getMessage());
                    }
                }
            }.runTaskLater(plugin, 1L);
            return;
        }
        int minSection = world.getMinHeight() >> 4;
        int maxSection = world.getMaxHeight() >> 4;
        List<Chunk> axisChunks = new ArrayList<>();
        for (Chunk loadedChunk : world.getLoadedChunks()) {
            int cx = loadedChunk.getX();
            int cz = loadedChunk.getZ();
            if (isWallChunk(cx, cz, WallMode.BOTH)) {
                axisChunks.add(loadedChunk);
            }
        }

        new BukkitRunnable() {
            final List<int[]> work = buildSectionWorkList(axisChunks, minSection, maxSection);
            int index = 0;
            @Override
            public void run() {
                if (!player.isOnline() || index >= work.size()) {
                    this.cancel();
                    return;
                }
                int batchSize = 16;
                for (int i = 0; i < batchSize && index < work.size(); i++, index++) {
                    int[] entry = work.get(index);
                    updateChunkSectionVisual(player, entry[0], entry[1], entry[2], newMode);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private List<int[]> buildSectionWorkList(List<Chunk> chunks, int minSection, int maxSection) {
        List<int[]> list = new ArrayList<>();
        for (Chunk chunk : chunks) {
            for (int sy = minSection; sy < maxSection; sy++) {
                list.add(new int[]{chunk.getX(), chunk.getZ(), sy});
            }
        }
        return list;
    }

    public void updateChunkSectionVisual(Player player, int cx, int cz, int sectionY, WallMode mode) {
        int minChunk = (-bufferSize) >> 4;
        int maxChunk = (bufferSize - 1) >> 4;
        boolean chunkHasXWall = (cx >= minChunk && cx <= maxChunk && mode != WallMode.Z);
        boolean chunkHasZWall = (cz >= minChunk && cz <= maxChunk && mode != WallMode.X);
        if (mode != WallMode.NONE && !chunkHasXWall && !chunkHasZWall) return;
        List<com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange.EncodedBlock> encoded = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                boolean isWallNow = isWallBlock(cx, cz, x, z, mode);
                boolean shouldPaint = isWallNow
                        || (mode == WallMode.NONE && isWallChunk(cx, cz, WallMode.BOTH));
                if (shouldPaint) {
                    int targetBlockId = (mode != WallMode.NONE && isWallNow) ? wallGlobalId : airGlobalId;
                    for (int y = 0; y < 16; y++) {
                        encoded.add(new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange.EncodedBlock(
                                targetBlockId, x, y, z
                        ));
                    }
                }
            }
        }
        if (encoded.isEmpty()) return;
        com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange packet =
                new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange(
                        new Vector3i(cx, sectionY, cz),
                        true,
                        encoded.toArray(new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange.EncodedBlock[0])
                );
        com.github.retrooper.packetevents.PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }
}