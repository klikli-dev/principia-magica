// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import com.klikli_dev.codedefinedgui.api.style.GuiStyleProperties;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.principiamagica.client.gui.GuiStyles;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.DoubleSupplier;

/**
 * A horizontal bar: the background sprite, and the fill sprite drawn inside a one pixel border. The fill is cropped from the right
 * to the fraction, so the sprite is never stretched. The fraction is read from a supplier each frame.
 */
public class ProgressBar extends AbstractWidget {

    private final DoubleSupplier fraction;
    private StyleContext style = WidgetParts.defaultStyle();

    /**
     * @param x left edge
     * @param y top edge
     * @param width width in pixels, including the border
     * @param height height in pixels, including the border
     * @param fraction the filled part, 0 to 1, read each frame
     */
    public ProgressBar(int x, int y, int width, int height, DoubleSupplier fraction) {
        super(x, y, width, height, Component.empty());
        this.fraction = fraction;
        this.active = false;
    }

    public ProgressBar withStyle(StyleContext style) {
        this.style = style;
        return this;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        GuiSprite background = this.style.get(WidgetParts.PROGRESS, GuiStyleProperties.SPRITE, GuiStyles.sprite("bar_background"));
        background.extractRenderState(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());

        GuiSprite fill = this.style.get(WidgetParts.PROGRESS, WidgetParts.FILL_SPRITE, GuiStyles.sprite("bar_fill"));
        int innerWidth = Math.min(this.getWidth() - 2, fill.width());
        int filled = Mth.clamp(Math.round(innerWidth * (float) this.fraction.getAsDouble()), 0, innerWidth);
        if (filled > 0) {
            fill.extractRenderState(graphics, fill.width(), fill.height(), 0, 0, this.getX() + 1, this.getY() + 1, filled, Math.min(this.getHeight() - 2, fill.height()));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
