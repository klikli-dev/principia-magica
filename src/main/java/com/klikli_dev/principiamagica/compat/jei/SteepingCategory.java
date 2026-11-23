// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.jei;

import com.klikli_dev.principiamagica.content.principle.PrincipleContents;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe;
import com.klikli_dev.principiamagica.content.principle.recipe.SteepingRecipe.SteepingRecipeInput;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class SteepingCategory extends AbstractRecipeCategory<RecipeHolder<SteepingRecipe>> {

    public static final int WIDTH = 100;
    public static final int HEIGHT = 33;

    private static final int TIME_Y = 23;

    private final IDrawableStatic plus;
    private final IDrawableStatic arrow;

    public SteepingCategory(IGuiHelper guiHelper) {
        super(JeiRecipeTypes.STEEPING, Component.translatable("jei.principiamagica.steeping"),
                guiHelper.createDrawableItemLike(ItemRegistry.CATCH_TROUGH.get()), WIDTH, HEIGHT);
        this.plus = guiHelper.getRecipePlusSign();
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<SteepingRecipe> recipe, IFocusGroup focuses) {
        builder.addInputSlot(JeiText.INPUT_X, JeiText.SLOT_Y).add(recipe.value().ingredient());

        List<PrincipleStack> principles = recipe.value().principles().stacks();
        for (int i = 0; i < principles.size(); i++) {
            PrincipleIngredientRenderer.addRecipeSlot(builder.addInputSlot(JeiText.inputX(JeiText.INPUT_X, i + 1), JeiText.SLOT_Y), principles.get(i));
        }

        ItemStack output = recipe.value().assemble(new SteepingRecipeInput(ItemStack.EMPTY, PrincipleContents.EMPTY));
        builder.addOutputSlot(JeiText.OUTPUT_X, JeiText.SLOT_Y).add(output);
    }

    @Override
    public void draw(RecipeHolder<SteepingRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        for (int i = 0; i < recipe.value().principles().stacks().size(); i++) {
            JeiText.drawPlus(guiGraphics, this.plus, JeiText.inputX(JeiText.INPUT_X, i), JeiText.inputX(JeiText.INPUT_X, i + 1), JeiText.SLOT_Y);
        }

        JeiText.drawArrow(guiGraphics, this.arrow, JeiText.ARROW_X);
        JeiText.drawCentered(guiGraphics, Minecraft.getInstance().font, JeiText.time(recipe.value().time()), WIDTH, TIME_Y);
    }
}
