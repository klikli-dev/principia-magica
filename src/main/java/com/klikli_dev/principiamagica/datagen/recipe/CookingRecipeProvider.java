// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Cooking recipes: unfired clay phials fire into clay phials in a furnace or on a campfire.
 */
public class CookingRecipeProvider implements DataProvider {
    private static final int SMELTING_TIME = 200;
    private static final int CAMPFIRE_TIME = 600;
    private static final float EXPERIENCE = 0.1F;

    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    private RegistryOps<JsonElement> registryOps;

    public CookingRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
        this.lookupProvider = lookupProvider;
    }

    @Override
    public String getName() {
        return "Cooking Recipes/" + PrincipiaMagica.MODID;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.lookupProvider.thenCompose(provider -> {
            this.registryOps = provider.createSerializationContext(JsonOps.INSTANCE);

            Map<Identifier, JsonObject> recipes = new LinkedHashMap<>();
            this.buildRecipes(RecipeDataWriter.collector(recipes));
            return RecipeDataWriter.save(output, this.pathProvider, recipes, Function.identity());
        });
    }

    private void buildRecipes(BiConsumer<Identifier, JsonObject> consumer) {
        this.cooking(consumer, "minecraft:smelting", "smelting/clay_phial", SMELTING_TIME);
        this.cooking(consumer, "minecraft:campfire_cooking", "campfire/clay_phial", CAMPFIRE_TIME);
    }

    private void cooking(BiConsumer<Identifier, JsonObject> consumer, String type, String path, int cookingTime) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty("category", "misc");
        json.add("ingredient", Ingredient.CODEC.encodeStart(this.registryOps, Ingredient.of(ItemRegistry.UNFIRED_CLAY_PHIAL.get())).getOrThrow());
        json.add("result", ItemStackTemplate.CODEC.encodeStart(this.registryOps,
                new ItemStackTemplate(ItemRegistry.CLAY_PHIAL.get().builtInRegistryHolder(), 1, DataComponentPatch.EMPTY)).getOrThrow());
        json.addProperty("experience", EXPERIENCE);
        json.addProperty("cookingtime", cookingTime);

        consumer.accept(PrincipiaMagica.loc(path), json);
    }
}
