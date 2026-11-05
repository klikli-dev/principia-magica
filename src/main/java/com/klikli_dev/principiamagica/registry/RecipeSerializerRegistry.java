// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.registry;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.recipe.RepairCrackedGolemRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RecipeSerializerRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, PrincipiaMagica.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RepairCrackedGolemRecipe>> REPAIR_CRACKED_GOLEM = RECIPE_SERIALIZERS.register("repair_cracked_golem",
            () -> new RecipeSerializer<>(RepairCrackedGolemRecipe.MAP_CODEC, RepairCrackedGolemRecipe.STREAM_CODEC));
}
