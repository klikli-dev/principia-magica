// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * An invisible area that shows a tooltip on hover and never takes clicks, so the widgets and slots below it keep working.
 * Hide it with {@code visible = false} when the area should not show a tooltip, e.g. over a slot that holds an item.
 */
public class TooltipArea extends AbstractWidget {

    /**
     * @param x left edge
     * @param y top edge
     * @param width width in pixels
     * @param height height in pixels
     * @param tooltip the tooltip shown on hover
     */
    public TooltipArea(int x, int y, int width, int height, Component tooltip) {
        super(x, y, width, height, Component.empty());
        this.active = false;
        this.setTooltip(Tooltip.create(tooltip));
    }

    /**
     * Replaces the tooltip. Creates a new tooltip object, so call it only when the text changes.
     */
    public void setText(Component tooltip) {
        this.setTooltip(Tooltip.create(tooltip));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
