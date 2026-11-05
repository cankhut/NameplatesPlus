package com.github.cankhut.nameplatesplus.client;

import com.github.cankhut.nameplatesplus.ClientConfig;
import com.github.cankhut.nameplatesplus.ColorPair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ColorPickerScreen extends Screen {

    private static final int PANEL_W = 214;
    private static final int PANEL_H = 242;
    private static final int PAD = 9;
    private static final int CONTENT_W = PANEL_W - PAD * 2;

    private static final int SV_H = 64;
    private static final int STRIP_H = 10;

    private static final int TAB_Y = 22;
    private static final int SV_Y = 44;
    private static final int HUE_Y = 114;
    private static final int ALPHA_Y = 130;
    private static final int ROW_Y = 146;
    private static final int PRESET_Y = 168;
    private static final int TOGGLE_Y = 192;
    private static final int BUTTON_Y = 214;
    private static final int SWATCH_COUNT = 10;
    private static final int SWATCH_W = 18;

    private static final int[] PRESETS = {
            0xFFFFFF, 0xAAAAAA, 0x000000, 0xE53935, 0xFB8C00,
            0xFDD835, 0x43A047, 0x00ACC1, 0x1E88E5, 0x8E24AA
    };

    private enum Drag {NONE, SV, HUE, ALPHA}

    private final @Nullable Screen parent;

    private int textColor;
    private int backgroundColor;
    private boolean showOwnNameplate;

    private boolean editingText = true;
    private float hue;
    private float sat;
    private float val;
    private int alpha;

    private Drag drag = Drag.NONE;
    private boolean suppressResponder;
    private boolean showOfflineNotice;

    private EditBox hexBox;
    private Button textTab;
    private Button backgroundTab;

    private int left;
    private int top;

    public ColorPickerScreen(@Nullable Screen parent) {
        super(Component.translatable("screen.nameplatesplus.title"));
        this.parent = parent;
        ColorPair saved = ClientConfig.get();
        this.textColor = saved.text();
        this.backgroundColor = saved.background();
        this.showOwnNameplate = ClientConfig.SHOW_OWN_NAMEPLATE.getAsBoolean();
    }

    @Override
    protected void init() {
        left = (this.width - PANEL_W) / 2;
        top = (this.height - PANEL_H) / 2;
        int contentX = left + PAD;

        textTab = Button.builder(Component.translatable("screen.nameplatesplus.text"), b -> selectTarget(true))
                .bounds(contentX, top + TAB_Y, 96, 16)
                .build();
        backgroundTab = Button.builder(Component.translatable("screen.nameplatesplus.background"), b -> selectTarget(false))
                .bounds(contentX + 100, top + TAB_Y, 96, 16)
                .build();
        addRenderableWidget(textTab);
        addRenderableWidget(backgroundTab);

        hexBox = new EditBox(this.font, contentX + 22, top + ROW_Y, 72, 16, Component.empty());
        hexBox.setMaxLength(7);
        hexBox.setResponder(this::onHexTyped);
        addRenderableWidget(hexBox);

        addRenderableWidget(CycleButton.onOffBuilder(showOwnNameplate)
                .create(contentX, top + TOGGLE_Y, CONTENT_W, 16,
                        Component.translatable("screen.nameplatesplus.show_own"),
                        (button, value) -> showOwnNameplate = value));

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> {
            apply();
            onClose();
        }).bounds(contentX, top + BUTTON_Y, 62, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("screen.nameplatesplus.reset"), b -> {
            textColor = ColorPair.DEFAULT_TEXT;
            backgroundColor = ColorPair.DEFAULT_BACKGROUND;
            loadTarget();
        }).bounds(contentX + 67, top + BUTTON_Y, 62, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                .bounds(contentX + 134, top + BUTTON_Y, 62, 20).build());

        showOfflineNotice = ClientGameEvents.isConnected() && !ClientGameEvents.serverHasMod();
        loadTarget();
    }

    private void selectTarget(boolean text) {
        storeTarget();
        editingText = text;
        loadTarget();
    }

    private void loadTarget() {
        int argb = editingText ? textColor : backgroundColor;
        alpha = ColorPair.alphaOf(argb);
        setFromRgb(argb & 0xFFFFFF);
        updateHexBox();
    }

    private void storeTarget() {
        int argb = ColorPair.withAlpha(hsvToRgb(hue, sat, val), alpha);
        if (editingText) {
            textColor = argb;
        } else {
            backgroundColor = argb;
        }
    }

    private void updateHexBox() {
        if (hexBox == null) {
            return;
        }
        suppressResponder = true;
        hexBox.setValue(ColorPair.toHex(hsvToRgb(hue, sat, val)));
        suppressResponder = false;
    }

    private void onHexTyped(String value) {
        if (suppressResponder) {
            return;
        }
        Integer rgb = ColorPair.parseHex(value);
        if (rgb == null) {
            return;
        }
        setFromRgb(rgb);
        storeTarget();
    }

    private void setFromRgb(int rgb) {
        float[] hsv = rgbToHsv(rgb);
        if (hsv[1] > 0.0F) {
            hue = hsv[0];
        }
        sat = hsv[1];
        val = hsv[2];
    }

    private void apply() {
        storeTarget();
        ClientConfig.save(new ColorPair(textColor, backgroundColor), showOwnNameplate);
        ClientGameEvents.applyLocally();
        ClientGameEvents.sendToServer();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public void renderBackground(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(gg, mouseX, mouseY, partialTick);
        gg.fill(left, top, left + PANEL_W, top + PANEL_H, 0xF0101010);
        outline(gg, left, top, PANEL_W, PANEL_H, 0xFF3C3C3C);
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        super.render(gg, mouseX, mouseY, partialTick);

        int contentX = left + PAD;

        gg.drawCenteredString(this.font, this.title, left + PANEL_W / 2, top + 8, 0xFFFFFFFF);

        outline(gg, (editingText ? textTab : backgroundTab).getX() - 1,
                top + TAB_Y - 1, 98, 18, 0xFF6AA9FF);

        drawSaturationValue(gg, contentX, top + SV_Y);
        drawHueStrip(gg, contentX, top + HUE_Y);
        drawAlphaStrip(gg, contentX, top + ALPHA_Y);

        gg.fill(contentX, top + ROW_Y, contentX + 20, top + ROW_Y + 16, 0xFF202020);
        drawChecker(gg, contentX + 1, top + ROW_Y + 1, 18, 14);
        gg.fill(contentX + 1, top + ROW_Y + 1, contentX + 19, top + ROW_Y + 15,
                ColorPair.withAlpha(hsvToRgb(hue, sat, val), alpha));

        drawPreview(gg, contentX + 100, top + ROW_Y);
        drawPresets(gg, contentX, top + PRESET_Y);

        if (showOfflineNotice) {
            gg.drawCenteredString(this.font,
                    Component.translatable("screen.nameplatesplus.offline"),
                    this.width / 2, top + PANEL_H + 6, 0xFFAAAAAA);
        }
    }

    private void drawSaturationValue(GuiGraphics gg, int x, int y) {
        int pure = hsvToRgb(hue, 1.0F, 1.0F);
        for (int i = 0; i < CONTENT_W; i++) {
            int topColor = 0xFF000000 | lerpRgb(0xFFFFFF, pure, (float) i / (CONTENT_W - 1));
            gg.fillGradient(x + i, y, x + i + 1, y + SV_H, topColor, 0xFF000000);
        }
        int cursorX = x + Math.round(sat * (CONTENT_W - 1));
        int cursorY = y + Math.round((1.0F - val) * (SV_H - 1));
        outline(gg, cursorX - 3, cursorY - 3, 7, 7, 0xFF000000);
        outline(gg, cursorX - 2, cursorY - 2, 5, 5, 0xFFFFFFFF);
    }

    private void drawHueStrip(GuiGraphics gg, int x, int y) {
        for (int i = 0; i < CONTENT_W; i++) {
            gg.fill(x + i, y, x + i + 1, y + STRIP_H,
                    0xFF000000 | hsvToRgb((float) i / (CONTENT_W - 1), 1.0F, 1.0F));
        }
        drawSliderKnob(gg, x + Math.round(hue * (CONTENT_W - 1)), y);
    }

    private void drawAlphaStrip(GuiGraphics gg, int x, int y) {
        drawChecker(gg, x, y, CONTENT_W, STRIP_H);
        int rgb = hsvToRgb(hue, sat, val);
        for (int i = 0; i < CONTENT_W; i++) {
            int a = Math.round(255.0F * i / (CONTENT_W - 1));
            gg.fill(x + i, y, x + i + 1, y + STRIP_H, ColorPair.withAlpha(rgb, a));
        }
        drawSliderKnob(gg, x + Math.round(alpha / 255.0F * (CONTENT_W - 1)), y);
    }

    private void drawSliderKnob(GuiGraphics gg, int centreX, int y) {
        outline(gg, centreX - 2, y - 2, 5, STRIP_H + 4, 0xFF000000);
        outline(gg, centreX - 1, y - 1, 3, STRIP_H + 2, 0xFFFFFFFF);
    }

    private void drawPresets(GuiGraphics gg, int x, int y) {
        for (int i = 0; i < SWATCH_COUNT; i++) {
            int sx = x + i * (SWATCH_W + 1);
            gg.fill(sx, y, sx + SWATCH_W, y + SWATCH_W, 0xFF000000 | PRESETS[i]);
            outline(gg, sx, y, SWATCH_W, SWATCH_W, 0xFF202020);
        }
    }

    private void drawPreview(GuiGraphics gg, int x, int y) {
        String name = this.minecraft != null && this.minecraft.player != null
                ? this.minecraft.player.getGameProfile().getName()
                : "Player";

        int previewText = editingText
                ? ColorPair.withAlpha(hsvToRgb(hue, sat, val), alpha)
                : textColor;
        int previewBackground = editingText
                ? backgroundColor
                : ColorPair.withAlpha(hsvToRgb(hue, sat, val), alpha);

        int w = 96;
        drawChecker(gg, x, y, w, 16);
        int nameWidth = this.font.width(name);
        int nameX = x + (w - nameWidth) / 2;
        gg.fill(nameX - 2, y + 3, nameX + nameWidth + 2, y + 13, previewBackground);
        gg.drawString(this.font, name, nameX, y + 4, ColorPair.clampTextAlpha(previewText), false);
        outline(gg, x, y, w, 16, 0xFF202020);
    }

    private static void drawChecker(GuiGraphics gg, int x, int y, int w, int h) {
        for (int cy = 0; cy < h; cy += 4) {
            for (int cx = 0; cx < w; cx += 4) {
                boolean light = ((cx / 4) + (cy / 4)) % 2 == 0;
                gg.fill(x + cx, y + cy,
                        Math.min(x + cx + 4, x + w), Math.min(y + cy + 4, y + h),
                        light ? 0xFFBFBFBF : 0xFF7F7F7F);
            }
        }
    }

    private static void outline(GuiGraphics gg, int x, int y, int w, int h, int color) {
        gg.fill(x, y, x + w, y + 1, color);
        gg.fill(x, y + h - 1, x + w, y + h, color);
        gg.fill(x, y, x + 1, y + h, color);
        gg.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int contentX = left + PAD;

        if (inside(mouseX, mouseY, contentX, top + SV_Y, CONTENT_W, SV_H)) {
            drag = Drag.SV;
            dragTo(mouseX, mouseY);
            return true;
        }
        if (inside(mouseX, mouseY, contentX, top + HUE_Y, CONTENT_W, STRIP_H)) {
            drag = Drag.HUE;
            dragTo(mouseX, mouseY);
            return true;
        }
        if (inside(mouseX, mouseY, contentX, top + ALPHA_Y, CONTENT_W, STRIP_H)) {
            drag = Drag.ALPHA;
            dragTo(mouseX, mouseY);
            return true;
        }
        if (inside(mouseX, mouseY, contentX, top + PRESET_Y, SWATCH_COUNT * (SWATCH_W + 1), SWATCH_W)) {
            int index = (int) ((mouseX - contentX) / (SWATCH_W + 1));
            if (index >= 0 && index < SWATCH_COUNT) {
                setFromRgb(PRESETS[index]);
                storeTarget();
                updateHexBox();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (drag != Drag.NONE) {
            dragTo(mouseX, mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        drag = Drag.NONE;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void dragTo(double mouseX, double mouseY) {
        int contentX = left + PAD;
        float px = clamp01((float) (mouseX - contentX) / (CONTENT_W - 1));

        switch (drag) {
            case SV -> {
                sat = px;
                val = 1.0F - clamp01((float) (mouseY - (top + SV_Y)) / (SV_H - 1));
            }
            case HUE -> hue = px;
            case ALPHA -> alpha = Math.round(px * 255.0F);
            default -> {
                return;
            }
        }
        storeTarget();
        updateHexBox();
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static float clamp01(float v) {
        return v < 0.0F ? 0.0F : Math.min(v, 1.0F);
    }

    private static int lerpRgb(int from, int to, float t) {
        int r = Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
        int g = Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return (r << 16) | (g << 8) | b;
    }

    private static int hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float hp = (h - (float) Math.floor(h)) * 6.0F;
        float x = c * (1.0F - Math.abs(hp % 2.0F - 1.0F));
        float m = v - c;

        float r;
        float g;
        float b;
        if (hp < 1.0F) {
            r = c; g = x; b = 0.0F;
        } else if (hp < 2.0F) {
            r = x; g = c; b = 0.0F;
        } else if (hp < 3.0F) {
            r = 0.0F; g = c; b = x;
        } else if (hp < 4.0F) {
            r = 0.0F; g = x; b = c;
        } else if (hp < 5.0F) {
            r = x; g = 0.0F; b = c;
        } else {
            r = c; g = 0.0F; b = x;
        }

        int ri = Math.round((r + m) * 255.0F);
        int gi = Math.round((g + m) * 255.0F);
        int bi = Math.round((b + m) * 255.0F);
        return (ri << 16) | (gi << 8) | bi;
    }

    private static float[] rgbToHsv(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255.0F;
        float g = ((rgb >> 8) & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h = 0.0F;
        if (delta > 0.0F) {
            if (max == r) {
                h = ((g - b) / delta) % 6.0F;
            } else if (max == g) {
                h = (b - r) / delta + 2.0F;
            } else {
                h = (r - g) / delta + 4.0F;
            }
            h /= 6.0F;
            if (h < 0.0F) {
                h += 1.0F;
            }
        }

        float s = max == 0.0F ? 0.0F : delta / max;
        return new float[]{h, s, max};
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
