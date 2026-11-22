// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.recipe;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Shared output of the recipe data providers: collects each recipe once under its id, then saves all of them as stable JSON.
 */
final class RecipeDataWriter {

    private RecipeDataWriter() {
    }

    /**
     * @return a consumer that puts recipes into the map and fails if an id is added twice
     */
    static <R> BiConsumer<Identifier, R> collector(Map<Identifier, R> recipes) {
        return (id, recipe) -> {
            if (recipes.put(id, recipe) != null) {
                throw new IllegalStateException("Duplicate recipe " + id);
            }
        };
    }

    /**
     * Saves every recipe as a JSON file in the recipe folder of the data pack.
     *
     * @param encoder turns a recipe into its JSON object
     */
    static <R> CompletableFuture<?> save(CachedOutput output, PackOutput.PathProvider pathProvider, Map<Identifier, R> recipes, Function<R, JsonObject> encoder) {
        List<CompletableFuture<?>> futures = recipes.entrySet().stream()
                .<CompletableFuture<?>>map(entry -> DataProvider.saveStable(output, encoder.apply(entry.getValue()), pathProvider.json(entry.getKey())))
                .toList();
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }
}
