// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.jei;

import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.recipe.CuringRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

import java.util.List;

public class CuringCategory extends AbstractRecipeCategory<RecipeHolder<CuringRecipe>> {

    public static final int WIDTH = 100;
    public static final int HEIGHT = 40;

    private static final int REQUIREMENT_Y = 23;
    private static final int TIME_Y = 32;

    private final Component requirement;
    private final IDrawableStatic plus;
    private final IDrawableStatic arrow;

    public CuringCategory(IGuiHelper guiHelper, IRecipeHolderType<CuringRecipe> recipeType, Component title, ItemLike catalyst, Component requirement) {
        super(recipeType, title, guiHelper.createDrawableItemLike(catalyst), WIDTH, HEIGHT);
        this.requirement = requirement;
        this.plus = guiHelper.getRecipePlusSign();
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CuringRecipe> recipe, IFocusGroup focuses) {
        List<PrincipleStack> inputs = recipe.value().inputs().stacks();
        for (int i = 0; i < inputs.size(); i++) {
            PrincipleIngredientRenderer.addRecipeSlot(builder.addInputSlot(JeiText.inputX(JeiText.INPUT_X, i), JeiText.SLOT_Y), inputs.get(i));
        }

        PrincipleIngredientRenderer.addRecipeSlot(builder.addOutputSlot(JeiText.OUTPUT_X, JeiText.SLOT_Y), recipe.value().result());
    }

    @Override
    public void draw(RecipeHolder<CuringRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        JeiText.drawPlus(guiGraphics, this.plus, JeiText.inputX(JeiText.INPUT_X, 0), JeiText.inputX(JeiText.INPUT_X, 1), JeiText.SLOT_Y);
        JeiText.drawArrow(guiGraphics, this.arrow, JeiText.ARROW_X);

        if (this.requirement != null) {
            JeiText.drawCentered(guiGraphics, font, this.requirement, WIDTH, REQUIREMENT_Y);
        }
        JeiText.drawCentered(guiGraphics, font, JeiText.timeAtFullLight(recipe.value().time()), WIDTH, this.requirement != null ? TIME_Y : REQUIREMENT_Y);
    }
}
