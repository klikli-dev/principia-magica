// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.menu;

import net.minecraft.world.Container;

/**
 * A slot of the golem inventory. Only the usable slots of the golem are active.
 */
class GolemSlot extends GolemSideSlot {

    GolemSlot(GolemMenu menu, Container container, int slot, int x, int y) {
        super(menu, container, slot, x, y);
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.getContainerSlot() < this.menu.getUsableGolemSlots();
    }
}
