package com.github.cankhut.nameplatesplus.net;

import com.github.cankhut.nameplatesplus.ColorPair;
import com.github.cankhut.nameplatesplus.NameplatesPlus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SetColorsPayload(
        int text,
        int background,
        boolean textGradientEnabled,
        int textGradientStart,
        int textGradientEnd
) implements CustomPacketPayload {

    public SetColorsPayload(int text, int background) {
        this(
                text,
                background,
                false,
                ColorPair.DEFAULT_GRADIENT_START,
                ColorPair.DEFAULT_GRADIENT_END
        );
    }

    public static final Type<SetColorsPayload> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            NameplatesPlus.MODID,
                            "set_colors"
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, SetColorsPayload> CODEC =
            StreamCodec.of(
                    (buf, msg) -> {
                        buf.writeInt(msg.text());
                        buf.writeInt(msg.background());

                        buf.writeBoolean(msg.textGradientEnabled());
                        buf.writeInt(msg.textGradientStart());
                        buf.writeInt(msg.textGradientEnd());
                    },
                    buf -> new SetColorsPayload(
                            buf.readInt(),
                            buf.readInt(),
                            buf.readBoolean(),
                            buf.readInt(),
                            buf.readInt()
                    )
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}