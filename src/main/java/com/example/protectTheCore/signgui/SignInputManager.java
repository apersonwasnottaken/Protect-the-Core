package com.example.protectTheCore.signgui;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateSign;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SignInputManager extends PacketListenerAbstract {

    private final Plugin plugin;

    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    public interface SignCallback {
        void onInput(Player player, String[] lines) throws Exception;
    }

    private static class Session {
        final BlockState original;
        final SignCallback callback;

        Session(BlockState original, SignCallback callback) {
            this.original = original;
            this.callback = callback;
        }
    }

    public SignInputManager(Plugin plugin) {
        this.plugin = plugin;
    }
    public void open(Player player, SignCallback callback) {
        Location loc = player.getLocation().add(0, 1, 0);
        Block block = loc.getBlock();
        BlockState original = block.getState();
        block.setType(org.bukkit.Material.OAK_SIGN);
        sessions.put(player.getUniqueId(), new Session(original, callback));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Sign sign = (Sign) block.getState();
            player.openSign(sign, org.bukkit.block.sign.Side.FRONT);

        }, 1L);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {

        if (event.getPacketType() != PacketType.Play.Client.UPDATE_SIGN)
            return;
        Player player = event.getPlayer();
        Session session = sessions.remove(player.getUniqueId());

        if (session == null) {
            return;
        }

        WrapperPlayClientUpdateSign packet =
                new WrapperPlayClientUpdateSign(event);

        String[] lines = packet.getTextLines();

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                session.callback.onInput(player, lines);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Location loc = player.getLocation().add(0, 1, 0);
            Block block = loc.getBlock();
            session.original.update(true, false);
        });
    }
}