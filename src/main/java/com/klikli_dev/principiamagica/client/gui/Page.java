// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui;

import net.minecraft.network.chat.Component;

/**
 * A screen of widgets shown inside a {@link ContentRect}. Pages are shown and hidden by a {@link PageStack}.
 * <p>
 * Implementations typically create their widgets once (on the first {@link #show(PageContext)}), keep them as fields
 * and only re-add them to the {@link WidgetGroup} on every show. This keeps widget state (e.g. the scroll offset of a
 * {@link com.klikli_dev.principiamagica.client.gui.widget.ScrollList}) when a detail page is popped again.
 */
public interface Page {

    /**
     * @return the title of the page, e.g. used as a tooltip of its tab
     */
    Component title();

    /**
     * Called when the page becomes the top page of its stack. Add all widgets of the page to the context's group.
     *
     * @param context the group to add widgets to, the bounds to lay out in and the stack to push or pop pages on
     */
    void show(PageContext context);

    /**
     * Called every tick while the page is the top page of a visible stack. Use it to refresh widgets or to pop
     * the page if the data it shows no longer exists.
     */
    default void tick() {
    }

    /**
     * Called right before the page's widgets are removed from the screen, because the page is covered by a detail
     * page or its tab is deselected. Widgets are removed by the stack, this hook is only for page state.
     */
    default void hide() {
    }
}
