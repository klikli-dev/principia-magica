// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
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
import java.util.function.Function;

/**
 * Crafting table recipes. Golems are a single item carrying a {@link GolemSpec} component, so each material gets its
 * own recipe whose result carries the component.
 */
public class ShapedRecipeProvider implements DataProvider {
    private static final String RECIPE_TYPE = "minecraft:crafting_shaped";

    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    private RegistryOps<JsonElement> registryOps;
    private HolderGetter<Item> items;

    public ShapedRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
        this.lookupProvider = lookupProvider;
    }

    @Override
    public String getName() {
        return "Recipes/" + PrincipiaMagica.MODID;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.lookupProvider.thenCompose(provider -> {
            this.registryOps = provider.createSerializationContext(JsonOps.INSTANCE);
            this.items = provider.lookupOrThrow(Registries.ITEM);

            Map<Identifier, JsonObject> recipes = new LinkedHashMap<>();
            this.buildRecipes(RecipeDataWriter.collector(recipes));
            return RecipeDataWriter.save(output, this.pathProvider, recipes, Function.identity());
        });
    }

    private void buildRecipes(BiConsumer<Identifier, JsonObject> consumer) {
        this.woodGolemRecipe(consumer);

        this.shaped(consumer, ItemRegistry.COPPER_STYLUS.get(),
                List.of("  C", " S ", "S  "),
                Map.of('C', Items.COPPER_INGOT, 'S', Items.STICK));

        this.shaped(consumer, ItemRegistry.LIBATION_SLAB.get(),
                List.of("TTT", "BBB"),
                Map.of('T', Items.SMOOTH_SANDSTONE_SLAB, 'B', Items.SANDSTONE));

        this.shaped(consumer, ItemRegistry.LIBATION_SPOUT.get(),
                List.of("TT", "B "),
                Map.of('T', Items.SMOOTH_SANDSTONE_SLAB, 'B', Items.SANDSTONE));

        this.shaped(consumer, ItemRegistry.VESSEL_TAP.get(),
                List.of("CC", " I"),
                Map.of('C', Items.COPPER_INGOT, 'I', Items.IRON_NUGGET));

        this.shaped(consumer, ItemRegistry.CATCH_TROUGH.get(),
                List.of("B B", "BBB"),
                Map.of('B', Items.BRICK));

        this.shaped(consumer, ItemRegistry.WABET_BASIN.get(),
                List.of("S S", "SCS"),
                Map.of('S', Items.SANDSTONE, 'C', Items.COPPER_INGOT));

        this.shaped(consumer, ItemRegistry.STEEPING_JAR.get(),
                List.of("BWB", "BCB", "BBB"),
                Map.of('B', Items.BRICK, 'W', Items.STRING, 'C', Items.COPPER_INGOT));

        this.shaped(consumer, ItemRegistry.DRYING_SLAB.get(),
                List.of("CCC", "SSS"),
                Map.of('C', Items.SUGAR_CANE, 'S', Items.SANDSTONE_SLAB));

        this.recipe(consumer, PrincipiaMagica.loc("clay_phial"), ItemRegistry.CLAY_PHIAL.get(), 8, DataComponentPatch.EMPTY,
                List.of("CQC", " C "),
                Map.of('C', Ingredient.of(Items.CLAY_BALL), 'Q', Ingredient.of(ItemRegistry.QUICKENED_CLAY.get())));
    }

    private void woodGolemRecipe(BiConsumer<Identifier, JsonObject> consumer) {
        DataComponentPatch patch = DataComponentPatch.builder()
                .set(DataComponentRegistry.GOLEM_SPEC.get(), GolemSpec.of(GolemMaterialRegistry.WOOD))
                .build();

        this.recipe(consumer, PrincipiaMagica.loc("golem/wood"), ItemRegistry.GOLEM.get(), 1, patch,
                List.of(" L ", "QLQ", "PSP"),
                Map.of(
                        'L', Ingredient.of(this.items.getOrThrow(ItemTags.LOGS)),
                        'Q', Ingredient.of(ItemRegistry.QUICKENED_CLAY.get()),
                        'P', Ingredient.of(this.items.getOrThrow(ItemTags.PLANKS)),
                        'S', Ingredient.of(ItemRegistry.COPPER_STYLUS.get())));
    }

    private void shaped(BiConsumer<Identifier, JsonObject> consumer, ItemLike result, List<String> pattern, Map<Character, ItemLike> keys) {
        Map<Character, Ingredient> ingredients = new LinkedHashMap<>();
        keys.forEach((key, item) -> ingredients.put(key, Ingredient.of(item)));
        this.shapedIngredients(consumer, result, pattern, ingredients);
    }

    private void shapedIngredients(BiConsumer<Identifier, JsonObject> consumer, ItemLike result, List<String> pattern, Map<Character, Ingredient> ingredients) {
        this.recipe(consumer, PrincipiaMagica.loc(this.itemPath(result)), result, 1, DataComponentPatch.EMPTY, pattern, ingredients);
    }

    private void recipe(BiConsumer<Identifier, JsonObject> consumer, Identifier id, ItemLike result, int count, DataComponentPatch patch, List<String> pattern, Map<Character, Ingredient> keys) {
        JsonObject json = new JsonObject();
        json.addProperty("type", RECIPE_TYPE);
        json.addProperty("category", "misc");
        json.add("result", ItemStackTemplate.CODEC.encodeStart(this.registryOps, new ItemStackTemplate(result.asItem().builtInRegistryHolder(), count, patch)).getOrThrow());

        JsonArray patternArray = new JsonArray();
        pattern.forEach(patternArray::add);
        json.add("pattern", patternArray);

        JsonObject keyObject = new JsonObject();
        keys.forEach((key, ingredient) -> keyObject.add(String.valueOf(key), Ingredient.CODEC.encodeStart(this.registryOps, ingredient).getOrThrow()));
        json.add("key", keyObject);

        consumer.accept(id, json);
    }

    private String itemPath(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }
}
