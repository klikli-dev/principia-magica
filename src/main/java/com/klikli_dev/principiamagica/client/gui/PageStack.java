// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A master-detail navigation stack. The root page is always at the bottom and is never popped.
 * <p>
 * Only the top page's widgets are present on the screen: pushing clears the group and shows the new page,
 * popping clears the group and shows the page below again. A stack that is not {@link #show() shown} keeps its pages
 * but has no widgets on the screen, which is how {@link TabHost} hides inactive tabs.
 */
public final class PageStack {

    private final Deque<Page> pages = new ArrayDeque<>();
    private final WidgetGroup widgets;
    private final ContentRect bounds;
    private boolean shown;

    public PageStack(Page root, WidgetGroup widgets, ContentRect bounds) {
        this.widgets = widgets;
        this.bounds = bounds;
        this.pages.push(root);
    }

    /**
     * Shows the top page. Has no effect on the screen if the stack is already shown.
     */
    public void show() {
        if (this.shown) {
            return;
        }

        this.shown = true;
        this.top().show(this.context());
    }

    /**
     * Removes all widgets of this stack from the screen. The pages stay on the stack.
     */
    public void hide() {
        if (this.shown) {
            this.top().hide();
        }
        this.shown = false;
        this.widgets.clear();
    }

    /**
     * Pushes a detail page on top of the current top page.
     */
    public void push(Page page) {
        if (this.shown) {
            this.top().hide();
            this.widgets.clear();
        }
        this.pages.push(page);
        if (this.shown) {
            page.show(this.context());
        }
    }

    /**
     * Pops the top page and shows the page below it.
     *
     * @return true if a page was popped, false if only the root page is left
     */
    public boolean pop() {
        if (this.pages.size() <= 1) {
            return false;
        }

        if (this.shown) {
            this.top().hide();
            this.widgets.clear();
        }
        this.pages.pop();
        if (this.shown) {
            this.top().show(this.context());
        }
        return true;
    }

    public boolean canPop() {
        return this.pages.size() > 1;
    }

    public Page top() {
        return this.pages.peek();
    }

    /**
     * Ticks the top page. Only meaningful while the stack is shown.
     */
    public void tick() {
        this.top().tick();
    }

    private PageContext context() {
        return new PageContext(this.widgets, this.bounds, this);
    }
}
