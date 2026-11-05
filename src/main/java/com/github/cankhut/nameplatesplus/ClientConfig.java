
package com.github.cankhut.nameplatesplus;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Locale;

public class ClientConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue TEXT_COLOR;
    public static final ModConfigSpec.IntValue BACKGROUND_COLOR;
    public static final ModConfigSpec.BooleanValue SHOW_OWN_NAMEPLATE;

    public static final ModConfigSpec.BooleanValue TEXT_GRADIENT_ENABLED;
    public static final ModConfigSpec.IntValue TEXT_GRADIENT_START;
    public static final ModConfigSpec.IntValue TEXT_GRADIENT_END;

    public static final ModConfigSpec.ConfigValue<String> PRESET_1;
    public static final ModConfigSpec.ConfigValue<String> PRESET_2;
    public static final ModConfigSpec.ConfigValue<String> PRESET_3;
    public static final ModConfigSpec.ConfigValue<String> PRESET_4;
    public static final ModConfigSpec.ConfigValue<String> PRESET_5;

    static {
        BUILDER.push("nameplates");

        TEXT_COLOR = BUILDER
                .comment("Colour of your name, as a packed ARGB integer.")
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
                .comment("Show your own nameplate in third person (F5).")
                .define("showOwnNameplate", false);

        TEXT_GRADIENT_ENABLED = BUILDER
                .comment("Enable gradient text color.")
                .define("text-gradient-enabled", false);

        TEXT_GRADIENT_START = BUILDER
                .comment("Gradient start color (RGB hex).")
                .defineInRange("text-gradient-start", 0xFF426B, 0, 0xFFFFFF);

        TEXT_GRADIENT_END = BUILDER
                .comment("Gradient end color (RGB hex).")
                .defineInRange("text-gradient-end", 0x36D1DC, 0, 0xFFFFFF);

        BUILDER.push("presets");

        PRESET_1 = BUILDER.define("slot1", "");
        PRESET_2 = BUILDER.define("slot2", "");
        PRESET_3 = BUILDER.define("slot3", "");
        PRESET_4 = BUILDER.define("slot4", "");
        PRESET_5 = BUILDER.define("slot5", "");

        BUILDER.pop();
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

    public static void savePreset(int slot, ColorPair colors) {
        String encoded = String.format(
                Locale.ROOT,
                "%08X:%08X:%d:%06X:%06X",
                colors.text(),
                colors.background(),
                colors.textGradientEnabled() ? 1 : 0,
                colors.textGradientStart(),
                colors.textGradientEnd()
        );

        presetValue(slot).set(encoded);
        SPEC.save();
    }

    public static ColorPair getPreset(int slot) {
        String encoded = presetValue(slot).get();

        if (encoded == null || encoded.isBlank()) {
            return null;
        }

        String[] parts = encoded.split(":", -1);

        if (parts.length != 5) {
            return null;
        }

        try {
            int text = Integer.parseUnsignedInt(parts[0], 16);
            int background = Integer.parseUnsignedInt(parts[1], 16);

            boolean gradientEnabled;

            if (parts[2].equals("1")) {
                gradientEnabled = true;
            } else if (parts[2].equals("0")) {
                gradientEnabled = false;
            } else {
                return null;
            }

            int gradientStart = Integer.parseInt(parts[3], 16);
            int gradientEnd = Integer.parseInt(parts[4], 16);

            return new ColorPair(
                    text,
                    background,
                    gradientEnabled,
                    gradientStart,
                    gradientEnd
            );

        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static ModConfigSpec.ConfigValue<String> presetValue(int slot) {
        return switch (slot) {
            case 0 -> PRESET_1;
            case 1 -> PRESET_2;
            case 2 -> PRESET_3;
            case 3 -> PRESET_4;
            case 4 -> PRESET_5;
            default -> throw new IllegalArgumentException(
                    "Invalid preset slot: " + slot
            );
        };
    }
}
