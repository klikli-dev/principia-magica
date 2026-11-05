// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.entity;

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

    public boolean isProblem() {
        return this != OK;
    }

    public String translationKey() {
        return "golem_status.principiamagica." + this.name().toLowerCase(Locale.ROOT);
    }
}
