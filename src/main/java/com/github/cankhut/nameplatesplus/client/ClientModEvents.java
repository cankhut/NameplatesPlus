package com.github.cankhut.nameplatesplus.client;

import com.github.cankhut.nameplatesplus.NameplatesPlus;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = NameplatesPlus.MODID, value = Dist.CLIENT)
public final class ClientModEvents {

    public static final KeyMapping OPEN_PICKER = new KeyMapping(
            "key.nameplatesplus.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            "key.categories.nameplatesplus"
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_PICKER);
    }
}
