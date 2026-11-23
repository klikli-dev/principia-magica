// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation.client;

import com.klikli_dev.principiamagica.client.recipe.ClientRecipes;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe.CuringEnvironment;
import com.klikli_dev.principiamagica.content.principle.recipe.DryingRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe;
import com.klikli_dev.principiamagica.registry.RecipeTypeRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Optional;

/**
 * Recipe lookups against the recipes synced to the client, for visuals and HUD. Mirrors the server side matching in
 * {@code LibationUtils}.
 */
public final class LibationRecipes {

    private LibationRecipes() {
    }

    public static Optional<CuringRecipe> curing(PrincipleContents principles, CuringEnvironment environment) {
        for (RecipeHolder<CuringRecipe> holder : ClientRecipes.byType(RecipeTypeRegistry.CURING.get())) {
            CuringRecipe recipe = holder.value();
            if (recipe.environment() == environment && principles.contains(recipe.inputs())) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    /**
     * @return the steeping recipe of the item with the principles, or empty if the principles are not sufficient.
     */
    public static Optional<SteepingRecipe> steeping(ItemStack stack, PrincipleContents principles) {
        for (RecipeHolder<SteepingRecipe> holder : ClientRecipes.byType(RecipeTypeRegistry.STEEPING.get())) {
            SteepingRecipe recipe = holder.value();
            if (recipe.ingredient().test(stack) && principles.contains(recipe.principles())) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    /**
     * @return the steeping recipe of the item, regardless of the principles, or empty if the item cannot be steeped.
     */
    public static Optional<SteepingRecipe> steepingFor(ItemStack stack) {
        for (RecipeHolder<SteepingRecipe> holder : ClientRecipes.byType(RecipeTypeRegistry.STEEPING.get())) {
            if (holder.value().ingredient().test(stack)) {
                return Optional.of(holder.value());
            }
        }
        return Optional.empty();
    }

    public static Optional<DryingRecipe> drying(ItemStack stack) {
        for (RecipeHolder<DryingRecipe> holder : ClientRecipes.byType(RecipeTypeRegistry.DRYING.get())) {
            if (holder.value().ingredient().test(stack)) {
                return Optional.of(holder.value());
            }
        }
        return Optional.empty();
    }
}
