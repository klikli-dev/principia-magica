// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.menu;

import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Exposes the golem's two item slots, the hand and the back, as a two-slot container. Every change goes straight to the golem.
 */
final class GolemCarryContainer implements Container {
    static final int HAND = 0;
    static final int BACK = 1;

    private final GolemEntity golem;

    GolemCarryContainer(GolemEntity golem) {
        this.golem = golem;
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return this.golem.getHandItem().isEmpty() && this.golem.getBackItem().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == HAND ? this.golem.getHandItem() : this.golem.getBackItem();
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = this.getItem(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = stack.split(count);
        this.setItem(slot, stack);
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = this.getItem(slot);
        this.setItem(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack value = stack.isEmpty() ? ItemStack.EMPTY : stack;
        if (slot == HAND) {
            this.golem.setHandItem(value);
        } else {
            this.golem.setBackItem(value);
        }
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
        this.setItem(HAND, ItemStack.EMPTY);
        this.setItem(BACK, ItemStack.EMPTY);
    }
}
