// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.client.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import java.util.Collection;

/**
 * Principle recipes synced from the server, for JEI and client HUD/tooltip code.
 */
public class ClientRecipes {
    private static RecipeMap recipes = RecipeMap.EMPTY;

    public static <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> type) {
        return recipes.byType(type);
    }

    public static void onRecipesReceived(RecipesReceivedEvent event) {
        recipes = event.getRecipeMap();
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        recipes = RecipeMap.EMPTY;
    }
}
