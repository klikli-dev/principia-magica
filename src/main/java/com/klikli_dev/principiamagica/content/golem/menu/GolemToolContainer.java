// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.menu;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Exposes the golem's main hand as a single-slot container.
 */
final class GolemToolContainer implements Container {
    private final GolemEntity golem;

    GolemToolContainer(GolemEntity golem) {
        this.golem = golem;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.golem.getMainHandItem().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.golem.getMainHandItem();
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = this.golem.getMainHandItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = stack.split(count);
        this.setItem(slot, stack);
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = this.golem.getMainHandItem();
        this.golem.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.golem.setItemSlot(EquipmentSlot.MAINHAND, stack);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return this.golem.isAlive();
    }

    @Override
    public void clearContent() {
        this.golem.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }
}
