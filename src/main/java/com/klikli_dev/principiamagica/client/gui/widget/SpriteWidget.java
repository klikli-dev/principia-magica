// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * Draws a sprite at a fixed size and never reacts to the mouse. Nine-slice and tiled sprites are stretched to the size.
 * Hide it with {@code visible = false} to remove it from the drawing, e.g. a ghost icon while the slot is filled.
 */
public class SpriteWidget extends AbstractWidget {

    private final Supplier<GuiSprite> sprite;

    /**
     * @param x left edge
     * @param y top edge
     * @param width width in pixels
     * @param height height in pixels
     * @param sprite the sprite to draw
     */
    public SpriteWidget(int x, int y, int width, int height, GuiSprite sprite) {
        this(x, y, width, height, () -> sprite);
    }

    /**
     * @param x left edge
     * @param y top edge
     * @param width width in pixels
     * @param height height in pixels
     * @param sprite the sprite to draw, read each frame, e.g. a status icon that changes
     */
    public SpriteWidget(int x, int y, int width, int height, Supplier<GuiSprite> sprite) {
        super(x, y, width, height, Component.empty());
        this.sprite = sprite;
        this.active = false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.sprite.get().extractRenderState(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
