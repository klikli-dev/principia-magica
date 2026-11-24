// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * A thin coloured bar under a container icon, showing the role of the link. Nothing is drawn while the colour supplier returns null.
 * Not part of cdg, as it is specific to the container role colours of the golem workplace.
 */
final class ContainerRoleMarkWidget extends AbstractWidget {

    private final Supplier<Integer> color;

    /**
     * @param x left edge
     * @param y top edge
     * @param width width in pixels
     * @param height height in pixels
     * @param color the ARGB colour to draw, read each frame, or null to draw nothing
     */
    ContainerRoleMarkWidget(int x, int y, int width, int height, Supplier<Integer> color) {
        super(x, y, width, height, Component.empty());
        this.color = color;
        this.active = false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Integer current = this.color.get();
        if (current != null) {
            graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), current);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
