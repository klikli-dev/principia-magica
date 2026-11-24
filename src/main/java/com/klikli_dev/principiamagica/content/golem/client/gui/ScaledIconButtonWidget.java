// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import com.klikli_dev.codedefinedgui.api.widget.IconToggleButtonWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * An icon button whose size differs from the 20px default. cdg draws the icon at its native size, so a smaller button would clip it. This one
 * draws the background at the button size and the icon scaled to {@code iconSize}, centred.
 */
final class ScaledIconButtonWidget extends IconToggleButtonWidget {

    private final GuiSprite icon;
    private final int iconSize;

    /**
     * @param size the button's width and height in pixels
     * @param icon the icon, drawn scaled
     * @param iconSize the icon's width and height in pixels
     */
    ScaledIconButtonWidget(int x, int y, int size, GuiSprite icon, int iconSize, Component tooltip, Runnable onPress) {
        super(x, y, size, icon, tooltip, onPress);
        this.icon = icon;
        this.iconSize = iconSize;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.backgroundSprite(mouseX, mouseY).extractRenderState(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());

        float scale = (float) this.iconSize / this.icon.width();
        int offset = (this.getWidth() - this.iconSize) / 2;
        graphics.pose().pushMatrix();
        graphics.pose().translate(this.getX() + offset, this.getY() + offset);
        graphics.pose().scale(scale, scale);
        this.icon.extractRenderState(graphics, 0, 0, this.icon.width(), this.icon.height());
        graphics.pose().popMatrix();
    }
}
