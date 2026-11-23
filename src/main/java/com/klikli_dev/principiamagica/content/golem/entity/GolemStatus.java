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
     * How a status is shown to the player. Both the golem GUI and the icon above the golem in the world derive their icon from this.
     */
    public enum Severity {
        /**
         * Working normally.
         */
        OK,
        /**
         * Without a purpose. A calm state, shown with an idle icon in the GUI only.
         */
        IDLE,
        /**
         * Can be fixed in the golem's setup, e.g. a missing tool.
         */
        WARNING,
        /**
         * Blocked, e.g. no zone or no deposit.
         */
        PROBLEM
    }

    /**
     * @return true if the status needs the player's attention. Purposeless is a calm state, not a problem.
     */
    public boolean isProblem() {
        return this != OK && this != NO_PURPOSE;
    }

    /**
     * @return how severe this status is for the player, see {@link Severity}.
     */
    public Severity severity() {
        return switch (this) {
            case OK -> Severity.OK;
            case NO_PURPOSE -> Severity.IDLE;
            case MISSING_TOOL, NEEDS_SUPPLIES -> Severity.WARNING;
            case NO_ZONE, NO_DEPOSIT, DEPOSIT_FULL -> Severity.PROBLEM;
        };
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
