package com.github.cankhut.nameplatesplus.mixin;

import com.github.cankhut.nameplatesplus.ColorPair;
import com.github.cankhut.nameplatesplus.NameplateColorStore;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EntityRenderer.class, priority = 1100)
public abstract class NameTagColor_Mixin<T extends Entity> {

    @WrapOperation(
            method = "renderNameTag",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
                    ordinal = 0
            )
    )
    private int nameplatesplus$firstPass(
            Font font,
            Component text,
            float x,
            float y,
            int color,
            boolean shadow,
            Matrix4f matrix,
            MultiBufferSource buffers,
            DisplayMode mode,
            int backgroundColor,
            int light,
            Operation<Integer> original, @Local(argsOnly = true) T entity
    ) {
        ColorPair colors = nameplatesplus$lookup(entity);
        if (colors != null) {
            color = (color & 0xFF000000) | (colors.text() & 0xFFFFFF);
            if (backgroundColor != 0) {
                backgroundColor = colors.background();
            }
        }

        return original.call(font, text, x, y, color, shadow, matrix, buffers, mode, backgroundColor, light);
    }

    @WrapOperation(
            method = "renderNameTag",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
                    ordinal = 1
            )
    )
    private int nameplatesplus$secondPass(
            Font font,
            Component text,
            float x,
            float y,
            int color,
            boolean shadow,
            Matrix4f matrix,
            MultiBufferSource buffers,
            DisplayMode mode,
            int backgroundColor,
            int light,
            Operation<Integer> original, @Local(argsOnly = true) T entity
    ) {
        ColorPair colors = nameplatesplus$lookup(entity);
        if (colors != null) {
            color = colors.text();
        }

        return original.call(font, text, x, y, color, shadow, matrix, buffers, mode, backgroundColor, light);
    }

    private static ColorPair nameplatesplus$lookup(Entity entity) {
        if (entity instanceof Player player) {
            return NameplateColorStore.get(player.getUUID());
        }
        return null;
    }
}
