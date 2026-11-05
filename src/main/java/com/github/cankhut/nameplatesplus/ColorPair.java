package com.github.cankhut.nameplatesplus;

public record ColorPair(int text, int background) {

    public static final int DEFAULT_TEXT = 0xFFFFFFFF;
    public static final int DEFAULT_BACKGROUND = 0x3F000000;

    public static final ColorPair DEFAULT = new ColorPair(DEFAULT_TEXT, DEFAULT_BACKGROUND);

    public ColorPair {
        text = clampTextAlpha(text);
    }

    public static int clampTextAlpha(int argb) {
        return alphaOf(argb) < 4 ? withAlpha(argb, 4) : argb;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public static int withAlpha(int argb, int alpha) {
        return ((alpha & 0xFF) << 24) | (argb & 0xFFFFFF);
    }

    public static int alphaOf(int argb) {
        return (argb >>> 24) & 0xFF;
    }

    public static String toHex(int argb) {
        return String.format("#%06X", argb & 0xFFFFFF);
    }

    public static Integer parseHex(String raw) {
        String s = raw.trim();
        if (s.startsWith("#")) {
            s = s.substring(1);
        }
        if (s.length() != 6) {
            return null;
        }
        try {
            return Integer.parseInt(s, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
