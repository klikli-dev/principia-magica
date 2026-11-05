// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.recipe;

import com.mojang.serialization.MapCodec;
import com.klikli_dev.principiamagica.content.golem.item.CrackedGolemItem;
import com.klikli_dev.principiamagica.content.golem.item.StylusItem;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.klikli_dev.principiamagica.registry.RecipeSerializerRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Repairs a cracked golem: one copper stylus, one cracked golem and one quickened clay in any slots.
 * The result is a golem item with all components of the cracked golem (axiom, upgrades, workplace).
 * The stylus is returned, the cracked golem and the clay are consumed.
 */
public class RepairCrackedGolemRecipe extends CustomRecipe {

    private static final RepairCrackedGolemRecipe INSTANCE = new RepairCrackedGolemRecipe();

    public static final MapCodec<RepairCrackedGolemRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, RepairCrackedGolemRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int stylus = 0;
        int cracked = 0;
        int clay = 0;

        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof StylusItem) {
                stylus++;
            } else if (stack.getItem() instanceof CrackedGolemItem) {
                cracked++;
            } else if (stack.is(ItemRegistry.QUICKENED_CLAY.get())) {
                clay++;
            } else {
                return false;
            }
        }

        return stylus == 1 && cracked == 1 && clay == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack cracked = findCracked(input);
        if (cracked.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack golem = new ItemStack(ItemRegistry.GOLEM.get());
        golem.applyComponents(cracked.getComponentsPatch());
        return golem;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainders = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.getItem() instanceof StylusItem) {
                remainders.set(slot, stack.copyWithCount(1));
            }
        }

        return remainders;
    }

    private static ItemStack findCracked(CraftingInput input) {
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.getItem() instanceof CrackedGolemItem) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return RecipeSerializerRegistry.REPAIR_CRACKED_GOLEM.get();
    }
}
