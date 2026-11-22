// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.principle.recipe;

import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.registry.RecipeSerializerRegistry;
import com.klikli_dev.principiamagica.registry.RecipeTypeRegistry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * One unit of the ingredient dissolves into principles when washed on the Libation Slab.
 */
public record LibationRecipe(Ingredient ingredient, PrincipleContents principles) implements PrincipleRecipe<SingleRecipeInput> {

    public static final MapCodec<LibationRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(LibationRecipe::ingredient),
            PrincipleContents.CODEC.fieldOf("principles").forGetter(LibationRecipe::principles)
    ).apply(instance, LibationRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LibationRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, LibationRecipe::ingredient,
            PrincipleContents.STREAM_CODEC, LibationRecipe::principles,
            LibationRecipe::new
    );

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return RecipeSerializerRegistry.LIBATION.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return RecipeTypeRegistry.LIBATION.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(this.ingredient);
    }
}
