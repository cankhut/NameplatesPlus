package com.github.cankhut.nameplatesplus.net;

import com.github.cankhut.nameplatesplus.NameplatesPlus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SetColorsPayload(int text, int background) implements CustomPacketPayload {

    public static final Type<SetColorsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(NameplatesPlus.MODID, "set_colors"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetColorsPayload> CODEC =
            StreamCodec.of(
                    (buf, msg) -> {
                        buf.writeInt(msg.text);
                        buf.writeInt(msg.background);
                    },
                    buf -> new SetColorsPayload(buf.readInt(), buf.readInt())
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
