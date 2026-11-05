// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.golem.spec.GolemMaterial;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.GolemMaterialRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
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
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Crafting table recipes. Golems are a single item carrying a {@link GolemSpec} component, so each material gets its
 * own recipe whose result carries the component.
 * <p>
 * The golem shape is the same for every material, only the material ingredient changes:
 * <pre>
 * M _ M
 * _ R _
 * M _ M
 * </pre>
 * where M is the material, R is redstone dust and _ is empty.
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
        this.golemRecipe(consumer, "wood", GolemMaterialRegistry.WOOD, ItemTags.LOGS);
        this.golemRecipe(consumer, "stone", GolemMaterialRegistry.STONE, Tags.Items.COBBLESTONES);
        this.golemRecipe(consumer, "metal", GolemMaterialRegistry.METAL, Tags.Items.INGOTS_IRON);

        this.shaped(consumer, ItemRegistry.FARMING_CORE.get(),
                List.of("SWS", "WAW", "SWS"),
                Map.of('S', Items.WHEAT_SEEDS, 'W', Items.WHEAT, 'A', Items.AMETHYST_SHARD));

        this.shaped(consumer, ItemRegistry.GOLEM_BATON.get(),
                List.of(" AG", " S ", "S  "),
                Map.of('A', Items.AMETHYST_SHARD, 'G', Items.GOLD_INGOT, 'S', Items.STICK));

        this.shaped(consumer, ItemRegistry.SWIFTNESS_UPGRADE.get(),
                List.of(" S ", "FAR"),
                Map.of('S', Items.SUGAR, 'F', Items.FEATHER, 'A', Items.AMETHYST_SHARD, 'R', Items.REDSTONE));

        this.shaped(consumer, ItemRegistry.CAPACITY_UPGRADE.get(),
                List.of("LCL", " A "),
                Map.of('L', Items.LEATHER, 'C', Items.CHEST, 'A', Items.AMETHYST_SHARD));
    }

    private void golemRecipe(BiConsumer<Identifier, JsonObject> consumer, String name, Holder<GolemMaterial> material, TagKey<Item> materialTag) {
        DataComponentPatch patch = DataComponentPatch.builder()
                .set(DataComponentRegistry.GOLEM_SPEC.get(), GolemSpec.of(material))
                .build();

        this.recipe(consumer, PrincipiaMagica.loc("golem/" + name), ItemRegistry.GOLEM.get(), 1, patch,
                List.of("M M", " R ", "M M"),
                Map.of('M', Ingredient.of(this.items.getOrThrow(materialTag)), 'R', Ingredient.of(Items.REDSTONE)));
    }

    private void shaped(BiConsumer<Identifier, JsonObject> consumer, ItemLike result, List<String> pattern, Map<Character, ItemLike> keys) {
        Map<Character, Ingredient> ingredients = new LinkedHashMap<>();
        keys.forEach((key, item) -> ingredients.put(key, Ingredient.of(item)));
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
