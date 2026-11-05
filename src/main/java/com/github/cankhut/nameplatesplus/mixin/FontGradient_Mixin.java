package com.github.cankhut.nameplatesplus.mixin;

import com.github.cankhut.nameplatesplus.client.render.GradientRenderContext;
import com.github.cankhut.nameplatesplus.client.render.GradientVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Font.class)
public abstract class FontGradient_Mixin {

    @ModifyVariable(
            method = "renderChar",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private VertexConsumer nameplatesplus$applyGradient(
            VertexConsumer original
    ) {
        GradientRenderContext.Context context =
                GradientRenderContext.current();

        if (context == null) {
            return original;
        }

        return new GradientVertexConsumer(original, context);
    }
}