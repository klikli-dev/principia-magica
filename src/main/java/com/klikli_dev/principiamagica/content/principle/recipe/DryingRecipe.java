// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.principle.recipe;

import com.klikli_dev.principiamagica.registry.RecipeSerializerRegistry;
import com.klikli_dev.principiamagica.registry.RecipeTypeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Dries a steeped item on the Drying Slab. The time is in ticks at full light (15).
 */
public record DryingRecipe(Ingredient ingredient, ItemStackTemplate result, int time) implements PrincipleRecipe<SingleRecipeInput> {

    public static final MapCodec<DryingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(DryingRecipe::ingredient),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(DryingRecipe::result),
            Codec.INT.fieldOf("time").forGetter(DryingRecipe::time)
    ).apply(instance, DryingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DryingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, DryingRecipe::ingredient,
            ItemStackTemplate.STREAM_CODEC, DryingRecipe::result,
            ByteBufCodecs.VAR_INT, DryingRecipe::time,
            DryingRecipe::new
    );

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return this.result.create();
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return RecipeSerializerRegistry.DRYING.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return RecipeTypeRegistry.DRYING.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(this.ingredient);
    }
}
