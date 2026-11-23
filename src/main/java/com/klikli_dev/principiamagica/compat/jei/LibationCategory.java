// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.jei;

import com.klikli_dev.principiamagica.content.libation.slab.LibationSlabBlockEntity;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.recipe.LibationRecipe;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class LibationCategory extends AbstractRecipeCategory<RecipeHolder<LibationRecipe>> {

    public static final int WIDTH = 140;
    public static final int HEIGHT = 32;

    private static final int WATER_X = JeiText.inputX(JeiText.INPUT_X, 1);

    private final IDrawableStatic plus;
    private final IDrawableStatic arrow;

    public LibationCategory(IGuiHelper guiHelper) {
        super(JeiRecipeTypes.LIBATION, Component.translatable("jei.principiamagica.libation"),
                guiHelper.createDrawableItemLike(ItemRegistry.LIBATION_SLAB.get()), WIDTH, HEIGHT);
        this.plus = guiHelper.getRecipePlusSign();
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<LibationRecipe> recipe, IFocusGroup focuses) {
        builder.addInputSlot(JeiText.INPUT_X, JeiText.SLOT_Y).add(recipe.value().ingredient());
        builder.addInputSlot(WATER_X, JeiText.SLOT_Y).add(NeoForgeTypes.FLUID_STACK,
                new FluidStack(Fluids.WATER, LibationSlabBlockEntity.waterCost(recipe.value().principles())));

        List<PrincipleStack> principles = recipe.value().principles().stacks();
        for (int i = 0; i < principles.size(); i++) {
            PrincipleIngredientRenderer.addRecipeSlot(builder.addOutputSlot(JeiText.OUTPUT_X + i * JeiText.SLOT_PITCH, JeiText.SLOT_Y), principles.get(i));
        }
    }

    @Override
    public void draw(RecipeHolder<LibationRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        JeiText.drawPlus(guiGraphics, this.plus, JeiText.INPUT_X, WATER_X, JeiText.SLOT_Y);
        JeiText.drawArrow(guiGraphics, this.arrow, JeiText.ARROW_X);
    }
}

