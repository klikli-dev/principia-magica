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

/**
 * A rectangular button with a centered text label. Disabled buttons (inactive) draw a muted label and ignore clicks.
 */
public class TextButton extends AbstractWidget {

    private final Runnable onPress;
    private StyleContext style = WidgetParts.defaultStyle();

    /**
     * @param x left edge
     * @param y top edge
     * @param width width in pixels
     * @param height height in pixels
     * @param label the text, keep it short enough to fit the width
     * @param onPress run when the button is clicked
     */
    public TextButton(int x, int y, int width, int height, Component label, Runnable onPress) {
        super(x, y, width, height, label);
        this.onPress = onPress;
    }

    public TextButton withStyle(StyleContext style) {
        this.style = style;
        return this;
    }

    public TextButton withTooltip(Component tooltip) {
        this.setTooltip(Tooltip.create(tooltip));
        return this;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        GuiSprite sprite;
        if (!this.active) {
            sprite = this.style.get(WidgetParts.BUTTON, GuiStyleProperties.SPRITE, GuiSprites.FILTER_BUTTON);
        } else if (this.isHovered()) {
            sprite = this.style.get(WidgetParts.BUTTON, GuiStyleProperties.HOVER_SPRITE, GuiSprites.FILTER_BUTTON_HOVER);
        } else {
            sprite = this.style.get(WidgetParts.BUTTON, GuiStyleProperties.SPRITE, GuiSprites.FILTER_BUTTON);
        }
        sprite.extractRenderState(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());

        int color = this.active
                ? this.style.get(WidgetParts.BUTTON, GuiStyleProperties.TEXT_COLOR, WidgetParts.DEFAULT_TEXT_COLOR)
                : this.style.get(WidgetParts.BUTTON, GuiStyleProperties.COLOR, WidgetParts.DISABLED_TEXT_COLOR);
        graphics.centeredText(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 8) / 2, color);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.onPress.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
