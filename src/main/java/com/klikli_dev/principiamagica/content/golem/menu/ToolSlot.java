// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.menu;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * The tool slot of the golem menu, which is the golem's main hand. Only accepts tools the golem's job requires.
 */
class ToolSlot extends GolemSideSlot {

    ToolSlot(GolemMenu menu, Container container, int slot, int x, int y) {
        super(menu, container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.menu.canPlaceTool(stack) && super.mayPlace(stack);
    }
}
