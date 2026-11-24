// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.codedefinedgui.api.widget.IconToggleButtonWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/**
 * An icon button whose size differs from the 20px default. cdg draws the icon at its native size at a whole-pixel offset, which is not
 * centred for a smaller button. This one draws the background at the button size, and the icon scaled to {@code iconSize}. The icon is
 * placed so the centre of its visible pixels, not of its canvas, lands on the centre of the button.
 */
final class ScaledIconButtonWidget extends IconToggleButtonWidget {

    private final GuiSprite icon;
    private final Runnable onPress;
    private final float scale;
    private final float inkCentreX;
    private final float inkCentreY;

    /**
     * @param size the button's width and height in pixels
     * @param icon the icon, a square sprite
     * @param iconSize the icon's width and height in pixels after scaling
     * @param inkLeft the left edge of the icon's visible pixels, in pixels of the icon's canvas
     * @param inkTop the top edge of the visible pixels, in pixels of the canvas
     * @param inkRight the right edge of the visible pixels (exclusive), in pixels of the canvas
     * @param inkBottom the bottom edge of the visible pixels (exclusive), in pixels of the canvas
     */
    ScaledIconButtonWidget(int x, int y, int size, GuiSprite icon, int iconSize, int inkLeft, int inkTop, int inkRight, int inkBottom,
            Component tooltip, Runnable onPress) {
        super(x, y, size, icon, tooltip, onPress);
        this.icon = icon;
        this.onPress = onPress;
        this.scale = (float) iconSize / icon.width();
        this.inkCentreX = (inkLeft + inkRight) / 2.0F * this.scale;
        this.inkCentreY = (inkTop + inkBottom) / 2.0F * this.scale;
    }

    /**
     * cdg's icon button only reacts to clicks. Like {@code AbstractButton}, an active button runs its action on Enter, Space or the keypad
     * Enter, so the unlink action is reachable from the keyboard too.
     */
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.active || !event.isSelection()) {
            return false;
        }

        this.onPress.run();
        return true;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.backgroundSprite(mouseX, mouseY).extractRenderState(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());

        // Move the scaled canvas so that its visible pixels are centred on the button. The origin may be fractional
        float originX = this.getX() + this.getWidth() / 2.0F - this.inkCentreX;
        float originY = this.getY() + this.getHeight() / 2.0F - this.inkCentreY;
        graphics.pose().pushMatrix();
        graphics.pose().translate(originX, originY);
        graphics.pose().scale(this.scale, this.scale);
        this.icon.extractRenderState(graphics, 0, 0, this.icon.width(), this.icon.height());
        graphics.pose().popMatrix();
    }
}
