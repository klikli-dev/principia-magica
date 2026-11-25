// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon.page;

import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlockEntity;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.recipe.LibationRecipe;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/**
 * Renders a libation recipe: the item and water on the Libation Slab, the arrow, and the principles it dissolves into.
 * Principles wrap to a second row after two. The libation has no time and no light requirement.
 */
public class BookLibationRecipePageRenderer extends PrincipiaRecipePageRenderer<LibationRecipe, BookLibationRecipePage> {

    /**
     * Height of a row of principles, including their labels.
     */
    private static final int OUTPUT_ROW_HEIGHT = SLOT_SIZE + LINE_HEIGHT + 4;
    private static final int OUTPUTS_PER_ROW = 2;

    public BookLibationRecipePageRenderer(BookLibationRecipePage page) {
        super(page, LibationRecipe.class);
    }

    @Override
    protected int getRecipeWidth(LibationRecipe recipe) {
        // The principles run in columns of SLOT_PITCH after the output start, so a row of two reaches a second column.
        int columns = Math.max(1, Math.min(OUTPUTS_PER_ROW, recipe.principles().stacks().size()));
        return outputX(2) + (columns - 1) * SLOT_PITCH + SLOT_SIZE;
    }

    @Override
    protected int getRecipeHeight(LibationRecipe recipe) {
        int rows = (recipe.principles().stacks().size() + OUTPUTS_PER_ROW - 1) / OUTPUTS_PER_ROW;
        return Math.max(STATION_HEIGHT, rows * OUTPUT_ROW_HEIGHT);
    }

    @Override
    protected void drawRecipe(GuiGraphicsExtractor guiGraphics, LibationRecipe recipe, int x, int y, int mouseX, int mouseY) {
        this.drawIngredient(guiGraphics, recipe.ingredient(), x, y, mouseX, mouseY);

        int waterAmount = LibationSlabBlockEntity.waterCost(recipe.principles());
        this.drawPlus(guiGraphics, x + SLOT_PITCH - 4, y);
        this.drawFluid(guiGraphics, new FluidStack(Fluids.WATER, waterAmount), x + SLOT_PITCH, y, mouseX, mouseY);

        this.drawArrow(guiGraphics, x + arrowX(2), y);
        this.drawStation(guiGraphics, ItemRegistry.LIBATION_SLAB.get(), x + arrowX(2) - 4, y + STATION_Y, mouseX, mouseY);

        List<PrincipleStack> outputs = recipe.principles().stacks();
        for (int i = 0; i < outputs.size(); i++) {
            int column = i % OUTPUTS_PER_ROW;
            int row = i / OUTPUTS_PER_ROW;
            this.drawPrinciple(guiGraphics, outputs.get(i), x + outputX(2) + column * SLOT_PITCH, y + row * OUTPUT_ROW_HEIGHT, mouseX, mouseY);
        }
    }
}
