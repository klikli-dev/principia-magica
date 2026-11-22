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
 * Draws a 16x16 item icon. The stack is read from a supplier each frame, so the icon follows the data.
 */
public class ItemIconWidget extends AbstractWidget {

    public static final int SIZE = 16;

    private final Supplier<ItemStack> stack;

    public ItemIconWidget(int x, int y, Supplier<ItemStack> stack) {
        super(x, y, SIZE, SIZE, Component.empty());
        this.stack = stack;
        this.active = false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.item(this.stack.get(), this.getX(), this.getY());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
