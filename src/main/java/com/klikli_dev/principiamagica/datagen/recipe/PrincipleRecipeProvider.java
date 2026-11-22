// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.content.principle.Principle;
import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe.CuringEnvironment;
import com.klikli_dev.principiamagica.content.principle.recipe.DryingRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.LibationRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.klikli_dev.principiamagica.registry.PrincipleRegistry;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.item.crafting.Recipe;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Recipes of the principle system: libation, curing, steeping and drying.
 * <p>
 * Each recipe is built as its record and encoded through {@link Recipe#CODEC}, so the JSON always matches the codecs
 * the game reads.
 * <p>
 * Steeping turns the unfired axioms into their own steeped items, which dry into the axioms, and turns clay into quickened clay,
 * which the axiom tablet is crafted from.
 */
public class PrincipleRecipeProvider implements DataProvider {
    private static final int CURING_AMOUNT = 25;
    private static final int CURING_TIME_LIGHT = 100;
    private static final int CURING_TIME_DARK = 160;
    private static final int DRYING_TIME = 400;
    private static final int STEEPING_TIME = 100;
    /**
     * One clay ball with 25 mB of unguent makes two quickened clay, 12.5 mB of unguent per quickened clay.
     */
    private static final int QUICKENED_CLAY_PRINCIPLES = 25;
    private static final int QUICKENED_CLAY_COUNT = 2;

    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    private RegistryOps<JsonElement> registryOps;
    private HolderGetter<Item> items;

    public PrincipleRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
        this.lookupProvider = lookupProvider;
    }

    @Override
    public String getName() {
        return "Principle Recipes/" + PrincipiaMagica.MODID;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.lookupProvider.thenCompose(provider -> {
            this.registryOps = provider.createSerializationContext(JsonOps.INSTANCE);
            this.items = provider.lookupOrThrow(Registries.ITEM);

            Map<Identifier, Recipe<?>> recipes = new LinkedHashMap<>();
            this.buildRecipes((id, recipe) -> {
                if (recipes.put(id, recipe) != null) {
                    throw new IllegalStateException("Duplicate recipe " + id);
                }
            });

            List<CompletableFuture<?>> futures = recipes.entrySet().stream()
                    .<CompletableFuture<?>>map(entry -> DataProvider.saveStable(output, this.encode(entry.getValue()), this.pathProvider.json(entry.getKey())))
                    .toList();
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    private JsonObject encode(Recipe<?> recipe) {
        return Recipe.CODEC.encodeStart(this.registryOps, recipe).getOrThrow().getAsJsonObject();
    }

    private void buildRecipes(BiConsumer<Identifier, Recipe<?>> consumer) {
        this.libations(consumer);
        this.curings(consumer);
        this.steepings(consumer);
        this.dryings(consumer);
    }

    private void libations(BiConsumer<Identifier, Recipe<?>> consumer) {
        Holder<Principle> silt = PrincipleRegistry.SILT;
        Holder<Principle> gleam = PrincipleRegistry.GLEAM;
        Holder<Principle> sap = PrincipleRegistry.SAP;
        Holder<Principle> ash = PrincipleRegistry.ASH;

        this.libation(consumer, "dirt", Ingredient.of(Items.DIRT), PrincipleContents.of(new PrincipleStack(silt, 50)));
        this.libation(consumer, "clay_ball", Ingredient.of(Items.CLAY_BALL), PrincipleContents.of(new PrincipleStack(silt, 50)));
        this.libation(consumer, "sand", Ingredient.of(this.items.getOrThrow(ItemTags.SAND)), PrincipleContents.of(new PrincipleStack(silt, 50)));
        this.libation(consumer, "cobblestone", Ingredient.of(Items.COBBLESTONE), PrincipleContents.of(new PrincipleStack(silt, 50)));

        this.libation(consumer, "glass", Ingredient.of(Items.GLASS), PrincipleContents.of(new PrincipleStack(gleam, 50)));
        this.libation(consumer, "gold_nugget", Ingredient.of(Items.GOLD_NUGGET), PrincipleContents.of(new PrincipleStack(gleam, 25)));
        this.libation(consumer, "glowstone_dust", Ingredient.of(Items.GLOWSTONE_DUST), PrincipleContents.of(new PrincipleStack(gleam, 50)));
        this.libation(consumer, "feather", Ingredient.of(Items.FEATHER), PrincipleContents.of(new PrincipleStack(gleam, 25)));

        this.libation(consumer, "wheat", Ingredient.of(Items.WHEAT), PrincipleContents.of(new PrincipleStack(sap, 50)));
        this.libation(consumer, "wheat_seeds", Ingredient.of(Items.WHEAT_SEEDS), PrincipleContents.of(new PrincipleStack(sap, 25)));
        this.libation(consumer, "saplings", Ingredient.of(this.items.getOrThrow(ItemTags.SAPLINGS)), PrincipleContents.of(new PrincipleStack(sap, 50)));
        this.libation(consumer, "leaves", Ingredient.of(this.items.getOrThrow(ItemTags.LEAVES)), PrincipleContents.of(new PrincipleStack(sap, 25)));

        this.libation(consumer, "coal", Ingredient.of(Items.COAL), PrincipleContents.of(new PrincipleStack(ash, 50)));
        this.libation(consumer, "charcoal", Ingredient.of(Items.CHARCOAL), PrincipleContents.of(new PrincipleStack(ash, 50)));
        this.libation(consumer, "flint", Ingredient.of(Items.FLINT), PrincipleContents.of(new PrincipleStack(ash, 50)));
        this.libation(consumer, "bone", Ingredient.of(Items.BONE), PrincipleContents.of(new PrincipleStack(ash, 50)));

        this.libation(consumer, "rotten_flesh", Ingredient.of(Items.ROTTEN_FLESH), PrincipleContents.of(new PrincipleStack(sap, 25), new PrincipleStack(ash, 25)));
    }

    private void libation(BiConsumer<Identifier, Recipe<?>> consumer, String name, Ingredient ingredient, PrincipleContents principles) {
        consumer.accept(PrincipiaMagica.loc("libation/" + name), new LibationRecipe(ingredient, principles));
    }

    private void curings(BiConsumer<Identifier, Recipe<?>> consumer) {
        this.curing(consumer, CuringEnvironment.LIGHT, CURING_TIME_LIGHT, "loam", PrincipleRegistry.LOAM, PrincipleRegistry.SILT, PrincipleRegistry.SAP);
        this.curing(consumer, CuringEnvironment.LIGHT, CURING_TIME_LIGHT, "shard", PrincipleRegistry.SHARD, PrincipleRegistry.SILT, PrincipleRegistry.ASH);
        this.curing(consumer, CuringEnvironment.LIGHT, CURING_TIME_LIGHT, "myrrh", PrincipleRegistry.MYRRH, PrincipleRegistry.GLEAM, PrincipleRegistry.SAP);
        this.curing(consumer, CuringEnvironment.LIGHT, CURING_TIME_LIGHT, "glaze", PrincipleRegistry.GLAZE, PrincipleRegistry.GLEAM, PrincipleRegistry.ASH);

        this.curing(consumer, CuringEnvironment.DARK, CURING_TIME_DARK, "mire", PrincipleRegistry.MIRE, PrincipleRegistry.SILT, PrincipleRegistry.SAP);
        this.curing(consumer, CuringEnvironment.DARK, CURING_TIME_DARK, "bitumen", PrincipleRegistry.BITUMEN, PrincipleRegistry.SILT, PrincipleRegistry.ASH);
        this.curing(consumer, CuringEnvironment.DARK, CURING_TIME_DARK, "unguent", PrincipleRegistry.UNGUENT, PrincipleRegistry.GLEAM, PrincipleRegistry.SAP);
        this.curing(consumer, CuringEnvironment.DARK, CURING_TIME_DARK, "kohl", PrincipleRegistry.KOHL, PrincipleRegistry.GLEAM, PrincipleRegistry.ASH);
    }

    private void curing(BiConsumer<Identifier, Recipe<?>> consumer, CuringEnvironment environment, int time, String name,
                        Holder<Principle> result, Holder<Principle> first, Holder<Principle> second) {
        PrincipleContents inputs = PrincipleContents.of(new PrincipleStack(first, CURING_AMOUNT), new PrincipleStack(second, CURING_AMOUNT));
        consumer.accept(PrincipiaMagica.loc("curing/" + environment.getSerializedName() + "/" + name),
                new CuringRecipe(inputs, new PrincipleStack(result, CURING_AMOUNT), environment, time));
    }

    private void steepings(BiConsumer<Identifier, Recipe<?>> consumer) {
        this.steeping(consumer, "axiom_of_harvest_unfired",
                Ingredient.of(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED.get()),
                PrincipleContents.of(new PrincipleStack(PrincipleRegistry.LOAM, 100)),
                new ItemStackTemplate(ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get(), 1));
        this.steeping(consumer, "axiom_of_felling_unfired",
                Ingredient.of(ItemRegistry.AXIOM_OF_FELLING_UNFIRED.get()),
                PrincipleContents.of(new PrincipleStack(PrincipleRegistry.SHARD, 100)),
                new ItemStackTemplate(ItemRegistry.AXIOM_OF_FELLING_UNFIRED_STEEPED.get(), 1));
        this.steeping(consumer, "quickened_clay",
                Ingredient.of(Items.CLAY_BALL),
                PrincipleContents.of(new PrincipleStack(PrincipleRegistry.UNGUENT, QUICKENED_CLAY_PRINCIPLES)),
                new ItemStackTemplate(ItemRegistry.QUICKENED_CLAY.get(), QUICKENED_CLAY_COUNT));
    }

    private void steeping(BiConsumer<Identifier, Recipe<?>> consumer, String name, Ingredient ingredient, PrincipleContents principles, ItemStackTemplate result) {
        consumer.accept(PrincipiaMagica.loc("steeping/" + name), new SteepingRecipe(ingredient, principles, result, STEEPING_TIME));
    }

    private void dryings(BiConsumer<Identifier, Recipe<?>> consumer) {
        this.drying(consumer, "axiom_of_harvest", ItemRegistry.AXIOM_OF_HARVEST_UNFIRED_STEEPED.get(), ItemRegistry.AXIOM_OF_HARVEST.get());
        this.drying(consumer, "axiom_of_felling", ItemRegistry.AXIOM_OF_FELLING_UNFIRED_STEEPED.get(), ItemRegistry.AXIOM_OF_FELLING.get());
    }

    private void drying(BiConsumer<Identifier, Recipe<?>> consumer, String name, Item steeped, Item result) {
        consumer.accept(PrincipiaMagica.loc("drying/" + name), new DryingRecipe(Ingredient.of(steeped), new ItemStackTemplate(result, 1), DRYING_TIME));
    }
}
