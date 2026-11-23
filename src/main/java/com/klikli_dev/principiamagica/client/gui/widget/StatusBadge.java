// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import com.klikli_dev.codedefinedgui.api.style.GuiStyleProperties;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A colored dot followed by a short status text, e.g. "Working" or "No seeds". The explanation is shown as tooltip.
 * Call {@link #update} whenever the status changes; the badge is not interactive.
 */
public class StatusBadge extends AbstractWidget {

    public static final int HEIGHT = 10;
    private static final int DOT_SIZE = 6;
    private static final int DOT_GAP = 4;
    private static final int OUTLINE_COLOR = 0xFF303030;

    private StyleContext style = WidgetParts.defaultStyle();
    private Component text;
    private int color;
    private @Nullable Component tooltip;

    /**
     * @param x left edge
     * @param y top edge
     * @param text the status text
     * @param color the ARGB color of the dot
     */
    public StatusBadge(int x, int y, Component text, int color) {
        super(x, y, 0, HEIGHT, text);
        this.text = text;
        this.color = color;
        this.active = false;
        this.updateWidth();
    }

    public StatusBadge withStyle(StyleContext style) {
        this.style = style;
        return this;
    }

    /**
     * Updates the status. Tooltip objects are only recreated when the explanation changes.
     *
     * @param text the status text
     * @param color the ARGB color of the dot
     * @param tooltip the explanation, or null for no tooltip
     */
    public void update(Component text, int color, @Nullable Component tooltip) {
        this.text = text;
        this.color = color;
        this.updateWidth();
        if (!Objects.equals(tooltip, this.tooltip)) {
            this.tooltip = tooltip;
            this.setTooltip(tooltip != null ? Tooltip.create(tooltip) : null);
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int dotTop = this.getY() + (HEIGHT - DOT_SIZE) / 2;
        graphics.fill(this.getX(), dotTop, this.getX() + DOT_SIZE, dotTop + DOT_SIZE, OUTLINE_COLOR);
        graphics.fill(this.getX() + 1, dotTop + 1, this.getX() + DOT_SIZE - 1, dotTop + DOT_SIZE - 1, this.color);

        int textColor = this.style.get(WidgetParts.TEXT, GuiStyleProperties.TEXT_COLOR, WidgetParts.DEFAULT_TEXT_COLOR);
        graphics.text(Minecraft.getInstance().font, this.text, this.getX() + DOT_SIZE + DOT_GAP, this.getY() + 1, textColor, false);
    }

    private void updateWidth() {
        this.setWidth(DOT_SIZE + DOT_GAP + Minecraft.getInstance().font.width(this.text.getVisualOrderText()));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
