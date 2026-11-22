// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.jei;

import com.klikli_dev.principiamagica.content.principle.recipe.DryingRecipe;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

public class DryingCategory extends AbstractRecipeCategory<RecipeHolder<DryingRecipe>> {

    public static final int WIDTH = 100;
    public static final int HEIGHT = 33;

    /**
     * Drying has no second input, so its arrow and output sit closer to the input than in the standard layout.
     */
    private static final int ARROW_X = 24;
    private static final int OUTPUT_X = 52;
    private static final int TIME_Y = 23;

    private final IDrawableStatic arrow;

    public DryingCategory(IGuiHelper guiHelper) {
        super(JeiRecipeTypes.DRYING, Component.translatable("jei.principiamagica.drying"),
                guiHelper.createDrawableItemLike(ItemRegistry.DRYING_SLAB.get()), WIDTH, HEIGHT);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<DryingRecipe> recipe, IFocusGroup focuses) {
        builder.addInputSlot(JeiText.INPUT_X, JeiText.SLOT_Y).add(recipe.value().ingredient());
        builder.addOutputSlot(OUTPUT_X, JeiText.SLOT_Y).add(recipe.value().result());
    }

    @Override
    public void draw(RecipeHolder<DryingRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        JeiText.drawArrow(guiGraphics, this.arrow, ARROW_X);
        JeiText.drawCentered(guiGraphics, font, JeiText.timeAtFullLight(recipe.value().time()), WIDTH, TIME_Y);
    }
}
