package com.github.cankhut.nameplatesplus;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue TEXT_COLOR;
    public static final ModConfigSpec.IntValue BACKGROUND_COLOR;
    public static final ModConfigSpec.BooleanValue SHOW_OWN_NAMEPLATE;

    public static final ModConfigSpec.BooleanValue TEXT_GRADIENT_ENABLED;
    public static final ModConfigSpec.IntValue TEXT_GRADIENT_START;
    public static final ModConfigSpec.IntValue TEXT_GRADIENT_END;

    static {
        BUILDER.push("nameplates");

        TEXT_COLOR = BUILDER
                .comment("Colour of your name, as a packed ARGB integer. Use the in game picker instead of editing this by hand.")
                .defineInRange(
                        "textColor",
                        ColorPair.DEFAULT_TEXT,
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE
                );

        BACKGROUND_COLOR = BUILDER
                .comment("Colour of the box behind your name, as a packed ARGB integer.")
                .defineInRange(
                        "backgroundColor",
                        ColorPair.DEFAULT_BACKGROUND,
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE
                );

        SHOW_OWN_NAMEPLATE = BUILDER
                .comment("Show your own nameplate in third person (F5). Only you see it.")
                .define("showOwnNameplate", false);

        TEXT_GRADIENT_ENABLED = BUILDER
                .comment("Enable gradient text color.")
                .define("text-gradient-enabled", false);

        TEXT_GRADIENT_START = BUILDER
                .comment("Gradient start color (RGB hex).")
                .defineInRange(
                        "text-gradient-start",
                        ColorPair.DEFAULT_GRADIENT_START,
                        0,
                        0xFFFFFF
                );

        TEXT_GRADIENT_END = BUILDER
                .comment("Gradient end color (RGB hex).")
                .defineInRange(
                        "text-gradient-end",
                        ColorPair.DEFAULT_GRADIENT_END,
                        0,
                        0xFFFFFF
                );

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static ColorPair get() {
        return new ColorPair(
                TEXT_COLOR.get(),
                BACKGROUND_COLOR.get(),
                TEXT_GRADIENT_ENABLED.get(),
                TEXT_GRADIENT_START.get(),
                TEXT_GRADIENT_END.get()
        );
    }

    public static void save(ColorPair colors, boolean showOwnNameplate) {
        TEXT_COLOR.set(colors.text());
        BACKGROUND_COLOR.set(colors.background());

        SHOW_OWN_NAMEPLATE.set(showOwnNameplate);

        TEXT_GRADIENT_ENABLED.set(colors.textGradientEnabled());
        TEXT_GRADIENT_START.set(colors.textGradientStart());
        TEXT_GRADIENT_END.set(colors.textGradientEnd());

        SPEC.save();
    }
}