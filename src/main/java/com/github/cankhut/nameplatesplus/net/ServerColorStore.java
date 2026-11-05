package com.github.cankhut.nameplatesplus.net;

import com.github.cankhut.nameplatesplus.ColorPair;
import com.github.cankhut.nameplatesplus.NameplatesPlus;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = NameplatesPlus.MODID)
public final class ServerColorStore {

    private static final Map<UUID, ColorPair> COLORS = new ConcurrentHashMap<>();

    private ServerColorStore() {
    }

    public static void update(ServerPlayer sender, ColorPair colors) {
        if (colors.isDefault()) {
            COLORS.remove(sender.getUUID());
        } else {
            COLORS.put(sender.getUUID(), colors);
        }

        SyncColorsPayload payload = SyncColorsPayload.single(sender.getUUID(), colors);
        for (ServerPlayer player : sender.server.getPlayerList().getPlayers()) {
            sendIfSupported(player, payload);
        }
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !COLORS.isEmpty()) {
            sendIfSupported(player, new SyncColorsPayload(Map.copyOf(COLORS)));
        }
    }

    @SubscribeEvent
    public static void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        COLORS.remove(event.getEntity().getUUID());
    }

    private static void sendIfSupported(ServerPlayer player, SyncColorsPayload payload) {
        if (player.connection.hasChannel(SyncColorsPayload.TYPE)) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }
}
