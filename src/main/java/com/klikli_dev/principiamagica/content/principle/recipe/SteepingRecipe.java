// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.principle.recipe;

import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
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
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Infuses an item in the Catch Trough. When the trough holds at least the required principles, exactly those are consumed
 * and the item is replaced by the result, which is a separate item (e.g. the steeped unfired axioms).
 * <p>
 * Steeping takes {@link #time()} ticks while the principles are sufficient. The time is not scaled by light.
 */
public record SteepingRecipe(Ingredient ingredient, PrincipleContents principles, ItemStackTemplate result, int time) implements PrincipleRecipe<SteepingRecipe.SteepingRecipeInput> {

    public static final MapCodec<SteepingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(SteepingRecipe::ingredient),
            PrincipleContents.CODEC.fieldOf("principles").forGetter(SteepingRecipe::principles),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(SteepingRecipe::result),
            Codec.INT.fieldOf("time").forGetter(SteepingRecipe::time)
    ).apply(instance, SteepingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SteepingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, SteepingRecipe::ingredient,
            PrincipleContents.STREAM_CODEC, SteepingRecipe::principles,
            ItemStackTemplate.STREAM_CODEC, SteepingRecipe::result,
            ByteBufCodecs.VAR_INT, SteepingRecipe::time,
            SteepingRecipe::new
    );

    @Override
    public boolean matches(SteepingRecipeInput input, Level level) {
        return this.ingredient.test(input.stack()) && input.principles().contains(this.principles);
    }

    @Override
    public ItemStack assemble(SteepingRecipeInput input) {
        return this.result.create();
    }

    @Override
    public RecipeSerializer<? extends Recipe<SteepingRecipeInput>> getSerializer() {
        return RecipeSerializerRegistry.STEEPING.get();
    }

    @Override
    public RecipeType<? extends Recipe<SteepingRecipeInput>> getType() {
        return RecipeTypeRegistry.STEEPING.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(this.ingredient);
    }

    /**
     * @param stack      the item placed in the trough.
     * @param principles the principles held by the trough.
     */
    public record SteepingRecipeInput(ItemStack stack, PrincipleContents principles) implements RecipeInput {

        @Override
        public ItemStack getItem(int index) {
            return this.stack;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return this.stack.isEmpty();
        }
    }
}
