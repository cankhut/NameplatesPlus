package com.github.cankhut.nameplatesplus.net;

import com.github.cankhut.nameplatesplus.ColorPair;
import com.github.cankhut.nameplatesplus.NameplatesPlus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record SyncColorsPayload(
        Map<UUID, ColorPair> colors
) implements CustomPacketPayload {

    public static final Type<SyncColorsPayload> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            NameplatesPlus.MODID,
                            "sync_colors"
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncColorsPayload> CODEC =
            StreamCodec.of(
                    (buf, msg) -> {
                        buf.writeVarInt(msg.colors().size());

                        for (Map.Entry<UUID, ColorPair> entry : msg.colors().entrySet()) {
                            ColorPair colors = entry.getValue();

                            buf.writeUUID(entry.getKey());

                            buf.writeInt(colors.text());
                            buf.writeInt(colors.background());

                            buf.writeBoolean(colors.textGradientEnabled());
                            buf.writeInt(colors.textGradientStart());
                            buf.writeInt(colors.textGradientEnd());
                        }
                    },
                    buf -> {
                        int size = buf.readVarInt();

                        Map<UUID, ColorPair> read = new HashMap<>(size);

                        for (int i = 0; i < size; i++) {
                            UUID id = buf.readUUID();

                            int text = buf.readInt();
                            int background = buf.readInt();

                            boolean gradientEnabled = buf.readBoolean();
                            int gradientStart = buf.readInt();
                            int gradientEnd = buf.readInt();

                            read.put(
                                    id,
                                    new ColorPair(
                                            text,
                                            background,
                                            gradientEnabled,
                                            gradientStart,
                                            gradientEnd
                                    )
                            );
                        }

                        return new SyncColorsPayload(read);
                    }
            );

    public static SyncColorsPayload single(UUID id, ColorPair colors) {
        return new SyncColorsPayload(Map.of(id, colors));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}