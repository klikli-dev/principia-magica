// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.entity;

public enum GolemMode {
    WORK,
    FOLLOW,
    STAY;

    public GolemMode next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
