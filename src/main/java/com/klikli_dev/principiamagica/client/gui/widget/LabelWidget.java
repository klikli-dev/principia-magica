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
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Multi-line, word-wrapped, non-interactive text. The text is read from a supplier each frame.
 * The height is the number of wrapped lines times the font line height, so it is only known after the first frame.
 */
public class LabelWidget extends AbstractWidget {

    public static final int LINE_HEIGHT = 9;
    private static final String ELLIPSIS = "…";

    private final Supplier<Component> text;
    private StyleContext style = WidgetParts.defaultStyle();
    private int color = -1;
    private int maxLines = Integer.MAX_VALUE;
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

    /**
     * Moves the label and sets the width it wraps at. The text is wrapped again on the next frame, so the label can follow a layout that changes.
     *
     * @param x the new left edge
     * @param width the new wrap width
     */
    public void place(int x, int width) {
        this.setX(x);
        this.setWidth(Math.max(1, width));
        this.wrappedFor = Component.empty();
    }

    /**
     * Limits the label to a number of lines. If the text needs more, the last line is cut and ends with an ellipsis, so the text never
     * spills out of its inset.
     *
     * @param maxLines the number of lines to show, at least 1
     */
    public LabelWidget withMaxLines(int maxLines) {
        this.maxLines = Math.max(1, maxLines);
        this.wrappedFor = Component.empty();
        return this;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        Component current = this.text.get();
        if (!Objects.equals(current, this.wrappedFor)) {
            this.wrappedFor = current;
            this.lines = this.wrap(font, current);
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

    private List<FormattedCharSequence> wrap(Font font, Component current) {
        List<FormattedText> parts = font.getSplitter().splitLines(current, this.getWidth(), Style.EMPTY);
        if (parts.size() <= this.maxLines) {
            return font.split(current, this.getWidth());
        }

        List<FormattedCharSequence> result = new ArrayList<>();
        for (int i = 0; i < this.maxLines - 1; i++) {
            result.add(font.split(parts.get(i), this.getWidth()).getFirst());
        }

        String last = parts.get(this.maxLines - 1).getString();
        int ellipsisWidth = font.width(ELLIPSIS);
        result.add(font.split(Component.literal(font.plainSubstrByWidth(last, this.getWidth() - ellipsisWidth) + ELLIPSIS), this.getWidth()).getFirst());
        return result;
    }
}
