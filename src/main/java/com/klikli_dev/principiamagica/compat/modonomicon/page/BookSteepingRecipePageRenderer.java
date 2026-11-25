// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon.page;

import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/**
 * Renders a steeping recipe: the item and principles in the Catch Trough, the arrow, and the steeped item.
 * The time is not scaled by light, so it is shown without the light note.
 */
public class BookSteepingRecipePageRenderer extends PrincipiaRecipePageRenderer<SteepingRecipe, BookSteepingRecipePage> {

    public BookSteepingRecipePageRenderer(BookSteepingRecipePage page) {
        super(page, SteepingRecipe.class);
    }

    @Override
    protected int getRecipeWidth(SteepingRecipe recipe) {
        return rowWidth(1 + recipe.principles().stacks().size());
    }

    @Override
    protected int getRecipeHeight(SteepingRecipe recipe) {
        return TIME_HEIGHT;
    }

    @Override
    protected void drawRecipe(GuiGraphicsExtractor guiGraphics, SteepingRecipe recipe, int x, int y, int mouseX, int mouseY) {
        List<PrincipleStack> principles = recipe.principles().stacks();
        int inputCount = 1 + principles.size();

        this.drawIngredient(guiGraphics, recipe.ingredient(), x, y, mouseX, mouseY);
        for (int i = 0; i < principles.size(); i++) {
            int slot = i + 1;
            this.drawPlus(guiGraphics, x + slot * SLOT_PITCH - 4, y);
            this.drawPrinciple(guiGraphics, principles.get(i), x + slot * SLOT_PITCH, y, mouseX, mouseY);
        }

        this.drawArrow(guiGraphics, x + arrowX(inputCount), y);
        this.drawItem(guiGraphics, recipe.result().create(), x + outputX(inputCount), y, mouseX, mouseY);
        this.drawStation(guiGraphics, ItemRegistry.CATCH_TROUGH.get(), x + arrowX(inputCount) - 4, y + STATION_Y, mouseX, mouseY);

        this.drawCentered(guiGraphics, time(recipe.time()), x + rowWidth(inputCount) / 2, y + TIME_Y);
    }
}
