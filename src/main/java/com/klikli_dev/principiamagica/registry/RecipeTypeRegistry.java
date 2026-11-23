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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RecipeTypeRegistry {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, PrincipiaMagica.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<LibationRecipe>> LIBATION = register("libation");
    public static final DeferredHolder<RecipeType<?>, RecipeType<CuringRecipe>> CURING = register("curing");
    public static final DeferredHolder<RecipeType<?>, RecipeType<SteepingRecipe>> STEEPING = register("steeping");
    public static final DeferredHolder<RecipeType<?>, RecipeType<DryingRecipe>> DRYING = register("drying");

    /**
     * Registers a recipe type that only needs to be identified by its class. Each call creates a distinct instance, which
     * is what the registry expects.
     */
    private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> register(String name) {
        return RECIPE_TYPES.register(name, () -> new RecipeType<T>() {
        });
    }

    /**
     * Sends the principle recipes to clients, where they are available via {@code ClientRecipes}.
     */
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(LIBATION.get(), CURING.get(), STEEPING.get(), DRYING.get());
    }
}
