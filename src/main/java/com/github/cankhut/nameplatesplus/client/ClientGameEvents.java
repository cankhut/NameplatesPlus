package com.github.cankhut.nameplatesplus.client;

import com.github.cankhut.nameplatesplus.ClientConfig;
import com.github.cankhut.nameplatesplus.ColorPair;
import com.github.cankhut.nameplatesplus.NameplateColorStore;
import com.github.cankhut.nameplatesplus.NameplatesPlus;
import com.github.cankhut.nameplatesplus.net.SetColorsPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = NameplatesPlus.MODID, value = Dist.CLIENT)
public final class ClientGameEvents {

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (ClientModEvents.OPEN_PICKER.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                minecraft.setScreen(new ColorPickerScreen(null));
            }
        }
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getEntity() == minecraft.player
                && ClientConfig.SHOW_OWN_NAMEPLATE.getAsBoolean()
                && Minecraft.renderNames()
                && !minecraft.player.isInvisible()) {
            event.setCanRender(TriState.TRUE);
        }
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        applyLocally();
        sendToServer();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        NameplateColorStore.clear();
    }

    public static void applyLocally() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            NameplateColorStore.put(minecraft.player.getUUID(), ClientConfig.get());
        }
    }

    public static boolean isConnected() {
        return Minecraft.getInstance().getConnection() != null;
    }

    public static boolean serverHasMod() {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        return listener != null && listener.hasChannel(SetColorsPayload.TYPE);
    }

    public static void sendToServer() {
        if (!serverHasMod()) {
            return;
        }
        ColorPair colors = ClientConfig.get();
        PacketDistributor.sendToServer(new SetColorsPayload(colors.text(), colors.background()));
    }
}
