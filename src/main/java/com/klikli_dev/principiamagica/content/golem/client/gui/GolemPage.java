// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.Page;
import com.klikli_dev.principiamagica.client.gui.PageContext;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Base for the golem GUI pages. Widgets are created once, on the first {@link #show(PageContext)}, and re-added to the
 * screen on every show. Widgets added while the page is shown (see {@link #addLive}) are added to the screen directly.
 */
abstract class GolemPage implements Page {

    protected final Supplier<GolemView> viewSupplier;
    private final List<AbstractWidget> widgets = new ArrayList<>();
    private @Nullable PageContext context;
    private boolean built;

    protected GolemPage(Supplier<GolemView> viewSupplier) {
        this.viewSupplier = viewSupplier;
    }

    @Override
    public void show(PageContext context) {
        this.context = context;
        if (!this.built) {
            this.built = true;
            this.build(context.bounds());
        }

        for (AbstractWidget widget : this.widgets) {
            context.widgets().add(widget);
        }
    }

    /**
     * Creates the widgets of the page. Called once, on the first show, and only with {@link #add}.
     */
    protected abstract void build(ContentRect bounds);

    protected GolemView view() {
        return this.viewSupplier.get();
    }

    protected int golemId() {
        return this.view().golemId();
    }

    protected <T extends AbstractWidget> T add(T widget) {
        this.widgets.add(widget);
        return widget;
    }

    /**
     * Adds a widget while the page is shown, e.g. from {@link #tick()}.
     */
    protected void addLive(AbstractWidget widget) {
        this.widgets.add(widget);
        this.context().widgets().add(widget);
    }

    /**
     * Removes a widget while the page is shown, e.g. from {@link #tick()}.
     */
    protected void removeLive(AbstractWidget widget) {
        this.widgets.remove(widget);
        this.context().widgets().remove(widget);
    }

    protected PageContext context() {
        return Objects.requireNonNull(this.context, "page is not shown");
    }
}
