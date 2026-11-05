package com.github.cankhut.nameplatesplus.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class GradientUtil {

    private GradientUtil() {}

    public static Component gradient(
            String text,
            int startColor,
            int endColor
    ) {
        Component result = Component.empty();

        int[] codePoints = text.codePoints().toArray();
        int length = codePoints.length;

        if (length == 0)
            return result;

        for (int i = 0; i < length; i++) {
            float t = length == 1
                    ? 0.0f
                    : (float) i / (length - 1);

            int color = lerpColor(startColor, endColor, t);

            String character = new String(
                    Character.toChars(codePoints[i])
            );

//            result.append(
//                    Component.literal(character)
//                            .setStyle(
//                                    Style.EMPTY.withColor(
//                                            TextColor.fromRgb(color)
//                                    )
//                            )
//            );
        }

        return result;
    }

    private static int lerpColor(int a, int b, float t) {
        int r1 = (a >> 16) & 0xFF;
        int g1 = (a >> 8) & 0xFF;
        int b1 = a & 0xFF;

        int r2 = (b >> 16) & 0xFF;
        int g2 = (b >> 8) & 0xFF;
        int b2 = b & 0xFF;

        int r = Math.round(r1 + (r2 - r1) * t);
        int g = Math.round(g1 + (g2 - g1) * t);
        int blue = Math.round(b1 + (b2 - b1) * t);

        return (r << 16) | (g << 8) | blue;
    }
}