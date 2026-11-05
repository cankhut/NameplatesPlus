
package com.github.cankhut.nameplatesplus.client;

import com.github.cankhut.nameplatesplus.ClientConfig;
import com.github.cankhut.nameplatesplus.ColorPair;
import com.github.cankhut.nameplatesplus.client.render.GradientRenderContext;
import com.github.cankhut.nameplatesplus.util.Gradient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ColorPickerScreen extends Screen {

    private static final int MAX_PANEL_W = 320;
    private static final int MAX_PANEL_H = 172;

    private static final int PAD = 10;
    private static final int COLUMN_GAP = 8;

    private static final int TAB_GAP = 2;

    private static final int SV_Y = 34;
    private static final int HUE_Y = 79;
    private static final int ALPHA_Y = 89;
    private static final int HEX_Y = 100;

    private static final int PREVIEW_Y = 34;
    private static final int PREVIEW_H = 18;

    private static final int PRESET_TITLE_Y = 55;
    private static final int PRESET_START_Y = 66;
    private static final int PRESET_ROW_H = 13;
    private static final int PRESET_ROW_GAP = 1;
    private static final int PRESET_COUNT = 5;

    private static final int TOGGLE_Y = 137;
    private static final int ACTION_Y = 153;

    private static final int[] PRESETS = {
            0xFFFFFF,
            0xAAAAAA,
            0x000000,
            0xE53935,
            0xFB8C00,
            0xFDD835,
            0x43A047,
            0x00ACC1,
            0x1E88E5,
            0x8E24AA
    };

    private enum Drag {
        NONE,
        SV,
        HUE,
        ALPHA
    }

    private enum ColorTarget {
        TEXT,
        BACKGROUND,
        GRADIENT_START,
        GRADIENT_END
    }

    private final @Nullable Screen parent;

    private int textColor;
    private int backgroundColor;

    private boolean textGradientEnabled;
    private int textGradientStart;
    private int textGradientEnd;

    private boolean showOwnNameplate;

    private ColorTarget target = ColorTarget.TEXT;

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
    private Button gradientStartTab;
    private Button gradientEndTab;

    private Button doneButton;
    private Button resetButton;
    private Button cancelButton;

    private int left;
    private int top;

    private int panelWidth;
    private int panelHeight;

    private int contentX;
    private int contentWidth;

    private int leftColumnWidth;
    private int rightX;
    private int rightColumnWidth;

    private int presetPreviewWidth;
    private int presetActionWidth;

    private float verticalScale = 1.0F;

    public ColorPickerScreen(@Nullable Screen parent) {
        super(Component.translatable("screen.nameplatesplus.title"));

        this.parent = parent;

        ColorPair saved = ClientConfig.get();

        textColor = saved.text();
        backgroundColor = saved.background();

        textGradientEnabled = saved.textGradientEnabled();
        textGradientStart = saved.textGradientStart();
        textGradientEnd = saved.textGradientEnd();

        showOwnNameplate =
                ClientConfig.SHOW_OWN_NAMEPLATE.getAsBoolean();
    }

    private int sy(int value) {
        return Math.round(value * verticalScale);
    }

    private int topY(int offset) {
        return top + sy(offset);
    }

    private int scaled(int value) {
        return Math.max(1, sy(value));
    }

    private int presetRowY(int slot) {
        return topY(PRESET_START_Y)
                + slot * (presetRowHeight() + presetRowGap());
    }

    private int presetRowHeight() {
        return Math.max(11, scaled(PRESET_ROW_H));
    }

    private int presetRowGap() {
        return Math.max(1, scaled(PRESET_ROW_GAP));
    }

    private int presetPreviewX() {
        return rightX + 10;
    }

    private int presetLoadX() {
        return presetPreviewX() + presetPreviewWidth + 2;
    }

    private int presetSaveX() {
        return presetLoadX() + presetActionWidth + 2;
    }

    private int toggleWidth() {
        return (contentWidth - 4) / 2;
    }

    private int toggleY() {
        return topY(TOGGLE_Y);
    }

    private int actionY() {
        return topY(ACTION_Y);
    }

    @Override
    protected void init() {
        panelWidth = Math.min(MAX_PANEL_W, this.width - 8);
        panelHeight = Math.min(MAX_PANEL_H, this.height - 8);

        left = (this.width - panelWidth) / 2;
        top = (this.height - panelHeight) / 2;

        verticalScale = Math.min(
                1.0F,
                Math.max(0.65F, (panelHeight - 8) / 164.0F)
        );

        contentX = left + PAD;
        contentWidth = panelWidth - PAD * 2;

        int availableColumns = contentWidth - COLUMN_GAP;

        leftColumnWidth = Math.round(availableColumns * 0.58F);
        rightColumnWidth = availableColumns - leftColumnWidth;

        rightX = contentX + leftColumnWidth + COLUMN_GAP;

        presetActionWidth = Math.max(
                18,
                Math.min(28, (rightColumnWidth - 26) / 2)
        );

        presetPreviewWidth = Math.max(
                12,
                rightColumnWidth - 14 - presetActionWidth * 2
        );

        createTabs();
        createHexBox();
        createActionButtons();

        showOfflineNotice =
                ClientGameEvents.isConnected()
                        && !ClientGameEvents.serverHasMod();

        loadTarget();
    }

    private void createTabs() {
        int tabWidth = (contentWidth - TAB_GAP * 3) / 4;
        int tabHeight = Math.max(12, scaled(14));
        int tabY = topY(15);

        textTab = Button.builder(
                        Component.literal("Text"),
                        b -> selectTarget(ColorTarget.TEXT)
                )
                .bounds(
                        contentX,
                        tabY,
                        tabWidth,
                        tabHeight
                )
                .build();

        backgroundTab = Button.builder(
                        Component.literal("BG"),
                        b -> selectTarget(ColorTarget.BACKGROUND)
                )
                .bounds(
                        contentX + tabWidth + TAB_GAP,
                        tabY,
                        tabWidth,
                        tabHeight
                )
                .build();

        gradientStartTab = Button.builder(
                        Component.literal("Grad A"),
                        b -> selectTarget(ColorTarget.GRADIENT_START)
                )
                .bounds(
                        contentX + (tabWidth + TAB_GAP) * 2,
                        tabY,
                        tabWidth,
                        tabHeight
                )
                .build();

        gradientEndTab = Button.builder(
                        Component.literal("Grad B"),
                        b -> selectTarget(ColorTarget.GRADIENT_END)
                )
                .bounds(
                        contentX + (tabWidth + TAB_GAP) * 3,
                        tabY,
                        tabWidth,
                        tabHeight
                )
                .build();

        addRenderableWidget(textTab);
        addRenderableWidget(backgroundTab);
        addRenderableWidget(gradientStartTab);
        addRenderableWidget(gradientEndTab);
    }

    private void createHexBox() {
        int hexX = contentX + 23;
        int hexY = topY(HEX_Y);

        int hexWidth = Math.max(30, leftColumnWidth - 23);
        int hexHeight = Math.max(12, scaled(15));

        hexBox = new EditBox(
                this.font,
                hexX,
                hexY,
                hexWidth,
                hexHeight,
                Component.empty()
        );

        hexBox.setMaxLength(7);
        hexBox.setResponder(this::onHexTyped);

        addRenderableWidget(hexBox);
    }

    private void createActionButtons() {
        int gap = 5;
        int buttonWidth = (contentWidth - gap * 2) / 3;
        int buttonHeight = Math.max(13, scaled(16));
        int y = actionY();

        doneButton = Button.builder(
                        Component.translatable("gui.done"),
                        b -> {
                            apply();
                            onClose();
                        }
                )
                .bounds(
                        contentX,
                        y,
                        buttonWidth,
                        buttonHeight
                )
                .build();

        resetButton = Button.builder(
                        Component.translatable("screen.nameplatesplus.reset"),
                        b -> resetColors()
                )
                .bounds(
                        contentX + buttonWidth + gap,
                        y,
                        buttonWidth,
                        buttonHeight
                )
                .build();

        cancelButton = Button.builder(
                        Component.translatable("gui.cancel"),
                        b -> onClose()
                )
                .bounds(
                        contentX + (buttonWidth + gap) * 2,
                        y,
                        buttonWidth,
                        buttonHeight
                )
                .build();

        addRenderableWidget(doneButton);
        addRenderableWidget(resetButton);
        addRenderableWidget(cancelButton);
    }

    private ColorPair currentColors() {
        return new ColorPair(
                textColor,
                backgroundColor,
                textGradientEnabled,
                textGradientStart,
                textGradientEnd
        );
    }

    private void selectTarget(ColorTarget newTarget) {
        storeTarget();

        target = newTarget;

        loadTarget();
    }

    private boolean isGradientTarget() {
        return target == ColorTarget.GRADIENT_START
                || target == ColorTarget.GRADIENT_END;
    }

    private void loadTarget() {
        int rgb;

        switch (target) {
            case TEXT -> {
                alpha = ColorPair.alphaOf(textColor);
                rgb = textColor & 0xFFFFFF;
            }

            case BACKGROUND -> {
                alpha = ColorPair.alphaOf(backgroundColor);
                rgb = backgroundColor & 0xFFFFFF;
            }

            case GRADIENT_START -> {
                alpha = 255;
                rgb = textGradientStart;
            }

            case GRADIENT_END -> {
                alpha = 255;
                rgb = textGradientEnd;
            }

            default -> {
                alpha = 255;
                rgb = ColorPair.DEFAULT_TEXT & 0xFFFFFF;
            }
        }

        setFromRgb(rgb);
        updateHexBox();
    }

    private void storeTarget() {
        int rgb = hsvToRgb(hue, sat, val);

        switch (target) {
            case TEXT ->
                    textColor = ColorPair.withAlpha(rgb, alpha);

            case BACKGROUND ->
                    backgroundColor = ColorPair.withAlpha(rgb, alpha);

            case GRADIENT_START ->
                    textGradientStart = rgb & 0xFFFFFF;

            case GRADIENT_END ->
                    textGradientEnd = rgb & 0xFFFFFF;
        }
    }

    private void updateHexBox() {
        if (hexBox == null) {
            return;
        }

        suppressResponder = true;

        hexBox.setValue(
                ColorPair.toHex(hsvToRgb(hue, sat, val))
        );

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

    private void resetColors() {
        textColor = ColorPair.DEFAULT_TEXT;
        backgroundColor = ColorPair.DEFAULT_BACKGROUND;

        textGradientEnabled = false;

        textGradientStart = ColorPair.DEFAULT_GRADIENT_START;
        textGradientEnd = ColorPair.DEFAULT_GRADIENT_END;

        loadTarget();
    }

    private void savePreset(int slot) {
        storeTarget();

        ClientConfig.savePreset(
                slot,
                currentColors()
        );
    }

    private void loadPreset(int slot) {
        ColorPair preset = ClientConfig.getPreset(slot);

        if (preset == null) {
            return;
        }

        textColor = preset.text();
        backgroundColor = preset.background();

        textGradientEnabled = preset.textGradientEnabled();
        textGradientStart = preset.textGradientStart();
        textGradientEnd = preset.textGradientEnd();

        loadTarget();

        // Apply immediately.
        ClientConfig.save(preset, showOwnNameplate);

        ClientGameEvents.applyLocally();
        ClientGameEvents.sendToServer(preset);
    }

    private void apply() {
        storeTarget();

        ColorPair colors = currentColors();

        ClientConfig.save(colors, showOwnNameplate);

        ClientGameEvents.applyLocally();
        ClientGameEvents.sendToServer(colors);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public void renderBackground(
            GuiGraphics gg,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.renderBackground(gg, mouseX, mouseY, partialTick);

        gg.fill(
                left,
                top,
                left + panelWidth,
                top + panelHeight,
                0xF0101010
        );

        outline(
                gg,
                left,
                top,
                panelWidth,
                panelHeight,
                0xFF3C3C3C
        );
    }

    @Override
    public void render(
            GuiGraphics gg,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.render(gg, mouseX, mouseY, partialTick);

        // Header

        gg.drawCenteredString(
                this.font,
                this.title,
                left + panelWidth / 2,
                topY(4),
                0xFFFFFFFF
        );

        if (showOfflineNotice) {
            gg.drawString(
                    this.font,
                    "Offline",
                    left + panelWidth - font.width("Offline") - PAD,
                    topY(5),
                    0xFFAAAAAA
            );
        }

        // Selected tab outline

        drawTabOutline(gg, textTab, target == ColorTarget.TEXT);
        drawTabOutline(gg, backgroundTab, target == ColorTarget.BACKGROUND);
        drawTabOutline(gg, gradientStartTab, target == ColorTarget.GRADIENT_START);
        drawTabOutline(gg, gradientEndTab, target == ColorTarget.GRADIENT_END);

        // Left column: color editor

        drawSaturationValue(
                gg,
                contentX,
                topY(SV_Y)
        );

        drawHueStrip(
                gg,
                contentX,
                topY(HUE_Y)
        );

        if (isGradientTarget()) {
            gg.fill(
                    contentX,
                    topY(ALPHA_Y),
                    contentX + leftColumnWidth,
                    topY(ALPHA_Y) + Math.max(6, scaled(7)),
                    0xFF303030
            );

            gg.drawCenteredString(
                    this.font,
                    "RGB only",
                    contentX + leftColumnWidth / 2,
                    topY(ALPHA_Y),
                    0xFFBBBBBB
            );
        } else {
            drawAlphaStrip(
                    gg,
                    contentX,
                    topY(ALPHA_Y)
            );
        }

        // Selected color swatch

        int selectedColor = isGradientTarget()
                ? hsvToRgb(hue, sat, val)
                : ColorPair.withAlpha(
                hsvToRgb(hue, sat, val),
                alpha
        );

        int hexY = topY(HEX_Y);
        int hexHeight = Math.max(12, scaled(15));

        gg.fill(
                contentX,
                hexY,
                contentX + 19,
                hexY + hexHeight,
                0xFF202020
        );

        drawChecker(
                gg,
                contentX + 1,
                hexY + 1,
                17,
                hexHeight - 2
        );

        gg.fill(
                contentX + 1,
                hexY + 1,
                contentX + 18,
                hexY + hexHeight - 1,
                selectedColor
        );

        // Right column: preview and presets

        gg.drawString(
                this.font,
                "Preview",
                rightX,
                topY(25),
                0xFFAAAAAA
        );

        drawNameplatePreview(
                gg,
                rightX,
                topY(PREVIEW_Y),
                rightColumnWidth,
                scaled(PREVIEW_H),
                currentColors()
        );

        gg.drawString(
                this.font,
                "Presets",
                rightX,
                topY(PRESET_TITLE_Y),
                0xFFFFFFFF
        );

        drawPresetRows(
                gg,
                mouseX,
                mouseY
        );

        // Bottom toggles

        drawToggle(
                gg,
                contentX,
                toggleY(),
                toggleWidth(),
                scaled(14),
                "Gradient",
                textGradientEnabled,
                mouseX,
                mouseY
        );

        drawToggle(
                gg,
                contentX + toggleWidth() + 4,
                toggleY(),
                toggleWidth(),
                scaled(14),
                "Own name",
                showOwnNameplate,
                mouseX,
                mouseY
        );
    }

    private void drawTabOutline(
            GuiGraphics gg,
            Button button,
            boolean selected
    ) {
        if (!selected || button == null) {
            return;
        }

        outline(
                gg,
                button.getX() - 1,
                button.getY() - 1,
                button.getWidth() + 2,
                button.getHeight() + 2,
                0xFF6AA9FF
        );
    }

    private void drawSaturationValue(
            GuiGraphics gg,
            int x,
            int y
    ) {
        int width = leftColumnWidth;
        int height = Math.max(36, scaled(43));

        int pure = hsvToRgb(hue, 1.0F, 1.0F);

        for (int i = 0; i < width; i++) {
            int topColor = 0xFF000000 |
                    lerpRgb(
                            0xFFFFFF,
                            pure,
                            (float) i / Math.max(1, width - 1)
                    );

            gg.fillGradient(
                    x + i,
                    y,
                    x + i + 1,
                    y + height,
                    topColor,
                    0xFF000000
            );
        }

        int cursorX = x + Math.round(
                sat * (width - 1)
        );

        int cursorY = y + Math.round(
                (1.0F - val) * (height - 1)
        );

        outline(
                gg,
                cursorX - 3,
                cursorY - 3,
                7,
                7,
                0xFF000000
        );

        outline(
                gg,
                cursorX - 2,
                cursorY - 2,
                5,
                5,
                0xFFFFFFFF
        );
    }

    private void drawHueStrip(
            GuiGraphics gg,
            int x,
            int y
    ) {
        int width = leftColumnWidth;
        int height = Math.max(6, scaled(7));

        for (int i = 0; i < width; i++) {
            gg.fill(
                    x + i,
                    y,
                    x + i + 1,
                    y + height,
                    0xFF000000 |
                            hsvToRgb(
                                    (float) i / Math.max(1, width - 1),
                                    1.0F,
                                    1.0F
                            )
            );
        }

        drawSliderKnob(
                gg,
                x + Math.round(hue * (width - 1)),
                y,
                height
        );
    }

    private void drawAlphaStrip(
            GuiGraphics gg,
            int x,
            int y
    ) {
        int width = leftColumnWidth;
        int height = Math.max(6, scaled(7));

        drawChecker(
                gg,
                x,
                y,
                width,
                height
        );

        int rgb = hsvToRgb(hue, sat, val);

        for (int i = 0; i < width; i++) {
            int a = Math.round(
                    255.0F * i / Math.max(1, width - 1)
            );

            gg.fill(
                    x + i,
                    y,
                    x + i + 1,
                    y + height,
                    ColorPair.withAlpha(rgb, a)
            );
        }

        drawSliderKnob(
                gg,
                x + Math.round(alpha / 255.0F * (width - 1)),
                y,
                height
        );
    }

    private void drawSliderKnob(
            GuiGraphics gg,
            int centerX,
            int y,
            int height
    ) {
        outline(
                gg,
                centerX - 2,
                y - 2,
                5,
                height + 4,
                0xFF000000
        );

        outline(
                gg,
                centerX - 1,
                y - 1,
                3,
                height + 2,
                0xFFFFFFFF
        );
    }

    private void drawNameplatePreview(
            GuiGraphics gg,
            int x,
            int y,
            int width,
            int height,
            ColorPair colors
    ) {
        drawChecker(
                gg,
                x,
                y,
                width,
                height
        );

        String name = width >= 55 ? "Player" : "P";

        int textWidth = font.width(name);

        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - 8) / 2;

        gg.fill(
                textX - 3,
                textY - 2,
                textX + textWidth + 3,
                textY + 10,
                colors.background()
        );

        int color = ColorPair.clampTextAlpha(colors.text());

        if (colors.textGradientEnabled() && textWidth > 0) {
            Gradient gradient = new Gradient(
                    colors.textGradientStart(),
                    colors.textGradientEnd()
            );

            try (GradientRenderContext.Scope ignored =
                         GradientRenderContext.push(
                                 gradient,
                                 gg.pose().last().pose(),
                                 textX,
                                 textY,
                                 textWidth
                         )) {

                gg.drawString(
                        this.font,
                        name,
                        textX,
                        textY,
                        color,
                        false
                );
            }
        } else {
            gg.drawString(
                    this.font,
                    name,
                    textX,
                    textY,
                    color,
                    false
            );
        }

        outline(
                gg,
                x,
                y,
                width,
                height,
                0xFF303030
        );
    }

    private void drawPresetRows(
            GuiGraphics gg,
            int mouseX,
            int mouseY
    ) {
        int rowHeight = presetRowHeight();
        int previewX = presetPreviewX();

        for (int i = 0; i < PRESET_COUNT; i++) {
            int y = presetRowY(i);

            gg.drawString(
                    this.font,
                    Integer.toString(i + 1),
                    rightX,
                    y + 2,
                    0xFFBBBBBB
            );

            ColorPair preset = ClientConfig.getPreset(i);

            if (preset == null) {
                drawEmptyPresetPreview(
                        gg,
                        previewX,
                        y,
                        presetPreviewWidth,
                        rowHeight
                );
            } else {
                drawPresetPreview(
                        gg,
                        previewX,
                        y,
                        presetPreviewWidth,
                        rowHeight,
                        preset
                );
            }

            boolean hoveringLoad = inside(
                    mouseX,
                    mouseY,
                    presetLoadX(),
                    y,
                    presetActionWidth,
                    rowHeight
            );

            boolean hoveringSave = inside(
                    mouseX,
                    mouseY,
                    presetSaveX(),
                    y,
                    presetActionWidth,
                    rowHeight
            );

            drawPresetAction(
                    gg,
                    presetLoadX(),
                    y,
                    presetActionWidth,
                    rowHeight,
                    preset != null ? "Load" : "L",
                    preset != null,
                    hoveringLoad
            );

            drawPresetAction(
                    gg,
                    presetSaveX(),
                    y,
                    presetActionWidth,
                    rowHeight,
                    "Save",
                    true,
                    hoveringSave
            );
        }
    }

    private void drawPresetPreview(
            GuiGraphics gg,
            int x,
            int y,
            int width,
            int height,
            ColorPair colors
    ) {
        gg.fill(
                x,
                y,
                x + width,
                y + height,
                colors.background()
        );

        int barHeight = Math.max(3, height / 3);
        int barY = y + (height - barHeight) / 2;

        if (colors.textGradientEnabled()) {
            Gradient gradient = new Gradient(
                    colors.textGradientStart(),
                    colors.textGradientEnd()
            );

            for (int i = 0; i < width; i++) {
                float t = (float) i / Math.max(1, width - 1);

                gg.fill(
                        x + i,
                        barY,
                        x + i + 1,
                        barY + barHeight,
                        ColorPair.withAlpha(
                                gradient.colorAt(t),
                                ColorPair.alphaOf(colors.text())
                        )
                );
            }
        } else {
            gg.fill(
                    x,
                    barY,
                    x + width,
                    barY + barHeight,
                    colors.text()
            );
        }

        outline(
                gg,
                x,
                y,
                width,
                height,
                0xFF303030
        );
    }

    private void drawEmptyPresetPreview(
            GuiGraphics gg,
            int x,
            int y,
            int width,
            int height
    ) {
        drawChecker(
                gg,
                x,
                y,
                width,
                height
        );

        gg.drawCenteredString(
                this.font,
                "-",
                x + width / 2,
                y + 2,
                0xFFAAAAAA
        );

        outline(
                gg,
                x,
                y,
                width,
                height,
                0xFF303030
        );
    }

    private void drawPresetAction(
            GuiGraphics gg,
            int x,
            int y,
            int width,
            int height,
            String label,
            boolean enabled,
            boolean hovered
    ) {
        int background = !enabled
                ? 0xFF202020
                : hovered
                ? 0xFF505050
                : 0xFF383838;

        int textColor = enabled
                ? 0xFFFFFFFF
                : 0xFF777777;

        gg.fill(
                x,
                y,
                x + width,
                y + height,
                background
        );

        outline(
                gg,
                x,
                y,
                width,
                height,
                0xFF555555
        );

        String displayLabel = width < 25
                ? (label.equals("Load") ? "L" : "S")
                : label;

        gg.drawCenteredString(
                this.font,
                displayLabel,
                x + width / 2,
                y + (height - 8) / 2,
                textColor
        );
    }

    private void drawToggle(
            GuiGraphics gg,
            int x,
            int y,
            int width,
            int height,
            String label,
            boolean enabled,
            int mouseX,
            int mouseY
    ) {
        boolean hovered = inside(
                mouseX,
                mouseY,
                x,
                y,
                width,
                height
        );

        int background = hovered
                ? 0xFF505050
                : 0xFF303030;

        int statusColor = enabled
                ? 0xFF70D990
                : 0xFFAAAAAA;

        gg.fill(
                x,
                y,
                x + width,
                y + height,
                background
        );

        outline(
                gg,
                x,
                y,
                width,
                height,
                0xFF555555
        );

        gg.drawString(
                this.font,
                label,
                x + 4,
                y + (height - 8) / 2,
                0xFFFFFFFF
        );

        String status = enabled ? "ON" : "OFF";

        gg.drawString(
                this.font,
                status,
                x + width - font.width(status) - 4,
                y + (height - 8) / 2,
                statusColor
        );
    }

    private static void drawChecker(
            GuiGraphics gg,
            int x,
            int y,
            int width,
            int height
    ) {
        for (int cy = 0; cy < height; cy += 4) {
            for (int cx = 0; cx < width; cx += 4) {
                boolean light =
                        ((cx / 4) + (cy / 4)) % 2 == 0;

                gg.fill(
                        x + cx,
                        y + cy,
                        Math.min(x + cx + 4, x + width),
                        Math.min(y + cy + 4, y + height),
                        light ? 0xFFBFBFBF : 0xFF7F7F7F
                );
            }
        }
    }

    private static void outline(
            GuiGraphics gg,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        if (width <= 0 || height <= 0) {
            return;
        }

        gg.fill(x, y, x + width, y + 1, color);
        gg.fill(x, y + height - 1, x + width, y + height, color);
        gg.fill(x, y, x + 1, y + height, color);
        gg.fill(x + width - 1, y, x + width, y + height, color);
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button == 0) {
            // Preset actions

            for (int i = 0; i < PRESET_COUNT; i++) {
                int y = presetRowY(i);
                int height = presetRowHeight();

                if (!inside(
                        mouseX,
                        mouseY,
                        presetLoadX(),
                        y,
                        presetActionWidth,
                        height
                ) && !inside(
                        mouseX,
                        mouseY,
                        presetSaveX(),
                        y,
                        presetActionWidth,
                        height
                )) {
                    continue;
                }

                if (inside(
                        mouseX,
                        mouseY,
                        presetLoadX(),
                        y,
                        presetActionWidth,
                        height
                )) {
                    loadPreset(i);
                } else {
                    savePreset(i);
                }

                return true;
            }

            // Gradient toggle

            if (inside(
                    mouseX,
                    mouseY,
                    contentX,
                    toggleY(),
                    toggleWidth(),
                    scaled(14)
            )) {
                textGradientEnabled = !textGradientEnabled;
                return true;
            }

            // Own nameplate toggle

            if (inside(
                    mouseX,
                    mouseY,
                    contentX + toggleWidth() + 4,
                    toggleY(),
                    toggleWidth(),
                    scaled(14)
            )) {
                showOwnNameplate = !showOwnNameplate;
                return true;
            }

            // Saturation / value

            if (inside(
                    mouseX,
                    mouseY,
                    contentX,
                    topY(SV_Y),
                    leftColumnWidth,
                    Math.max(36, scaled(43))
            )) {
                drag = Drag.SV;
                dragTo(mouseX, mouseY);
                return true;
            }

            // Hue

            if (inside(
                    mouseX,
                    mouseY,
                    contentX,
                    topY(HUE_Y),
                    leftColumnWidth,
                    Math.max(6, scaled(7))
            )) {
                drag = Drag.HUE;
                dragTo(mouseX, mouseY);
                return true;
            }

            // Alpha

            if (!isGradientTarget() && inside(
                    mouseX,
                    mouseY,
                    contentX,
                    topY(ALPHA_Y),
                    leftColumnWidth,
                    Math.max(6, scaled(7))
            )) {
                drag = Drag.ALPHA;
                dragTo(mouseX, mouseY);
                return true;
            }

            // Preset color swatches

            int swatchY = topY(HEX_Y) + scaled(18);

            if (inside(
                    mouseX,
                    mouseY,
                    contentX,
                    swatchY,
                    leftColumnWidth,
                    scaled(10)
            )) {
                int swatchWidth = Math.max(
                        1,
                        leftColumnWidth / PRESETS.length
                );

                int index = (int) (
                        (mouseX - contentX) / swatchWidth
                );

                index = Math.max(
                        0,
                        Math.min(PRESETS.length - 1, index)
                );

                setFromRgb(PRESETS[index]);
                storeTarget();
                updateHexBox();

                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        if (drag != Drag.NONE) {
            dragTo(mouseX, mouseY);
            return true;
        }

        return super.mouseDragged(
                mouseX,
                mouseY,
                button,
                dragX,
                dragY
        );
    }

    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        drag = Drag.NONE;

        return super.mouseReleased(
                mouseX,
                mouseY,
                button
        );
    }

    private void dragTo(
            double mouseX,
            double mouseY
    ) {
        float px = clamp01(
                (float) (mouseX - contentX)
                        / Math.max(1, leftColumnWidth - 1)
        );

        switch (drag) {
            case SV -> {
                sat = px;

                val = 1.0F - clamp01(
                        (float) (mouseY - topY(SV_Y))
                                / Math.max(1, scaled(43) - 1)
                );
            }

            case HUE -> hue = px;

            case ALPHA -> {
                if (!isGradientTarget()) {
                    alpha = Math.round(px * 255.0F);
                }
            }

            default -> {
                return;
            }
        }

        storeTarget();
        updateHexBox();
    }

    private static boolean inside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }

    private static float clamp01(float value) {
        return value < 0.0F
                ? 0.0F
                : Math.min(value, 1.0F);
    }

    private static int lerpRgb(
            int from,
            int to,
            float t
    ) {
        int r = Math.round(
                ((from >> 16) & 0xFF)
                        + (((to >> 16) & 0xFF)
                        - ((from >> 16) & 0xFF)) * t
        );

        int g = Math.round(
                ((from >> 8) & 0xFF)
                        + (((to >> 8) & 0xFF)
                        - ((from >> 8) & 0xFF)) * t
        );

        int b = Math.round(
                (from & 0xFF)
                        + ((to & 0xFF) - (from & 0xFF)) * t
        );

        return (r << 16) | (g << 8) | b;
    }

    private static int hsvToRgb(
            float h,
            float s,
            float v
    ) {
        float c = v * s;
        float hp = (h - (float) Math.floor(h)) * 6.0F;
        float x = c * (1.0F - Math.abs(hp % 2.0F - 1.0F));
        float m = v - c;

        float r;
        float g;
        float b;

        if (hp < 1.0F) {
            r = c;
            g = x;
            b = 0.0F;
        } else if (hp < 2.0F) {
            r = x;
            g = c;
            b = 0.0F;
        } else if (hp < 3.0F) {
            r = 0.0F;
            g = c;
            b = x;
        } else if (hp < 4.0F) {
            r = 0.0F;
            g = x;
            b = c;
        } else if (hp < 5.0F) {
            r = x;
            g = 0.0F;
            b = c;
        } else {
            r = c;
            g = 0.0F;
            b = x;
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
