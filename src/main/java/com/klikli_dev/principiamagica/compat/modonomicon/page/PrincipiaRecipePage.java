// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon.page;

import com.klikli_dev.modonomicon.book.entries.BookContentEntry;
import com.klikli_dev.modonomicon.book.page.BookRecipePage;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

/**
 * Base of the recipe pages of the principle recipes.
 * <p>
 * Modonomicon's recipe pages draw their recipe from the vanilla recipe displays, which the principle recipes do not provide.
 * These pages therefore only keep the recipe ids, and their renderers look the recipes up in the recipes synced to the client.
 * This keeps the pages working without a recipe viewer such as JEI.
 *
 * @param <R> the recipe type the page shows.
 */
public abstract class PrincipiaRecipePage<R extends Recipe<?>> extends BookRecipePage<R> {

    public PrincipiaRecipePage(JsonDataHolder common) {
        super(common);
    }

    public PrincipiaRecipePage(NetworkDataHolder common) {
        super(common);
    }

    @Override
    public void build(Level level, BookContentEntry parentEntry, int pageNum) {
        //there are no vanilla displays to resolve, the renderer looks the recipes up itself
        this.attach(parentEntry, pageNum);
        this.swapRecipes();
    }

    @Override
    protected void swapRecipes() {
        if (this.recipeKey1 == null && this.recipeKey2 != null) {
            this.recipeKey1 = this.recipeKey2;
            this.recipeKey2 = null;
        }
    }
}
