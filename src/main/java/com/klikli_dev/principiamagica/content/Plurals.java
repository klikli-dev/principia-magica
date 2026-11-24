// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content;

import net.minecraft.network.chat.Component;

/**
 * Counted text for lang keys with a zero, one and other form, e.g. {@code base.one} for "1 zone" and {@code base.other} for "2 zones".
 * The count is passed to the key, so each form contains the {@code %s}.
 */
public final class Plurals {

    public static final String FOOTPRINTS = "counts.principiamagica.footprints";
    public static final String CONTAINER_LINKS = "counts.principiamagica.container_links";
    public static final String UNAVAILABLE_LINKS = "counts.principiamagica.unavailable_links";

    private Plurals() {
    }

    /**
     * @param baseKey the lang key without the form suffix, e.g. {@code gui.principiamagica.golem.zones}
     * @param count the count, the form is {@code zero} for 0, {@code one} for 1 and {@code other} for everything else
     * @return the translatable text of the form, with the count in it
     */
    public static Component of(String baseKey, int count) {
        String form = count == 0 ? "zero" : count == 1 ? "one" : "other";
        return Component.translatable(baseKey + "." + form, count);
    }
}
