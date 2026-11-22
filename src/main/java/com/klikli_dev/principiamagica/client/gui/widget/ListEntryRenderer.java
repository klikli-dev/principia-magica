// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Draws one row of a {@link ScrollList}. Called only for rows that are at least partially visible, with the graphics
 * already clipped to the list.
 *
 * @param <T> the entry type
 */
@FunctionalInterface
public interface ListEntryRenderer<T> {

    /**
     * @param graphics the graphics to draw on
     * @param entry the entry of this row
     * @param index the index of the entry in the list
     * @param x left edge of the row content
     * @param y top edge of the row, may be negative or extend past the list (clipped)
     * @param width width of the row content
     * @param height height of the row
     * @param hovered whether the mouse hovers this row
     * @param textColor the default text color of the list style
     */
    void extract(GuiGraphicsExtractor graphics, T entry, int index, int x, int y, int width, int height, boolean hovered, int textColor);
}
