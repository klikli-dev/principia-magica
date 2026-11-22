// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.entity;

import com.klikli_dev.principiamagica.registry.AttributeRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class GolemInventory extends SimpleContainer {
    public static final int MAX_SLOTS = 9;

    private final GolemEntity golem;

    public GolemInventory(GolemEntity golem) {
        super(MAX_SLOTS);
        this.golem = golem;
    }

    public int getUsableSlots() {
        return Mth.clamp((int) this.golem.getAttributeValue(AttributeRegistry.GOLEM_INVENTORY_SLOTS), 0, MAX_SLOTS);
    }

    /**
     * @return true if no usable slot is empty. Stacks may still have room for more items.
     */
    public boolean isFull() {
        for (int i = 0; i < this.getUsableSlots(); i++) {
            if (this.getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public int countItem(Item item) {
        int count = 0;
        for (int i = 0; i < this.getUsableSlots(); i++) {
            ItemStack stack = this.getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * @return how many items matching the template's item and components fit into the usable slots
     */
    public int fitAmount(ItemStack template) {
        int fit = 0;
        for (int i = 0; i < this.getUsableSlots(); i++) {
            ItemStack stack = this.getItem(i);
            if (stack.isEmpty()) {
                fit += template.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(stack, template)) {
                fit += stack.getMaxStackSize() - stack.getCount();
            }
        }
        return fit;
    }

    /**
     * Inserts as much of the stack as fits into usable slots. Shrinks the given stack in place and returns it.
     */
    public ItemStack insert(ItemStack stack) {
        for (int i = 0; i < this.getUsableSlots() && !stack.isEmpty(); i++) {
            ItemStack slot = this.getItem(i);
            if (slot.isEmpty()) {
                int amount = Math.min(stack.getCount(), stack.getMaxStackSize());
                this.setItem(i, stack.copyWithCount(amount));
                stack.shrink(amount);
            } else if (ItemStack.isSameItemSameComponents(slot, stack)) {
                int amount = Math.min(stack.getCount(), slot.getMaxStackSize() - slot.getCount());
                if (amount > 0) {
                    slot.grow(amount);
                    stack.shrink(amount);
                }
            }
        }

        this.setChanged();
        return stack;
    }

    public void save(ValueOutput output) {
        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            ItemStack stack = this.getItem(slot);
            if (!stack.isEmpty()) {
                output.store(slotKey(slot), ItemStack.CODEC, stack);
            }
        }
    }

    /**
     * Each slot is stored under its own key, so items keep their slot when the capacity changes between saves.
     */
    public void load(ValueInput input) {
        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            int index = slot;
            input.read(slotKey(slot), ItemStack.CODEC).ifPresent(stack -> this.setItem(index, stack));
        }
    }

    private static String slotKey(int slot) {
        return "Slot" + slot;
    }
}
