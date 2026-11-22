// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.principle.recipe;

import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.registry.RecipeSerializerRegistry;
import com.klikli_dev.principiamagica.registry.RecipeTypeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Cures pairs of primal principles into a compound principle in the Wabet Basin (light) or Steeping Jar (dark).
 * <p>
 * The input matches when the vessel holds at least the inputs and the environment matches.
 * The result is a principle stack, not an item, so {@link #assemble} returns an empty stack.
 */
public record CuringRecipe(PrincipleContents inputs, PrincipleStack result, CuringEnvironment environment, int time) implements Recipe<CuringRecipe.CuringRecipeInput> {

    public static final MapCodec<CuringRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PrincipleContents.CODEC.fieldOf("inputs").forGetter(CuringRecipe::inputs),
            PrincipleStack.CODEC.fieldOf("result").forGetter(CuringRecipe::result),
            CuringEnvironment.CODEC.fieldOf("environment").forGetter(CuringRecipe::environment),
            Codec.INT.fieldOf("time").forGetter(CuringRecipe::time)
    ).apply(instance, CuringRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CuringRecipe> STREAM_CODEC = StreamCodec.composite(
            PrincipleContents.STREAM_CODEC, CuringRecipe::inputs,
            PrincipleStack.STREAM_CODEC, CuringRecipe::result,
            StreamCodec.of(RegistryFriendlyByteBuf::writeEnum, buf -> buf.readEnum(CuringEnvironment.class)), CuringRecipe::environment,
            ByteBufCodecs.VAR_INT, CuringRecipe::time,
            CuringRecipe::new
    );

    @Override
    public boolean matches(CuringRecipeInput input, Level level) {
        return input.environment() == this.environment && input.principles().contains(this.inputs);
    }

    @Override
    public ItemStack assemble(CuringRecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<? extends Recipe<CuringRecipeInput>> getSerializer() {
        return RecipeSerializerRegistry.CURING.get();
    }

    @Override
    public RecipeType<? extends Recipe<CuringRecipeInput>> getType() {
        return RecipeTypeRegistry.CURING.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    /**
     * @param principles the principles held by the vessel.
     * @param environment the environment of the vessel.
     */
    public record CuringRecipeInput(PrincipleContents principles, CuringEnvironment environment) implements RecipeInput {

        @Override
        public ItemStack getItem(int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return this.principles.isEmpty();
        }
    }

    public enum CuringEnvironment implements StringRepresentable {
        LIGHT("light"),
        DARK("dark");

        public static final Codec<CuringEnvironment> CODEC = StringRepresentable.fromEnum(CuringEnvironment::values);

        private final String serializedName;

        CuringEnvironment(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }
    }
}
