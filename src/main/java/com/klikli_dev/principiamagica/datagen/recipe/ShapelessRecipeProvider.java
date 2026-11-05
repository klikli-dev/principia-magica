// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Crafting table recipes without a fixed shape.
 */
public class ShapelessRecipeProvider implements DataProvider {
    private static final String RECIPE_TYPE = "minecraft:crafting_shapeless";

    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    private RegistryOps<JsonElement> registryOps;
    private HolderGetter<Item> items;

    public ShapelessRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
        this.lookupProvider = lookupProvider;
    }

    @Override
    public String getName() {
        return "Shapeless Recipes/" + PrincipiaMagica.MODID;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.lookupProvider.thenCompose(provider -> {
            this.registryOps = provider.createSerializationContext(JsonOps.INSTANCE);
            this.items = provider.lookupOrThrow(Registries.ITEM);

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
        this.shapeless(consumer, ItemRegistry.PRINCIPIA.get(), 1, List.of(Items.BOOK, Items.CLAY_BALL));

        this.shapeless(consumer, ItemRegistry.WET_TABLET.get(), 1, List.of(ItemRegistry.COPPER_STYLUS.get(), Items.CLAY_BALL, Items.CLAY_BALL));

        this.shapelessIngredients(consumer, ItemRegistry.UNFIRED_AXIOM_OF_HARVEST.get(), 1, List.of(
                Ingredient.of(ItemRegistry.COPPER_STYLUS.get()),
                Ingredient.of(ItemRegistry.WET_TABLET.get()),
                Ingredient.of(Items.WHEAT),
                Ingredient.of(Items.WHEAT_SEEDS)));

        this.shapelessIngredients(consumer, ItemRegistry.UNFIRED_AXIOM_OF_FELLING.get(), 1, List.of(
                Ingredient.of(ItemRegistry.COPPER_STYLUS.get()),
                Ingredient.of(ItemRegistry.WET_TABLET.get()),
                Ingredient.of(this.items.getOrThrow(ItemTags.SAPLINGS)),
                Ingredient.of(this.items.getOrThrow(ItemTags.LOGS))));
    }

    private void shapeless(BiConsumer<Identifier, JsonObject> consumer, ItemLike result, int count, List<ItemLike> ingredients) {
        this.shapelessIngredients(consumer, result, count, ingredients.stream().map(Ingredient::of).toList());
    }

    private void shapelessIngredients(BiConsumer<Identifier, JsonObject> consumer, ItemLike result, int count, List<Ingredient> ingredients) {
        JsonObject json = new JsonObject();
        json.addProperty("type", RECIPE_TYPE);
        json.addProperty("category", "misc");

        JsonArray ingredientArray = new JsonArray();
        ingredients.forEach(ingredient -> ingredientArray.add(Ingredient.CODEC.encodeStart(this.registryOps, ingredient).getOrThrow()));
        json.add("ingredients", ingredientArray);

        json.add("result", ItemStackTemplate.CODEC.encodeStart(this.registryOps, new ItemStackTemplate(result.asItem().builtInRegistryHolder(), count, DataComponentPatch.EMPTY)).getOrThrow());

        consumer.accept(PrincipiaMagica.loc(this.itemPath(result)), json);
    }

    private String itemPath(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }
}
