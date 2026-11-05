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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Furnace recipes.
 */
public class SmeltingRecipeProvider implements DataProvider {
    private static final String RECIPE_TYPE = "minecraft:smelting";
    private static final int COOKING_TIME = 200;
    private static final float EXPERIENCE = 0.35f;

    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    private RegistryOps<JsonElement> registryOps;

    public SmeltingRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
        this.lookupProvider = lookupProvider;
    }

    @Override
    public String getName() {
        return "Smelting Recipes/" + PrincipiaMagica.MODID;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.lookupProvider.thenCompose(provider -> {
            this.registryOps = provider.createSerializationContext(JsonOps.INSTANCE);

            Map<Identifier, JsonObject> recipes = new LinkedHashMap<>();
            this.buildRecipes((id, json) -> {
                if (recipes.put(id, json) != null) {
                    throw new IllegalStateException("Duplicate recipe " + id);
                }
            });

            List<CompletableFuture<?>> futures = recipes.entrySet().stream()
                    .<CompletableFuture<?>>map(entry -> DataProvider.saveStable(output, entry.getValue(), this.pathProvider.json(entry.getKey())))
                    .toList();
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    private void buildRecipes(BiConsumer<Identifier, JsonObject> consumer) {
        this.smelting(consumer, ItemRegistry.AXIOM_OF_HARVEST.get(), ItemRegistry.UNFIRED_AXIOM_OF_HARVEST.get());
        this.smelting(consumer, ItemRegistry.AXIOM_OF_FELLING.get(), ItemRegistry.UNFIRED_AXIOM_OF_FELLING.get());
    }

    private void smelting(BiConsumer<Identifier, JsonObject> consumer, ItemLike result, ItemLike ingredient) {
        JsonObject json = new JsonObject();
        json.addProperty("type", RECIPE_TYPE);
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", COOKING_TIME);
        json.addProperty("experience", EXPERIENCE);
        json.add("ingredient", Ingredient.CODEC.encodeStart(this.registryOps, Ingredient.of(ingredient)).getOrThrow());
        json.add("result", ItemStackTemplate.CODEC.encodeStart(this.registryOps, new ItemStackTemplate(result.asItem().builtInRegistryHolder(), 1, DataComponentPatch.EMPTY)).getOrThrow());

        consumer.accept(PrincipiaMagica.loc(this.itemPath(result)), json);
    }

    private String itemPath(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }
}
