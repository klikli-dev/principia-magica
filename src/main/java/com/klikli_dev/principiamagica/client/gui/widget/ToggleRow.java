// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import com.klikli_dev.codedefinedgui.api.style.GuiStyleProperties;
import com.klikli_dev.codedefinedgui.api.style.StyleContext;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.codedefinedgui.api.texture.GuiSprites;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * A single-line row with a short on/off indicator and a label. Clicking it toggles the value.
 * The value is read from a supplier each frame, so the row follows the data.
 */
public class ToggleRow extends AbstractWidget {

    private final Supplier<Boolean> value;
    private final Runnable onToggle;
    private StyleContext style = WidgetParts.defaultStyle();

    /**
     * @param x left edge
     * @param y top edge
     * @param width width in pixels
     * @param height height in pixels
     * @param label the label, drawn at the left
     * @param value the current value
     * @param onToggle called on click; the server should set the negated value
     */
    public ToggleRow(int x, int y, int width, int height, Component label, Supplier<Boolean> value, Runnable onToggle) {
        super(x, y, width, height, label);
        this.value = value;
        this.onToggle = onToggle;
    }

    public ToggleRow withStyle(StyleContext style) {
        this.style = style;
        return this;
    }

    /**
     * @param description shown as tooltip, explains what the option does
     */
    public ToggleRow withDescription(Component description) {
        this.setTooltip(Tooltip.create(description));
        return this;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        GuiSprite sprite = this.isHovered()
                ? this.style.get(WidgetParts.BUTTON, GuiStyleProperties.HOVER_SPRITE, GuiSprites.FILTER_BUTTON_HOVER)
                : this.style.get(WidgetParts.BUTTON, GuiStyleProperties.SPRITE, GuiSprites.FILTER_BUTTON);
        sprite.extractRenderState(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());

        boolean on = this.value.get();
        GuiSprite indicator = on
                ? this.style.get(WidgetParts.TOGGLE, GuiStyleProperties.ON_SPRITE, GuiSprites.FILTER_INDICATOR_ON)
                : this.style.get(WidgetParts.TOGGLE, GuiStyleProperties.OFF_SPRITE, GuiSprites.FILTER_INDICATOR_OFF);
        int indicatorX = this.getX() + this.getWidth() - indicator.width() - 6;
        int indicatorY = this.getY() + (this.getHeight() - indicator.height()) / 2;
        indicator.extractRenderState(graphics, indicatorX, indicatorY, indicator.width(), indicator.height());

        int labelRight = indicatorX - 4;
        int color = this.style.get(WidgetParts.TOGGLE, GuiStyleProperties.TEXT_COLOR, WidgetParts.DEFAULT_TEXT_COLOR);
        graphics.enableScissor(this.getX() + 4, this.getY(), labelRight, this.getY() + this.getHeight());
        graphics.text(Minecraft.getInstance().font, this.getMessage(), this.getX() + 6, this.getY() + (this.getHeight() - 8) / 2, color, false);
        graphics.disableScissor();
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.onToggle.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
