// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.DryingRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.LibationRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RecipeTypeRegistry {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, PrincipiaMagica.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<LibationRecipe>> LIBATION = RECIPE_TYPES.register("libation", () -> new RecipeType<LibationRecipe>() {
    });
    public static final DeferredHolder<RecipeType<?>, RecipeType<CuringRecipe>> CURING = RECIPE_TYPES.register("curing", () -> new RecipeType<CuringRecipe>() {
    });
    public static final DeferredHolder<RecipeType<?>, RecipeType<SteepingRecipe>> STEEPING = RECIPE_TYPES.register("steeping", () -> new RecipeType<SteepingRecipe>() {
    });
    public static final DeferredHolder<RecipeType<?>, RecipeType<DryingRecipe>> DRYING = RECIPE_TYPES.register("drying", () -> new RecipeType<DryingRecipe>() {
    });

    /**
     * Sends the principle recipes to clients, where they are available via {@code ClientRecipes}.
     */
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(LIBATION.get(), CURING.get(), STEEPING.get(), DRYING.get());
    }
}
