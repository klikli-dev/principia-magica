// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.menu;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * The hand or back slot of the golem menu. The hand accepts any item except an axiom, the back only accepts the job's tool,
 * since a non-tool item on the back would never be used. Only shown and usable on the golem tab.
 */
class GolemCarrySlot extends GolemSideSlot {
    private final boolean back;

    GolemCarrySlot(GolemMenu menu, Container container, int slot, int x, int y) {
        super(menu, container, slot, x, y);
        this.back = slot == GolemCarryContainer.BACK;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (this.back) {
            return this.menu.canPlaceTool(stack) && super.mayPlace(stack);
        }

        return !this.menu.isAxiom(stack) && super.mayPlace(stack);
    }
}
