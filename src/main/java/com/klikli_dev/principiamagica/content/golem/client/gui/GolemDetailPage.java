// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.widget.LabelWidget;
import com.klikli_dev.principiamagica.client.gui.widget.TextButton;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * Base for the detail pages of the golem GUI: a back button and a title in the header, content below it.
 */
abstract class GolemDetailPage extends GolemPage {

    private final Component title;

    protected GolemDetailPage(Supplier<GolemView> viewSupplier, Component title) {
        super(viewSupplier);
        this.title = title;
    }

    @Override
    public Component title() {
        return this.title;
    }

    /**
     * Adds the header (back button and title) to the page.
     *
     * @return the y coordinate where the content starts
     */
    protected int addHeader(ContentRect bounds) {
        int x = bounds.x() + 8;
        int y = bounds.y() + 20;
        this.add(new TextButton(x, y, 44, 16, Component.translatable("gui.principiamagica.golem.back"), () -> this.context().pop()));
        this.add(new LabelWidget(x + 52, y + 4, bounds.width() - 68, () -> this.title));
        return y + 26;
    }
}
