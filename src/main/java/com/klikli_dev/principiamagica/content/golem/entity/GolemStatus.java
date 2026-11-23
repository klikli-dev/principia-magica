// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.entity;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Status shown in the GUI. Declaration order is priority: when several blockers are active, the first one wins.
 */
public enum GolemStatus {
    OK,
    NO_PURPOSE,
    NO_ZONE,
    MISSING_TOOL,
    NO_DEPOSIT,
    DEPOSIT_FULL,
    NEEDS_SUPPLIES;

    /**
     * @return true if the status needs the player's attention. Purposeless is a calm state, not a problem.
     */
    public boolean isProblem() {
        return this != OK && this != NO_PURPOSE;
    }

    public String translationKey() {
        return "golem_status.principiamagica." + this.name().toLowerCase(Locale.ROOT);
    }

    /**
     * @return the one-line status text, e.g. "Carve a footprint with the Copper Stylus." The GUI can show it as a hint or a tooltip.
     */
    public Component description() {
        return Component.translatable(this.translationKey() + ".description");
    }
}
