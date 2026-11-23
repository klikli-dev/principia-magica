// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * Draws an item icon at a given size. The item is drawn at 16x16 and scaled with the pose, so the item model is untouched.
 * The stack is read from a supplier each frame, and nothing is drawn while it is empty.
 */
public class ItemIcon extends AbstractWidget {

    public static final int SIZE = 16;

    private final Supplier<ItemStack> stack;

    /**
     * @param x left edge
     * @param y top edge
     * @param size width and height in pixels, the item is scaled from its 16x16 icon
     * @param stack the item to draw, read each frame
     */
    public ItemIcon(int x, int y, int size, Supplier<ItemStack> stack) {
        super(x, y, size, size, Component.empty());
        this.stack = stack;
        this.active = false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ItemStack current = this.stack.get();
        if (current.isEmpty()) {
            return;
        }

        float scale = (float) this.getWidth() / SIZE;
        graphics.pose().pushMatrix();
        graphics.pose().translate(this.getX(), this.getY());
        graphics.pose().scale(scale, scale);
        graphics.item(current, 0, 0);
        graphics.pose().popMatrix();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
