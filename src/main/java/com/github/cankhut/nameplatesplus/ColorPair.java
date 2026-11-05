package com.github.cankhut.nameplatesplus;

import java.util.Locale;

public record ColorPair(
        int text,
        int background,
        boolean textGradientEnabled,
        int textGradientStart,
        int textGradientEnd
) {

    public static final int DEFAULT_TEXT = 0xFFFFFFFF;
    public static final int DEFAULT_BACKGROUND = 0xFFFFFFFF;

    public static final int DEFAULT_GRADIENT_START = 0xFF426B;
    public static final int DEFAULT_GRADIENT_END = 0x36D1DC;

    public static final ColorPair DEFAULT = new ColorPair(
            DEFAULT_TEXT,
            DEFAULT_BACKGROUND,
            false,
            DEFAULT_GRADIENT_START,
            DEFAULT_GRADIENT_END
    );

    public ColorPair(int text, int background) {
        this(
                text,
                background,
                false,
                DEFAULT_GRADIENT_START,
                DEFAULT_GRADIENT_END
        );
    }

    public ColorPair {
        textGradientStart &= 0xFFFFFF;
        textGradientEnd &= 0xFFFFFF;
    }

    public boolean isDefault() {
        return equals(DEFAULT);
    }

    public static int alphaOf(int argb) {
        return (argb >>> 24) & 0xFF;
    }

    public static int withAlpha(int rgb, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));

        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    public static int clampTextAlpha(int argb) {
        return withAlpha(argb & 0xFFFFFF, alphaOf(argb));
    }

    public static String toHex(int rgb) {
        return String.format(Locale.ROOT, "%06X", rgb & 0xFFFFFF);
    }

    public static Integer parseHex(String value) {
        if (value == null) {
            return null;
        }

        String hex = value.trim();

        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        } else if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        }

        if (hex.length() != 6) {
            return null;
        }

        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}