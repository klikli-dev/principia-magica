// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui;

/**
 * Passed to {@link Page#show(PageContext)}.
 *
 * @param widgets the group the page must add its widgets to
 * @param bounds the rectangle the page lays its widgets out in
 * @param stack the stack the page is part of, used to push detail pages and to pop the page itself
 */
public record PageContext(WidgetGroup widgets, ContentRect bounds, PageStack stack) {

    /**
     * Shows a new page on top of this page. The current top page is hidden.
     */
    public void push(Page page) {
        this.stack.push(page);
    }

    /**
     * Pops the top page and shows the page below it again.
     *
     * @return true if a page was popped, false if this is the root page
     */
    public boolean pop() {
        return this.stack.pop();
    }
}
