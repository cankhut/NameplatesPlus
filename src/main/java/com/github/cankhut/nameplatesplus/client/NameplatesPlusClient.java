package com.github.cankhut.nameplatesplus.client;

import com.github.cankhut.nameplatesplus.NameplatesPlus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = NameplatesPlus.MODID, dist = Dist.CLIENT)
public class NameplatesPlusClient {

    public NameplatesPlusClient(ModContainer container) {
        IConfigScreenFactory factory = (c, parent) -> new ColorPickerScreen(parent);
        container.registerExtensionPoint(IConfigScreenFactory.class, factory);
    }
}
