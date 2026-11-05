// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.client.gui.Tab;

import java.util.function.Supplier;

/**
 * Base for the root pages of the golem GUI tabs.
 */
abstract class GolemTab extends GolemPage implements Tab {

    protected GolemTab(Supplier<GolemView> viewSupplier) {
        super(viewSupplier);
    }
}
