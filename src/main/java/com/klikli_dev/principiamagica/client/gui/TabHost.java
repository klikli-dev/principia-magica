// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.gui;

import com.klikli_dev.codedefinedgui.api.screen.GuiHost;
import com.klikli_dev.principiamagica.client.gui.widget.TabBar;
import com.klikli_dev.principiamagica.client.gui.widget.TabButton;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Hosts a vertical tab bar on the left edge of a content area. Each tab owns its own {@link PageStack}, so every tab
 * remembers its detail pages while another tab is selected.
 * <p>
 * Switching tabs hides the old stack (clearing its widgets from the screen) and shows the new one. The tab bar buttons
 * live in their own group and stay on the screen for the whole lifetime of the host.
 */
public final class TabHost {

    private final List<Tab> tabs;
    private final List<PageStack> stacks = new ArrayList<>();
    private final TabBar tabBar;
    private final IntConsumer onSelected;
    private int selected = -1;

    /**
     * Creates the host and builds the tab bar. Nothing is shown until {@link #select(int)} is called.
     *
     * @param host the screen to add widgets to
     * @param area the content area the tabs' pages are laid out in
     * @param tabs the tabs, top to bottom in the tab bar
     * @param onSelected called before a tab is shown, with the new tab index. Use it to sync the selection to the server
     */
    public TabHost(GuiHost host, ContentRect area, List<Tab> tabs, IntConsumer onSelected) {
        this.tabs = List.copyOf(tabs);
        this.onSelected = onSelected;

        WidgetGroup chrome = new WidgetGroup(host);
        List<TabButton> buttons = new ArrayList<>();
        for (int i = 0; i < this.tabs.size(); i++) {
            Tab tab = this.tabs.get(i);
            int index = i;
            buttons.add(new TabButton(0, 0, tab.icon(), tab.title(), () -> this.select(index)));
            this.stacks.add(new PageStack(tab, new WidgetGroup(host), area));
        }

        this.tabBar = new TabBar(chrome, area.x() - TabButton.SIZE + TabBar.OVERLAP, area.y() + TabBar.TOP_MARGIN, TabBar.SPACING, buttons);
    }

    /**
     * Selects a tab: hides the currently visible stack, notifies the listener and shows the new stack.
     */
    public void select(int index) {
        if (index < 0 || index >= this.tabs.size() || index == this.selected) {
            return;
        }

        if (this.selected >= 0) {
            this.stacks.get(this.selected).hide();
        }

        this.selected = index;
        this.onSelected.accept(index);
        this.stacks.get(index).show();
        this.tabBar.setSelected(index);
    }

    public int selected() {
        return this.selected;
    }

    public Tab selectedTab() {
        return this.tabs.get(this.selected);
    }

    /**
     * Ticks the top page of the selected tab.
     */
    public void tick() {
        if (this.selected >= 0) {
            this.stacks.get(this.selected).tick();
        }
    }

    /**
     * Pops a detail page of the selected tab, if there is one.
     *
     * @return true if a detail page was popped, false if the selected tab shows its root page
     */
    public boolean popPage() {
        return this.selected >= 0 && this.stacks.get(this.selected).pop();
    }
}
