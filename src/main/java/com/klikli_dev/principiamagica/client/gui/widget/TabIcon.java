// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import com.klikli_dev.codedefinedgui.api.texture.GuiSprite;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/**
 * Draws a 16x16 icon on a {@link TabButton}.
 */
@FunctionalInterface
public interface TabIcon {

    /**
     * @param graphics the graphics to draw on
     * @param x the left edge of the 16x16 icon area
     * @param y the top edge of the 16x16 icon area
     */
    void extract(GuiGraphicsExtractor graphics, int x, int y);

    static TabIcon item(ItemStack stack) {
        return (graphics, x, y) -> graphics.item(stack, x, y);
    }

    static TabIcon sprite(GuiSprite sprite) {
        return (graphics, x, y) -> sprite.extractRenderState(graphics, x, y);
    }
}
