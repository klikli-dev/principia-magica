// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.principle.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Shared defaults of the principle recipes, which are never shown as a toast and are not grouped in the recipe book.
 */
public interface PrincipleRecipe<I extends RecipeInput> extends Recipe<I> {

    @Override
    default boolean showNotification() {
        return false;
    }

    @Override
    default String group() {
        return "";
    }

    @Override
    default RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
