// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon.page;

import com.klikli_dev.principiamagica.content.principle.recipe.DryingRecipe;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Renders a drying recipe: the item on the Drying Slab, the arrow, and the dried item.
 * The time is given at full light, which is the light hint of the recipe.
 */
public class BookDryingRecipePageRenderer extends PrincipiaRecipePageRenderer<DryingRecipe, BookDryingRecipePage> {

    public BookDryingRecipePageRenderer(BookDryingRecipePage page) {
        super(page, DryingRecipe.class);
    }

    @Override
    protected int getRecipeWidth(DryingRecipe recipe) {
        return rowWidth(1);
    }

    @Override
    protected int getRecipeHeight(DryingRecipe recipe) {
        return TIME_HEIGHT;
    }

    @Override
    protected void drawRecipe(GuiGraphicsExtractor guiGraphics, DryingRecipe recipe, int x, int y, int mouseX, int mouseY) {
        this.drawIngredient(guiGraphics, recipe.ingredient(), x, y, mouseX, mouseY);

        this.drawArrow(guiGraphics, x + arrowX(1), y);
        this.drawItem(guiGraphics, recipe.result().create(), x + outputX(1), y, mouseX, mouseY);
        this.drawStation(guiGraphics, ItemRegistry.DRYING_SLAB.get(), x + arrowX(1) - 4, y + STATION_Y, mouseX, mouseY);

        this.drawCentered(guiGraphics, timeAtFullLight(recipe.time()), x + rowWidth(1) / 2, y + TIME_Y);
    }
}
