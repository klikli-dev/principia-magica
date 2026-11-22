// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.jei;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.client.recipe.ClientRecipes;
import com.klikli_dev.principiamagica.content.libation.vessel.WabetBasinBlockEntity;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe.CuringEnvironment;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import com.klikli_dev.principiamagica.registry.PrincipiaMagicaRegistries;
import com.klikli_dev.principiamagica.registry.RecipeTypeRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

@JeiPlugin
public class PrincipiaMagicaJeiPlugin implements IModPlugin {

    /**
     * Principles are listed like fluids, one bucket in mB, so their ingredient entries carry 1000 mB.
     */
    private static final int INGREDIENT_AMOUNT = 1000;

    @Override
    public Identifier getPluginUid() {
        return PrincipiaMagica.loc("jei");
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        List<PrincipleStack> principles = PrincipiaMagicaRegistries.PRINCIPLE.listElements()
                .map(holder -> new PrincipleStack(holder, INGREDIENT_AMOUNT))
                .toList();

        registration.register(PrincipleIngredientType.TYPE, principles, new PrincipleIngredientHelper(), new PrincipleIngredientRenderer(), PrincipleStack.CODEC);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new LibationCategory(guiHelper),
                new CuringCategory(guiHelper, JeiRecipeTypes.WABET_CURING, Component.translatable("jei.principiamagica.wabet_curing"),
                        ItemRegistry.WABET_BASIN.get(), Component.translatable("jei.principiamagica.curing.light_min", WabetBasinBlockEntity.MIN_LIGHT)),
                new CuringCategory(guiHelper, JeiRecipeTypes.STEEPING_JAR_CURING, Component.translatable("jei.principiamagica.steeping_jar_curing"),
                        ItemRegistry.STEEPING_JAR.get(), null),
                new SteepingCategory(guiHelper),
                new DryingCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(JeiRecipeTypes.LIBATION, List.copyOf(ClientRecipes.byType(RecipeTypeRegistry.LIBATION.get())));
        registration.addRecipes(JeiRecipeTypes.WABET_CURING, curings(CuringEnvironment.LIGHT));
        registration.addRecipes(JeiRecipeTypes.STEEPING_JAR_CURING, curings(CuringEnvironment.DARK));
        registration.addRecipes(JeiRecipeTypes.STEEPING, List.copyOf(ClientRecipes.byType(RecipeTypeRegistry.STEEPING.get())));
        registration.addRecipes(JeiRecipeTypes.DRYING, List.copyOf(ClientRecipes.byType(RecipeTypeRegistry.DRYING.get())));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(JeiRecipeTypes.LIBATION, ItemRegistry.LIBATION_SLAB.get());
        registration.addCraftingStation(JeiRecipeTypes.WABET_CURING, ItemRegistry.WABET_BASIN.get());
        registration.addCraftingStation(JeiRecipeTypes.STEEPING_JAR_CURING, ItemRegistry.STEEPING_JAR.get());
        registration.addCraftingStation(JeiRecipeTypes.STEEPING, ItemRegistry.CATCH_TROUGH.get());
        registration.addCraftingStation(JeiRecipeTypes.DRYING, ItemRegistry.DRYING_SLAB.get());
    }

    private static List<RecipeHolder<CuringRecipe>> curings(CuringEnvironment environment) {
        return ClientRecipes.byType(RecipeTypeRegistry.CURING.get()).stream()
                .filter(holder -> holder.value().environment() == environment)
                .toList();
    }
}
