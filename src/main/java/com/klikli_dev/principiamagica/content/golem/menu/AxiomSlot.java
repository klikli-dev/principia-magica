// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.menu;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * The axiom slot of the golem menu. Only accepts axioms the player may give to the golem.
 */
class AxiomSlot extends GolemSideSlot {

    AxiomSlot(GolemMenu menu, Container container, int slot, int x, int y) {
        super(menu, container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.menu.canPlaceAxiom(stack) && super.mayPlace(stack);
    }
}
