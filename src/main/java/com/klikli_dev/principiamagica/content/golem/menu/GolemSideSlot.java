// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A slot on the golem side of the golem menu. Only shown and usable on the golem tab.
 */
abstract class GolemSideSlot extends Slot {
    protected final GolemMenu menu;

    GolemSideSlot(GolemMenu menu, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.menu = menu;
    }

    @Override
    public boolean isActive() {
        return this.menu.isGolemSideActive();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.isActive() && super.mayPlace(stack);
    }
}
