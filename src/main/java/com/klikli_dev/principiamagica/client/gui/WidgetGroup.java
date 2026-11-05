// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui;

import com.klikli_dev.codedefinedgui.api.screen.GuiHost;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A set of widgets that are added to a {@link GuiHost} together and removed together.
 * <p>
 * Pages and tabs own one group each. Showing a page adds its widgets to the group, hiding it clears the group,
 * which removes the widgets from the screen (so they are neither rendered nor clickable anymore).
 */
public final class WidgetGroup {

    private final GuiHost host;
    private final Set<AbstractWidget> widgets = new LinkedHashSet<>();

    public WidgetGroup(GuiHost host) {
        this.host = host;
    }

    /**
     * Adds the widget to the host and to this group. Adding a widget that is already in the group does nothing.
     *
     * @return the widget, for chaining
     */
    public <T extends AbstractWidget> T add(T widget) {
        if (this.widgets.add(widget)) {
            this.host.addGuiWidget(widget);
        }
        return widget;
    }

    /**
     * Removes the widget from the host and from this group, if it is part of the group.
     */
    public void remove(AbstractWidget widget) {
        if (this.widgets.remove(widget)) {
            this.host.removeGuiWidget(widget);
        }
    }

    /**
     * Removes all widgets of this group from the host.
     */
    public void clear() {
        for (AbstractWidget widget : this.widgets) {
            this.host.removeGuiWidget(widget);
        }
        this.widgets.clear();
    }
}
