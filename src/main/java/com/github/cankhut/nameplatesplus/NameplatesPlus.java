package com.github.cankhut.nameplatesplus;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(NameplatesPlus.MODID)
public class NameplatesPlus {

    public static final String MODID = "nameplatesplus";

    public NameplatesPlus(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }
}
