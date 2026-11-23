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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RecipeSerializerRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, PrincipiaMagica.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<LibationRecipe>> LIBATION = RECIPE_SERIALIZERS.register("libation",
            () -> new RecipeSerializer<>(LibationRecipe.MAP_CODEC, LibationRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CuringRecipe>> CURING = RECIPE_SERIALIZERS.register("curing",
            () -> new RecipeSerializer<>(CuringRecipe.MAP_CODEC, CuringRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SteepingRecipe>> STEEPING = RECIPE_SERIALIZERS.register("steeping",
            () -> new RecipeSerializer<>(SteepingRecipe.MAP_CODEC, SteepingRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DryingRecipe>> DRYING = RECIPE_SERIALIZERS.register("drying",
            () -> new RecipeSerializer<>(DryingRecipe.MAP_CODEC, DryingRecipe.STREAM_CODEC));
}
