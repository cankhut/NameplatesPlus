package com.github.cankhut.nameplatesplus.net;

import com.github.cankhut.nameplatesplus.ColorPair;
import com.github.cankhut.nameplatesplus.NameplateColorStore;
import com.github.cankhut.nameplatesplus.NameplatesPlus;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = NameplatesPlus.MODID)
public final class NetworkRegister {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();

        registrar.playToServer(
                SetColorsPayload.TYPE,
                SetColorsPayload.CODEC,
                NetworkRegister::handleSetColors
        );

        registrar.playToClient(
                SyncColorsPayload.TYPE,
                SyncColorsPayload.CODEC,
                NetworkRegister::handleSyncColors
        );
    }

    private static void handleSetColors(SetColorsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                ServerColorStore.update(sender, new ColorPair(payload.text(), payload.background()));
            }
        });
    }

    private static void handleSyncColors(SyncColorsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> payload.colors().forEach(NameplateColorStore::put));
    }
}
