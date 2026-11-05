// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import com.klikli_dev.codedefinedgui.api.style.GuiStyleProperties;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Multi-line, word-wrapped, non-interactive text. The text is read from a supplier each frame.
 * The height is the number of wrapped lines times the font line height, so it is only known after the first frame.
 */
public class LabelWidget extends AbstractWidget {

    public static final int LINE_HEIGHT = 9;

    private final Supplier<Component> text;
    private StyleContext style = WidgetParts.defaultStyle();
    private int color = -1;
    private Component wrappedFor = Component.empty();
    private List<FormattedCharSequence> lines = List.of();

    /**
     * @param x left edge
     * @param y top edge
     * @param width the width the text wraps at
     * @param text the text to draw
     */
    public LabelWidget(int x, int y, int width, Supplier<Component> text) {
        super(x, y, width, LINE_HEIGHT, Component.empty());
        this.text = text;
        this.active = false;
    }

    public LabelWidget withStyle(StyleContext style) {
        this.style = style;
        return this;
    }

    /**
     * @param color an ARGB color, overrides the style's text color
     */
    public LabelWidget withColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        Component current = this.text.get();
        if (!Objects.equals(current, this.wrappedFor)) {
            this.wrappedFor = current;
            this.lines = font.split(current, this.getWidth());
            this.setHeight(Math.max(1, this.lines.size()) * LINE_HEIGHT);
        }

        int textColor = this.color != -1
                ? this.color
                : this.style.get(WidgetParts.TEXT, GuiStyleProperties.TEXT_COLOR, WidgetParts.DEFAULT_TEXT_COLOR);
        for (int i = 0; i < this.lines.size(); i++) {
            graphics.text(font, this.lines.get(i), this.getX(), this.getY() + i * LINE_HEIGHT, textColor, false);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
