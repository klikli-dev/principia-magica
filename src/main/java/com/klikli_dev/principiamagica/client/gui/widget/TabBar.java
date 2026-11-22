// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui.widget;

import com.klikli_dev.principiamagica.client.gui.WidgetGroup;

import java.util.List;

/**
 * Places a vertical column of {@link TabButton}s and keeps their selected state in sync.
 * This is a layout helper, not a widget: the buttons are added to the given {@link WidgetGroup}.
 */
public final class TabBar {

    /**
     * How far the tab bar overlaps the frame it is attached to, in pixels.
     */
    public static final int OVERLAP = 4;
    public static final int TOP_MARGIN = 4;
    public static final int SPACING = 2;

    private final List<TabButton> buttons;

    /**
     * @param group the group the buttons are added to
     * @param x left edge of the column
     * @param y top edge of the first button
     * @param spacing vertical gap between buttons
     * @param buttons the buttons, top to bottom
     */
    public TabBar(WidgetGroup group, int x, int y, int spacing, List<TabButton> buttons) {
        this.buttons = List.copyOf(buttons);
        for (int i = 0; i < this.buttons.size(); i++) {
            TabButton button = this.buttons.get(i);
            button.setX(x);
            button.setY(y + i * (TabButton.SIZE + spacing));
            group.add(button);
        }
    }

    /**
     * Marks the button at the index as selected and all others as not selected.
     */
    public void setSelected(int index) {
        for (int i = 0; i < this.buttons.size(); i++) {
            this.buttons.get(i).setSelected(i == index);
        }
    }
}
