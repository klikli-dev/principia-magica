// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon.page;

import com.klikli_dev.principiamagica.content.libation.vessel.VesselProperties;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe.CuringEnvironment;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

import java.util.List;

/**
 * Renders a curing recipe: the principles in the vessel, the arrow, and the principle it cures into.
 * The Wabet Basin (light) shows its minimum light, the Steeping Jar (dark) has no light requirement.
 */
public class BookCuringRecipePageRenderer extends PrincipiaRecipePageRenderer<CuringRecipe, BookCuringRecipePage> {

    public BookCuringRecipePageRenderer(BookCuringRecipePage page) {
        super(page, CuringRecipe.class);
    }

    @Override
    protected int getRecipeWidth(CuringRecipe recipe) {
        return rowWidth(recipe.inputs().stacks().size());
    }

    @Override
    protected int getRecipeHeight(CuringRecipe recipe) {
        return recipe.environment() == CuringEnvironment.LIGHT ? HINT_HEIGHT : TIME_HEIGHT;
    }

    @Override
    protected void drawRecipe(GuiGraphicsExtractor guiGraphics, CuringRecipe recipe, int x, int y, int mouseX, int mouseY) {
        List<PrincipleStack> inputs = recipe.inputs().stacks();
        int inputCount = inputs.size();
        for (int i = 0; i < inputCount; i++) {
            if (i > 0) {
                this.drawPlus(guiGraphics, x + i * SLOT_PITCH - 4, y);
            }
            this.drawPrinciple(guiGraphics, inputs.get(i), x + i * SLOT_PITCH, y, mouseX, mouseY);
        }

        this.drawArrow(guiGraphics, x + arrowX(inputCount), y);
        this.drawPrinciple(guiGraphics, recipe.result(), x + outputX(inputCount), y, mouseX, mouseY);

        boolean light = recipe.environment() == CuringEnvironment.LIGHT;
        ItemLike station = light ? ItemRegistry.WABET_BASIN.get() : ItemRegistry.STEEPING_JAR.get();
        this.drawStation(guiGraphics, station, x + arrowX(inputCount) - 4, y + STATION_Y, mouseX, mouseY);

        int centerX = x + rowWidth(inputCount) / 2;
        this.drawCentered(guiGraphics, timeAtFullLight(recipe.time()), centerX, y + TIME_Y);
        if (light) {
            this.drawCentered(guiGraphics, Component.translatable("jei.principiamagica.curing.light_min", VesselProperties.WABET_BASIN.minLight()), centerX, y + HINT_Y);
        }
    }
}
