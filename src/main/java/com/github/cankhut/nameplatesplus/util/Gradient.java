package com.github.cankhut.nameplatesplus.util;
public final class Gradient {

    private final int start;
    private final int end;

    public Gradient(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int colorAt(float t) {
        t = Math.clamp(t, 0.0f, 1.0f);

        int r = interpolate(start >> 16 & 0xFF, end >> 16 & 0xFF, t);
        int g = interpolate(start >> 8 & 0xFF, end >> 8 & 0xFF, t);
        int b = interpolate(start & 0xFF, end & 0xFF, t);

        return (r << 16) | (g << 8) | b;
    }

    private int interpolate(int a, int b, float t) {
        return Math.round(a + (b - a) * t);
    }
}